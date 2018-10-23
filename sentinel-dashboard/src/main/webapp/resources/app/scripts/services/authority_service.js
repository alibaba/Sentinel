/**
 * Authority rule service.
 */
angular.module('sentinelDashboardApp').service('AuthorityRuleService', ['$http', function ($http) {
    this.queryMachineRules = function(app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/authority/rules',
            params: param,
            method: 'GET'
        });
    };

    this.addNewRule = function(rule) {
        return $http({
            url: '/authority/rule',
            data: rule,
            method: 'POST'
        });
    };

    this.saveRule = function (entity) {
        return $http({
            url: '/authority/rule/' + entity.id,
            data: entity,
            method: 'PUT'
        });
    };

    this.deleteRule = function (entity) {
        return $http({
            url: '/authority/rule/' + entity.id,
            method: 'DELETE'
        });
    };
}]);
