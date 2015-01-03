package be.cytomine.apps.iris


/**
 * This class maps from the Cytomine client domain models to the IRIS 
 * domain models.
 * 
 * @author Philipp Kainz
 *
 */
class DomainMapper {

	def grailsApplication
	def log
	String cmHost
	String irisHost

	/**
	 * Constructor accepting a 'grailsApplication' configuration object
	 * 
	 * @param grailsApplication
	 */
	DomainMapper(def grailsApplication){
		this.grailsApplication = grailsApplication
		this.cmHost = grailsApplication.config.grails.cytomine.host
		this.irisHost = grailsApplication.config.grails.cytomine.apps.iris.host
	}

	/**
	 * Map a user.
	 * 
	 * @param cmUser a Cytomine user (be.cytomine.client.models.User)
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
	 * Map a project.
	 * 
	 * @param cmProject be.cytomine.client.models.Project
	 * @param irisProject the IRIS domain model project
	 * @return the IRIS project
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
		irisProject.setCmNumberOfImages(cmProject.getInt("numberOfImages"))
		
		return irisProject
	}

	/**
	 * Map an image.
	 * 
	 * @param cmImage be.cytomine.client.models.ImageInstance
	 * @param irisImage the IRIS domain model image, creates a new one, if <code>null</code>
	 * @return the IRIS image
	 */
	Image mapImage(be.cytomine.client.models.ImageInstance cmImage, Image irisImage, boolean blindMode){
		if (irisImage == null){
			irisImage = new Image()
		}

		// TODO map required properties from the client model
		irisImage.setCmID(cmImage.getId())

		// replace the host in the macro url
		irisImage.setMacroURL(cmImage.get("macroURL").toString()
				.replace(cmHost,irisHost))

		irisImage.setWidth(cmImage.get("width"))
		irisImage.setHeight(cmImage.get("height"))
		
		if (blindMode){
			irisImage.setOriginalFilename("[BLIND]" + cmImage.getId())
		} else {
			irisImage.setOriginalFilename(cmImage.get("originalFilename"))
		}
		irisImage.setNumberOfAnnotations(cmImage.get("numberOfAnnotations"))

		return irisImage
	}

	/**
	 * Map an annotation. 
	 *
	 * @param cmAnnotation be.cytomine.client.models.Annotation
	 * @param irisAnnotation the IRIS domain model annotation, if <code>null</code> a new annotation will be created
	 * @return the IRIS annotation
	 */
	Annotation mapAnnotation(be.cytomine.client.models.Annotation cmAnnotation, Annotation irisAnnotation) throws Exception{
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
		irisAnnotation.setSmallCropURL(cmAnnotation.getStr("smallCropURL").toString()
				.replace(cmHost,irisHost))

		// map centroid and location object
		try {
			irisAnnotation.setCmLocation(cmAnnotation.getStr("location"))
		} catch(Exception e){
			log.warn("'Location' information does not exist for this instance of " + cmAnnotation.getClass() + ".")
		}

		// the centroid can be in a distinct object, or in extra coordinates
		try {
			if (cmAnnotation.get("centroid") == null){
				irisAnnotation.setCmCentroidX(Double.valueOf(cmAnnotation.get("x")))
				irisAnnotation.setCmCentroidY(Double.valueOf(cmAnnotation.get("y")))
			} else {
				irisAnnotation.setCmCentroidX(Double.valueOf(cmAnnotation.get("centroid").get("x")))
				irisAnnotation.setCmCentroidY(Double.valueOf(cmAnnotation.get("centroid").get("y")))
			}
		} catch(Exception e){
			log.warn("'Centroid' information does not exist for this instance of " + cmAnnotation.getClass() + ".")
		}
		
		return irisAnnotation
	}
}
