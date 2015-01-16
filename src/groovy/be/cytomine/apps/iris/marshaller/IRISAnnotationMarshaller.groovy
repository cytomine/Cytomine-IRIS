package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.model.IRISAnnotation;
import be.cytomine.apps.iris.IRISMarshaller;
import grails.converters.JSON

class IRISAnnotationMarshaller implements IRISMarshaller {

	@Override
	void register() {
		JSON.registerObjectMarshaller(IRISAnnotation) {
			def ann = [:]
			ann['class'] = it.getClass()

			ann['cmID'] = it.cmID
			ann['cmProjectID'] = it.cmProjectID
			ann['cmImageID'] = it.cmImageID
			ann['cmCreatorUserID'] = it.cmCreatorUserID
			ann['cmImageURL'] = it.cmImageURL
			ann['cmCropURL'] = it.cmCropURL
			ann['drawIncreasedAreaURL'] = it.drawIncreasedAreaURL
			ann['cmSmallCropURL'] = it.cmSmallCropURL
			ann['smallCropURL'] = it.smallCropURL
			ann['cmTermID'] = it.cmTermID
			ann['cmTermName'] = it.cmTermName
			ann['cmOntologyID'] = it.cmOntologyID
			ann['cmUserID'] = it.cmUserID
			ann['location'] = it.cmLocation
			ann['x'] = it.cmCentroidX
			ann['y'] = it.cmCentroidY

			ann['settings'] = it.settings
			ann['imageSettings'] = it.imageSettings

			return ann
		}
	}
}
