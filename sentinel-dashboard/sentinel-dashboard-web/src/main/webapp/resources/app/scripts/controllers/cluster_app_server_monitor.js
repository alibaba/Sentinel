var app = angular.module('sentinelDashboardApp');

app.controller('SentinelClusterAppServerMonitorController', ['$scope', '$stateParams', 'ngDialog',
    'MachineService', 'ClusterStateService',
    function ($scope, $stateParams, ngDialog, MachineService, ClusterStateService) {
        $scope.app = $stateParams.app;
        const UNSUPPORTED_CODE = 4041;

        const CLUSTER_MODE_SERVER = 1;

        $scope.tmp = {
            curChosenServer: {},
        };

        function convertSetToString(set) {
            if (set === undefined) {
                return '';
            }
            let s = '';
            for (let i = 0; i < set.length; i++) {
                s = s + set[i];
                if (i < set.length - 1) {
                    s = s + ',';
                }
            }
            return s;
        }

        function processServerData(serverVO) {
            if (serverVO.state && serverVO.state.namespaceSet) {
                serverVO.state.namespaceSetStr = convertSetToString(serverVO.state.namespaceSet);
            }
        }

        $scope.generateConnectionSet = (data) => {
            let connectionSet = data;
            let s = '';
            if (connectionSet) {
                s = s + '[';
                for (let i = 0; i < connectionSet.length; i++) {
                    s = s + connectionSet[i].address;
                    if (i < connectionSet.length - 1) {
                        s = s + ', ';
                    }
                }
                s = s + ']';
            } else {
                s = '[]';
            }
            return s;
        };

        $scope.onChosenServerChange = () => {

        };

        function retrieveClusterServerInfo() {
            ClusterStateService.fetchClusterServerStateOfApp($scope.app).success(function (data) {
                if (data.code === 0 && data.data) {
                    $scope.loadError = undefined;
                    $scope.serverVOList = data.data;
                    $scope.serverVOList.forEach(processServerData);

                    if ($scope.serverVOList.length > 0) {
                        $scope.tmp.curChosenServer = $scope.serverVOList[0];
                        $scope.onChosenServerChange();
                    }
                } else {
                    $scope.serverVOList = {};
                    if (data.code === UNSUPPORTED_CODE) {
                        $scope.loadError = {message: '该应用的 Sentinel 客户端不支持集群限流，请升级至 1.4.0 以上版本并引入相关依赖。'}
                    } else {
                        $scope.loadError = {message: data.msg};
                    }
                }
            }).error(() => {
                $scope.loadError = {message: '未知错误'};
            });
        }

        retrieveClusterServerInfo();

        $scope.macsInputConfig = {
            searchField: ['text', 'value'],
            persist: true,
            create: false,
            maxItems: 1,
            render: {
                item: function (data, escape) {
                    return '<div>' + escape(data.text) + '</div>';
                }
            },
            onChange: function (value, oldValue) {
                $scope.macInputModel = value;
            }
        };
    }]);
