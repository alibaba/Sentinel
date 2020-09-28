var app = angular.module('sentinelDashboardApp');

app.service('KieIdentityService', ['$http', function ($http) {
    this.queryIdentityOfService = function (ip, port, serchKey) {
        var param = {
            ip: ip,
            port: port,
            searchKey: serchKey
        };
        return $http({
            url: 'resource/machineResource.json',
            params: param,
            method: 'GET'
        });
    }
}])