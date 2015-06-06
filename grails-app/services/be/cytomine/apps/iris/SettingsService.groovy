
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
import be.cytomine.client.models.Project
import grails.transaction.Transactional

/**
 * Service methods for project settings (to be administered by the
 * project coordinator).
 *
 * @author Philipp Kainz
 */
@Transactional
class SettingsService {

    /**
     * Provides a user list for the requested project ID.
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the calling IRISUser
     * @param cmProjectID the Cytomine project ID
     * @param options a map of options
     * @param offset pagination offset
     * @param max pagination maximum
     * @return
     * @throws CytomineException
     * @throws Exception
     */
    def getUserList(Cytomine cytomine, IRISUser irisUser,
                    Long cmProjectID, Map options, int offset, int max)
            throws CytomineException, Exception{

        // try to get the project from cytomine (checks ACL)
        Project cmProject = cytomine.getProject(cmProjectID)

        // fetch the users from the database



    }
}
