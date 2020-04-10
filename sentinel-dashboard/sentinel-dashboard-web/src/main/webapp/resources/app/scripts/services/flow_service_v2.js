var app = angular.module('sentinelDashboardApp');

app.service('FlowServiceV2', ['$http', function ($http) {
    this.queryMachineRules = function (app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/v2/flow/rules',
            params: param,
            method: 'GET'
        });
    };

    this.newRule = function (rule) {
        return $http({
            url: '/v2/flow/rule',
            data: rule,
            method: 'POST'
        });
    };

    this.saveRule = function (rule) {
        return $http({
            url: '/v2/flow/rule/' + rule.id,
            data: rule,
            method: 'PUT'
        });
    };

    this.deleteRule = function (rule) {
        return $http({
            url: '/v2/flow/rule/' + rule.id,
            method: 'DELETE'
        });
    };

    function notNumberAtLeastZero(num) {
        return num === undefined || num === '' || isNaN(num) || num < 0;
    }

    function notNumberGreaterThanZero(num) {
        return num === undefined || num === '' || isNaN(num) || num <= 0;
    }

    this.checkRuleValid = function (rule) {
        if (rule.resource === undefined || rule.resource === '') {
            alert('资源名称不能为空');
            return false;
        }
        if (rule.count === undefined || rule.count < 0) {
            alert('限流阈值必须大于等于 0');
            return false;
        }
        if (rule.strategy === undefined || rule.strategy < 0) {
            alert('无效的流控模式');
            return false;
        }
        if (rule.strategy == 1 || rule.strategy == 2) {
            if (rule.refResource === undefined || rule.refResource == '') {
                alert('请填写关联资源或入口');
                return false;
            }
        }
        if (rule.controlBehavior === undefined || rule.controlBehavior < 0) {
            alert('无效的流控整形方式');
            return false;
        }
        if (rule.controlBehavior == 1 && notNumberGreaterThanZero(rule.warmUpPeriodSec)) {
            alert('预热时长必须大于 0');
            return false;
        }
        if (rule.controlBehavior == 2 && notNumberGreaterThanZero(rule.maxQueueingTimeMs)) {
            alert('排队超时时间必须大于 0');
            return false;
        }
        if (rule.clusterMode && (rule.clusterConfig === undefined || rule.clusterConfig.thresholdType === undefined)) {
            alert('集群限流配置不正确');
            return false;
        }
        return true;
    };
}]);
