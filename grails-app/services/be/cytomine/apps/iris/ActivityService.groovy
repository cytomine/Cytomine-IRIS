package be.cytomine.apps.iris

import grails.transaction.Transactional

@Transactional
class ActivityService {

    def logForAll(User user, Long cmPjID, Long cmImgID, Long cmAnnID, String description) {
		Activity ac = new Activity(user:user, cmProjectID:cmPjID, cmImageID: cmImgID, cmAnnotationID:cmAnnID, description:description)
		ac.save(failOnError:true)
	}
	
	def logForProject(User user, Long cmPjID, String description) {
		Activity ac = new Activity(user:user, cmProjectID:cmPjID, description:description)
		ac.save(failOnError:true)
	}
	
	def logForProjectImage(User user, Long cmPjID, Long cmImgID, String description) {
		Activity ac = new Activity(user:user, cmProjectID:cmPjID, cmImageID: cmImgID, description:description)
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
