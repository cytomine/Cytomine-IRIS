package be.cytomine.apps.iris

import grails.transaction.Transactional

@Transactional
class ActivityService {

    def log(User user, Project irisProject, Image irisImage, String description) {
		Activity ac = new Activity(user:user, project:irisProject, image: irisImage, description:description)
		ac.save(failOnError:true)
	}
	
	def log(User user, String description) {
		Activity ac = new Activity(user:user, description:description)
		ac.save(failOnError:true)
	}
	
	def log(String description) {
		Activity ac = new Activity(description:description)
		ac.save(failOnError:true)
	}
}
