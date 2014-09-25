var iris = angular.module("irisApp");

iris.constant("image")

iris.controller("mapCtrl", function($scope, $log, $location, $http, sharedService, sessionService, imageService, projectService){
	console.log("mapCtrl");
	
	
	var imageInstance = 94255021;
	var abstractImage = 94255014;

    var zoomify_width = 56640;
	var zoomify_height = 39163;
	var zoomify_url = "http://image.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/";
	var path = "&tileGroup=0&z={z}&x={x}&y={y}&layer=0&timeframe=0&mimeType=image/tiff"; 

    var map, zoomify;

	var http = null;

	var getZoomifyURL = function(abstrImageID,imgInstanceID){
		//imageService.fetchImageServerURLs(abstrImageID,imgInstanceID)
	};
	
	var getImageServerURLs = function(){
		console.log(http.readyState)
		if (http.readyState == 4){
			var urlArray = http.responseText;
			console.log(urlArray)
		}
	};
	
	var getTileURL = function (bounds) {		
			var res = this.map.getResolution();
			//console.log('resolution=' + res);
			var x = Math.round ((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
			var y = Math.round ((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
			var z = this.map.getZoom();

			path.replace("{z}", z).replace("{y}", y).replace("{x}", x);
			return zoomify_url + path;
		};

		
		// configure the map
		angular.extend($scope, {
		    defaults: {
		        layers: {
		            main: {
		                source: {
		                    type: "OSM",
//		                    url: "http://{a-c}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png",
		                    url: zoomify_url + path,
		                }
		            }
		        },
		        controls: {
		        	rotate: false,
			    	attribution: false
			    },
		        maxZoom: 8
		    },
		    center: {
                lat: 51.505,
                lon: -0.09,
                zoom: 8
            },
		});
});