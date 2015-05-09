var iris = angular.module("irisApp");

iris.controller("userTreeCtrl", [
"$rootScope", "$scope", "$routeParams", "$timeout", "$log", "sessionService", "imageService", "projectService", "sharedService",
                                  function($rootScope, $scope, $routeParams, $timeout, $log, sessionService, imageService, projectService, sharedService){
	
	$log.debug("userTreeCtrl");

	var demoTree = [{"sipAccount":null,"id":140736295,"username":"rhoyoux","algo":false,"color":null,"email":"Renaud.Hoyoux@yahoo.fr","updated":null,"created":"1412246855717","lastname":"Hoyoux","class":"be.cytomine.security.User","firstname":"Renaud","deleted":null},{"sipAccount":null,"id":14,"username":"rmaree","algo":false,"color":"#FF0000","email":"raphael.maree@ulg.ac.be","updated":"1402643851750","created":"1311163667193","lastname":"Marée","class":"be.cytomine.security.User","firstname":"Raphaël","deleted":null},{"sipAccount":null,"id":151649280,"username":"rsedivy","algo":false,"color":null,"email":"roland.sedivy@stpoelten.lknoe.at","updated":null,"created":"1419264474034","lastname":"Sedivy","class":"be.cytomine.security.User","firstname":"Roland","deleted":null},{"sipAccount":null,"id":151649217,"username":"aaigelsreiter","algo":false,"color":null,"email":"ariane.aigelsreiter@medunigraz.at","updated":null,"created":"1419264352378","lastname":"Aigelsreiter","class":"be.cytomine.security.User","firstname":"Ariane","deleted":null},{"sipAccount":null,"id":151649238,"username":"pregitnig","algo":false,"color":null,"email":"peter.regitnig@medunigraz.at","updated":null,"created":"1419264390493","lastname":"Regitnig","class":"be.cytomine.security.User","firstname":"Peter","deleted":null},{"sipAccount":null,"id":107758880,"username":"hahammer","algo":false,"color":null,"email":"helmut.ahammer@medunigraz.at","updated":"1426756447481","created":"1395236852917","lastname":"Ahammer","class":"be.cytomine.security.User","firstname":"Helmut","deleted":null},{"sipAccount":null,"id":107758862,"username":"masslaber","algo":false,"color":null,"email":"martin.asslaber@medunigraz.at","updated":"1419289572988","created":"1395236852759","lastname":"Asslaber","class":"be.cytomine.security.User","firstname":"Martin","deleted":null},{"publicKey":"0880e4b4-fe26-4967-8169-f15ed2f9be5c","privateKey":"a511a35c-5941-4932-9b40-4c8c4c76c7e7","passwordExpired":false,"class":"be.cytomine.security.User","lastname":"Kainz","firstname":"Philipp","deleted":null,"id":93518990,"sipAccount":null,"username":"pkainz","algo":false,"color":null,"created":"1389716961603","updated":"1421594903273","email":"philipp.kainz@medunigraz.at"},{"sipAccount":null,"id":16,"username":"lrollus","algo":false,"color":"#00FF00","email":"lrollus@ulg.ac.be","updated":"1413721689268","created":"1311163668201","lastname":"Rollus","class":"be.cytomine.security.User","firstname":"Loïc","deleted":null}]
	var checkedUsers = [];
	
	$scope.tree = {
		loading: false
	};
	
	var initTree = function(){
		$scope.showTree = true;
		$scope.checkAllUsers();
	};

	//$scope.refreshUsers = function(){
	//	$scope.projectID = $routeParams['projectID'];
	//	$scope.tree.loading = true;

		// get the users and initialize the tree
		projectService.fetchUsers($scope.projectID, { 'adminOnly':false }, function(users){
			//$scope.treeData = demoTree;
			$scope.treeData = users;
			$scope.tree.loading = false;

			// initialize the tree (show and expand all)
			initTree();
		}, function(data, status){
			sharedService.addAlert("Cannot retrieve users. Status " + status + ".", "danger");
		});
	//};

	$scope.selectedNode = {};
	
	$scope.treeOptions = {
		    nodeChildren: "children",
		    dirSelectable: false
		};
	
    $scope.selectOrUnselectUser = function(evt, userID) {
        var targ;
        
        if (!evt) {
            var evt = window.event;
        } else {
        	var evt = evt;
        }
        
    	if (evt.target) { 
    		targ=evt.target;
    	} else if (evt.srcElement) {
    		targ=evt.srcElement;
    	}
    	$log.debug(targ);
        
        // get the ID of the clicked user
        var id = Number(targ.id.split(":")[1]);
        
        // find the user in the tree data
        var user = getUser($scope.treeData, id);
        
        var chbxID = "chbxUser:"+id;
        
        // if the user is checked, it is in the checked list
        var idx = checkedUsers.indexOf(id);
        var chbx = document.getElementById(chbxID);
        if (isNaN(id)){
        	$log.debug("Nothing has been selected.");
        }
        else if (idx === -1){
        	// add the user
        	checkedUsers.push(id);
        	// select the checkbox
        	user.checked = true;
        }else {
        	// remove the user from the array
        	checkedUsers.splice(idx,1);
        	// unselect the checkbox
        	user.checked = false;
        }

        // notify other instances about the change
        $rootScope.$broadcast("userFilterChange", { id : checkedUsers, action : 'selectedUsers' });
        
        $log.debug("Active users: {" + checkedUsers.toString() + "}.");
    };
    
    $scope.clearSelected = function() {
        $scope.selectedNode = undefined;
    };
    
    $scope.checkAllUsers = function(){
    	checkedUsers = [];
    	
    	for (var i=0; i < $scope.treeData.length; i++){
    		$scope.treeData[i].checked = true;
    		checkedUsers.push($scope.treeData[i].id)
    	}
    	
    	$rootScope.$broadcast("userFilterChange", { id : checkedUsers, action : 'selectedUsers' });
    	
    	$log.debug("Active users: {" + checkedUsers.toString() + "}.");
    	$log.debug("checked all users: " + checkedUsers.length);
    };
    
    $scope.uncheckAllUsers = function(){
    	checkedUsers = [];
    	
    	for (var i=0; i<$scope.treeData.length; i++){
    		$scope.treeData[i].checked = false;
    	}

    	$rootScope.$broadcast("userFilterChange", { id : checkedUsers, action : 'selectedUsers' });
    	$log.debug("Active users: {" + checkedUsers.toString() + "}.");
    	$log.debug("UNchecked all users.")
    };
    
      
}]);

function getUser(userList, id){
	for (var i=0; i<userList.length; i++){
		var obj = userList[i];
		if (obj.id == id) {
			return obj;
		}
	}
	return null;
}

