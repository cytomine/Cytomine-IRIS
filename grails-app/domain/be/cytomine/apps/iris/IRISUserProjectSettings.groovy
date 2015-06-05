
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

/**
 * An IRISUserProjectSettings object is used to synchronize a project with Cytomine.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class IRISUserProjectSettings {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        user nullable: false
        cmProjectID nullable: false
    }

    /**
     * The user working with this project.
     */
    IRISUser user
    /**
     * The Cytomine domain identifier.
     */
    Long cmProjectID
    /**
     * The Cytomine ontology ID of the project.
     */
    Long cmOntologyID
    /**
     * The currently opened image ID in this project.
     */
    Long currentCmImageInstanceID = null
    /**
     * Per default, disable the project in IRIS.
     * Configuration has to be checked by the admin, which then enables the project.
     */
    Boolean enabled = false
    /**
     * Per default, show all images in the image list view.
     */
    Boolean hideCompletedImages = false
    /**
     * A time stamp, when the settings got deleted.
     * <br>
     * <b>HINT: This property overrides the 'enabled' property!</b>
     */
    Long deleted = null
    /**
     * Flag whether the user in this settings object is a project coordinator.
     */
//    Boolean irisCoordinator = false
}
