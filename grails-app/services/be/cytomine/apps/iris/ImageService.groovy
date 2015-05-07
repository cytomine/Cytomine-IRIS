package be.cytomine.apps.iris

import be.cytomine.apps.iris.model.IRISImage
import be.cytomine.apps.iris.model.IRISProject
import grails.converters.JSON
import grails.transaction.Transactional
import org.json.simple.JSONArray
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
    def springSecurityService
    def sessionService
    def projectService

    /**
     * Gets a list of images from Cytomine without computing the user's labeling progress.
     * This service checks the 'enabled' status of the images for the querying user.
     * Particular images may be disabled for particular users, and these will be filtered out.
     *
     * @param cytomine a Cytomine instance
     * @param user the IRIS user
     * @param cmProjectID the Cytomine project ID
     * @param offset a pagination parameter
     * @param max a pagination parameter
     *
     * @return a list of (blinded) IRIS images, which are enabled on IRIS for this project
     */
    List<IRISImage> getImages(Cytomine cytomine, IRISUser user,
                              Long cmProjectID,
                              Integer offset, Integer max)
            throws CytomineException, Exception {

        // set the client properties for pagination
        cytomine.setOffset(offset)
        cytomine.setMax(max)

        IRISProject irisProject = projectService.getProject(cytomine, user, cmProjectID)

        // get all images from the server
        ImageInstanceCollection cmImageCollection = cytomine.getImageInstances(cmProjectID)

        // compute the total number of images
        int nImages = cmImageCollection.size()

        int accessibleImages = 0
        List<IRISImage> irisImageList = new ArrayList<IRISImage>(nImages)
        // add all settings into the image instance
        for (int i = 0; i < nImages; i++) {
            ImageInstance cmImage = cmImageCollection.get(i)

            // map the client image to the IRIS image WITHOUT SAVING to IRIS db
            IRISImage irisImage = new DomainMapper(grailsApplication).mapImage(cmImage, null, irisProject.cmBlindMode)

            // get the settings for this user on this image
            IRISUserImageSettings settings = IRISUserImageSettings
                    .findByUserAndCmImageInstanceID(user, cmImage.getId())

            // write local settings, if they don't exist
            if (!settings) {
                settings = new IRISUserImageSettings(
                        user: user,
                        cmImageInstanceID: cmImage.getId(),
                        numberOfAnnotations: irisImage.numberOfAnnotations,
                        cmProjectID: irisProject.cmID
                )

                // save them locally
                settings.save(flush: true)
            }

            // HIDE DISABLED IMAGES ON THE CLIENT, JUST ADD ENABLED ONES
            if (settings.enabled){
                accessibleImages += 1

                // add the settings objects
                irisImage.setSettings(settings)
                irisImage.setProjectSettings(irisProject.settings)

                // add it to the result list
                irisImageList.add(irisImage)
            }
        }

        return irisImageList
    }


    /**
     * Gets a list of all images from Cytomine without computing the user's labeling progress.
     *
     * @param cytomine a Cytomine instance
     * @param user the IRIS user
     * @param cmProjectID the Cytomine project ID
     * @param offset a pagination parameter
     * @param max a pagination parameter
     *
     * @return a list of all (blinded) IRIS images
     */
    List<IRISImage> getAllImages(Cytomine cytomine, IRISUser user,
                              Long cmProjectID,
                              Integer offset, Integer max)
            throws CytomineException, Exception {

        // set the client properties for pagination
        cytomine.setOffset(offset)
        cytomine.setMax(max)

        IRISProject irisProject = projectService.getProject(cytomine, user, cmProjectID)

        // get all images from the server
        ImageInstanceCollection cmImageCollection = cytomine.getImageInstances(cmProjectID)

        // compute the total number of images
        int nImages = cmImageCollection.size()

        List<IRISImage> irisImageList = new ArrayList<IRISImage>(nImages)
        // add all settings into the image instance
        for (int i = 0; i < nImages; i++) {
            ImageInstance cmImage = cmImageCollection.get(i)

            // map the client image to the IRIS image WITHOUT SAVING to IRIS db
            IRISImage irisImage = new DomainMapper(grailsApplication).mapImage(cmImage, null, irisProject.cmBlindMode)

            // add it to the result list
            irisImageList.add(irisImage)
        }

        return irisImageList
    }

    /**
     * Get an URL for the image tiles.
     *
     * @param cytomine
     * @param abstrImgID
     * @param imgInstID
     * @return
     */
    @Deprecated
    String getImageServerURL(Cytomine cytomine, long abstrImgID, long imgInstID) {
        def urls = getImageServerURLs(cytomine, abstrImgID, imgInstID)
        def url = new org.codehaus.groovy.grails.web.json.JSONObject(urls.toString()).getAt("imageServersURLs")[0]
        return url.toString()
    }

    /**
     * Gets the image server URLs for a given image.

     * @param cytomine
     * @param abstrImgID
     * @param imgInstID
     * @return the URLs for a given abstractImage for the OpenLayers instance as JSONObject
     */
    def getImageServerURLs(Cytomine cytomine, long abstrImgID, long imgInstID) {
        String irisHost = grailsApplication.config.grails.cytomine.apps.iris.host;
        String cmHost = grailsApplication.config.grails.cytomine.host;

        // perform a synchronous get request to the Cytomine host server
        String urls = cytomine.doGet("/api/abstractimage/" + abstrImgID + "/imageservers.json?imageinstance=" + imgInstID)

        //	obj = {"imageServersURLs":["http://image3.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/","http://image4.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/","http://image5.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/","http://image.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990//1412329489945/BM_GRAZ_HE_0006.svs/"]}

        // urls are a long string, so break them up in a JSONarray
        org.codehaus.groovy.grails.web.json.JSONObject obj = new org.codehaus.groovy.grails.web.json.JSONObject(urls)

        // prepend the application context on the server
        String appContext = grailsApplication.metadata['app.context']

        // imageServersURLs is a property of the object
        def newUrls = obj.imageServersURLs.collect {
            appContext + "/image/tile" + it.substring(it.indexOf("?"), it.length())
        }
        obj.putAt("imageServersURLs", newUrls)

        return obj as JSON
    }
}
