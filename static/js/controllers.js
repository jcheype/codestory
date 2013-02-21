'use strict';

/* Controllers */


function MyCtrl1($scope) {
    $scope.screen = "vide";

    $scope.send = function(){
        console.log($scope.sentence);
    }
}


function MyCtrl2() {
}
MyCtrl2.$inject = [];


