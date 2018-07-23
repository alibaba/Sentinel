'use strict';

/**
 * @ngdoc overview
 * @name sentinelDashboardApp
 * @description
 * # sentinelDashboardApp
 *
 * Main module of the application.
 */

angular
  .module('sentinelDashboardApp', [
    'oc.lazyLoad',
    'ui.router',
    'ui.bootstrap',
    'angular-loading-bar',
    'ngDialog',
    'ui.bootstrap.datetimepicker',
    'ui-notification',
    'rzTable',
    'angular-clipboard',
    'selectize',
    'angularUtils.directives.dirPagination'
  ])
  .config(['$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider',
    function ($stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {
    $ocLazyLoadProvider.config({
      debug: false,
      events: true,
    });

    $urlRouterProvider.otherwise('/dashboard/home');

    $stateProvider
      .state('dashboard', {
        url: '/dashboard',
        templateUrl: 'app/views/dashboard/main.html',
        resolve: {
          loadMyDirectives: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load(
              {
                name: 'sentinelDashboardApp',
                files: [
                  'app/scripts/directives/header/header.js',
                  'app/scripts/directives/sidebar/sidebar.js',
                  'app/scripts/directives/sidebar/sidebar-search/sidebar-search.js',
                ]
              });
          }]
        }
      })

      .state('dashboard.home', {
        url: '/home',
        templateUrl: 'app/views/dashboard/home.html',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/main.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.flow', {
        templateUrl: 'app/views/flow.html',
        url: '/flow/:app',
        controller: 'FlowCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/flow.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.degrade', {
        templateUrl: 'app/views/degrade.html',
        url: '/degrade/:app',
        controller: 'DegradeCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/degrade.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.system', {
        templateUrl: 'app/views/system.html',
        url: '/system/:app',
        controller: 'SystemCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/system.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.machine', {
        templateUrl: 'app/views/machine.html',
        url: '/app/:app',
        controller: 'MachineCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/machine.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.identity', {
        templateUrl: 'app/views/identity.html',
        url: '/identity/:app',
        controller: 'IdentityCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/identity.js',
              ]
            });
          }]
        }
      })
      .state('dashboard.metric', {
        templateUrl: 'app/views/metric.html',
        url: '/metric/:app',
        controller: 'MetricCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/metric.js',
              ]
            });
          }]
        }
      });
  }]);
