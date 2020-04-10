/**
 * Cluster state control service.
 *
 * @author Eric Zhao
 */
angular.module('sentinelDashboardApp').service('ClusterStateService', ['$http', function ($http) {

    this.fetchClusterUniversalStateSingle = function(app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/cluster/state_single',
            params: param,
            method: 'GET'
        });
    };

    this.fetchClusterUniversalStateOfApp = function(app) {
        return $http({
            url: '/cluster/state/' + app,
            method: 'GET'
        });
    };

    this.fetchClusterServerStateOfApp = function(app) {
        return $http({
            url: '/cluster/server_state/' + app,
            method: 'GET'
        });
    };

    this.fetchClusterClientStateOfApp = function(app) {
        return $http({
            url: '/cluster/client_state/' + app,
            method: 'GET'
        });
    };

    this.modifyClusterConfig = function(config) {
        return $http({
            url: '/cluster/config/modify_single',
            data: config,
            method: 'POST'
        });
    };

    this.applyClusterFullAssignOfApp = function(app, clusterMap) {
        return $http({
            url: '/cluster/assign/all_server/' + app,
            data: clusterMap,
            method: 'POST'
        });
    };

    this.applyClusterSingleServerAssignOfApp = function(app, request) {
        return $http({
            url: '/cluster/assign/single_server/' + app,
            data: request,
            method: 'POST'
        });
    };

    this.applyClusterServerBatchUnbind = function(app, machineSet) {
        return $http({
            url: '/cluster/assign/unbind_server/' + app,
            data: machineSet,
            method: 'POST'
        });
    };
}]);
