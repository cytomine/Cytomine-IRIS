
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

import be.cytomine.apps.iris.IRISConstants
import be.cytomine.apps.iris.IRISUserAnnotationTermSettings
import be.cytomine.apps.iris.IRISUserImageSettings

class IRISAnnotation {

	// the Cytomine ID of this annotation
	Long cmID = 0L
	// the parent Cytomine project and image ID for this annotation
	Long cmProjectID = 0L
	Long cmImageID = 0L
	
	// the Cytomine user ID of the person who created this annotation
	Long cmCreatorUserID = 0L
	
	// the URL of the image explorer
	String cmImageURL = null
	String cmCropURL = null
	String drawIncreasedAreaURL = null
	String cmSmallCropURL = null
	String cmLocation = null
	Double cmCentroidX = null
	Double cmCentroidY = null
	
	String smallCropURL = null
	
	// the assigned Cytomine term ID by the user
	/**
	 * Default term ID is -99 (no term assigned).
	 */
	Long cmTermID = IRISConstants.ANNOTATION_NO_TERM_ASSIGNED
	String cmTermName = null
	Long cmOntologyID = 0L
	// the Cytomine user ID of the person who assigned this term
	Long cmUserID = 0L
	// a flag determining whether the annotation is already labeled
	Boolean alreadyLabeled = false

	IRISUserAnnotationTermSettings settings

	IRISUserImageSettings imageSettings
}
