
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

import be.cytomine.client.CytomineException
import org.json.simple.JSONObject;

import be.cytomine.client.Cytomine;
import grails.transaction.Transactional

import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec

@Transactional
class AdminService {

    def grailsApplication
    def mailService

    /**
     * Gets the application name
     * @return the name of the application
     */
    String getAppName() {
        return grailsApplication.metadata['app.name']
    }

    /**
     * Gets the version of the IRIS module.
     * @return the version as string
     */
    String getAppVersion() {
        return grailsApplication.metadata['app.version']
    }

    /**
     * Gets the context of the IRIS module.
     * @return the context as string
     */
    String getAppContext() {
        return grailsApplication.metadata['app.context']
    }

    /**
     * Gets the Grails version of the IRIS module.
     * @return the version as string
     */
    String getGrailsVersion() {
        return grailsApplication.metadata['app.grails.version']
    }

    /**
     * Gets the servlet version of the IRIS module.
     * @return the version as string
     */
    String getServletVersion() {
        return grailsApplication.metadata['app.servlet.version']
    }

    /**
     * Gets the Cytomine host URL from the configuration.
     * @return the URL of the host in Config.groovy and the connection status
     */
    JSONObject getCytomineHostAddress(Cytomine cytomine) {
        JSONObject hostInfo = new JSONObject()
        hostInfo.put("host", grailsApplication.config.grails.cytomine.host)
        if (!cytomine.testHostConnection()) {
            hostInfo.put("available", false)
        } else {
            hostInfo.put("available", true)
        }
        return hostInfo
    }

    /**
     * Gets the IRIS host URL from the configuration.
     * @return the URL of the host in Config.groovy
     */
    String getIrisHostAddress(Cytomine cytomine) {
        return grailsApplication.config.grails.serverURL
    }

    /**
     * Gets the Cytomine web URL from the configuration.
     * @return the URL of the web page in Config.groovy
     */
    String getCytomineWebAddress() {
        return grailsApplication.config.grails.cytomine.web
    }

    /**
     * Return a flag indicating the success of sending the email to the administrator.
     * @return true, if mail was sent successfully, false otherwise
     */
    Boolean testMailSettings() throws Exception {
        String subj = ("[Cytomine-IRIS: " + grailsApplication.config.grails.host + "] Mail Configuration Successful")
        String bdy = ("Dear Cytomine-IRIS administrator,\nthis email is to confirm that the mail configuration is OK.\n\n" +
                "Best regards,\n" +
                "Cytomine-IRIS at host " + grailsApplication.config.grails.host + ":" + grailsApplication.config.grails.port)

        mailService.sendMail {
            async false
            to grailsApplication.config.grails.cytomine.apps.iris.server.admin.email
            subject subj
            body bdy
        }

        return true
    }
    JSONObject getDatabaseStatistics() {
        // TODO number of users

        // last activity of any user

        // last activity of each user

    }

    def getActivityLog() {
        // TODO get all activities (in form of a log)
    }
}
