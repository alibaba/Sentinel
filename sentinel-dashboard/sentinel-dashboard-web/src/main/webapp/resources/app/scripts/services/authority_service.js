/**
 * Authority rule service.
 */
angular.module('sentinelDashboardApp').service('AuthorityRuleService', ['$http', function ($http) {
    this.queryMachineRules = function(app, ip, port) {
        var data = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/authority/rules',
            params: data,
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

    this.saveRule = function (rule) {
        return $http({
            url: '/authority/rule/' + rule.id,
            data: rule,
            method: 'PUT'
        });
    };

    this.deleteRule = function (rule) {
        return $http({
            url: '/authority/rule/' + rule.id,
            data: rule,
            method: 'DELETE',
            headers: {'Content-Type': 'application/json'}
        });
    };

    this.checkRuleValid = function checkRuleValid(rule) {
        if (rule.resource === undefined || rule.resource === '') {
            alert('资源名称不能为空');
            return false;
        }
        if (rule.limitApp === undefined || rule.limitApp === '') {
            alert('流控针对应用不能为空');
            return false;
        }
        if (rule.strategy === undefined) {
            alert('必须选择黑白名单模式');
            return false;
        }
        return true;
    };
}]);
