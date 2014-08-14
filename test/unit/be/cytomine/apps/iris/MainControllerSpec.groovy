package be.cytomine.apps.iris

import be.cytomine.apps.iris.MainController;
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(MainController)
class MainControllerSpec extends Specification {

    def setup() {
	}

    def cleanup() {
    }
	
	void "test app name"() {
		when: 
		controller.appName()
		
		then:
		response.text == "${grailsApplication.metadata.'app.name'}"
	}
	
	void "test app version"() {
		when:
		controller.appVersion()
		
		then:
		response.text == "${grailsApplication.metadata.'app.version'}"
	}
}
