var app = angular.module('sentinelDashboardApp');

app.service('KieFlowService', ['$http', function ($http) {
    this.getKieFlowRules = function (id) {
        var param = {
            id: id
        };
        console.log("param", param);
        return $http({
            url: '/kie/flow/rules',
            params: param,
            method: 'GET'
        })
    }
}]);