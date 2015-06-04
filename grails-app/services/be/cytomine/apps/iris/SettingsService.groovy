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
