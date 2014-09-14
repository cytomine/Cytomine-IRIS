package be.cytomine.apps.iris

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.springframework.aop.aspectj.RuntimeTestWalker.ThisInstanceOfResidueTestVisitor;

import be.cytomine.apps.iris.Image;
import be.cytomine.apps.iris.Project;
import be.cytomine.apps.iris.User;
import grails.converters.JSON;

/**
 * This class maps from the client domain models to the IRIS 
 * domain models.
 * 
 * @author Philipp Kainz
 *
 */
class DomainMapper {

	def grailsApplication

	DomainMapper(def grailsApplication){
		this.grailsApplication = grailsApplication
	}

	/**
	 * Map the be.cytomine.client.models.User model to the IRIS domain model of a user.
	 * @param cmUser a Cytomine user
	 * @param irisUser an IRIS user, or null (then a new user is generated)
	 * @return an IRIS user instance
	 */
	User mapUser(be.cytomine.client.models.User cmUser, User irisUser){
		if (irisUser == null){
			irisUser = new User()
		}

		// map the properties from the client user to the IRIS user model
		irisUser.setCmID(cmUser.getId())
		irisUser.setCmUserName(cmUser.getStr("username"))
		irisUser.setCmLastName(cmUser.getStr("lastname"))
		irisUser.setCmFirstName(cmUser.getStr("firstname"))
		irisUser.setCmPublicKey(cmUser.getStr("publicKey"))
		irisUser.setCmPrivateKey(cmUser.getStr("privateKey"))
		irisUser.setCmEmail(cmUser.getStr("email"))
		irisUser.setCmClass(cmUser.getStr("class"))
		irisUser.setCmPasswordExpired(cmUser.getBool("passwordExpired"))
		irisUser.setCmDeleted(cmUser.getBool("deleted"))
		irisUser.setCmAdminByNow(cmUser.getBool("adminByNow"))
		irisUser.setCmAlgo(cmUser.getBool("algo"))
		irisUser.setCmAdmin(cmUser.getBool("admin"))
		irisUser.setCmGuestByNow(cmUser.getBool("guestByNow"))
		irisUser.setCmIsSwitched(cmUser.getBool("isSwitched"))
		irisUser.setCmGuest(cmUser.getBool("guest"))
		irisUser.setCmUser(cmUser.getBool("user"))

		return irisUser
	}

	/**
	 * 
	 * 
	 * @param cmProject
	 * @param irisProject
	 * @return
	 */
	Project mapProject(be.cytomine.client.models.Project cmProject, Project irisProject){
		if (irisProject == null){
			irisProject = new Project()
		}

		// TODO map required properties from the client model
		irisProject.setCmID(cmProject.getId())
		irisProject.setCmName(cmProject.getStr("name"))
		irisProject.setCmBlindMode(cmProject.getBool("blindMode"))
		irisProject.setCmOntology(cmProject.getLong("ontology"))

		return irisProject
	}

	/**
	 * 
	 * @param cmImage
	 * @param irisImage
	 * @return
	 */
	Image mapImage(be.cytomine.client.models.ImageInstance cmImage, Image irisImage, boolean blindMode){
		if (irisImage == null){
			irisImage = new Image()
		}

		// TODO map required properties from the client model
		irisImage.setCmID(cmImage.getId())
		if (blindMode){
			irisImage.setOriginalFilename("[BLIND]" + cmImage.getId())
		} else {
			irisImage.setOriginalFilename(cmImage.get("originalFilename"))
		}
		irisImage.setNumberOfAnnotations(cmImage.get("numberOfAnnotations"))

		return irisImage
	}

	/**
	 *
	 * @param cmAnnotation
	 * @param irisAnnotation
	 * @return
	 */
	Annotation mapAnnotation(be.cytomine.client.models.Annotation cmAnnotation, Annotation irisAnnotation){
		if (irisAnnotation == null){
			irisAnnotation = new Annotation()
		}

		// TODO map required properties from the client model
		irisAnnotation.setCmID(cmAnnotation.getId())
		irisAnnotation.setCmProjectID(cmAnnotation.getLong("project"))
		irisAnnotation.setCmImageID(cmAnnotation.getLong("image"))
		irisAnnotation.setCmCreatorUserID(cmAnnotation.get("user"))
		irisAnnotation.setCmImageURL(cmAnnotation.getStr("imageURL"))
		irisAnnotation.setCmCropURL(cmAnnotation.getStr("cropURL"))
		irisAnnotation.setDrawIncreasedAreaURL(grailsApplication.config.grails.cytomine.host
							+ "/api/annotation/"
							+ cmAnnotation.getId()
							+ "/crop.png?increaseArea=8&maxSize=256&draw=true")
		irisAnnotation.setCmSmallCropURL(cmAnnotation.getStr("smallCropURL"))

		return irisAnnotation
	}
}
