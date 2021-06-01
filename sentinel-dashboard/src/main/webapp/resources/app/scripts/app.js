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
  .factory('AuthInterceptor', ['$window', '$state', function ($window, $state) {
    var authInterceptor = {
      'responseError' : function(response) {
        if (response.status === 401) {
          // If not auth, clear session in localStorage and jump to the login page
          $window.localStorage.removeItem('session_sentinel_admin');
          $state.go('login');
        }

        return response;
      },
      'response' : function(response) {
        return response;
      },
      'request' : function(config) {
        // Resolved resource loading failure after configuring ContextPath
    	  var baseUrl = $window.document.getElementsByTagName('base')[0].href;
    	  config.url = baseUrl + config.url;
        return config;
      },
      'requestError' : function(config){
        return config;
      }
    };
    return authInterceptor;
  }])
  .config(['$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider', '$httpProvider',
    function ($stateProvider, $urlRouterProvider, $ocLazyLoadProvider, $httpProvider) {
      $httpProvider.interceptors.push('AuthInterceptor');

      $ocLazyLoadProvider.config({
        debug: false,
        events: true,
      });

      $urlRouterProvider.otherwise('/dashboard/home');

      $stateProvider
        .state('login', {
            url: '/login',
            templateUrl: 'app/views/login.html',
            controller: 'LoginCtl',
            resolve: {
                loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load({
                        name: 'sentinelDashboardApp',
                        files: [
                            'app/scripts/controllers/login.js',
                        ]
                    });
                }]
            }
        })

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

      .state('dashboard.flowV1', {
        templateUrl: 'app/views/flow_v1.html',
        url: '/flow/:app',
        controller: 'FlowControllerV1',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/flow_v1.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.flow', {
          templateUrl: 'app/views/flow_v2.html',
          url: '/v2/flow/:app',
          controller: 'FlowControllerV2',
          resolve: {
              loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                  return $ocLazyLoad.load({
                      name: 'sentinelDashboardApp',
                      files: [
                          'app/scripts/controllers/flow_v2.js',
                      ]
                  });
              }]
          }
      })

      .state('dashboard.paramFlow', {
        templateUrl: 'app/views/param_flow.html',
        url: '/paramFlow/:app',
        controller: 'ParamFlowController',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/param_flow.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.clusterAppAssignManage', {
          templateUrl: 'app/views/cluster_app_assign_manage.html',
          url: '/cluster/assign_manage/:app',
          controller: 'SentinelClusterAppAssignManageController',
          resolve: {
              loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                  return $ocLazyLoad.load({
                      name: 'sentinelDashboardApp',
                      files: [
                          'app/scripts/controllers/cluster_app_assign_manage.js',
                      ]
                  });
              }]
          }
      })

      .state('dashboard.clusterAppServerList', {
          templateUrl: 'app/views/cluster_app_server_list.html',
          url: '/cluster/server/:app',
          controller: 'SentinelClusterAppServerListController',
          resolve: {
              loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                  return $ocLazyLoad.load({
                      name: 'sentinelDashboardApp',
                      files: [
                          'app/scripts/controllers/cluster_app_server_list.js',
                      ]
                  });
              }]
          }
      })

      .state('dashboard.clusterAppClientList', {
          templateUrl: 'app/views/cluster_app_client_list.html',
          url: '/cluster/client/:app',
          controller: 'SentinelClusterAppTokenClientListController',
          resolve: {
              loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                  return $ocLazyLoad.load({
                      name: 'sentinelDashboardApp',
                      files: [
                          'app/scripts/controllers/cluster_app_token_client_list.js',
                      ]
                  });
              }]
          }
      })

      .state('dashboard.clusterSingle', {
          templateUrl: 'app/views/cluster_single_config.html',
          url: '/cluster/single/:app',
          controller: 'SentinelClusterSingleController',
          resolve: {
              loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                  return $ocLazyLoad.load({
                      name: 'sentinelDashboardApp',
                      files: [
                          'app/scripts/controllers/cluster_single.js',
                      ]
                  });
              }]
          }
      })

      .state('dashboard.authority', {
            templateUrl: 'app/views/authority.html',
            url: '/authority/:app',
            controller: 'AuthorityRuleController',
            resolve: {
                loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load({
                        name: 'sentinelDashboardApp',
                        files: [
                            'app/scripts/controllers/authority.js',
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

      .state('dashboard.gatewayIdentity', {
        templateUrl: 'app/views/gateway/identity.html',
        url: '/gateway/identity/:app',
        controller: 'GatewayIdentityCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/gateway/identity.js',
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
      })

      .state('dashboard.gatewayApi', {
        templateUrl: 'app/views/gateway/api.html',
        url: '/gateway/api/:app',
        controller: 'GatewayApiCtl',
        resolve: {
          loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
              name: 'sentinelDashboardApp',
              files: [
                'app/scripts/controllers/gateway/api.js',
              ]
            });
          }]
        }
      })

      .state('dashboard.gatewayFlow', {
          templateUrl: 'app/views/gateway/flow.html',
          url: '/gateway/flow/:app',
          controller: 'GatewayFlowCtl',
          resolve: {
              loadMyFiles: ['$ocLazyLoad', function ($ocLazyLoad) {
                  return $ocLazyLoad.load({
                      name: 'sentinelDashboardApp',
                      files: [
                          'app/scripts/controllers/gateway/flow.js',
                      ]
                  });
              }]
          }
      });
  }]);