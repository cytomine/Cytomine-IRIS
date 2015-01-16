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
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(flush: true, failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(flush: true, failOnError: true)
		def superAdminRole = Role.findByAuthority('ROLE_SUPER_ADMIN') ?: new Role(authority: 'ROLE_SUPER_ADMIN').save(flush: true, failOnError: true)

		def adminUser = User.findByUsername('admin') ?: new User(
				username: 'admin',
				password: 'admin',
				enabled: true).save(flush: true, failOnError: true)

		if (!adminUser.authorities.contains(adminRole)) {
			UserRole.create adminUser, adminRole, true
		}

		def superAdminUser = User.findByUsername('superadmin') ?: new User(
				username: 'superadmin',
				password: 'superadmin',
				enabled: true).save(flush: true, failOnError: true)

		if (!superAdminUser.authorities.contains(superAdminRole)) {
			UserRole.create superAdminUser, superAdminRole, true
		}

		def testUser = User.findByUsername('testuser') ?: new User(
				username: 'testuser',
				password: 'testuser',
				enabled: true).save(flush: true, failOnError: true)

		if (!testUser.authorities.contains(userRole)) {
			UserRole.create testUser, userRole, true
		}

		def system = User.findByUsername('system') ?: new User(
				username: 'system',
				password: '>w%K6Cx/%D>>.G=~96)[Q"YG^{pR?W',
				enabled: true).save(flush: true, failOnError: true)

		if (!system.authorities.contains(superAdminRole)) {
			UserRole.create system, superAdminRole, true
		}

		activityService.log("Done running bootstrap.groovy.")
	}

	def destroy = {
	}
}
