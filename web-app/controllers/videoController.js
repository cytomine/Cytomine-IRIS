var iris = angular.module("irisApp");

iris
		.controller(
				"videoCtrl", [
				"$scope", "$rootScope", "$log", "$sce",
				function($scope, $rootScope, $log, $sce) {
					var defaultTheme = "lib/videogular/videogular.min.css";

					var defaultControls = {
						autoHide : true,
						autoHideTime : 5000
					}

					$scope.video = {
						dnd : {
							galleryTree : {
								config : {
									sources : [
											{
												src : $sce
														.trustAsResourceUrl("videos/help/gallery/iris-gallery-dnd-tree.mp4"),
												type : "video/mp4"
											},
											{
												src : $sce
														.trustAsResourceUrl("videos/help/gallery/iris-gallery-dnd-tree.webm"),
												type : "video/webm"
											}
											],
									tracks : [],
									theme : defaultTheme,
									plugins : {
										poster : "videos/help/gallery/thumbnails/iris-gallery-dnd-tree.png",
										controls : defaultControls
									}
								}
							},
							panelHeader : {
								config : {
									sources : [
											{
												src : $sce
														.trustAsResourceUrl("videos/help/gallery/iris-gallery-dnd-panel.mp4"),
												type : "video/mp4"
											},
											{
												src : $sce
														.trustAsResourceUrl("videos/help/gallery/iris-gallery-dnd-panel.webm"),
												type : "video/webm"
											}
											],
									tracks : [],
									theme : defaultTheme,
									plugins : {
										poster : "videos/help/gallery/thumbnails/iris-gallery-dnd-panel.png",
										controls : defaultControls
									}
								}
							}
						},
						multiAssign : {
							config : {
								sources : [
										{
												src : $sce
														.trustAsResourceUrl("videos/help/gallery/iris-gallery-multi-assignment.mp4"),
												type : "video/mp4"
											},
											{
												src : $sce
														.trustAsResourceUrl("videos/help/gallery/iris-gallery-multi-assignment.webm"),
												type : "video/webm"
											}
										],
								tracks : [],
								theme : defaultTheme,
								plugins : {
									poster : "videos/help/gallery/thumbnails/iris-gallery-multi-assignment.png",
									controls : defaultControls
								}
							}
						}, 
						tutorial : {
							config : {
								sources : [
										{
												src : $sce
														.trustAsResourceUrl("videos/tutorial/iris-tutorial.mp4"),
												type : "video/mp4"
											},
											{
												src : $sce
														.trustAsResourceUrl("videos/tutorial/iris-tutorial.webm"),
												type : "video/webm"
											}
										],
								tracks : [],
								theme : defaultTheme,
								plugins : {
									poster : "videos/tutorial/thumbnails/iris-tutorial.png",
									controls : defaultControls
								}
							}
						}
					}
				}]);