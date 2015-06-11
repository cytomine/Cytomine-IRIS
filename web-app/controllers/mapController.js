var iris = angular.module("irisApp");

iris.controller(
    "mapCtrl", [
        "$scope", "$route", "$timeout", "$log", "$location", "$http", "olData", "navService", "sharedService",
        "sessionService", "imageService", "projectService", "$routeParams",
        function ($scope, $route, $timeout, $log, $location, $http, olData, navService, sharedService,
                  sessionService, imageService, projectService, $routeParams) {
            $log.debug("mapCtrl");

            $scope.debug = false;

            // update the image
            // some OL map settings
            var animateMap = false;
            // TODO read from local storage due to a current bug in OL3!!
            var currImg = sessionService.getCurrentImage();

            // check whether the map controller should continue
            if (currImg == null) return;

            var imgWidth = currImg.width;
            var imgHeight = currImg.height;
            var imgDepth = currImg.depth;
            var imgCenter = [ imgWidth / 2, - imgHeight / 2 ];
            var centroid = { x: imgCenter[0], y: imgCenter[1] };

            // style the annotation layer
            var annStyle = new ol.style.Style({
                fill: new ol.style.Fill({
                    color: [0xff, 0xff, 0xff, 0.0]
                }),
                stroke: new ol.style.Stroke({
                    width: 5,
//							color : [ 0x5b, 0xc0, 0xde, 0.75 ]
//							color : [ 0x00, 0x00, 0x00, 0.5 ]
                    color: [0xff, 0xff, 0xff, 1]
                })
            });

            var overlayStyle = new ol.style.Style({
                fill: new ol.style.Fill({
                    color: [0xff, 0xff, 0xff, 0.0]
                }),
                stroke: new ol.style.Stroke({
                    width: 3,
//							color : [ 0x5b, 0xc0, 0xde, 0.75 ]
                    color: [0x00, 0x00, 0x00, 0.5]
                })
            });

			var host = $location.protocol() + "://" + $location.host() + ":" + $location.port()
            var url = host + currImg.olTileServerURL;// "http://localhost:8080/image/tile?zoomify=/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/";

            var crossOrigin = 'anonymous';

            // create the zoomify dummy projection
            var proj = new ol.proj.Projection({
                code: 'Zoomify',
                units: 'pixels',
                extent: [0, 0, imgWidth, imgHeight]
                // don't skip that, otherwise the image is not rendered
            });

            // configure the openlayers map properties
            $scope.initializeMap = function () {
                $log.debug("initializing map");
                angular.extend($scope, {
                    layers: {
                        // identifier = name of the layer
                        image: {
                            opacity: 1,
                            type: "Zoomify",
                            source: {
                                type: "Zoomify",
                                url: url,
                                width: imgWidth,
                                height: imgHeight,
                                crossOrigin: crossOrigin,
                                projection: proj
                            }
                        },
                        // identifier = name of the layer
                        annotations: {
                            opacity: 1,
                            type: 'Vector',
                            source: {
                                type: 'Vector'
                            },
                            style: {
                                fill: {
                                    color: annStyle.getFill()
                                        .getColor()
                                },
                                stroke: {
                                    width: annStyle.getStroke()
                                        .getWidth(),
                                    color: annStyle.getStroke()
                                        .getColor()
                                }

                            }
                        },
                        overlay: {
                            opacity: 1,
                            type: 'Vector',
                            source: {
                                type: 'Vector'
                            },
                            style: {
                                fill: {
                                    color: overlayStyle.getFill()
                                        .getColor()
                                },
                                stroke: {
                                    width: overlayStyle.getStroke()
                                        .getWidth(),
                                    color: overlayStyle.getStroke()
                                        .getColor()
                                }

                            }
                        }
                    },

                    // initially move to image center
                    center: {
                        lon: imgCenter[0],
                        lat: imgCenter[1],
                        zoom: 0,
                        bounds: [],
                        projection: proj, // important: pass the projection here
                        centerUrlHash: false
                    },

                    // these default controls are applied to the map
                    // at creation time
                    defaults: {
                        controls: {
                            rotate: false,
                            attribution: false
                        },
                        maxZoom: imgDepth,
                        minZoom: 0
                    }
                });
            };

            var createAnnotationFeature = function (annotation) {
                var format = new ol.format.WKT();
                var feature = format
                    .readFeature(annotation["location"]);

                // var feature = new ol.Feature({
                // geometry: new ol.geom.Circle([ centroid.x, centroid.y
                // ], 40),
                // name: 'annotation center'
                // });

                var geometry = feature.getGeometry();
                var coords = geometry.getCoordinates();

                // transform the coordinates
                // TODO a better solution would be writing a custom
                // ol.proj.addCoordinateTransforms(source, destination,
                // forward, inverse) function
                for (var i = 0; i < coords[0].length; i++) {
                    var xy = coords[0][i];
                    xy[1] = xy[1] - imgHeight;
                }

                geometry.setCoordinates(coords, geometry.getLayout());

                $log.debug("Transformed coordinates of feature");

                // write out the extent
                $scope.extent = geometry.getExtent();
                return feature;
            };

            // move to the annotation and draw the shape
            var setFeature = function (feature, layer, style) {
                // construct the layer if it its null
                if (layer) {
                    $log.debug(layer.get('name'));
                    // add the feature to the source
                    layer.getSource().clear();
                    layer.getSource().addFeature(feature);
                } else {
                    $log.debug("#########NEWLAYER");
                    var layer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: [feature]
                        }),
                        style: style
                    });
                }

                return feature;
            };

            // fit the geometry to the center
            $scope.resetView = function (optimizeZoom) {
                olData.getMap().then(
                    function (map) {
                        var view = map.getView();
                        $log.debug(view.getProjection().getCode());
                        if (view.getProjection().getCode() !== 'Zoomify') {
                            // DUE TO A CURRENT BUG IN THE OL3 SYSTEM
                            // WE HAVE TO ENTIRELY RELOAD THE PATH
                            // EPSG:3857 is automatically set to default
                            // workaround for openlayers problem with refreshing page
                            // on browser history navigation
                            navService.resumeAtLabelingPage();
                        }

                        if (animateMap) {
                            var duration = 1000;
                            var start = +new Date();
                            var pan = ol.animation.pan({
                                duration: duration,
                                source: (view.getCenter() === null) ? [0, 0] : view.getCenter(),
                                start: start
                            });
                            var bounce = ol.animation.bounce({
                                duration: duration,
                                resolution: 2 * view.getResolution(),
                                start: start
                            });
                            map.beforeRender(pan, bounce);
                        }

                        if (optimizeZoom) {
                            var polygon = feature.getGeometry();
                            var size = map.getSize();
                            view.fitGeometry(polygon, size, {
                                constrainResolution: true
                            })
                        }
                        view.setCenter([centroid.x, centroid.y - imgHeight]);
                    });
            };

            $scope.panToCenter = function () {
                $scope.resetView(false);
            };

            // compute the view for a given annotation
            $scope.showAnnotation = function (annotation) {
                olData.getMap().then(function (map) {
                    olData.getLayers().then(function (layers) {
                        centroid = {
                            x: annotation.x,
                            y: annotation.y
                        };
                        feature = createAnnotationFeature($scope.annotation);
                        setFeature(feature, layers.annotations, annStyle);
                        setFeature(feature, layers.overlay, overlayStyle);
                        $scope.resetView(true);
                    });
                });
            };

            // add a listener to the map's layers
            olData.getMap().then(function (map) {
                map.getLayers().on('add', function (e) {
                    var layer = e.element;
                    var layerName = (layer.get('name'));
                    if (layerName === 'annotations') {
                        if ($scope.annotation === undefined) {
                            return;
                        }
                        //$scope.showAnnotation($scope.annotation) // handles initial view
                    }
                })
            });

            // if the annotation changes, set the map view
            $scope.$watch('annotation', function (annotation) {
                if ($scope.debug) {
                    console.log("$watched annotation: " + annotation);
                }
                if (annotation === undefined) {
                    return;
                }
                $scope.showAnnotation(annotation);
            });

            // the feature variable
            var feature;
            // construct the map
            $scope.initializeMap();

            $log.debug("annotation: " + $scope.annotation);

            // set the view
            if ($scope.annotation !== undefined) {
                $scope.showAnnotation($scope.annotation);
            }
            // test the annotation
            //$timeout(function(){$scope.annotation = annotation; },3000)
        }]);
