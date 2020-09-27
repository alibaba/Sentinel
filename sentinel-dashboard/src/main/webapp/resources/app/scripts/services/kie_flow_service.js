var app = angular.module('sentinelDashboardApp');

app.service('KieFlowService', ['$http', function ($http) {
    this.getKieFlowRules = function (service_id) {
        var param = {
            serverId: service_id
        };
        return $http({
            url: '/kie/flow/rules',
            params: param,
            method: 'GET'
        })
    }

    this.updateKieFlowRule = function (service_id, param) {
        return $http({
            url: '/kie/flow/' + service_id + '/rule',
            method: 'PUT',
            data: param
        })
    }

    this.deleteKieFlowRule = function(service_id, rule_id) {
        return $http({
            url: '/kie/flow/' + service_id + '/rule/' + rule_id,
            method: 'DELETE'
        });
    }

    this.addKieFlowRule = function(service_id, param) {
        return $http({
            url: '/kie/flow/' + service_id + '/rule',
            method: 'POST',
            data: param
        });
    }
}]);