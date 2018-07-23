/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sentinelDashboardApp')
  .directive('header', [function () {
    return {
      templateUrl: 'app/scripts/directives/header/header.html',
      restrict: 'E',
      replace: true,
      controller: function ($scope) {
      }
    }
  }]);
