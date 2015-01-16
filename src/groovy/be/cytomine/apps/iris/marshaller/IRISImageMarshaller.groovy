package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISUserProjectSettings
import grails.converters.JSON
import be.cytomine.apps.iris.IRISMarshaller
import be.cytomine.apps.iris.model.IRISImage

class IRISImageMarshaller implements IRISMarshaller{

	@Override
	void register() {
		JSON.registerObjectMarshaller(IRISImage) {
			def img = [:]
			img['class'] = it.getClass()

			img['cmID'] = it.cmID
			img['cmProjectID'] = it.cmProjectID
			img['cmCreated'] = it.cmCreated
			img['cmBaseImageID'] = it.cmBaseImageID
			img['cmReviewed'] = it.cmReviewed
			img['cmInReview'] = it.cmInReview

			img['magnification'] = it.magnification
			img['resolution'] = it.resolution
			img['depth'] = it.depth

			img['originalFilename'] = it.originalFilename
			img['instanceFilename'] = it.instanceFilename

			img['goToURL'] = it.goToURL
			img['olTileServerURL'] = it.olTileServerURL
			img['macroURL'] = it.macroURL

			img['width'] = it.width
			img['height'] = it.height

			img['numberOfAnnotations'] = it.numberOfAnnotations

			img['settings'] = it.settings

			img['projectSettings'] = it.projectSettings

			return img
		}
	}
}
