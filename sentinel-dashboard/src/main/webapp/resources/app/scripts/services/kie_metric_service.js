var app = angular.module('sentinelDashboardApp');

app.service('KieMetricService', ['$http', function ($http) {
    this.queryTopResourceMetric = function (params) {
        return $http({
            url: '/metric/queryTopResourceMetric.json',
            params: params,
            method: 'GET'
        });
    }
}]);