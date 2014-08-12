package be.cytomine.apps.iris

class MainController {

    def grailsApplication

    def welcome() {
        render "Welcome in ${grailsApplication.metadata.'app.name'} (${grailsApplication.metadata.'app.version'})"
    }
	
	def aMethod() {
		render "The answer to life, the universe and everything is 42!"
	}
}
