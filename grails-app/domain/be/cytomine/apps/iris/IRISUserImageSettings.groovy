
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
 * An IRISUserImageSettings object is used to synchronize the user progress with Cytomine.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class IRISUserImageSettings {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        user nullable: false
        cmImageInstanceID nullable: false
    }

    /**
     * The user working with this image instance.
     */
    IRISUser user
    /**
     * The Cytomine domain model identifier.
     */
    Long cmImageInstanceID
    /**
     * The Cytomine domain model identifier of the project the image belongs to.
     */
    Long cmProjectID
    /**
     * The current annotation in this image.
     */
    Long currentCmAnnotationID = null
    /**
     * Whether the image is enabled or not, default is true.
     */
    Boolean enabled = true
    /**
     * The absolute number of annotations labeled by the user.
     */
    Long labeledAnnotations = 0L
    /**
     * The total number of annotations in this image.
     */
    Long numberOfAnnotations = 0L
    /**
     * The user progress
     */
    Integer progress = 0
    /**
     * A flag whether the user hides already labeled annotations in the navigation.
     */
    Boolean hideCompletedAnnotations = false

    /**
     * The timestamp when the image settings have been deleted, or NULL, if it is still active.
     */
    Long deleted = null

    /**
     * Convenient wrapper method to compute the current progress.
     *
     * @return progress in Integer percent
     */
    Integer computeProgress(){
        this.progress = (this.numberOfAnnotations==0L?0:(int) (this.labeledAnnotations*100/this.numberOfAnnotations))
    }
}
