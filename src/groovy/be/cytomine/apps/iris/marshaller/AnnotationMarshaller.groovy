package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.Annotation;
import be.cytomine.apps.iris.IRISMarshaller;
import grails.converters.JSON

class AnnotationMarshaller implements IRISMarshaller {

	@Override
	void register() {
		JSON.registerObjectMarshaller(Annotation) {
			def ann = [:]
			ann['class'] = it.getClass()
			ann['id'] = it.id
			ann['cmID'] = it.cmID
			ann['cmProjectID'] = it.cmProjectID
			ann['cmImageID'] = it.cmImageID
			ann['cmCreatorUserID'] = it.cmCreatorUserID
			ann['cmImageURL'] = it.cmImageURL
			ann['cmCropURL'] = it.cmCropURL
			ann['drawIncreasedAreaURL'] = it.drawIncreasedAreaURL
			ann['cmSmallCropURL'] = it.cmSmallCropURL
			ann['cmTermID'] = it.cmTermID
			ann['cmTermName'] = it.cmTermName
			ann['cmOntology'] = it.cmOntology
			ann['cmUserID'] = it.cmUserID
			ann['prefs'] = it.prefs
			ann['image'] = (it.image==null?null:it.image.id)
			ann['location'] = it.cmLocation
			ann['x'] = it.cmCentroidX
			ann['y'] = it.cmCentroidY
			
			// TODO marshal the image server URLs
			return ann
		}
	}
}
