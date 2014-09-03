import grails.converters.JSON

import org.springframework.web.context.support.WebApplicationContextUtils

import be.cytomine.apps.iris.Annotation
import be.cytomine.apps.iris.DomainMapper
import be.cytomine.apps.iris.Image
import be.cytomine.apps.iris.Project
import be.cytomine.apps.iris.Session
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

//		///////////////////////////////////////
//		Cytomine cm = new Cytomine("http://beta.cytomine.be", "0880e4b4-fe26-4967-8169-f15ed2f9be5c", "a511a35c-5941-4932-9b40-4c8c4c76c7e7", "./");
//		be.cytomine.client.models.User cmUser = cm.getUser("0880e4b4-fe26-4967-8169-f15ed2f9be5c");
//		DomainMapper dm = new DomainMapper()
//
//		// find the user by the cytomine ID
//		User tmp = User.findByCmID(cmUser.getId())
//		User irisUser = dm.mapUser(cmUser, tmp)
//		Session sess = new Session()
//		(1..3).each {
//			Project p = new Project()
//			(1..5).each {
//				Image img = new Image()
//				(1..20).each {
//					Annotation ann = new Annotation()
//					img.addToAnnotations(ann)
//					ann.prefs.put("annotation.key", new Random().nextInt(100) + "")
//				}
//				p.addToImages(img)
//				img.prefs.put("image.pref", new Random().nextInt(100) + "")
//			}
//			sess.addToProjects(p)
//		}
//
//		// try to get an existing session
//		if (irisUser.getSession() == null){
//			// associate the session with the user
//			irisUser.setSession(sess)
//		}
//		
//		// save the user
//		irisUser.save(flush:true,failOnError:true)
//
//		////////////////////////
//		be.cytomine.client.models.User cmMartin = cm.getUser("9024a776-a288-46f2-83c5-fc0267806908");
//		User martin = dm.mapUser(cmMartin, null)
//
//		martin.setSession(new Session())
//
//		martin.save(flush:true,failOnError:true)

	}

	def destroy = {
	}
}
