var app = angular.module('sentinelDashboardApp');

app.service('KieService', ['$http', function ($http) {
    this.getProjects = function () {
        return $http({
            url: '/kie/projects',
            method: 'GET'
        });
    }

    
    this.getKieInfos = function (project) {
        return $http({
            url: '/kie/' + project + '/kieInfos',
            method: 'GET'
        });
    }
}]);