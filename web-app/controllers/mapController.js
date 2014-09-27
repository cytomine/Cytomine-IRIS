var iris = angular.module("irisApp");

iris.constant("image")

iris.controller("mapCtrl", function($scope, $log, $location, $http, olData, sharedService, sessionService, imageService, projectService, olHelpers){
	console.log("mapCtrl");
	
	$scope.annotation = {"class":"be.cytomine.ontology.UserAnnotation","id":95166527,"created":"1390206214759","updated":null,"deleted":null,"location":"POLYGON ((37390 14006, 37393.75693066626 14012.325030868275, 37394.6164357884 14019.6313154549, 37392.42989912241 14026.655530779337, 37387.57539272194 14032.183126199, 37380.892304845416 14035.258330249198, 37373.53620210064 14035.349412212301, 37366.77902148027 14032.440623189437, 37361.78914090065 14027.034919228176, 37359.42935604561 14020.066995652698, 37360.107695154584 14012.741669750802, 37363.7068672331 14006.325556919424, 37369.60454273133 14001.928061355664, 37376.78095997694 14000.309549992488, 37383.995251232445 14001.749878148303, 37390 14006))","image":94255021,"geometryCompression":0.0,"project":93519082,"container":93519082,"user":93518990,"nbComments":0,"area":403.0,"perimeterUnit":"mm","areaUnit":"micron²","perimeter":0.0,"centroid":{"x":37377.0,"y":14018.0},"term":[95202360],"similarity":null,"rate":null,"idTerm":null,"idExpectedTerm":null,"cropURL":"http://beta.cytomine.be/api/userannotation/95166527/crop.jpg","smallCropURL":"http://beta.cytomine.be/api/userannotation/95166527/crop.png?maxSize=256","url":"http://beta.cytomine.be/api/userannotation/95166527/crop.jpg","imageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-95166527","reviewed":false};

	var currImg = sessionService.getCurrentImage();

	var imgWidth = currImg.cytomine.width;//56640;
	var imgHeight = currImg.cytomine.height;//39163;
	// top right
	//var annotation = {"class":"be.cytomine.ontology.UserAnnotation","id":138246291,"created":"1411712567955","updated":null,"deleted":null,"location":"POLYGON ((56590.3125 39104.125, 56590.3125 39156.125, 56638.3125 39156.125, 56638.3125 39104.125, 56590.3125 39104.125))","image":94255021,"geometryCompression":0.0,"project":93519082,"container":93519082,"user":93518990,"nbComments":0,"area":1055.0,"perimeterUnit":"mm","areaUnit":"micron²","perimeter":0.0,"centroid":{"x":56614.3125,"y":39130.125},"term":[95795881],"similarity":null,"rate":null,"idTerm":null,"idExpectedTerm":null,"cropURL":"http://beta.cytomine.be/api/userannotation/138246291/crop.jpg","smallCropURL":"http://beta.cytomine.be/api/userannotation/138246291/crop.png?maxSize=256","url":"http://beta.cytomine.be/api/userannotation/138246291/crop.jpg","imageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-138246291","reviewed":false};
	//var annotation = {"class":"be.cytomine.ontology.UserAnnotation","id":138246683,"created":"1411712682787","updated":null,"deleted":null,"location":"POLYGON ((56630.8125 1.6250000000000009, 56630.8125 9.625, 56638.8125 9.625, 56638.8125 1.6250000000000009, 56630.8125 1.6250000000000009))","image":94255021,"geometryCompression":0.0,"project":93519082,"container":93519082,"user":93518990,"nbComments":0,"area":27.0,"perimeterUnit":"mm","areaUnit":"micron²","perimeter":0.0,"centroid":{"x":56634.8125,"y":5.625},"term":[95795881],"similarity":null,"rate":null,"idTerm":null,"idExpectedTerm":null,"cropURL":"http://beta.cytomine.be/api/userannotation/138246683/crop.jpg","smallCropURL":"http://beta.cytomine.be/api/userannotation/138246683/crop.png?maxSize=256","url":"http://beta.cytomine.be/api/userannotation/138246683/crop.jpg","imageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-138246683","reviewed":false}
	var centroid = $scope.annotation.centroid;		
	
	var pointStyle = new ol.style.Style({
	    fill: new ol.style.Fill({
	      color: [0xff, 0xff, 0xff, 0.0]
	    }),
		stroke: new ol.style.Stroke({
		  width: 3,
	      color: [0xff, 0xff, 0xff, 1.0]
	    })
	}); 
	
	var url = currImg.olTileServerURL;"http://localhost:8080/image/tile?zoomify=/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/";

	var crossOrigin = 'anonymous';
	var imgCenter = [imgWidth / 2, imgHeight / 2];
	//centroid = { x: imgCenter[0], y: imgCenter[1] };
	
	// create the zoomify dummy projection
	var proj = new ol.proj.Projection({
		code: 'Zoomify',
		units: 'pixels',
		extent: [0, 0, imgWidth, imgHeight] // don't skip that, otherwise the image is not rendered
	});

	// configure the openlayers map properties
	var initializeMap = function(){
		angular.extend($scope, {
	        layers: {
	            main: {
	            	opacity: 1,
	            	type: "Zoomify",
	                source: {
	                	type: "Zoomify",
	                    url: url,
	                    width: imgWidth,
	                    height: imgHeight,
	                    crossOrigin: crossOrigin,
	                },
	            },
	            annotationMarker: {
	            	opacity: 1,
	            	type: 'Vector',
	            	source: {
	            		type: 'Vector',
	            	},
	            	style: {
	            		fill: {
	            			color: pointStyle.getFill().getColor()
	            		},
	            		stroke: {
	            			width: pointStyle.getStroke().getWidth(),
	            			color: pointStyle.getStroke().getColor()
	            		}
	            	
	            	}
	            },
	    	},
	        
	        //view: {
	        //	projection: proj,
	        //},
	        
			// initially zoom to annotation center
		    center: {
	            lon: centroid.x,
	            lat: centroid.y-imgHeight, 
	            zoom: 8,
	            bounds: [],
	            projection: proj,
	            centerUrlHash: false
	        },
	        
	        // these default controls are applied to the map
	        // at creation time
		    defaults: {
		        controls: {
		        	rotate: false,
			    	attribution: false
			    },
//			    interactions: {
//			    	
//			    },
		        maxZoom: 8,
		        minZoom: 0
		    },
		});
	};
	
	var createAnnotationFeature = function (annotation){
//		console.log("Shape: " + annotation["location"]);
//		console.log("Center: " + centroid.x + ", " + centroid.y);

		var format = new ol.format.WKT();
		//var feature = format.readFeature(annotation["location"]);
		var feature = new ol.Feature({ 
				geometry: new ol.geom.Circle([ centroid.x, centroid.y ], 40),
				name: 'annotation center'
				});
		//var poly = format.readFeature("POLYGON ((500 500, 500 1500, 1500 1500, 1500 500, 500 500))");
		
		var coords = feature.getGeometry().flatCoordinates;
		
		// transform the coordinates 
		// TODO a better solution would be writing a custom
		// ol.proj.addCoordinateTransforms(source, destination, forward, inverse) function	
		for (var i=1; i <= coords.length; i+=2){
			coords[i] = coords[i] - imgHeight;
		}
		
		console.log("Transformed Center: " + coords[0] + ", " + coords[1]);
		
		return feature;
	};
	
	// move to the annotation and draw the shape
	var setFeature = function (feature, layer) {
		// construct the layer if it its null
		if (layer){
			// add the feature to the source
			layer.getSource().clear();
			layer.getSource().addFeature(feature);
		}else {
			var layer = new ol.layer.Vector({
			  	source: new ol.source.Vector({
			    	features: [feature]
					}),
				style: pointStyle
				});
		}
	};
	
	$scope.addAnnotation = function() {
        olData.getMap().then(function(map) {
            olData.getLayers().then(function(layers) {
            	setFeature(createAnnotationFeature($scope.annotation), layers.annotationMarker);
            });
        });
    };
	// make the feature and construct the map
    initializeMap();
    
    $scope.moveToCenter = function(){
    	$scope.center.lon = centroid.x;
    	$scope.center.lat = centroid.y-imgHeight;
    }
});

