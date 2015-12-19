/* Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.cytomine.apps.iris

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.ImageInstanceCollection
import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import grails.transaction.Transactional
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat

/**
 * This service is responsible for exporting data to the client.
 */
@Transactional
class ExportService {

    def statisticsService
    def grailsApplication
    def activityService

    def exportAgreementListToPDF() {

    }

    def exportAgreementListToCSV(Cytomine cytomine,
                                 IRISUser irisUser,
                                 long cmProjectID,
                                 def annotationIDs,
                                 def userIDs,
                                 def termIDs,
                                 def imageIDs,
                                 def settings) throws CytomineException, Exception {

        activityService.log(irisUser, "Requesting export of the agreement list to CSV.")



    }

    /**
     * Export an image dataset according to given settings:
     *        // client settings is a JSON object
     *        settings = {
     *            scope: 'all', // can be one of {all | filtered | currentPage | pages }
     *            pages: "e.g. 1-3; 5; 10;", // is read, if scope == 'pages'
     *            minBB: true, // export the minimum bounding box, i.e. just the annotation
     *            fixedWindow: false, // export a fixed window around the annotations centroid
     *            fixedWindowDimensions: { width: 100, height: 100 }, // the window dimensions
     *            dynamicWindow: false, // export a dynamic window around the annotation's minimum bounding box
     *            dynamicWindowBorder: 0, // the border around the annotation's minimum bounding box, is read when dynamicWindow == true
     *            levels: "0" // level 0 is the original maximum resolution of the image (bottom pyramid level)
     *        }
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the IRISUser
     * @param cmProjectID the Cytomine project ID
     * @param annotationIDs a List of Cytomine annotation IDs to be exported
     * @param userIDs a List of Cytomine user IDs to be exported
     * @param termIDs a List of Cytomine ontology term IDs to be exported
     * @param imageIDs a List of Cytomine image IDs to be exported
     * @param settings the settings object received from the client
     * @returns the path to the storage file
     * @throws CytomineException
     * @throws Exception
     */
    def exportDataset(Cytomine cytomine,
                      IRISUser irisUser,
                      long cmProjectID,
                      def annotationIDs,
                      def userIDs,
                      def termIDs,
                      def imageIDs,
                      def settings) throws CytomineException, Exception {

        activityService.log(irisUser, "Requesting export of IRIS image dataset.")

        // some date parameter settings
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm")
        Date date = new Date()

        DomainMapper dm = new DomainMapper(grailsApplication)
        Utils utils = new Utils()

        // the directory on the file server where the data is stored
        String fileServerKey = 'storage'

        // the folder on the local (server) hard disk
        String path_local_storage_root = grailsApplication.config.grails.plugins.fileserver.paths[fileServerKey]
        // the sub directory name for this export request
        String subDirName = UUID.randomUUID().toString()

        // the export directory (usually a (temporary) directory in the storage of the server)
        def path_export_root = new File(path_local_storage_root + File.separator + subDirName)
        def path_export_root_str = path_export_root.toString() + File.separator
        if (!path_export_root.exists()) {
            path_export_root.mkdirs()
        }

        // the image format to be exported (standard is JPG without alpha channel
        String image_format = settings['format']==null?"jpg":settings['format']
        boolean removeAlpha = true
        if (image_format.endsWith("_alpha")) {
            image_format = image_format.substring(0, image_format.length() - "_alpha".length())
            removeAlpha = false
        }

        // create a file name (current date)
        String zipFileName = "IRISImageDataset_" + sdf.format(date) + ".zip"
        // put the zip file into the subdirectory of the storage
        String path_local_zipfile = path_export_root_str + zipFileName

        // for some exports, we need additional annotation infos
        def filter_options = [:]
        if (settings['dynamicWindow']) {
            filter_options['showWKT'] = "true"
        }

        // get the images
        ImageInstanceCollection images = cytomine.getImageInstances(cmProjectID)
        // construct a map for quick lookup of image dimensions
        def imageDimensions = [:]
        images.list.each { imageDimensions[it['id']] = [it['width'], it['height']] }

        // get the agreement list in the correct format
        def statistics = statisticsService.getAgreementList(
                cytomine,
                irisUser,
                cmProjectID,
                imageIDs.join(','),
                termIDs.join(','),
                userIDs.join(','),
                filter_options,
                0,
                0)

        // a file where the export warnings go (e.g. if an annotation does not have the requested resolution levels)
        def warningsFile = new File(path_export_root_str + "warnings.txt")

        // filter out only the annotations in the query list (NOT ideal for performance)
        annotationIDs = annotationIDs.collect {
            Long.valueOf(it)
        }
        def annotations = statistics.annotationStats.findAll {
            it.cmID in annotationIDs
        }

        // record some statistics of the dataset
        def ds_classes = [:] // number of classes in the dataset
        def ds_unique_images = [:] // number of unique images in the dataset

        // run through the list of all filtered annotations and download the images accordingly
        // also, store the information on the specific annotation in a text file with the name
        int i = 1;
        annotations.each {
                // each annotation is a HashMap
            annotation ->
                // change the key for "assignmentRanking" to "labelDistribution"
                annotation["labelDistribution"] = annotation["assignmentRanking"]
                annotation.remove("assignmentRanking")

                // record statistics
                if (!ds_unique_images.containsKey(annotation['cmImageID']))
                    ds_unique_images[annotation['cmImageID']] = 1
                else
                    ds_unique_images[annotation['cmImageID']] = ds_unique_images[annotation['cmImageID']] + 1

                for (def entry in annotation['labelDistribution']){
                    ds_classes[entry['termID']] = 1
                }

                // make a distinct directory for each annotation
                def annDirectory = new File(path_export_root_str + annotation['cmID'])
                if (!annDirectory.exists()) {
                    annDirectory.mkdirs()
                }
                // store the information file
                def infoFile = new File(annDirectory.toString() + File.separator + annotation['cmID'] + ".txt")
                infoFile.withWriter('UTF-8') { writer ->
                    writer.write((annotation as JSON).toString(true))
                }

                // ##########################
                // download the image data
                // for each zoom level
                for (def lvl in settings['levels']) {
                    lvl = Integer.valueOf(String.valueOf(lvl))
                    // skip negative levels
                    if (lvl < 0) {
                        warningsFile.withWriterAppend('UTF-8') { writer ->
                            writer.println "Skipping image resolution level " + lvl
                        }
                        continue
                    }

                    try {
                        // make a new subdirectory for each image resolution level
                        def lvlDir = new File(annDirectory.toString() + File.separator + "resolution_" + lvl)
                        def lvlDir_str = lvlDir.toString() + File.separator
                        if (!lvlDir.exists()) {
                            lvlDir.mkdirs()
                        }

                        // minimum bounding box image
                        if (settings['minBB']) {
                            // create the directory
                            def minBBDir = new File(lvlDir_str + "minBB")
                            if (!minBBDir.exists()) {
                                minBBDir.mkdirs()
                            }

                            String targetFileName = minBBDir.toString() + File.separator + annotation['cmID'] + "." + image_format

                            // construct the download URL
                            String downloadURL_minBB = cytomine.host + "/api/userannotation/" + annotation['cmID'] + "/crop.png?zoom=" + lvl
                            // println "Annotation minBB URL: " + downloadURL_minBB
                            def successful = false
                            int attempts = 0
                            while (!successful) {
                                try {
                                    //cytomine.downloadPicture(downloadURL_minBB, targetFileName, image_format)
                                    BufferedImage bi = cytomine.downloadPictureAsBufferedImage(downloadURL_minBB, 'png')
                                    if (removeAlpha){
                                        bi = utils.removeAlphaChannel(bi)
                                    }
                                    ImageIO.write(bi, image_format, new File(targetFileName))
                                    successful = true
                                } catch (Exception ex) {
                                    def errorMsg = "Cannot download minimum bounding box image for annotation [" +
                                            annotation['cmID'] + "] at resolution " + lvl + ". Trying again..."
                                    log.error(errorMsg, ex)
                                    attempts++
                                    if (attempts > 50)
                                        throw new CytomineException(404, errorMsg)
                                }
                            }
                        }

                        // fixed window size image
                        if (settings['fixedWindow']) {
                            def fWDir = new File(lvlDir_str + "fixedWindow")
                            if (!fWDir.exists()) {
                                fWDir.mkdirs()
                            }

                            def width = Integer.valueOf(settings['fixedWindowDimensions']['width'])
                            def height = Integer.valueOf(settings['fixedWindowDimensions']['height'])

                            // get the centroid (x/y)
                            double cX = annotation["x"]
                            double cY = annotation["y"]

                            double window_offset_x = (1.0d * width / 2)
                            double window_offset_y = (1.0d * height / 2)

                            int minX_window = Math.round(cX - window_offset_x)
                            // tricky: get the correct Y coordinates of the center
                            int minY_window = Math.round((imageDimensions[annotation["cmImageID"]][1] - cY) - window_offset_y)

                            String targetFileName = fWDir.toString() + File.separator + annotation['cmID'] + "." + image_format

                            // download the window around the annotation center
                            String window_url = cytomine.host + "/api/imageinstance/" + annotation["cmImageID"] + "/window-" + minX_window + "-" + minY_window + "-" + width + "-" + height + ".png?zoom=" + lvl
                            // println "Window url: " + window_url;
                            def successful = false
                            int attempts = 0
                            while (!successful) {
                                try {
                                    //cytomine.downloadPicture(window_url, targetFileName, image_format);
                                    BufferedImage bi = cytomine.downloadPictureAsBufferedImage(window_url, 'png')
                                    if (removeAlpha){
                                        bi = utils.removeAlphaChannel(bi)
                                    }
                                    ImageIO.write(bi, image_format, new File(targetFileName))
                                    successful = true
                                } catch (Exception ex) {
                                    def errorMsg = "Cannot download fixed window image for annotation [" +
                                            annotation['cmID'] + "] at resolution " + lvl + ". Trying again..."
                                    log.error(errorMsg)
                                    attempts++
                                    if (attempts > 50)
                                        throw new CytomineException(404, errorMsg)
                                }
                            }
                        }

                        // dynamic window size image
                        if (settings['dynamicWindow']) {
                            def dWDir = new File(lvlDir_str + "dynamicWindow")
                            if (!dWDir.exists()) {
                                dWDir.mkdirs()
                            }

                            // TODO possible future implementation: allow dynamic border to be negative
                            def border = Integer.valueOf(settings['dynamicWindowBorder'])

                            //Construct Java geometry object from Cytomine annotation location
                            Geometry annotation_geometry = new WKTReader().read(annotation['location'])

                            Envelope envelope = annotation_geometry.getEnvelopeInternal()

                            // get original coordinates
                            double minX = envelope.getMinX()
                            double maxX = envelope.getMaxX()
                            double minY = imageDimensions[annotation["cmImageID"]][1] - envelope.getMinY()
                            double maxY = imageDimensions[annotation["cmImageID"]][1] - envelope.getMaxY()

                            // expand the envelope
                            double crop_minX = 1.0d * Math.max(0, minX - border)
                            double crop_maxX = 1.0d * Math.min(imageDimensions[annotation["cmImageID"]][0], maxX + border)
                            double crop_minY = 1.0d * Math.max(0, minY - envelope.getHeight() - border)
                            double crop_maxY = 1.0d * Math.min(imageDimensions[annotation["cmImageID"]][1], maxY + envelope.getHeight() + border)

                            double cX = envelope.centre().x
                            double cY = envelope.centre().y

                            int minX_window = Math.round(crop_minX)
                            int minY_window = Math.round(crop_minY)
                            int width = Math.abs(Math.round(crop_maxX - crop_minX))
                            int height = Math.abs(Math.round(crop_maxY - crop_minY))

                            String targetFileName = dWDir.toString() + File.separator + annotation['cmID'] + "." +image_format

                            // construct the URL
                            String window_url = cytomine.host + "/api/imageinstance/" + annotation["cmImageID"] + "/window-" + minX_window + "-" + minY_window + "-" + width + "-" + height + ".png?zoom=" + lvl
                            //println "tile url: " + tile_url;

                            def successful = false
                            int attempts = 0
                            while (!successful) {
                                try {
                                    //cytomine.downloadPicture(window_url, targetFileName, image_format);
                                    BufferedImage bi = cytomine.downloadPictureAsBufferedImage(window_url, 'png')
                                    if (removeAlpha){
                                        bi = utils.removeAlphaChannel(bi)
                                    }
                                    ImageIO.write(bi, image_format, new File(targetFileName))
                                    successful = true
                                } catch (Exception ex) {
                                    def errorMsg = "Cannot download dynamic window image for annotation [" +
                                            annotation['cmID'] + "] at resolution " + lvl + ". Trying again..."
                                    log.error(errorMsg)
                                    attempts++
                                    if (attempts > 50)
                                        throw new CytomineException(404, errorMsg)
                                }
                            }

                        }


                    } catch (CytomineException ex) {
                        def errorMsg = "Cannot download resolution level " + lvl + " for annotation " + annotation['cmID']
                        log.error(errorMsg, ex)
                        warningsFile.withWriterAppend('UTF-8') { writer ->
                            writer.println errorMsg
                        }
                    }
                }

                int progress = (int) i * 100 / annotations.size()
                i++
                if (progress % 25 == 0)
                    log.info(progress + "% done")
        }

        // write the ontology JSON to a txt file
        def termsJSON = statistics.terms as JSON
        def termsFile = new File(path_export_root_str + "ontology.txt")
        termsFile.withWriter('UTF-8') { writer ->
            writer.write(termsJSON.toString(true))
        }

        // write the user JSON to a txt file
        def usersJSON = statistics.users as JSON
        def usersFile = new File(path_export_root_str + "users.txt")
        usersFile.withWriter('UTF-8') { writer ->
            writer.write(usersJSON.toString(true))
        }

        // write the export parameters to a README txt file
        def settingsJSON = settings as JSON
        def settingsFile = new File(path_export_root_str + "README.txt")
        settingsFile.withWriter('UTF-8') { writer ->
            writer.println("Image Dataset Export")
            writer.println("====================")

            writer.println("Image database: " + grailsApplication.config.grails.cytomine.host)
            writer.println("IRIS host: " + grailsApplication.config.grails.cytomine.apps.iris.host)
            writer.println("IRIS version: " + grailsApplication.metadata['app.version'])
            writer.println("")
            writer.println("Exporting user: " + irisUser.cmFirstName + " " + irisUser.cmLastName)
            writer.println("Date/Time of export: " + sdf.format(date))
            writer.println("Number of annotations: " + annotations.size())
            writer.println("Number of unique images (statistics below): " + ds_unique_images.size())
            writer.println("Number of unique terms (IDs below): " + ds_classes.size())
            writer.println("Number of users: " + statistics.users.size())
            writer.println("Number of (all) ontology terms: " + statistics.terms.size())
            writer.println("")

            writer.write("Number of annotations per unique image: " + (ds_unique_images as JSON).toString(true))
            writer.println("")

            writer.write("IDs of unique terms: " + (ds_classes.collect { k,v -> k} as JSON).toString(true))
            writer.println("")
            writer.println("")


            // write the parameters
            writer.println("Export Parameters")
            writer.println("=================")
            writer.write(settingsJSON.toString(true))
        }

        // ZIP THE FOLDER
        def ant = new AntBuilder()
        ant.zip(destfile: path_local_zipfile,
                basedir: path_export_root_str,
                includes: "**/*")

        // return the path to the file on the server
        // don't use leading slash, since the client needs to assemble the full URL
        return 'api/download/' + fileServerKey + "/" + subDirName + "/" + zipFileName
    }
}
