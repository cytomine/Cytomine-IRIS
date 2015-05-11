var iris = angular.module("irisApp");

iris.controller(
	"userStatsCtrl", [
		"$rootScope", "$scope", "$http", "$filter",
		"$document", "$timeout", "$location", "$route",
		"$routeParams", "$log", "hotkeys",
		"cytomineService", "projectService", "imageService", "sessionService",
		"helpService", "sharedService", "navService", "annotationService", "statisticsService",
		"ngTableParams",
		function($rootScope, $scope, $http, $filter,
				 $document, $timeout, $location, $route,
				 $routeParams, $log, hotkeys,
				 cytomineService, projectService, imageService, sessionService,
				 helpService, sharedService, navService, annotationService, statisticsService,
				 ngTableParams) {
			$log.debug("userStatsCtrl");

			var demoData = {"userStats":{"107758880":{"summary":{"total":0},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":0,"95202360":0,"95202354":0,"95202348":0,"95202334":0,"95202328":0,"95202322":0,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}},"151649217":{"summary":{"total":0},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":0,"95202360":0,"95202354":0,"95202348":0,"95202334":0,"95202328":0,"95202322":0,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}},"107758862":{"summary":{"total":9},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":2,"95202360":2,"95202354":1,"95202348":1,"95202334":1,"95202328":1,"95202322":1,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}},"140736295":{"summary":{"total":0},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":0,"95202360":0,"95202354":0,"95202348":0,"95202334":0,"95202328":0,"95202322":0,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}},"93518990":{"summary":{"total":116},"stats":{"141097687":7,"151994413":3,"152509195":0,"95795881":26,"95202366":8,"95202360":6,"95202354":8,"95202348":8,"95202334":3,"95202328":4,"95202322":9,"95202311":5,"95202287":5,"95202281":3,"95202274":1,"95202266":5,"95202372":15}},"14":{"summary":{"total":0},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":0,"95202360":0,"95202354":0,"95202348":0,"95202334":0,"95202328":0,"95202322":0,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}},"151649238":{"summary":{"total":0},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":0,"95202360":0,"95202354":0,"95202348":0,"95202334":0,"95202328":0,"95202322":0,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}},"16":{"summary":{"total":3},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":1,"95202360":1,"95202354":0,"95202348":1,"95202334":0,"95202328":0,"95202322":0,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}},"151649280":{"summary":{"total":0},"stats":{"141097687":0,"151994413":0,"152509195":0,"95795881":0,"95202366":0,"95202360":0,"95202354":0,"95202348":0,"95202334":0,"95202328":0,"95202322":0,"95202311":0,"95202287":0,"95202281":0,"95202274":0,"95202266":0,"95202372":0}}},"users":[{"sipAccount":null,"id":107758880,"username":"hahammer","algo":false,"color":null,"email":"helmut.ahammer@medunigraz.at","updated":"1426756447481","created":"1395236852917","lastname":"Ahammer","class":"be.cytomine.security.User","firstname":"Helmut","deleted":null},{"sipAccount":null,"id":151649217,"username":"aaigelsreiter","algo":false,"color":null,"email":"ariane.aigelsreiter@medunigraz.at","updated":null,"created":"1419264352378","lastname":"Aigelsreiter","class":"be.cytomine.security.User","firstname":"Ariane","deleted":null},{"sipAccount":null,"id":107758862,"username":"masslaber","algo":false,"color":null,"email":"martin.asslaber@medunigraz.at","updated":"1419289572988","created":"1395236852759","lastname":"Asslaber","class":"be.cytomine.security.User","firstname":"Martin","deleted":null},{"sipAccount":null,"id":140736295,"username":"rhoyoux","algo":false,"color":null,"email":"Renaud.Hoyoux@yahoo.fr","updated":null,"created":"1412246855717","lastname":"Hoyoux","class":"be.cytomine.security.User","firstname":"Renaud","deleted":null},{"publicKey":"0880e4b4-fe26-4967-8169-f15ed2f9be5c","privateKey":"a511a35c-5941-4932-9b40-4c8c4c76c7e7","passwordExpired":false,"class":"be.cytomine.security.User","lastname":"Kainz","firstname":"Philipp","deleted":null,"id":93518990,"sipAccount":null,"username":"pkainz","algo":false,"color":null,"created":"1389716961603","updated":"1421594903273","email":"philipp.kainz@medunigraz.at"},{"sipAccount":null,"id":14,"username":"rmaree","algo":false,"color":"#FF0000","email":"raphael.maree@ulg.ac.be","updated":"1402643851750","created":"1311163667193","lastname":"Marée","class":"be.cytomine.security.User","firstname":"Raphaël","deleted":null},{"sipAccount":null,"id":151649238,"username":"pregitnig","algo":false,"color":null,"email":"peter.regitnig@medunigraz.at","updated":null,"created":"1419264390493","lastname":"Regitnig","class":"be.cytomine.security.User","firstname":"Peter","deleted":null},{"sipAccount":null,"id":16,"username":"lrollus","algo":false,"color":"#00FF00","email":"lrollus@ulg.ac.be","updated":"1413721689268","created":"1311163668201","lastname":"Rollus","class":"be.cytomine.security.User","firstname":"Loïc","deleted":null},{"sipAccount":null,"id":151649280,"username":"rsedivy","algo":false,"color":null,"email":"roland.sedivy@stpoelten.lknoe.at","updated":null,"created":"1419264474034","lastname":"Sedivy","class":"be.cytomine.security.User","firstname":"Roland","deleted":null}],"terms":[{"hideCheckbox":false,"data":"Z00_Unknown","class":"be.cytomine.ontology.Term","parent":null,"children":[],"attr":{"id":141097687,"type":"be.cytomine.ontology.Term"},"id":141097687,"title":"Z00_Unknown","isFolder":false,"color":"#b84900","name":"Z00_Unknown","checked":false,"key":141097687,"parentName":"root"},{"hideCheckbox":false,"data":"Z01_Lymphocyte","class":"be.cytomine.ontology.Term","parent":null,"children":[],"attr":{"id":151994413,"type":"be.cytomine.ontology.Term"},"id":151994413,"title":"Z01_Lymphocyte","isFolder":false,"color":"#2bfdcf","name":"Z01_Lymphocyte","checked":false,"key":151994413,"parentName":"root"},{"hideCheckbox":false,"data":"Z02_Plasma Cell","class":"be.cytomine.ontology.Term","parent":null,"children":[],"attr":{"id":152509195,"type":"be.cytomine.ontology.Term"},"id":152509195,"title":"Z02_Plasma Cell","isFolder":false,"color":"#2bfdcf","name":"Z02_Plasma Cell","checked":false,"key":152509195,"parentName":"root"},{"hideCheckbox":false,"data":"Z99_Background","class":"be.cytomine.ontology.Term","parent":null,"children":[],"attr":{"id":95795881,"type":"be.cytomine.ontology.Term"},"id":95795881,"title":"Z99_Background","isFolder":false,"color":"#677dff","name":"Z99_Background","checked":false,"key":95795881,"parentName":"root"},{"hideCheckbox":false,"data":"E01_Pronormoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202366,"type":"be.cytomine.ontology.Term"},"id":95202366,"title":"E01_Pronormoblast","isFolder":false,"color":"#2bfdcf","name":"E01_Pronormoblast","checked":false,"key":95202366,"parentName":"Erythropoiesis"},{"hideCheckbox":false,"data":"E02_Basophilic Normoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202360,"type":"be.cytomine.ontology.Term"},"id":95202360,"title":"E02_Basophilic Normoblast","isFolder":false,"color":"#2bfdcf","name":"E02_Basophilic Normoblast","checked":false,"key":95202360,"parentName":"Erythropoiesis"},{"hideCheckbox":false,"data":"E03_Polychromatophilic Normoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202354,"type":"be.cytomine.ontology.Term"},"id":95202354,"title":"E03_Polychromatophilic Normoblast","isFolder":false,"color":"#2bfdcf","name":"E03_Polychromatophilic Normoblast","checked":false,"key":95202354,"parentName":"Erythropoiesis"},{"hideCheckbox":false,"data":"E04_Orthochromatic Normoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202348,"type":"be.cytomine.ontology.Term"},"id":95202348,"title":"E04_Orthochromatic Normoblast","isFolder":false,"color":"#2bfdcf","name":"E04_Orthochromatic Normoblast","checked":false,"key":95202348,"parentName":"Erythropoiesis"},{"hideCheckbox":false,"data":"E05_Reticulocyte","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202334,"type":"be.cytomine.ontology.Term"},"id":95202334,"title":"E05_Reticulocyte","isFolder":false,"color":"#2bfdcf","name":"E05_Reticulocyte","checked":false,"key":95202334,"parentName":"Erythropoiesis"},{"hideCheckbox":false,"data":"E06_Erythrocyte","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202328,"type":"be.cytomine.ontology.Term"},"id":95202328,"title":"E06_Erythrocyte","isFolder":false,"color":"#2bfdcf","name":"E06_Erythrocyte","checked":false,"key":95202328,"parentName":"Erythropoiesis"},{"hideCheckbox":false,"data":"G01_Myeloblast","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202322,"type":"be.cytomine.ontology.Term"},"id":95202322,"title":"G01_Myeloblast","isFolder":false,"color":"#2bfdcf","name":"G01_Myeloblast","checked":false,"key":95202322,"parentName":"Granulopoiesis"},{"hideCheckbox":false,"data":"G02_Promyelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202311,"type":"be.cytomine.ontology.Term"},"id":95202311,"title":"G02_Promyelocyte","isFolder":false,"color":"#2bfdcf","name":"G02_Promyelocyte","checked":false,"key":95202311,"parentName":"Granulopoiesis"},{"hideCheckbox":false,"data":"G03_Myelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202287,"type":"be.cytomine.ontology.Term"},"id":95202287,"title":"G03_Myelocyte","isFolder":false,"color":"#2bfdcf","name":"G03_Myelocyte","checked":false,"key":95202287,"parentName":"Granulopoiesis"},{"hideCheckbox":false,"data":"G04_Metamyelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202281,"type":"be.cytomine.ontology.Term"},"id":95202281,"title":"G04_Metamyelocyte","isFolder":false,"color":"#2bfdcf","name":"G04_Metamyelocyte","checked":false,"key":95202281,"parentName":"Granulopoiesis"},{"hideCheckbox":false,"data":"G05_Band Cell","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202274,"type":"be.cytomine.ontology.Term"},"id":95202274,"title":"G05_Band Cell","isFolder":false,"color":"#2bfdcf","name":"G05_Band Cell","checked":false,"key":95202274,"parentName":"Granulopoiesis"},{"hideCheckbox":false,"data":"G06_Granulocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202266,"type":"be.cytomine.ontology.Term"},"id":95202266,"title":"G06_Granulocyte","isFolder":false,"color":"#2bfdcf","name":"G06_Granulocyte","checked":false,"key":95202266,"parentName":"Granulopoiesis"},{"hideCheckbox":false,"data":"M01_Megakaryocyte","class":"be.cytomine.ontology.Term","parent":136483515,"children":[],"attr":{"id":95202372,"type":"be.cytomine.ontology.Term"},"id":95202372,"title":"M01_Megakaryocyte","isFolder":false,"color":"#2bfdcf","name":"M01_Megakaryocyte","checked":false,"key":95202372,"parentName":"Megakaryopoiesis"}]};

			// retrieve the project parameter from the URL
			$scope.projectID = $routeParams["projectID"];

			// get the current date as long
			$scope.today = sharedService.today;

			$scope.userstats = {
				error : {},
				opening : {}
			};

			$scope.barChartOptions = {
				chart: {
					type: 'discreteBarChart',
					height: 300,
					margin : {
						top: 20,
						right: 20,
						bottom: 60,
						left: 55
					},
					x: function(series){return series.label;},
					y: function(series){return series.value;},
					showValues: false,
					valueFormat: function(value){
						return d3.format(',.2f')(value);
					},
					transitionDuration: 500,
					xAxis: {
						axisLabel: 'Terms',
						rotateLabels: -45
						//width: 600
					},
					yAxis: {
						axisLabel: 'Frequency',
						axisLabelDistance: 10
					}
				}
			};

			// refresh the page
			$scope.refreshPage = function(){
				// show the loading button
				$scope.loading = true;

				statisticsService.fetchUserStatistics($scope.projectID, null, null, null, function(data){

					$scope.globalStats = data.annotations;

					$scope.dataset = {};

					for (var k = 0; k < data.users.length; k++){
						var values = data.users[k].userStats;
						$scope.dataset[k] = [ {
							"key" : (data.users[k].lastname + " " + data.users[k].firstname),
							"user" : data.users[k],
							"values" : values
							} ];
					}

					$scope.loading=false;
				}, function(data, status){
					// fetching failed
					$scope.userstats.error.retrieve = {
						status : status,
						message : data.error.message
					};
					$scope.loading = false;
				});
			};

//	fetch the annotation
	$scope.refreshPage();

//	//////////////////////////////////////////
//	declare additional methods
//	//////////////////////////////////////////
//	Determine the row's background color class according 
//	to the current labeling progress.
			$scope.addKeys = function(url){
				try {
					return cytomineService.addKeys(url);
				} catch (e){
					return '';
				}
			};
		}]);
