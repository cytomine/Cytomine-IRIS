
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

import be.cytomine.apps.iris.Activity;
import be.cytomine.apps.iris.IRISMarshaller;
import grails.converters.JSON;

class ActivityMarshaller implements IRISMarshaller{

	@Override
	void register() {
		JSON.registerObjectMarshaller(Activity) {
			def act = [:]
			act['class'] = it.getClass()
			act['id'] = it.id
			return act
		}
	}
	
}
