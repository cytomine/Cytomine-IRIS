
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
 * Domain class holding per user information
 * on the synchronization status of a project.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class ImageSyncStatus {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
    }

    /**
     * The user this status belongs to.
     */
    IRISUser user

    /**
     * The project ID of the image being synchronized.
     */
    Long cmProjectID

    /**
     * The image ID being synchronized.
     */
    Long cmImageInstanceID

    /**
     * The progress in Integer percent.
     */
    Integer progress = null
}
