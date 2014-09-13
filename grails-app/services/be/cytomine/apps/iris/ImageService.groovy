package be.cytomine.apps.iris

import be.cytomine.client.Cytomine
import grails.transaction.Transactional
import org.json.simple.JSONObject

@Transactional
class ImageService {
	
	/**
	 * Injected grails application properties.
	 */
	def grailsApplication

	def getImages(Cytomine cytomine, long projectID){
		def imageList = cytomine.getImageInstances(projectID).list
		imageList.each {
			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			it.goToURL = grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + projectID + "-" + it.id + "-"

			// inject the blinded file name in each image, if required
			if (blindMode){
				it.originalFilename = "[BLIND]" + it.id
			}
		}
		
		return imageList
	}
	
	def getImagesWithProgress(Cytomine cytomine, long projectID, String publicKey){

		long userID = cytomine.getUser(publicKey).getId()

		// important for blinding image names
		boolean blindMode = false

		be.cytomine.apps.iris.User u = be.cytomine.apps.iris.User.find { cmID == userID }

		// get the session
		Session sess = u.getSession();

		// try to search in local database
		Project irisProject = sess.getProjects().find { it.cmID == projectID }

		if (irisProject == null){
			def cmProject = cytomine.getProject(projectID)
			// update the project in the local database
			irisProject = new DomainMapper().mapProject(cmProject, irisProject)
			irisProject.save(failOnError:true)

			// get the blind mode
			blindMode = cmProject.get("blindMode")
		} else {
			blindMode = irisProject.cmBlindMode
		}

		// TODO implement paging using max and offset parameters from the request params
		//int offset = params.long("offset")
		//int max = params.long("max")
		//cytomine.setMax(5); //max 5 images

		def imageList = cytomine.getImageInstances(projectID).list
		imageList.each {
			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			it.goToURL = grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + projectID + "-" + it.id + "-"

			// inject the blinded file name in each image, if required
			if (blindMode){
				it.originalFilename = "[BLIND]" + it.id
			}

			// retrieve the user's progress on each image and return it in the object
			JSONObject annInfo = new Utils().getUserProgress(cytomine, projectID, it.id, userID)
			// resolving the values from the JSONObject to each image as property
			it.labeledAnnotations = annInfo.get("labeledAnnotations")
			it.userProgress = annInfo.get("userProgress")
		}

		return imageList
	}


}
