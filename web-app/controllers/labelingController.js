var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("labelingCtrl", function($scope, $http, $filter, $location,
		$routeParams, $log, hotkeys, helpService, annotationService,
		projectService, sessionService, sharedService, imageService, cytomineService) {
	console.log("labelingCtrl");
	
	// preallocate the objects
	$scope.labeling = {
		annotation : {},
		ontology : {},
		error : {}
	};

	$scope.labeling.projectID = $routeParams["projectID"];
	$scope.labeling.imageID = $routeParams["imageID"];
	$scope.labeling.annotation.id = $routeParams["annID"];
	
	// set content url for the help page
	helpService.setContentUrl("content/help/labelingHelp.html");
	
	$scope.saving = {
		status : "",
	};

	// TODO DEBUG
	$scope.item = {
		userProgress : 45,
		labeledAnnotations : 45,
		numberOfAnnotations : 100
	};

	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope).add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	}).add({
		combo : 'n',
		description : 'Proceed to next annotation',
		callback : function() {
			// TODO
			console.log("move to next annotation")
		}
	}).add({
		combo : 'p',
		description : 'Go back to the previous annotation',
		callback : function() {
			// TODO
			console.log("go back to the previous annotation")
		}
	}).add({
		// TODO remove
		combo : 'd',
		description : 'print debug info on the project',
		callback : function() {
			// TODO
			
		}
	});
		
	
	// get the image URL for the crop view
	cytomineService.getCytomineHost(function(host){
			$scope.labeling.annotation.cropURL = host + "/api/annotation/" + $scope.labeling.annotation.id +
					"/crop.png?&increaseArea=8&maxSize=256&draw=true";
			
			$scope.labeling.annotation.goToURL = sessionService.getCurrentImage().goToURL + $scope.labeling.annotation.id
			console.log($scope.labeling.annotation.goToURL)
	});
	

	$scope.removeTerm = function(){
		console.log("remove term");
		// TODO
		$scope.saving.status="saving"
	};
	

	// TODO

	$scope.maxSize = 4;
	$scope.totalItems = 64;
	$scope.currentPage = 1;
	$scope.numPages = 64;

});

