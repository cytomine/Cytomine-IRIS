var iris = angular.module("irisApp");

iris.constant("image")

iris.controller("mapCtrl", function($scope, $log, $location, $http, sharedService, sessionService, imageService, projectService){
	console.log("mapCtrl");
	
	
	var imageInstance = 94255021;
	var abstractImage = 94255014;

    var zoomify_width = 56640;
	var zoomify_height = 39163;
	var zoomify_url = "http://image1.cytomine.be/image/tile?zoomify=/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/";
	//var params = "&tileGroup=0&z=3&x=3&y=4&channels=0&layer=0&timeframe=0&mimeType=image/tiff";

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

			var path = '&tileGroup=0&z=' + z + "&x=" + x + "&y=" + y + "&layer=0&timeframe=0&mimeType=image/tiff"; 
			return zoomify_url + path;
		};

    $scope.init = function(){
		// TODO implement getting the ZOOMIFY URL for specific imageinstance
		
       /* First we initialize the zoomify pyramid (to get number of tiers) */
        var zoomify = new OpenLayers.Layer.Zoomify( "Zoomify", zoomify_url, 
	  		new OpenLayers.Size( zoomify_width, zoomify_height ));
		
		// Substitute the default method with my own	
		zoomify.getURL = getTileURL;

       /* Map with raster coordinates (pixels) from Zoomify image */
        var options = {
            maxExtent: new OpenLayers.Bounds(0, 0, zoomify_width, zoomify_height),
            maxResolution: Math.pow(2, zoomify.numberOfTiers-1 ),
            numZoomLevels: zoomify.numberOfTiers,
            units: 'pixels'
        };

        map = new OpenLayers.Map(document.getElementById("map"), options);
        map.addLayer(zoomify);
		
		map.addControl(new OpenLayers.Control.ScaleLine({
			topInUnits : "µm",
			topOutUnits : "µm",
			bottomInUnits : "X",
			bottomOutUnits : "X"
		}));
		
//		var points = [
//			new OpenLayers.Geometry.Point(646.984495410125,152.82261010986718),
//			new OpenLayers.Geometry.Point(646.7418222505303,154.68092265647545),
//			new OpenLayers.Geometry.Point(647.2759730948837,156.47727970860782),
//			new OpenLayers.Geometry.Point(648.4945884095887,157.90107481255916),
//			new OpenLayers.Geometry.Point(650.1869585359601,158.70612086008384),
//			new OpenLayers.Geometry.Point(652.0604573044484,158.75321807595105),
//			new OpenLayers.Geometry.Point(653.7911397577798,158.03422292367262),
//			new OpenLayers.Geometry.Point(655.079755177017,156.67345619694336),
//			new OpenLayers.Geometry.Point(655.7034902494703,154.9062068249924),
//			new OpenLayers.Geometry.Point(655.5544955146572,153.0380482791866),
//			new OpenLayers.Geometry.Point(654.6585335158017,151.39200214378332),
//			new OpenLayers.Geometry.Point(653.170524222065,150.25268474945352),
//			new OpenLayers.Geometry.Point(651.3477579584742,149.81709442405037),
//			new OpenLayers.Geometry.Point(649.5054075713159,150.16054869204999),
//			new OpenLayers.Geometry.Point(647.9620321797577,151.22366119033114),
//			new OpenLayers.Geometry.Point(646.984495410125,152.82261010986718)
//		];
//		
//		 var points2 = [   new OpenLayers.Geometry.Point(0, 0),
//		    new OpenLayers.Geometry.Point(0, 100),
//		    new OpenLayers.Geometry.Point(100, 100),
//		    new OpenLayers.Geometry.Point(100, 0)
//		];
//		
//		var ring = new OpenLayers.Geometry.LinearRing(points);
//		var polygon = new OpenLayers.Geometry.Polygon([ring]);
//
//		// create some attributes for the feature
//		var attributes = {name: "annotation" };
//
//		var feature = new OpenLayers.Feature.Vector(polygon, attributes);
//		var annLayer = new OpenLayers.Layer.Vector("Annotations");
//		annLayer.addFeatures([feature]);

//		var success = map.addLayer(annLayer);
//		console.log(success?"layer added":"layer not added");
//		 TODO
		
        map.setBaseLayer(zoomify);
        map.zoomToMaxExtent();
		
		// TODO set the center to the centroid of the annotation
		//map.setCenter(new OpenLayers.LonLat(point.x, point.y), 5);
	};
	
	$scope.init();
});