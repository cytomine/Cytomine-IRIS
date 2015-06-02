
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

import be.cytomine.apps.iris.IRISUserProjectSettings
import grails.converters.JSON
import be.cytomine.apps.iris.IRISMarshaller
import be.cytomine.apps.iris.model.IRISImage

class IRISImageMarshaller implements IRISMarshaller{

	@Override
	void register() {
		JSON.registerObjectMarshaller(IRISImage) {
			def img = [:]
			img['class'] = it.getClass()

			img['cmID'] = it.cmID
			img['cmProjectID'] = it.cmProjectID
			img['cmCreated'] = it.cmCreated
			img['cmBaseImageID'] = it.cmBaseImageID
			img['cmReviewed'] = it.cmReviewed
			img['cmInReview'] = it.cmInReview

			img['magnification'] = it.magnification
			img['resolution'] = it.resolution
			img['depth'] = it.depth

			img['originalFilename'] = it.originalFilename
			img['instanceFilename'] = it.instanceFilename

			img['goToURL'] = it.goToURL
			img['olTileServerURL'] = it.olTileServerURL
			img['macroURL'] = it.macroURL
			img['mime'] = it.mime

			img['width'] = it.width
			img['height'] = it.height

			img['numberOfAnnotations'] = it.numberOfAnnotations

			img['settings'] = it.settings

			img['projectSettings'] = it.projectSettings

			return img
		}
	}
}
