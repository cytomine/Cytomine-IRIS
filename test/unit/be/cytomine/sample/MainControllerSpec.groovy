package be.cytomine.sample

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

    void "test a custom method"() {
		when:
		controller.aMethod()
		
		then:
		response.text == "The answer to the life, the universe and everything is 42!"
	}
}