iris.controller("termCtrl", function($scope, $log, $filter, $routeParams, sharedService, sessionService, projectService, ngTableParams) {
	console.log("termCtrl")

	// this corresponds to resolvedOntology.attr
	// 136435754 (2x hierarchical)
	// 95190197 (BM-01) 1x hierarchical
	// 585609 (ALCIAN BLUE) // flat
	
	var cp = sessionService.getCurrentProject();
	//$log.debug(cp)
	
	// TODO get the ontologyID from the current project
	projectService.fetchOntology(cp.cytomine.ontology, { flat : true }, function(data) {
		$scope.ontology = data;
		
		// build the ontology table
		$scope.tableParams = new ngTableParams({
			page : 1, // show first page
			count : 25, // count per page
			// leave this blank for no sorting at all
			sorting : {
				name : 'asc' // initial sorting
			},
			filter : {
				// applies filter to the "data" object before sorting
			}
		}, {
			total : $scope.ontology.length, // number of terms
			getData : function($defer, params) {
				// use build-in angular filter
				var newData = $scope.ontology;
				// use build-in angular filter
				newData = params.filter() ? $filter('filter')(newData,
						params.filter()) : newData;
				newData = params.sorting() ? $filter('orderBy')(newData,
						params.orderBy()) : newData;
				$scope.data = newData.slice((params.page() - 1)
						* params.count(), params.page() * params.count());
				params.total(newData.length); // set total for recalc
												// pagination
				$defer.resolve($scope.data);
			},
			filterDelay : 0,
		});
	});

	// {"hideCheckbox":true,"state":"open","data":"BM-01","children":[{"hideCheckbox":false,"data":"Z99_Background","class":"be.cytomine.ontology.Term","parent":null,"children":[],"attr":{"id":95795881,"type":"be.cytomine.ontology.Term"},"id":95795881,"title":"Z99_Background","isFolder":false,"color":"#677dff","name":"Z99_Background","checked":false,"key":95795881},{"hideCheckbox":true,"data":"Erythrocytopoesis","class":"be.cytomine.ontology.Term","parent":null,"children":[{"hideCheckbox":false,"data":"E01_Pronormoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202366,"type":"be.cytomine.ontology.Term"},"id":95202366,"title":"E01_Pronormoblast","isFolder":false,"color":"#2bfdcf","name":"E01_Pronormoblast","checked":false,"key":95202366},{"hideCheckbox":false,"data":"E02_BasophilicNormoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202360,"type":"be.cytomine.ontology.Term"},"id":95202360,"title":"E02_BasophilicNormoblast","isFolder":false,"color":"#2bfdcf","name":"E02_BasophilicNormoblast","checked":false,"key":95202360},{"hideCheckbox":false,"data":"E03_PolychromatophilicNormoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202354,"type":"be.cytomine.ontology.Term"},"id":95202354,"title":"E03_PolychromatophilicNormoblast","isFolder":false,"color":"#2bfdcf","name":"E03_PolychromatophilicNormoblast","checked":false,"key":95202354},{"hideCheckbox":false,"data":"E04_OrthochromaticNormoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202348,"type":"be.cytomine.ontology.Term"},"id":95202348,"title":"E04_OrthochromaticNormoblast","isFolder":false,"color":"#2bfdcf","name":"E04_OrthochromaticNormoblast","checked":false,"key":95202348},{"hideCheckbox":false,"data":"E05_Reticulocyte","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202334,"type":"be.cytomine.ontology.Term"},"id":95202334,"title":"E05_Reticulocyte","isFolder":false,"color":"#2bfdcf","name":"E05_Reticulocyte","checked":false,"key":95202334},{"hideCheckbox":false,"data":"E06_Erythrocyte","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202328,"type":"be.cytomine.ontology.Term"},"id":95202328,"title":"E06_Erythrocyte","isFolder":false,"color":"#2bfdcf","name":"E06_Erythrocyte","checked":false,"key":95202328}],"attr":{"id":136483400,"type":"be.cytomine.ontology.Term"},"id":136483400,"title":"Erythrocytopoesis","isFolder":true,"color":"#2bfdcf","name":"Erythrocytopoesis","checked":false,"key":136483400},{"hideCheckbox":true,"data":"Granulocytopoesis","class":"be.cytomine.ontology.Term","parent":null,"children":[{"hideCheckbox":false,"data":"G01_Myeloblast","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202322,"type":"be.cytomine.ontology.Term"},"id":95202322,"title":"G01_Myeloblast","isFolder":false,"color":"#2bfdcf","name":"G01_Myeloblast","checked":false,"key":95202322},{"hideCheckbox":false,"data":"G02_Promyelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202311,"type":"be.cytomine.ontology.Term"},"id":95202311,"title":"G02_Promyelocyte","isFolder":false,"color":"#2bfdcf","name":"G02_Promyelocyte","checked":false,"key":95202311},{"hideCheckbox":false,"data":"G03_Myelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202287,"type":"be.cytomine.ontology.Term"},"id":95202287,"title":"G03_Myelocyte","isFolder":false,"color":"#2bfdcf","name":"G03_Myelocyte","checked":false,"key":95202287},{"hideCheckbox":false,"data":"G04_Metamyelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202281,"type":"be.cytomine.ontology.Term"},"id":95202281,"title":"G04_Metamyelocyte","isFolder":false,"color":"#2bfdcf","name":"G04_Metamyelocyte","checked":false,"key":95202281},{"hideCheckbox":false,"data":"G05_BandCell","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202274,"type":"be.cytomine.ontology.Term"},"id":95202274,"title":"G05_BandCell","isFolder":false,"color":"#2bfdcf","name":"G05_BandCell","checked":false,"key":95202274},{"hideCheckbox":false,"data":"G06_Granulocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202266,"type":"be.cytomine.ontology.Term"},"id":95202266,"title":"G06_Granulocyte","isFolder":false,"color":"#2bfdcf","name":"G06_Granulocyte","checked":false,"key":95202266}],"attr":{"id":136483532,"type":"be.cytomine.ontology.Term"},"id":136483532,"title":"Granulocytopoesis","isFolder":true,"color":"#2bfdcf","name":"Granulocytopoesis","checked":false,"key":136483532},{"hideCheckbox":true,"data":"Megakariocytopoesis","class":"be.cytomine.ontology.Term","parent":null,"children":[{"hideCheckbox":false,"data":"M01_Megakaryocyte","class":"be.cytomine.ontology.Term","parent":136483515,"children":[],"attr":{"id":95202372,"type":"be.cytomine.ontology.Term"},"id":95202372,"title":"M01_Megakaryocyte","isFolder":false,"color":"#2bfdcf","name":"M01_Megakaryocyte","checked":false,"key":95202372}],"attr":{"id":136483515,"type":"be.cytomine.ontology.Term"},"id":136483515,"title":"Megakariocytopoesis","isFolder":true,"color":"#2bfdcf","name":"Megakariocytopoesis","checked":false,"key":136483515}],"class":"be.cytomine.ontology.Ontology","attr":{"id":95190197,"type":"be.cytomine.ontology.Ontology"},"deleted":null,"id":95190197,"projects":[{"ontologyName":"BM-01","hideAdminsLayers":false,"class":"be.cytomine.project.Project","numberOfSlides":0,"ontology":95190197,"deleted":null,"blindMode":true,"retrievalProjects":[],"id":93519082,"numberOfReviewedAnnotations":2,"numberOfJobAnnotations":0,"created":"1389717327194","updated":"1408524340543","disciplineName":"HISTOLOGY","name":"MEDUNIGRAZ-BONE-MARROW","isReadOnly":false,"numberOfAnnotations":128,"retrievalAllOntology":false,"hideUsersLayers":true,"discipline":26481,"isClosed":false,"numberOfImages":2,"retrievalDisable":true}],"title":"BM-01","isFolder":true,"created":"1390234826742","updated":null,"name":"BM-01","user":93518990,"key":95190197};

	$scope.assign = function(term) {
		$log.debug("assigning term '" + term.name + "' to annotation");
		sharedService.addAlert("Term " + term.name + " has been assigned.");
	}

	$scope.postAssignment = function(child, annotation) {

	}
	

});
