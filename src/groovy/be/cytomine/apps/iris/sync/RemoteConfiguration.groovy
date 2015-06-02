
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
package be.cytomine.apps.iris.sync
/**
 * A remote configuration is a JSON object, which is placed in the properties of a Cytomine domain object.
 * Though the remote configuration object can be altered at the Cytomine properties page, this is <b>strongly</b>
 * discouraged.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class RemoteConfiguration {
    /**
     * The host where IRIS is running.
     */
    String irisHost
    /**
     * The IRIS version running on the host.
     */
    String irisVersion
    /**
     * The administrator of this iris instance.
     */
    String adminEmail

    /**
     * Creates a default set of a remote configuration for this IRIS instance.
     *
     * @param grailsApplication required, since dependency injection does not work in groovy source classes
     * @return a new RemoteConfiguration object
     */
    static RemoteConfiguration createDefault(def grailsApplication) {
        RemoteConfiguration rConf = new RemoteConfiguration()

        // override some default values for this instance
        rConf.setIrisHost(grailsApplication.config.grails.cytomine.apps.iris.sync.irisHost)
        rConf.setIrisVersion(grailsApplication.metadata['app.version'])
        rConf.setAdminEmail(grailsApplication.config.grails.cytomine.apps.iris.server.admin.email)

        return rConf
    }
}
