
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
 * An IRISUserAnnotationTermSettings object is used to synchronize the annotations with Cytomine.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class IRISUserAnnotationTermSettings {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        user nullable: false
        cmAnnotationID nullable: false
    }

    /**
     * The user working with this annotation.
     */
    IRISUser user
    /**
     * The Cytomine project ID of this annotation.
     */
    Long cmProjectID
    /**
     * The Cytomine image instance ID this annotation belongs to.
     */
    Long cmImageInstanceID
    /**
     * The Cytomine domain identifier.
     */
    Long cmAnnotationID
    /**
     * The Cytomine ontology domain identifier.
     */
    Long cmOntologyID
    /**
     * The Cytomine term domain identifier.
     */
    Long cmTermID
}
