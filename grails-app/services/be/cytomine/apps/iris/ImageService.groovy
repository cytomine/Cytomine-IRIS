package be.cytomine.apps.iris

import grails.converters.JSON
import grails.transaction.Transactional

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.codehaus.groovy.grails.web.json.JSONElement;
import org.json.simple.JSONArray
import org.json.simple.JSONObject

import com.google.gson.JsonObject;

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
	 * Gets a single image from Cytomine and computes the user progress.
	 *
	 * @param cytomine a Cytomine instance
	 * @param cmProjectID the Cytomine project ID
	 * @param cmImageInstanceID the Cytomine imageinstance ID
	 * @return the IRIS image
	 */
	JSONElement getImage(Cytomine cytomine, long cmProjectID, long cmImageInstanceID, String publicKey){
		ImageInstance cmImage = cytomine.getImageInstance(cmImageInstanceID)
		Project p = Project.find { cmID == cmProjectID }
		User user = User.find { cmPublicKey == publicKey}

		Image irisImage = new DomainMapper(grailsApplication).mapImage(cmImage, null, p.getCmBlindMode())

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
		JSONElement imageJSON = sessionService.injectCytomineImageInstance(cytomine, irisImage, cmImage, p.getCmBlindMode())

		return imageJSON
	}

	/**
	 * Gets images from Cytomine without computing the user's labeling progress.
	 * 
	 * @param cytomine a Cytomine instance
	 * @param cmProjectID the Cytomine project ID
	 * @param withTileURL optionally compute the tile URLs for each image
	 * @return a list of (blinded) IRIS images for the project
	 */
	JSONArray getImages(Cytomine cytomine, long cmProjectID, boolean withTileURL, int offset, int max) throws CytomineException{
		
		// set the client properties for pagination
		cytomine.setOffset(offset)
		cytomine.setMax(max)
		
		ImageInstanceCollection cmImageCollection = cytomine.getImageInstances(cmProjectID)
		def cmProject = cytomine.getProject(cmProjectID)
		
		if (cmProject.getAttr().getAt("success") != null){
			throw new CytomineException(404, "The requested project is not available.");
		}
		
		boolean blindMode = cmProject.get("blindMode")
		int nImages = cmImageCollection.size()

		JSONArray irisImageList = new JSONArray()
		for (int i = 0; i < nImages; i++) {
			ImageInstance cmImage = cmImageCollection.get(i)

			// map the client image to the IRIS image
			Image irisImage = new DomainMapper(grailsApplication).mapImage(cmImage, null, blindMode)

			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			irisImage.setGoToURL(grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + cmProjectID + "-" + cmImage.getId() + "-")
			if (withTileURL) {
				irisImage.setOlTileServerURL(imageService.getImageServerURL(cytomine, cmImage.get("baseImage"), cmImage.getId()));
			}

			// set the Cytomine image as "cytomine" property in the irisImage
			def imageJSON = sessionService.injectCytomineImageInstance(cytomine, irisImage, cmImage, blindMode)

			// add it to the result list
			irisImageList.add(irisImage)
		}

		return irisImageList
	}
	
	/**
	 * Gets the images with progress (paged from Cytomine).
	 *
	 * @param cytomine a Cytomine instance
	 * @param cmProjectID the Cytomine project ID
	 * @param publicKey the public key of the user
	 * @param withTileURL optionally compute the tile URLs for each image
	 *
	 * @return a list of IRIS images
	 *
	 * @throws CytomineException if the user is not found
	 */
	JSONArray getImagesWithProgress(Cytomine cytomine, long cmProjectID, String publicKey, boolean withTileURL) throws CytomineException{
		// ######################## PARALLEL IMPLEMENTATION
		long userID = cytomine.getUser(publicKey).getId()

		// find the session of the calling user
		User u = User.findByCmID(userID)

		// get the session
		Session sess = u.getSession();

		// search for the requested project in the session
		Project irisProject = sess.getProjects().find { it.cmID == cmProjectID }

		// update the project in the local database
		def cmProject = cytomine.getProject(cmProjectID)
		if (cmProject.getAttr().getAt("success") != null){
			throw new CytomineException(404, "The requested project is not available.")
		}
				
		// map the project and overwrite it
		irisProject = new DomainMapper(grailsApplication).mapProject(cmProject, irisProject)
		irisProject.save(flush:true, failOnError:true)
		
		// important for blinding image names
		boolean blindMode = irisProject.cmBlindMode

		def start = System.currentTimeMillis()

		// get all images for that project
		SortedSet<Image> irisImages = irisProject.getImages()

		// cytomine images of this project
		ImageInstanceCollection cmImageCollection = cytomine.getImageInstances(cmProjectID)
		
		int nImages = cmImageCollection.size()
		JSONArray irisImageList = new JSONArray()
		for (int j = 0; j < nImages; j++){
			ImageInstance cmImage = cmImageCollection.get(j)

			// get the image from the list
			Image irisImage = irisImages.find { it.cmID == cmImage.getId() }
			// update the information
			irisImage = new DomainMapper(grailsApplication).mapImage(cmImage, irisImage, blindMode)

			irisProject.addToImages(irisImage)

			// set the Cytomine image as "cytomine" property in the irisImage
			def imageJSON = sessionService.injectCytomineImageInstance(cytomine, irisImage, cmImage, blindMode)

			// add it to the result list
			irisImageList.add(imageJSON)
		}
				
		return irisImageList
	}

	/**
	 * Gets the images with progress (paged from Cytomine).
	 * 
	 * @param cytomine a Cytomine instance
	 * @param cmProjectID the Cytomine project ID
	 * @param publicKey the public key of the user
	 * @param withTileURL optionally compute the tile URLs for each image
	 * 
	 * @return a list of IRIS images
	 * 
	 * @throws CytomineException if the user is not found
	 */
	JSONObject getPagedImagesWithProgress(Cytomine cytomine, long cmProjectID, String publicKey, boolean withTileURL, int offset, int max) throws CytomineException{
		
		// set the client properties for pagination
		cytomine.setOffset(offset)
		cytomine.setMax(max)
		
		// ######################## PARALLEL IMPLEMENTATION
		long userID = cytomine.getUser(publicKey).getId()

		// find the session of the calling user
		User u = User.findByCmID(userID)

		// get the session
		Session sess = u.getSession();

		// search for the requested project in the session
		Project irisProject = sess.getProjects().find { it.cmID == cmProjectID }

		// update the project in the local database
		def cmProject = cytomine.getProject(cmProjectID)
		if (cmProject.getAttr().getAt("success") != null){
			throw new CytomineException(404, "The requested project is not available.")
		}
				
		// map the project and overwrite it
		irisProject = new DomainMapper(grailsApplication).mapProject(cmProject, irisProject)
		irisProject.save(flush:true, failOnError:true)
		
		// important for blinding image names
		boolean blindMode = irisProject.cmBlindMode

		def start = System.currentTimeMillis()

		// get all images for that project from the Cytomine core server
		ImageInstanceCollection cmImageCollection = cytomine.getImageInstances(cmProjectID)

		int nImages = cmImageCollection.size()
		JSONArray irisImageList = new JSONArray()
		for (int o = 0; o < nImages; o++){
			irisImageList.add(null)
		}
		Utils utils = new Utils()

		// define the number of parallel threads
		int nThreads = grailsApplication.config.grails.cytomine.execution.threads

		// multi-threaded implementation of computing the status
		def threadPool = Executors.newFixedThreadPool(nThreads)
		// split the list in subparts, get the indices of splitting
		def splitIndices = utils.getSplitIndices(cmImageCollection, nThreads)
		int nSplits = -1
		if (splitIndices == [0])
			nSplits = 0
		else if (nImages <= nThreads)
			nSplits = nImages - 1
		else if (nImages > nThreads)
			if (nThreads == 1)
				nSplits = splitIndices.size()-1
			else
				nSplits = splitIndices.size()

		log.debug("Splitting the image array " + nSplits + "x")

		int startIdx = 0;
		int endIdx = 0;

		// kick off the threads to work on each list
		for (int i = 0; i <= nSplits; i++){
			log.debug("#### Running part " + i + " of the image list ###")

			boolean islastrun = (i == nSplits)

			// wait for all threads to finish
			Runnable worker = new Runnable() {

						private int sIdx = 0;
						private int eIdx = 0;

						@Override
						public void run() {
							// compute the progress for each item in a list
							log.debug("Worker " + Thread.currentThread().getName() + " processes " + this.sIdx + "-" + this.eIdx)

							// operate on the lists
							for (int j = this.sIdx; j <= this.eIdx; j++) {
								ImageInstance cmImage = cmImageCollection.get(j)
								log.debug("Processing image " + j)

								// map the client image to the IRIS image
								Image irisImage = new DomainMapper(grailsApplication).mapImage(cmImage, null, blindMode)

								//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
								irisImage.setGoToURL(grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + cmProjectID + "-" + cmImage.getId() + "-")
								// this performs an synchronous GET request to the cytomine server
								// and may thus hamper performance
								if (withTileURL) {
									irisImage.setOlTileServerURL(imageService.getImageServerURL(cytomine, cmImage.get("baseImage"), cmImage.getId()));
								}
									
								// retrieve the user's progress on each image and return it in the object
								JSONObject annInfo = utils.getUserProgress(cytomine, cmProjectID, cmImage.getId(), userID)
								// resolving the values from the JSONObject to each image as property
								irisImage.setLabeledAnnotations(annInfo.get("labeledAnnotations"))
								irisImage.setUserProgress(annInfo.get("userProgress"))
								irisImage.setNumberOfAnnotations(annInfo.get("totalAnnotations"))

								// set the Cytomine image as "cytomine" property in the irisImage
								def imageJSON = sessionService.injectCytomineImageInstance(cytomine, irisImage, cmImage, blindMode)

								// add it to the result list
								irisImageList.set(j, imageJSON)
							}
						}

						public void setRange(int startIndex, int endIndex){
							this.sIdx = startIndex;
							this.eIdx = endIndex;
						}
					}


			if (islastrun){
				endIdx = nImages-1;
			} else {
				endIdx = splitIndices.get(i)
			}
			worker.setRange(startIdx, endIdx)
			threadPool.execute(worker);
			startIdx = endIdx+1;
		}

		// wait for the threads to finish
		threadPool.shutdown()
		threadPool.awaitTermination(5L, TimeUnit.MINUTES)
		log.debug("Finished all threads " + threadPool.isTerminated())

		log.debug("Resolving image status for " + nImages +  " images lasted " +
				(System.currentTimeMillis()-start)/1000 + " seconds.")
				
		// the collection object
		JSONObject collection = new JSONObject()
		
		int totalItems = irisProject.cmNumberOfImages
		max = (max==0?totalItems:max)
		
		int pages = 0
		int currentPage = 0
		
		if (totalItems != 0 && offset < totalItems) {
			// compute pages
			pages = Math.ceil(totalItems/max)
			currentPage = (offset/max) + 1
		}
		
		collection.put("currentPage", currentPage) // overwrite current page
		collection.put("pages", pages) // overwrite total number of pages
		collection.put("totalItems", irisProject.cmNumberOfImages) // overwrite total images
		collection.put("images", irisImageList) // set the list of IRIS images
		collection.put("pageItems", irisImageList.size()) // number of page items
				
		return collection
	}

	String getImageServerURL(Cytomine cytomine, long abstrImgID, long imgInstID){
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
		// TODO hard-code the tile urls for performance improvement
		String urls = cytomine.doGet("/api/abstractimage/" + abstrImgID + "/imageservers.json?imageinstance=" + imgInstID)

		//	obj = {"imageServersURLs":["http://image3.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/","http://image4.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/","http://image5.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/","http://image.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/"]}

		// urls are a long string, so break them up in a JSONarray
		org.codehaus.groovy.grails.web.json.JSONObject obj = new org.codehaus.groovy.grails.web.json.JSONObject(urls)

		// prepend the application context on the server
		String appContext = grailsApplication.metadata['app.context']
		
		// imageServersURLs is a property of the object
		def newUrls = obj.imageServersURLs.collect {
			// TODO do not add the irisHost to the URL (currently this errors in saving the image in the DB)
			appContext + "/image/tile" + it.substring(it.indexOf("?"), it.length())
		}
		obj.putAt("imageServersURLs", newUrls)

		return obj as JSON
	}


}
