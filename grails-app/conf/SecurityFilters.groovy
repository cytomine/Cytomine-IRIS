import org.junit.After;

import be.cytomine.client.Cytomine

class SecurityFilters {
    def springSecurityService


    def filters = {

        api(uri:'/api/**') {
            before = {
                String publicKey = params.get("publicKey")
                String privateKey = params.get("privateKey")

				//log.debug "running filter"
				
                if(publicKey || privateKey) {
					// store a new cytomine instance (from the client-JAR) 
					// to the request object (will be available in the controllers etc.)
                    request['cytomine'] = new Cytomine(grailsApplication.config.grails.cytomine.host, publicKey, privateKey, "./");
                    request['cytomine'].setMax(0); //no max
                }
            }
            after = {

            }
            afterView = {

            }
        }
    }

}


