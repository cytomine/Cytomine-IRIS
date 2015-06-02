
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
package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.model.IRISAnnotation;
import be.cytomine.apps.iris.IRISMarshaller;
import grails.converters.JSON

class IRISAnnotationMarshaller implements IRISMarshaller {

	@Override
	void register() {
		JSON.registerObjectMarshaller(IRISAnnotation) {
			def ann = [:]
			ann['class'] = it.getClass()

			ann['cmID'] = it.cmID
			ann['cmProjectID'] = it.cmProjectID
			ann['cmImageID'] = it.cmImageID
			ann['cmCreatorUserID'] = it.cmCreatorUserID
			ann['cmImageURL'] = it.cmImageURL
			ann['cmCropURL'] = it.cmCropURL
			ann['drawIncreasedAreaURL'] = it.drawIncreasedAreaURL
			ann['cmSmallCropURL'] = it.cmSmallCropURL
			ann['smallCropURL'] = it.smallCropURL
			ann['cmTermID'] = it.cmTermID
			ann['cmTermName'] = it.cmTermName
			ann['cmOntologyID'] = it.cmOntologyID
			ann['cmUserID'] = it.cmUserID
			ann['location'] = it.cmLocation
			ann['x'] = it.cmCentroidX
			ann['y'] = it.cmCentroidY

			ann['settings'] = it.settings
			ann['imageSettings'] = it.imageSettings

			return ann
		}
	}
}
