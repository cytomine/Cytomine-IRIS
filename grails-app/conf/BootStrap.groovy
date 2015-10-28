
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
import be.cytomine.apps.iris.IRISUserProjectSettings
import be.cytomine.apps.iris.auth.Role
import be.cytomine.apps.iris.auth.User
import be.cytomine.apps.iris.auth.UserRole
import grails.converters.JSON

import org.springframework.web.context.support.WebApplicationContextUtils

class BootStrap {

	def activityService

	def init = { servletContext ->

		activityService.logSync("Running bootstrap.groovy.")

		def springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
		
		// they are just valid, if JSON is not rendered 'deep'
		// return each JSON date format in long
		JSON.registerObjectMarshaller(Date){
			return it.getTime()
		}
		
		// register the custom marshalling classes
		springContext.getBean("irisObjectMarshallers").register()

		// create users for access control
		def userRole = Role.findByAuthority('ROLE_USER') ?:
				new Role(authority: 'ROLE_USER').save(flush: true, failOnError: true)
		def systemRole = Role.findByAuthority('ROLE_SYSTEM') ?:
				new Role(authority: 'ROLE_SYSTEM').save(flush: true, failOnError: true)

		// IRIS server-specific roles
		def IRISAdminRole = Role.findByAuthority('ROLE_IRIS_ADMIN') ?:
				new Role(authority: 'ROLE_IRIS_ADMIN').save(flush: true, failOnError: true)

		// IRIS application/project-specific roles
		def IRISProjectAdminRole = Role.findByAuthority('ROLE_IRIS_PROJECT_ADMIN') ?:
				new Role(authority: 'ROLE_IRIS_PROJECT_ADMIN').save(flush: true, failOnError: true)
		def IRISProjectCoordRole = Role.findByAuthority('ROLE_IRIS_PROJECT_COORDINATOR') ?:
				new Role(authority: 'ROLE_IRIS_PROJECT_COORDINATOR').save(flush: true, failOnError: true)

		/**
		 * The admin (root) user of the Cytomine IRIS instance.
		 */
		def adminUser = User.findByUsername('admin') ?: new User(
				username: 'admin',
				password: 'admin',
				enabled: true).save(flush: true, failOnError: true)

		if (!adminUser.authorities.contains(IRISAdminRole)) {
			UserRole.create adminUser, IRISAdminRole, true
		}

		/**
		 * A simple test user.
		 */
		def testUser = User.findByUsername('testuser') ?: new User(
				username: 'testuser',
				password: 'testuser',
				enabled: true).save(flush: true, failOnError: true)

		if (!testUser.authorities.contains(userRole)) {
			UserRole.create testUser, userRole, true
		}

		/**
		 * System user for activity logging, notification services and so forth.
 		 */

		def system = User.findByUsername('system') ?: new User(
				username: 'system',
				password: '>w%K6Cx/%D>>.G=~96)[Q"YG^{pR?W',
				enabled: true).save(flush: true, failOnError: true)

		if (!system.authorities.contains(systemRole)) {
			UserRole.create system, systemRole, true
		}

		/**
		 * Migration settings for database updates.
		 */

		/*
		For each user, check the be.cytomine.apps.iris.IRISUserProjectSettings for coordinator
		rules.
		Required for updating from earlier versions than 2.7.
		 */
		IRISUserProjectSettings.withTransaction {
			// automatically set all users to be NO coordinators!
			def nullCoordinators = IRISUserProjectSettings.findAllByIrisCoordinator(null)
			nullCoordinators.each {
				it.setIrisCoordinator(false)
			}
		}

		activityService.log("Done running bootstrap.groovy.")
	}

	def destroy = {
	}
}
