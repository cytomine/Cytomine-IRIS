var iris = angular.module("irisApp");

iris.constant("image")

iris.controller("mapCtrl", function($scope, $log, $location, $http, sharedService, sessionService, imageService, projectService, olHelpers){
	console.log("mapCtrl");
	
	
//	var imageInstance = 94255021;
//	var abstractImage = 94255014;
//
    var zoomify_width = 56640;
	var zoomify_height = 39163;
	var zoomify_url = "http://image.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/";
	var path = "&tileGroup=0&z={z}&x={x}&y={y}&layer=0&timeframe=0&mimeType=image/tiff"; 

//    var map, zoomify;
//
//	var http = null;
//
//	var getZoomifyURL = function(abstrImageID,imgInstanceID){
//		//imageService.fetchImageServerURLs(abstrImageID,imgInstanceID)
//	};
//	
//	var getImageServerURLs = function(){
//		console.log(http.readyState)
//		if (http.readyState == 4){
//			var urlArray = http.responseText;
//			console.log(urlArray)
//		}
//	};
//	
//	var getTileURL = function (bounds) {		
//			var res = this.map.getResolution();
//			//console.log('resolution=' + res);
//			var x = Math.round ((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
//			var y = Math.round ((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
//			var z = this.map.getZoom();
//
//			path.replace("{z}", z).replace("{y}", y).replace("{x}", x);
//			return zoomify_url + path;
//		};
	
		var centroid = {
						x:26813.516100906003,
			 			y:12158.247119646
			 			}

		
		// configure the map
		angular.extend($scope, {
		    defaults: {
		        layers: {
		            main: {
		                source: {
//							type: "OSM",
//		                    url: "http://{a-c}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png",

		                	type: "Zoomify",
		                    url: zoomify_url,
		                    width: zoomify_width,
		                    height: zoomify_height,
		                    crossOrigin: 'anonymous',
//		                    tileUrlFunction : TODO ????
		                }
		            }
		        },
		        controls: {
		        	rotate: false,
			    	attribution: false
			    },
		        maxZoom: 8
		    },
		    
		    // initially zoom to XY
		    center: {
                lat: 51.505, // y
                lon: -0.09, // x
                zoom: 8,
                bounds: [],
                centerUrlHash: true
            },
		});
		
//		$scope.$on("centerUrlHash", function(event, centerHash) {
//            console.log("url", centerHash);
//            $location.search({ c: centerHash });
//        });
		
		$scope.$watch("offset", function(offset) {
            $scope.center.bounds[0] += parseFloat(offset, 10);
            $scope.center.bounds[1] += parseFloat(offset, 10);
            $scope.center.bounds[2] -= parseFloat(offset, 10);
            $scope.center.bounds[3] -= parseFloat(offset, 10);
        });
});