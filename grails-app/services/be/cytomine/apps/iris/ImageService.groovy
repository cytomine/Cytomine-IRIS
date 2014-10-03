package be.cytomine.apps.iris

import grails.converters.JSON;
import grails.transaction.Transactional

import org.json.simple.JSONArray
import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.ImageInstanceCollection
import be.cytomine.client.models.ImageInstance

@Transactional
class ImageService {
	
	/**
	 * Injected grails application properties.
	 */
	def grailsApplication
	def sessionService
	def imageService

	/**
	 * Gets a single image from Cytomine.
	 *
	 * @param cytomine a Cytomine instance
	 * @param cmProjectID the Cytomine project ID
	 * @param cmImageInstanceID the Cytomine imageinstance ID
	 * @return the IRIS image
	 */
	def getImage(Cytomine cytomine, long cmProjectID, long cmImageInstanceID, String publicKey){
		ImageInstance cmImage = cytomine.getImageInstance(cmImageInstanceID)
		Project p = Project.find { cmID == cmProjectID }
		User user = User.find { cmPublicKey == publicKey}
		
		Image irisImage = new DomainMapper().mapImage(cmImage, null, p.getCmBlindMode())
		
		//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
		irisImage.setGoToURL(grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + cmProjectID + "-" + cmImage.getId() + "-")
		irisImage.setOlTileServerURL(imageService.getImageServerURL(cytomine, cmImage.get("baseImage"), cmImage.getId()));
		
		// retrieve the user's progress on each image and return it in the object
		JSONObject annInfo = new Utils().getUserProgress(cytomine, cmProjectID, cmImage.getId(), user.getCmID())
		// resolving the values from the JSONObject to each image as property
		irisImage.setLabeledAnnotations(annInfo.get("labeledAnnotations"))
		irisImage.setUserProgress(annInfo.get("userProgress"))
		irisImage.setNumberOfAnnotations(annInfo.get("totalAnnotations"))
		
		// set the Cytomine image as "cytomine" property in the irisImage
		def imageJSON = sessionService.injectCytomineImageInstance(cytomine, irisImage, cmImage, p.getCmBlindMode())
		
		return imageJSON
	}
	
	/**
	 * Gets images from Cytomine.
	 * 
	 * @param cytomine a Cytomine instance
	 * @param cmProjectID the Cytomine project ID
	 * @return a list of Cytomine images for the project
	 */
	def getImages(Cytomine cytomine, long cmProjectID){
		def imageList = cytomine.getImageInstances(cmProjectID).list
		def cmProject = cytomine.getProject(cmProjectID)
		imageList.each {
			// inject the blinded file name in each image, if required
			if (cmProject.get("blindMode") == true){
				it.originalFilename = "[BLIND]" + it.id
			}
		}
		
		return imageList
	}
	
	/**
	 * 
	 * 
	 * @param cytomine a Cytomine instance
	 * @param cmProjectID the Cytomine project ID
	 * @param publicKey the public key of the user
	 * 
	 * @return a list of IRIS images
	 * 
	 * @throws CytomineException if the user is not found
	 */
	def getImagesWithProgress(Cytomine cytomine, long cmProjectID, String publicKey) throws CytomineException{

		long userID = cytomine.getUser(publicKey).getId()

		// important for blinding image names
		boolean blindMode = false

		// find the session of the calling user
		User u = User.findByCmID(userID)

		// get the session
		Session sess = u.getSession();

		// search for the requested project in the session
		Project irisProject = sess.getProjects().find { it.cmID == cmProjectID }

		if (irisProject == null){
			def cmProject = cytomine.getProject(cmProjectID)
			// update the project in the local database
			irisProject = new DomainMapper().mapProject(cmProject, irisProject)
			irisProject.save(flush:true,failOnError:true)

			// get the blind mode
			blindMode = cmProject.get("blindMode")
		} else {
			blindMode = irisProject.cmBlindMode
		}

		ImageInstanceCollection cmImageCollection = cytomine.getImageInstances(cmProjectID)
		// TODO speedup of status computation required urgently!
		int nImages = cmImageCollection.size()
		def irisImageList = new JSONArray()
		
		def start = System.currentTimeMillis()
		for (int i = 0; i < nImages; i++) {
			ImageInstance cmImage = cmImageCollection.get(i)
			
			// map the client image to the IRIS image
			Image irisImage = new DomainMapper().mapImage(cmImage, null, blindMode)
			
			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			irisImage.setGoToURL(grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + cmProjectID + "-" + cmImage.getId() + "-")
			irisImage.setOlTileServerURL(imageService.getImageServerURL(cytomine, cmImage.get("baseImage"), cmImage.getId()));
			
			// retrieve the user's progress on each image and return it in the object
			JSONObject annInfo = new Utils().getUserProgress(cytomine, cmProjectID, cmImage.getId(), userID)
			// resolving the values from the JSONObject to each image as property
			irisImage.setLabeledAnnotations(annInfo.get("labeledAnnotations"))
			irisImage.setUserProgress(annInfo.get("userProgress"))
			irisImage.setNumberOfAnnotations(annInfo.get("totalAnnotations"))
			
			// set the Cytomine image as "cytomine" property in the irisImage
			def imageJSON = sessionService.injectCytomineImageInstance(cytomine, irisImage, cmImage, blindMode)
			
			// add it to the result list
			irisImageList.add(imageJSON)
		}
		println "Resolving image status for " + nImages +  " images lasted " + (System.currentTimeMillis()-start)/1000 + " seconds."

		return irisImageList
	}
	
	def getImageServerURL(Cytomine cytomine, long abstrImgID, long imgInstID){
		def urls = imageService.getImageServerURLs(cytomine, abstrImgID, imgInstID)
		def url = new org.codehaus.groovy.grails.web.json.JSONObject(urls.toString()).getAt("imageServersURLs")[0]
		return url.toString()
	}
	
	/**
	 * Gets the image server URLs for a given image.
	 * @return the URLs for a given abstractImage for the OpenLayers instance as JSONObject
	 */
	def getImageServerURLs(Cytomine cytomine, long abstrImgID, long imgInstID){
		String irisHost = grailsApplication.config.grails.cytomine.apps.iris.host;
		String cmHost = grailsApplication.config.grails.cytomine.host;
		
		// perform a synchronous get request to the Cytomine host server
		String urls = cytomine.doGet("/api/abstractimage/" + abstrImgID + "/imageservers.json?imageinstance=" + imgInstID)
		
		// urls are a long string, so break them up in a JSONarray
		org.codehaus.groovy.grails.web.json.JSONObject obj = new org.codehaus.groovy.grails.web.json.JSONObject(urls)
		
		def newUrls = obj.imageServersURLs.collect {
			irisHost + "/image/tile" + it.substring(it.indexOf("?"), it.length())
		}
		obj.putAt("imageServersURLs", newUrls)
		
		return obj as JSON
	}
	
	
	
}
