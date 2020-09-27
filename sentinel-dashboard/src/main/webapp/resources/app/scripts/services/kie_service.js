var app = angular.module('sentinelDashboardApp');

app.service('KieService', ['$http', function ($http) {
    this.getProjects = function () {
        return $http({
            url: '/kie/projects',
            method: 'GET'
        });
    }
    this.getKieInfos = function (project, environment) {
        return $http({
            url: '/kie/kieInfos',
            method: 'GET',
            params: {
                project: project,
                environment: environment
            }
        })
    }
}]);