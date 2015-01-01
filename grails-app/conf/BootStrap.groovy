import grails.converters.JSON

import org.springframework.web.context.support.WebApplicationContextUtils

import be.cytomine.apps.iris.Annotation
import be.cytomine.apps.iris.DomainMapper
import be.cytomine.apps.iris.Image
import be.cytomine.apps.iris.Project
import be.cytomine.apps.iris.Session
import be.cytomine.apps.iris.SynchronizeUserProgressJob;
import be.cytomine.apps.iris.User
import be.cytomine.client.Cytomine

class BootStrap {

	def init = { servletContext ->
		def springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
		
		
		// they are just valid, if JSON is not rendered 'deep'
		// return each JSON date format in long
		JSON.registerObjectMarshaller(Date){
			return it.getTime()
		}
		
		// register the custom marshalling classes
		springContext.getBean("irisObjectMarshallers").register()
	}

	def destroy = {
	}
}
