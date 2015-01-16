import be.cytomine.apps.iris.marshaller.ActivityMarshaller
import be.cytomine.apps.iris.marshaller.IRISAnnotationMarshaller
import be.cytomine.apps.iris.marshaller.IRISObjectMarshallers
import be.cytomine.apps.iris.marshaller.IRISImageMarshaller
import be.cytomine.apps.iris.marshaller.IRISProjectMarshaller
import be.cytomine.apps.iris.marshaller.IRISUserSessionMarshaller
import be.cytomine.apps.iris.marshaller.IRISUserMarshaller
import grails.plugin.executor.PersistenceContextExecutorWrapper

import java.util.concurrent.Executors

// Place your Spring DSL code here
beans = {
	// define custom object marshallers for "as JSON" conversion
	irisObjectMarshallers(IRISObjectMarshallers) {
		irisMarshallers = [
//				new IRISUserSessionMarshaller(),
				new IRISProjectMarshaller(),
				new IRISAnnotationMarshaller(),
//				new ActivityMarshaller(),
				new IRISImageMarshaller()
//				new IRISUserMarshaller()
			]
	}

	executorService( PersistenceContextExecutorWrapper ) { bean ->
		bean.destroyMethod = 'destroy'
		persistenceInterceptor = ref("persistenceInterceptor")
		// use a single thread executor, since each persistence context should
		// be executed sequentially
		executor = Executors.newSingleThreadExecutor()
	}
}
