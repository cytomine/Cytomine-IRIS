
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

import be.cytomine.apps.iris.model.IRISAnnotation
import be.cytomine.apps.iris.model.IRISImage
import be.cytomine.apps.iris.model.IRISProject
import be.cytomine.apps.iris.sync.RemoteConfiguration
import org.apache.log4j.Logger
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

/**
 * This class maps from the Cytomine client domain models to the IRIS 
 * domain models.
 *
 * @author Philipp Kainz
 *
 */
class DomainMapper {

    def grailsApplication
    String cmHost
    String irisHost
    String appContext

    /**
     * Constructor accepting a 'grailsApplication' configuration object
     *
     * @param grailsApplication
     */
    DomainMapper(def grailsApplication) {
        this.grailsApplication = grailsApplication
        this.cmHost = grailsApplication.config.grails.cytomine.host
        this.irisHost = grailsApplication.config.grails.cytomine.apps.iris.host
        this.appContext = grailsApplication.metadata['app.context']
    }

    /**
     * Map a user. If the irisUser is null, a new user will be generated, otherwise the
     * values are overwritten and the object is returned.
     *
     * @param cmUser a Cytomine user (be.cytomine.client.models.User)
     * @param irisUser an IRIS user, or null (then a new user is generated)
     * @param options a map with options such as ['noAPIKeys':true]
     * @return an IRIS user instance
     */
    IRISUser mapUser(be.cytomine.client.models.User cmUser, IRISUser irisUser, Map options=['noAPIKeys':false]) {
        if (irisUser == null) {
            irisUser = new IRISUser()
        }

        // map the properties from the client user to the IRIS user model
        irisUser.setCmID(cmUser.getId())
        irisUser.setCmUserName(cmUser.getStr("username"))
        irisUser.setCmLastName(cmUser.getStr("lastname"))
        irisUser.setCmFirstName(cmUser.getStr("firstname"))
        if (options != null
                && options['noAPIKeys'] != null
                && options['noAPIKeys'] == true){
            // skip the api key mapping
        }else {
            irisUser.setCmPublicKey(cmUser.getStr("publicKey"))
            irisUser.setCmPrivateKey(cmUser.getStr("privateKey"))
        }
        irisUser.setCmEmail(cmUser.getStr("email"))
        irisUser.setCmPasswordExpired(cmUser.getBool("passwordExpired"))

        irisUser.setCmCreated(cmUser.getLong("created"))
        irisUser.setCmUpdated(cmUser.getLong("updated"))
        irisUser.setCmDeleted(cmUser.getLong("deleted"))

        return irisUser
    }

    /**
     * Map a project. If the irisProject is null, a new project will be generated, otherwise the
     * values are overwritten and the object is returned.
     *
     * @param cmProject be.cytomine.client.models.Project
     * @param irisProject the IRIS domain model project
     * @return the IRIS project
     */
    IRISProject mapProject(be.cytomine.client.models.Project cmProject, IRISProject irisProject) {
        if (irisProject == null) {
            irisProject = new IRISProject()
        }

        irisProject.setCmID(cmProject.getId())
        irisProject.setCmName(cmProject.getStr("name"))
        irisProject.setCmBlindMode(cmProject.getBool("blindMode"))
        irisProject.setCmOntologyID(cmProject.getLong("ontology"))
        irisProject.setCmOntologyName(cmProject.getStr("ontologyName"))
        irisProject.setCmDisciplineID(cmProject.getLong("discipline"))
        irisProject.setCmDisciplineName(cmProject.getStr("disciplineName"))

        irisProject.setCmCreated(cmProject.getLong("created"))
        irisProject.setCmUpdated(cmProject.getLong("updated"))

        irisProject.setIsReadOnly(cmProject.getBool("isReadOnly"))
        irisProject.setIsClosed(cmProject.getBool("isClosed"))
        irisProject.setHideAdminsLayers(cmProject.getBool("hideAdminsLayers"))
        irisProject.setHideUsersLayers(cmProject.getBool("hideUsersLayers"))
        irisProject.setRetrievalDisable(cmProject.getBool("retrievalDisable"))
        irisProject.setRetrieveAllOntology(cmProject.getBool("retrieveAllOntology"))

        irisProject.setCmNumberOfAnnotations(cmProject.getLong("numberOfAnnotations"))

        irisProject.setCmNumberOfImages(cmProject.getLong("numberOfImages"))

        return irisProject
    }

