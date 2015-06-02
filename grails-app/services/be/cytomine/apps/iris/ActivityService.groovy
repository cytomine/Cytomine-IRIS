
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

import be.cytomine.client.models.User
import grails.transaction.Transactional

/**
 * The activity service logs certain (user) activities in the database for evaluation.
 *
 * @author Philipp Kainz
 * @since 1.6
 */
@Transactional
class ActivityService {

    def logProjectCreate(Long cmPjID) {
        String d = "Created project (sync)"
        Activity ac = new Activity(cmProjectID: cmPjID,
                description: d, action: ActivityAction.CREATE)
        ac.save()
    }

    def logProjectUpdate(Long cmPjID) {
        String d = "Updated project (sync)"
        Activity ac = new Activity(cmProjectID: cmPjID,
                description: d, action: ActivityAction.UPDATE)
        ac.save()
    }

    def logProjectCreate(IRISUser user, Long cmPjID) {
        String d = "Created project"
        Activity ac = new Activity(user: user, cmProjectID: cmPjID,
                description: d, action: ActivityAction.CREATE)
        ac.save()
    }

    def logProjectRead(IRISUser user, Long cmPjID) {
        String d = "Read project"
        Activity ac = new Activity(user: user, cmProjectID: cmPjID,
                description: d, action: ActivityAction.READ)
        ac.save()
    }

    def logProjectUpdate(IRISUser user, Long cmPjID) {
        String d = "Updated project"
        Activity ac = new Activity(user: user, cmProjectID: cmPjID,
                description: d, action: ActivityAction.UPDATE)
        ac.save()
    }

    def logProjectDelete(IRISUser user, Long cmPjID) {
        String d = "Deleted project"
        Activity ac = new Activity(user: user, cmProjectID: cmPjID,
                description: d, action: ActivityAction.DELETE)
        ac.save()
    }

    def logImageUpdate(IRISUser user, Long cmPjID, Long cmImgID) {
        String d = "Updated image"
        Activity ac = new Activity(user: user, cmProjectID: cmPjID,
                cmImageID: cmImgID, description: d, action: ActivityAction.UPDATE)
        ac.save()
    }

    def logTermAssign(IRISUser user, Long cmPjID, Long cmImgID, Long cmAnnID, Long cmTermID) {
        String d = "Assigned term " + cmTermID + " to annotation " + cmAnnID + "."
        Activity ac = new Activity(user: user, cmProjectID: cmPjID,
                cmImageID: cmImgID, cmAnnotationID: cmAnnID,
                description: d, action: ActivityAction.TERM_ASSIGN)
        ac.save()
    }

    def logTermDelete(IRISUser user, Long cmPjID, Long cmImgID, Long cmAnnID, Long cmTermID) {
        String d = "Deleted term " + cmTermID + " from annotation " + cmAnnID + "."
        Activity ac = new Activity(user: user, cmProjectID: cmPjID,
                cmImageID: cmImgID, cmAnnotationID: cmAnnID,
                description: d, action: ActivityAction.TERM_DELETE)
        ac.save()
    }

    // GENERIC LOG METHODS
    def logCreate(IRISUser user, String description) {
        Activity ac = new Activity(user: user, description: description,
                action: ActivityAction.CREATE)
        ac.save()
    }


    def logRead(IRISUser user, String description) {
        Activity ac = new Activity(user: user, description: description,
                action: ActivityAction.READ)
        ac.save()
    }

    def logUpdate(IRISUser user, String description) {
        Activity ac = new Activity(user: user, description: description,
                action: ActivityAction.UPDATE)
        ac.save()
    }

    def logDelete(IRISUser user, String description) {
        Activity ac = new Activity(user: user, description: description,
                action: ActivityAction.DELETE)
        ac.save()
    }

    def log(IRISUser user, String description) {
        Activity ac = new Activity(user: user, description: description,
                action: ActivityAction.UNSPECIFIED)
        ac.save()
    }

    def logUserCreate(User cmUser) {
        Activity ac = new Activity(description: "User '" + cmUser.getStr("username") + "' " +
                "has been created from Cytomine client model" , action: ActivityAction.CREATE)
        ac.save()
    }

    def logUserUpdate(User cmUser) {
        Activity ac = new Activity(description: "User '" + cmUser.getStr("username") + "' " +
                "has been updated by Cytomine client model" , action: ActivityAction.UPDATE)
        ac.save()
    }

    def log(String description) {
        Activity ac = new Activity(description: description, action: ActivityAction.SYSTEM_LOG)
        ac.save()
    }


    def logSync(String description) {
        Activity ac = new Activity(description: description, action: ActivityAction.SYNC_JOB)
        ac.save()
    }

    def logSync(IRISUser usr, String description) {
        Activity ac = new Activity(description: description, user: usr, action: ActivityAction.SYNC_JOB)
        ac.save()
    }

}
