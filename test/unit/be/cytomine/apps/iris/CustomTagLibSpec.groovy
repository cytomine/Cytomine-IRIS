package be.cytomine.apps.iris

import be.cytomine.apps.iris.CustomTagLib;
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(CustomTagLib)
class CustomTagLibSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "Test angular application entry point"() {
		when:
		tagLib.redirectIndex()
		
		then:
		response.redirectedUrl.endsWith("index.html")
    }
}
