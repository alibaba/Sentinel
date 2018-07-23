/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */

angular.module('sentinelDashboardApp')
  .directive('sidebarSearch', function () {
    return {
      templateUrl: 'app/scripts/directives/sidebar/sidebar-search/sidebar-search.html',
      restrict: 'E',
      replace: true,
      scope: {
      },
      controller: function ($scope) {
        $scope.selectedMenu = 'home';
      }
    }
  });
