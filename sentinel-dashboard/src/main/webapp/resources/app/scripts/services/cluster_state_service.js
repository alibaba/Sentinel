/**
 * Parameter flow control service.
 *
 * @author Eric Zhao
 */
angular.module('sentinelDashboardApp').service('ClusterStateService', ['$http', function ($http) {

    this.fetchClusterUniversalState = function(app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/cluster/state',
            params: param,
            method: 'GET'
        });
    };

    this.modifyClusterConfig = function(config) {
        return $http({
            url: '/cluster/config/modify',
            data: config,
            method: 'POST'
        });
    };
}]);
