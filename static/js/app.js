'use strict';

console.log("toto");


// Declare app level module which depends on filters, and services
angular.module('myApp', ['myApp.filters', 'myApp.services', 'myApp.directives']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/view1', {templateUrl: 'static/partials/partial1.html', controller: MyCtrl1});
    $routeProvider.otherwise({redirectTo: '/view1'});
  }]);
