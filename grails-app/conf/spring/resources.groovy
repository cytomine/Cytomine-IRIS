
/* Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
