
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
package be.cytomine.apps.iris.model

import be.cytomine.apps.iris.IRISUserProjectSettings

class IRISProject {
    // class members
    Long cmID = 0L
    String cmName = "defaultProject"
    Boolean cmBlindMode = false
    Long cmOntologyID = 0L
    String cmOntologyName = "defaultOntology"
    Long cmDisciplineID = 0L
    String cmDisciplineName = null
    Long cmCreated = null
    Long cmUpdated = null

    Boolean isReadOnly = false
    Boolean isClosed = false
    Boolean hideAdminsLayers = false
    Boolean hideUsersLayers = false
    Boolean retrievalDisable = false
    Boolean retrieveAllOntology = false

    Long cmNumberOfAnnotations = 0L
    Long cmNumberOfImages = 0

    /**
     * Custom settings (injected for each user on request).
     */
    IRISUserProjectSettings settings
}
