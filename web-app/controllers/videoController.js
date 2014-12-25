var iris = angular.module("irisApp");

iris
		.controller(
				"videoCtrl", [
				"$scope", "$rootScope", "$log", "$sce",
				function($scope, $rootScope, $log, $sce) {
					$scope.video = {
						dnd : {
							galleryTree : {
								config : {
									sources : [
											{
												src : $sce
														.trustAsResourceUrl("videos/iris-gallery-dnd-tree.mp4"),
												type : "video/mp4"
											},
											{
												src : $sce
														.trustAsResourceUrl("videos/iris-gallery-dnd-tree.webm"),
												type : "video/webm"
											}
											],
									tracks : [
									// {
									// src:
									// "http://www.videogular.com/assets/subs/pale-blue-dot.vtt",
									// kind: "subtitles",
									// srclang: "en",
									// label: "English",
									// default: ""
									// }
									],
									theme : "lib/videogular/videogular.css",
									plugins : {
										poster : "videos/poster.png"
									}
								}
							},
							panelHeader : {
								config : {
									sources : [
											{
												src : $sce
														.trustAsResourceUrl("videos/iris-gallery-dnd-panel.mov"),
												type : "video/mp4"
											}, 
											],
									tracks : [
									// {
									// src:
									// "http://www.videogular.com/assets/subs/pale-blue-dot.vtt",
									// kind: "subtitles",
									// srclang: "en",
									// label: "English",
									// default: ""
									// }
									],
									theme : "lib/videogular/videogular.css",
									plugins : {
										poster : "videos/poster.png"
									}
								}
							}
						},
						multiAssign : {
							config : {
								sources : [
										{
											src : $sce
													.trustAsResourceUrl("videos/iris-gallery-multi-assignment.mov"),
											type : "video/mp4"
										}, 
										],
								tracks : [
								// {
								// src:
								// "http://www.videogular.com/assets/subs/pale-blue-dot.vtt",
								// kind: "subtitles",
								// srclang: "en",
								// label: "English",
								// default: ""
								// }
								],
								theme : "lib/videogular/videogular.css",
								plugins : {
									poster : "videos/poster.png"
								}
							}
						}
					}
				}]);