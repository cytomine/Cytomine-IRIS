import be.cytomine.apps.iris.marshaller.ActivityMarshaller
import be.cytomine.apps.iris.marshaller.AnnotationMarshaller
import be.cytomine.apps.iris.marshaller.IRISObjectMarshallers
import be.cytomine.apps.iris.marshaller.ImageMarshaller
import be.cytomine.apps.iris.marshaller.PreferenceMarshaller
import be.cytomine.apps.iris.marshaller.ProjectMarshaller
import be.cytomine.apps.iris.marshaller.SessionMarshaller
import be.cytomine.apps.iris.marshaller.UserMarshaller

// Place your Spring DSL code here
beans = {
	irisObjectMarshallers(IRISObjectMarshallers) {
		irisMarshallers = [
				new SessionMarshaller(),
				new ProjectMarshaller(),
				new AnnotationMarshaller(),
				new ActivityMarshaller(),
				new ImageMarshaller(),
				new PreferenceMarshaller(),
				new UserMarshaller()
			]
	}
}
