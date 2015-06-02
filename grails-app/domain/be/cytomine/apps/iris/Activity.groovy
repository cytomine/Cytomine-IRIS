
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
 * An Activity represents an action on the server which will be persisted to the DB.
 *
 * @author Philipp Kainz
 * @since 1.5
 */
class Activity {
    def springSecurityService

    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        description size: 0..5000, blank: true
    }

    def beforeInsert = {
        // TODO enable
//		this.createdBy = springSecurityService.getCurrentUser()?IRISUser.findByUserName('system')?null
    }

    def beforeUpdate = {
//		this.lastUpdatedBy = springSecurityService.getCurrentUser()?IRISUser.findByUserName('system')?null
    }

    /**
     * Activity was updated last by this IRIS user.
     */
    IRISUser lastUpdatedBy
    /**
     * Activity has been created by this IRIS user.
     */
    IRISUser createdBy
    /**
     * The activity regards this user (null also allowed).
     */
    IRISUser user
    /**
     * A short description of the activity
     */
    String description

    /**
     * If present, this activity relates to this Cytomine project.
     */
    Long cmProjectID
    /**
     * If present, this activity relates to this Cytomine image instance.
     */
    Long cmImageID
    /**
     * If present, this activity relates to this Cytomine annotation.
     */
    Long cmAnnotationID
    /**
     * The action as String a user or job did.
     */
    ActivityAction action = ActivityAction.UNSPECIFIED
}
