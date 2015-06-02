
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

import be.cytomine.apps.iris.IRISUserImageSettings
import be.cytomine.apps.iris.IRISUserProjectSettings

class IRISImage {
    // Cytomine stuff
    Long cmID = 0L
    Long cmProjectID = 0L
    Long cmCreated = 0L
    Long cmBaseImageID = 0L
    Boolean cmReviewed = false
    Boolean cmInReview = false

    Long magnification = 0L
    Double resolution = 0.0d
    Long depth = 0L

    String originalFilename = "defaultIRISImage"
    String instanceFilename = "defaultIRISImage"

    String goToURL = null
    String olTileServerURL = null
    String macroURL = null
    String mime = null

    Long width = 0
    Long height = 0

    Long numberOfAnnotations = 0L

    /**
     * Custom settings (injected for each user on request).
     */
    IRISUserImageSettings settings

    /**
     * The project settings for the parent project.
     */
    IRISUserProjectSettings projectSettings
}
