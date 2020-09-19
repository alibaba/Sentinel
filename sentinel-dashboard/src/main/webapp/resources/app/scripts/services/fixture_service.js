var app = angular.module('sentinelDashboardApp');

app.service('FixtureService', [function () {
    this.getProjects = function () {
        return {
            "success": true,
            "code": 0,
            "msg": "success",
            "data": [
                "default"
            ]
        }
    }

    this.getKieInfos = function () {
        return {
            "success": true,
            "code": 0,
            "msg": "success",
            "data": [{
                "id": "c28b6bd5-7d99-474e-a16b-2a973656c112",
                "hostname": null,
                "ip": null,
                "port": 0,
                "heartbeatVersion": 1600412742397,
                "lastHeartbeat": 1600412742653,
                "healthy": false,
                "sentinelVersion": "1.8.0",
                "project": "default",
                "app": "appA",
                "service": "serviceA",
                "environment": "test",
                "serverVersion": "1.0.0",
                "kieAddress": "121.37.174.21:30110"
            }, {
                "id": "c28b6bd5-7d99-474e-a16b-2a973656c113",
                "hostname": null,
                "ip": null,
                "port": 0,
                "heartbeatVersion": 1600412742397,
                "lastHeartbeat": 1600412742653,
                "healthy": false,
                "sentinelVersion": "1.8.0",
                "project": "default",
                "app": "appA",
                "service": "serviceB",
                "environment": "test",
                "serverVersion": "1.0.0",
                "kieAddress": "121.37.174.21:30110"
            }]
        }
    }

    this.getKieFlowRules = function (id) {
        switch(id) {
            case "c28b6bd5-7d99-474e-a16b-2a973656c112":
                return {
                    "success": true,
                    "code": 0,
                    "msg": "success",
                    "data": [{
                        "id": null,
                        "ruleId": "f069a768-1ac4-4dc3-b5b1-3f392d9bc6e4",
                        "app": null,
                        "ip": null,
                        "port": null,
                        "limitApp": "default",
                        "resource": "abc",
                        "grade": 1,
                        "count": 20.0,
                        "strategy": 0,
                        "refResource": null,
                        "controlBehavior": 0,
                        "warmUpPeriodSec": 10,
                        "maxQueueingTimeMs": 500,
                        "clusterMode": false,
                        "clusterConfig": null,
                        "gmtCreate": null,
                        "gmtModified": null
                    }, {
                        "id": null,
                        "ruleId": "f069a768-1ac4-4dc3-b5b1-3f392d9bc6e4",
                        "app": null,
                        "ip": null,
                        "port": null,
                        "limitApp": "default",
                        "resource": "abc1",
                        "grade": 1,
                        "count": 20.0,
                        "strategy": 0,
                        "refResource": null,
                        "controlBehavior": 0,
                        "warmUpPeriodSec": 10,
                        "maxQueueingTimeMs": 500,
                        "clusterMode": false,
                        "clusterConfig": null,
                        "gmtCreate": null,
                        "gmtModified": null
                    }]
                }
        }
    }
}]);