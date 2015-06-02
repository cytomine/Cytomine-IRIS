
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

import be.cytomine.apps.iris.IRISMarshaller
import be.cytomine.apps.iris.model.IRISProject
import grails.converters.JSON

class IRISProjectMarshaller implements IRISMarshaller{
	
	@Override
	void register(){
		JSON.registerObjectMarshaller(IRISProject) {
			def prj = [:]
			prj['class'] = it.getClass()

			prj['cmID'] = it.cmID
			prj['cmName'] = it.cmName
			prj['cmBlindMode'] = it.cmBlindMode
			prj['cmOntologyID'] = it.cmOntologyID
			prj['cmOntologyName'] = it.cmOntologyName
			prj['cmDisciplineID'] = it.cmDisciplineID
			prj['cmDisciplineName'] = it.cmDisciplineName
			prj['cmCreated'] = it.cmCreated
			prj['cmUpdated'] = it.cmUpdated

			prj['isReadOnly'] = it.isReadOnly
			prj['isClosed'] = it.isClosed
			prj['hideAdminsLayers'] = it.hideAdminsLayers
			prj['hideUsersLayers'] = it.hideUsersLayers
			prj['retrievalDisable'] = it.retrievalDisable
			prj['retrieveAllOntology'] = it.retrievalDisable

			prj['cmNumberOfAnnotations'] = it.cmNumberOfAnnotations
			prj['cmNumberOfImages'] = it.cmNumberOfImages

			prj['settings'] = it.settings

			return prj
		}
	}
}