    /**
     * Map an image.
     *
     * @param cmImage be.cytomine.client.models.ImageInstance
     * @param irisImage the IRIS domain model image, creates a new one, if <code>null</code>
     * @return the IRIS image
     */
    IRISImage mapImage(be.cytomine.client.models.ImageInstance cmImage, IRISImage irisImage, boolean blindMode) {
        if (irisImage == null) {
            irisImage = new IRISImage()
        }

        // map required properties from the client model
        irisImage.setCmID(cmImage.getId())
        irisImage.setCmProjectID(cmImage.getLong("project"))
        irisImage.setCmCreated(cmImage.getLong("created"))
        irisImage.setCmBaseImageID(cmImage.getLong("baseImage"))
        irisImage.setCmReviewed(cmImage.getBool("reviewed"))
        irisImage.setCmInReview(cmImage.getBool("inReview"))
        irisImage.setMagnification(cmImage.getLong("magnification"))
        irisImage.setResolution(cmImage.getDbl("resolution"))
        irisImage.setDepth(cmImage.getLong("depth"))

        if (blindMode) {
            irisImage.setOriginalFilename("[BLIND]" + cmImage.getId())
            irisImage.setInstanceFilename(irisImage.getOriginalFilename())
        } else {
            irisImage.setOriginalFilename(cmImage.getStr("originalFilename"))
            irisImage.setInstanceFilename(cmImage.getStr("instanceFilename"))
        }

        // direct link to the image explorer on the Cytomine host
        irisImage.setGoToURL(cmHost + "/#tabs-image-" + cmImage.getLong("project") + "-" + cmImage.getId() + "-")
        // hard-code relative tile url
        irisImage.setOlTileServerURL(appContext + "/image/tile?&mimeType=" + cmImage.getStr("mime") + "&zoomify=" + cmImage.get("fullPath") + "/")
        // replace the host in the macro url
        irisImage.setMacroURL(cmImage.getStr("macroURL").replace(cmHost, irisHost))
        irisImage.setMime(cmImage.getStr("mime"))

        irisImage.setWidth(cmImage.getLong("width"))
        irisImage.setHeight(cmImage.getLong("height"))

        // store the total number of annotations in this image
        irisImage.setNumberOfAnnotations(cmImage.getLong("numberOfAnnotations"))

        return irisImage
    }

    /**
     * Map an annotation.
     *
     * @param cmAnnotation be.cytomine.client.models.Annotation
     * @param irisAnnotation the IRIS domain model annotation, if <code>null</code> a new annotation will be created
     * @return the IRIS annotation
     */
    IRISAnnotation mapAnnotation(be.cytomine.client.models.Annotation cmAnnotation, IRISAnnotation irisAnnotation) throws Exception {
        if (irisAnnotation == null) {
            irisAnnotation = new IRISAnnotation()
        }

        // map required properties from the client model
        irisAnnotation.setCmID(cmAnnotation.getId())
        irisAnnotation.setCmProjectID(cmAnnotation.getLong("project"))
        irisAnnotation.setCmImageID(cmAnnotation.getLong("image"))
        irisAnnotation.setCmCreatorUserID(cmAnnotation.get("user"))
        irisAnnotation.setCmImageURL(cmAnnotation.getStr("imageURL"))
        irisAnnotation.setCmCropURL(cmAnnotation.getStr("cropURL"))
        irisAnnotation.setDrawIncreasedAreaURL(cmHost
                + "/api/annotation/"
                + cmAnnotation.getId()
                + "/crop.png?increaseArea=8&maxSize=256&draw=true")
        irisAnnotation.setCmSmallCropURL(cmAnnotation.getStr("smallCropURL"))
        irisAnnotation.setSmallCropURL(cmAnnotation.getStr("smallCropURL").toString()
                .replace(cmHost, irisHost))

        // map centroid and location object
        try {
            irisAnnotation.setCmLocation(cmAnnotation.getStr("location"))
        } catch (Exception e) {
            Logger.getLogger(DomainMapper.class).warn("'Location' information does not exist for this instance of " + cmAnnotation.getClass() + ".")
        }

        // the centroid can be in a distinct object, or in extra coordinates
        try {
            if (cmAnnotation.get("centroid") == null) {
                irisAnnotation.setCmCentroidX(Double.valueOf(cmAnnotation.get("x")))
                irisAnnotation.setCmCentroidY(Double.valueOf(cmAnnotation.get("y")))
            } else {
                irisAnnotation.setCmCentroidX(Double.valueOf(cmAnnotation.get("centroid").get("x")))
                irisAnnotation.setCmCentroidY(Double.valueOf(cmAnnotation.get("centroid").get("y")))
            }
        } catch (Exception e) {
            Logger.getLogger(DomainMapper.class).warn("'Centroid' information does not exist for this instance of " + cmAnnotation.getClass() + ".")
        }

        return irisAnnotation
    }

    RemoteConfiguration mapRemoteConfiguration(String rcStr, RemoteConfiguration rConf) {
        if (rConf == null) {
            rConf = new RemoteConfiguration()
        }

        JSONParser parser = new JSONParser()
        Object rcObject = parser.parse(new StringReader(rcStr))
        JSONObject rcJSON = (JSONObject) rcObject

        // TODO VALIDATE CONFIGURATION
        rConf.setIrisHost(rcJSON.get("irisHost"))
        rConf.setIrisVersion(rcJSON.get("irisVersion"))
        rConf.setAdminEmail(rcJSON.get("adminEmail"))

        return rConf
    }

    /**
     * Updates the values of the destination configuration using the values from the source configuration.
     *
     * @param src source
     * @param dest destination
     * @return the destination object
     */
    RemoteConfiguration updateRemoteConfiguration(RemoteConfiguration src, RemoteConfiguration dest) {
        if (dest == null) {
            dest = new RemoteConfiguration()
        }

        dest.setIrisHost(src.getIrisHost())
        dest.setIrisVersion(src.getIrisVersion())
        dest.setAdminEmail(src.getAdminEmail())

        return dest
    }
}
