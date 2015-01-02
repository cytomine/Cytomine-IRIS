package be.cytomine.apps.iris

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import be.cytomine.client.Cytomine;
import be.cytomine.client.collections.ImageInstanceCollection;
import be.cytomine.client.collections.ProjectCollection;
import be.cytomine.client.models.ImageInstance;

import org.json.simple.JSONArray
import org.json.simple.JSONObject;

/**
 * Maintenance script for synchronizing the cached user progress in the IRIS database. 
 * It updates the total number of annotations and computes the user progress for each user.
 * 
 * @author Philipp Kainz
 * @since 1.4
 */
class SynchronizeUserProgressJob {
	static triggers = {
		simple name: 'initAtStartup', startDelay: 10000L, repeatCount: 0 // runs once at server start
		cron name: 'every2Hours', cronExpression: "0 0 0/2 * * ?" // runs every two hours
	}

	def description = "Synchronize user labeling progress from the Cytomine core server. " +
	" Running once at server startup and every two hours."

	// the configuration of the IRIS server
	def grailsApplication
	def sessionService
	def imageService
	def activityService
	def mailService

	/**
	 * Custom constructor for calling via controller. 
	 * 
	 * @param grailsApplication
	 * @param sessionService
	 * @param imageService
	 * @param activityService
	 * @param mailService
	 */
	SynchronizeUserProgressJob(def grailsApplication, def sessionService, def imageService, def activityService, def mailService){
		this.grailsApplication = grailsApplication
		this.sessionService = sessionService
		this.imageService = imageService
		this.activityService = activityService
		this.mailService = mailService
	}

	/**
	 * Empty constructor for scheduled access.
	 */
	SynchronizeUserProgressJob(){
	}

	/**
	 * Worker method.
	 * @return
	 * @throws Exception
	 */
	def execute() throws Exception{
		log.info("Starting user progress synchronization...")
		// get all users from the IRIS database
		List<User> users = User.getAll()

		try {
			// for each IRIS user, refresh the labeling progress in the projects
			users.each { user ->
				log.info("Synchronizing user " + user.cmUserName + "...")
				// create a new cytomine connection
				Cytomine cytomine = new Cytomine(grailsApplication.config.grails.cytomine.host,
						user.cmPublicKey, user.cmPrivateKey, "./")

				// get the user's session
				Session sess = user.getSession()
				// get the projects from this session
				SortedSet<Project> irisProjects = sess.getProjects()

				// get the projects of this user from Cytomine
				ProjectCollection cmProjects = cytomine.getProjectsByUser(user.cmID)

				// the domain mapper
				DomainMapper domainMapper = new DomainMapper(grailsApplication)

				int nProjects = cmProjects.size()

				// update the projects of the user
				for (int pIdx = 0; pIdx < nProjects; pIdx++){
					be.cytomine.client.models.Project cmProject = cmProjects.get(pIdx)
					long cmProjectID = cmProject.getId()

					// find the project in the local database
					Project irisProject = irisProjects.find { it.cmID == cmProjectID }

					// map the client domain model to IRIS
					irisProject = domainMapper.mapProject(cmProject, irisProject)

					// persist the project
					sess.addToProjects(irisProject)

					// get all images for that project from the Cytomine core server
					ImageInstanceCollection cmImageCollection = cytomine.getImageInstances(cmProjectID)

					// get all images in this session
					SortedSet<Image> irisImages = irisProject.getImages()

					// FOR EACH IMAGE
					for (int iIdx = 0; iIdx < cmImageCollection.size(); iIdx++){
						ImageInstance cmImage = cmImageCollection.get(iIdx)
						// find the IRIS image in the list
						Image irisImage = irisImages.find {
							it.cmID == cmImage.getId()
						}

						// map each image to the IRIS model (updates all annotations)
						irisImage = domainMapper.mapImage(cmImage,irisImage,irisProject.cmBlindMode)

						//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
						irisImage.setGoToURL(grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + cmProjectID + "-" + cmImage.getId() + "-")

						// this performs an synchronous GET request to the cytomine server
						irisImage.setOlTileServerURL(imageService.getImageServerURL(cytomine, cmImage.get("baseImage"), cmImage.getId()));

						// compute the user progress
						JSONObject annInfo = new Utils().getUserProgress(cytomine, cmProjectID, cmImage.getId(), user.cmID)
						// resolving the values from the JSONObject to each image as property
						irisImage.setLabeledAnnotations(annInfo.get("labeledAnnotations"))
						irisImage.setUserProgress(annInfo.get("userProgress"))
						irisImage.setNumberOfAnnotations(annInfo.get("totalAnnotations"))

						// IMPORTANT: do NOT update lastActivityDate!

						// persist the IRIS image
						irisProject.addToImages(irisImage)
					}
					log.info("Syncing projects... [" + irisProject.cmName + ", " + ((pIdx+1)*100/nProjects) + "%]")
				}
				log.info("Done synchronizing user " + user.cmUserName + ".")
				activityService.log(user, "Successfully (auto)synchronized user progress.")
			}

		} catch(Exception ex){
			log.error("Cannot complete the user progress synchronization!", ex)
			activityService.log("(Auto)synchronization of user progress failed!")
 
			String recepient = grailsApplication.config.grails.cytomine.apps.iris.server.admin.email
			
			log.info("Sending email to admin...")
			
			// notify the admin
			mailService.sendMail {
				async true
				from "cytomine-iris@pkainz.com"
				to recepient
				subject "Progress synchronization job failed"
				body ('Could not complete the user synchronization!\n' + ex)
			}
			return false
		}
		
		log.info("Done synchronizing user progress.")
		return true
	}
}
