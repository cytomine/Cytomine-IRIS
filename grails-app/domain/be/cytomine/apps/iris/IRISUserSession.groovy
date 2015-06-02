
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

/**
 * An IRISUserSession reflects the current progress of a user in the application.
 *
 * @author Philipp Kainz
 * @since 0.3
 */
class IRISUserSession{
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

	static constraints = {
		user nullable: false
	}

	/**
	 * The user of this session.
	 */
	IRISUser user
	/**
 	 * The most recently opened project instance.
	 */
	Long currentCmProjectID = null
}
