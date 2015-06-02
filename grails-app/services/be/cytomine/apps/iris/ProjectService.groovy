
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

import be.cytomine.apps.iris.model.IRISProject
import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.ProjectCollection
import be.cytomine.client.collections.UserCollection
import be.cytomine.client.models.Description
import be.cytomine.client.models.Project
import grails.converters.JSON
import grails.transaction.Transactional
import org.json.simple.JSONArray
import org.json.simple.JSONObject

/**
 * This service handles all CRUD operations of a Project object.
 * It also communicates with the Cytomine host instance, if
 * required. Thus passing the <code>request['cytomine']</code> instance
 * is mandatory for some class methods.
 *
 * @author Philipp Kainz
 *
 */
@Transactional
class ProjectService {
    def springSecurityService
    def grailsApplication
    def syncService
    def activityService

    /**
     * Update an existing IRISUserProjectSettings object by a JSON object delivered in the payload.
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the IRIS user
     * @param cmProjectID the Cytomine project ID
     * @param payload the IRISUserProjectSettings as JSON
     * @return
     * @throws CytomineException
     * @throws Exception
     */
    IRISProject updateProjectSettings(Cytomine cytomine, IRISUser irisUser, Long cmProjectID,
                                      def payload) throws CytomineException, Exception {

        IRISProject irisProject = getProject(cytomine, irisUser, cmProjectID)

        // update the settings
        IRISUserProjectSettings settings = irisProject.getSettings()
        settings.setHideCompletedImages(Boolean.valueOf(payload['hideCompletedImages']))
        settings.save()

        activityService.logProjectUpdate(irisUser, irisProject.cmID)

        return irisProject
    }

    /**
     * Fetches the project list of the user executing this query.
     *
     * @param cytomine a Cytomine instance
     * @param the IRIS user
     *
     * @return a list of IRISProject instances including their settings on this IRIS host as JSON
     */
    JSONObject getAllProjects(Cytomine cytomine, IRISUser irisUser) throws CytomineException, Exception {

        ProjectCollection projectList = cytomine.getProjectsByUser(irisUser.cmID)

        Utils utils = new Utils()

        def projectListJSON = new JSONArray()
        int enabledProjects = 0
        for (int i = 0; i < projectList.size(); i++) {
            Project cmProject = projectList.get(i)

            IRISProject irisProject = new DomainMapper(grailsApplication).mapProject(cmProject, null)
            // get the settings for this user from the DB
            IRISUserProjectSettings settings = IRISUserProjectSettings.
                    findByUserAndCmProjectID(irisUser, cmProject.getId())

            // if no local settings exist for this project
            if (!settings) {
                // create and save them
                settings = new IRISUserProjectSettings(
                        user: irisUser,
                        cmProjectID: cmProject.getId(),
                )

                //set the demo project on this instance initially enabled for each user
                def demoProjectID = grailsApplication.config.grails.cytomine.apps.iris.demoProject.cmID
                if (cmProject.getId() == demoProjectID) {
                    settings.enabled = true
                }

                settings.save()
            }

            // set the ontology ID to the settings
            settings.setCmOntologyID(irisProject.cmOntologyID)

            // count the accessible projects for this user
            if (settings.enabled == true) {
                enabledProjects += 1
            }

            // inject the settings
            irisProject.setSettings(settings)

            // add the project, no matter if it is enabled or not
            projectListJSON.add(utils.toJSONObject(irisProject))
        }

        JSONObject result = new JSONObject()
        result.put("projects", projectListJSON)
        result.put("accessibleProjects", enabledProjects)

        return result
    }

    /**
     * Fetches the current project from the Cytomine host, maps to IRIS domain model
     * and returns the IRIS project.
     *
     * @param cytomine a Cytomine instance
     * @param user the IRIS user
     * @param cmProjectID the Cytomine project ID to get
     * @return the IRIS project with injected cytomine domain
     *
     * @throws CytomineException if the project is is not available for the
     * querying user
     * @throws Exception
     */
    IRISProject getProject(Cytomine cytomine, IRISUser user, Long cmProjectID) throws CytomineException, Exception {

        // TODO get the user from springsecurity service context
//        springSecurityService.getCurrentUser()

        IRISProject irisProject = syncService.synchronizeProject(cytomine, user, cmProjectID)

        // check if the project is available for this IRIS instance
        if (!irisProject.settings.enabled) {
            log.info("Project [" + cmProjectID +
                    "] is not available to '" + user.cmID + "' on this IRIS instance.")

            throw new CytomineException(403, "This project is not available on this IRIS host.")
        }

        // get the user's session
        IRISUserSession sess = user.getSession()
        sess.setCurrentCmProjectID(cmProjectID)

        // store the user session
        sess.merge(flush: true)

        return irisProject
    }

    /**
     * Gets the description of a project from Cytomine.
     *
     * @param cytomine a Cytomine instance
     * @param projectID the Cytomine ID of the project
     *
     * @return the project description
     * @throws CytomineException if the project does not have a description
     */
    Description getProjectDescription(Cytomine cytomine, long projectID) throws CytomineException {
        Description description = cytomine.getDescription(projectID, IRISConstants.CM_PROJECT_DOMAINNAME)
        return description
    }

    /**
     * Checks, whether or not the calling user has access to a
     * requested project.
     *
     * @param cytomine a Cytomine instance
     * @param projectID the requested project ID
     * @return <code>true</code> if the project is available for this user, <code>false</code>
     * otherwise
     *
     * @throws CytomineException if the project does not exist
     * @throws Exception on any exception
     */
    boolean isAvailable(Cytomine cytomine, long projectID) throws CytomineException {

        def p = cytomine.getProject(projectID)

        if (p != null) {
            // project exists and user is authorized
            return true
        } else {
            // project exists and user is NOT authorized
            return false
        }
    }

    /**
     * Get the project users.
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the IRISUser
     * @param cmProjectID the Cytomine project ID
     * @param options a map of options, e.g. 'adminOnly' retrieves all admins exclusively
     * @return a UserCollection
     * @throws CytomineException
     * @throws Exception
     */
    UserCollection getProjectUsers(Cytomine cytomine, IRISUser irisUser, Long cmProjectID, Map options)
        throws CytomineException, Exception{

        UserCollection projectUsers

        if (new Boolean(options['adminOnly']).booleanValue()){
            projectUsers = cytomine.getProjectAdmins(cmProjectID)
        } else {
            projectUsers = cytomine.getProjectUsers(cmProjectID)
        }

        return projectUsers
    }
}
