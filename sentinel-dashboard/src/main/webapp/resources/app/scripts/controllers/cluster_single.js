var app = angular.module('sentinelDashboardApp');

app.controller('SentinelClusterSingleController', ['$scope', '$stateParams', 'ngDialog',
    'MachineService', 'ClusterStateService',
    function ($scope, $stateParams, ngDialog, MachineService, ClusterStateService) {
        $scope.app = $stateParams.app;
        const UNSUPPORTED_CODE = 4041;

        const CLUSTER_MODE_CLIENT = 0;
        const CLUSTER_MODE_SERVER = 1;

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

        function convertStrToNamespaceSet(str) {
            if (str === undefined || str === '') {
                return [];
            }
            let arr = [];
            let spliced = str.split(',');
            spliced.forEach((v) => {
                arr.push(v.trim());
            });
            return arr;
        }

        function fetchMachineClusterState() {
            if (!$scope.macInputModel || $scope.macInputModel === '') {
                return;
            }
            let mac = $scope.macInputModel.split(':');
            ClusterStateService.fetchClusterUniversalStateSingle($scope.app, mac[0], mac[1]).success(function (data) {
                if (data.code == 0 && data.data) {
                    $scope.loadError = undefined;
                    $scope.stateVO = data.data;
                    $scope.stateVO.currentMode = $scope.stateVO.stateInfo.mode;
                    if ($scope.stateVO.server && $scope.stateVO.server.namespaceSet) {
                        $scope.stateVO.server.namespaceSetStr = convertSetToString($scope.stateVO.server.namespaceSet);
                    }
                } else {
                    $scope.stateVO = {};
                    if (data.code === UNSUPPORTED_CODE) {
                        $scope.loadError = {message: '机器 ' + mac[0] + ':' + mac[1] + ' 的 Sentinel 客户端版本不支持集群限流，请升级至 1.4.0 以上版本并引入相关依赖。'}
                    } else {
                        $scope.loadError = {message: data.msg};
                    }
                }
            }).error((data, header, config, status) => {
                $scope.loadError = {message: '未知错误'};
            });
        }

        fetchMachineClusterState();

        function checkValidClientConfig(stateVO) {
            if (!stateVO.client || !stateVO.client.clientConfig) {
                alert('不合法的配置');
                return false;
            }
            let config = stateVO.client.clientConfig;
            if (!config.serverHost || config.serverHost.trim() == '') {
                alert('请输入有效的 Token Server IP');
                return false;
            }
            if (config.serverPort === undefined || config.serverPort <= 0 ||  config.serverPort > 65535) {
                alert('请输入有效的 Token Server 端口');
                return false;
            }
            if (config.requestTimeout === undefined || config.requestTimeout <= 0) {
                alert('请输入有效的请求超时时长');
                return false;
            }
            return true;
        }

        function sendClusterClientRequest(stateVO) {
            if (!checkValidClientConfig(stateVO)) {
                return;
            }
            if (!$scope.macInputModel) {
                return;
            }
            let mac = $scope.macInputModel.split(':');
            let request = {
                app: $scope.app,
                ip: mac[0],
                port: mac[1],
            };
            request.mode = CLUSTER_MODE_CLIENT;
            request.clientConfig = stateVO.client.clientConfig;
            ClusterStateService.modifyClusterConfig(request).success(function (data) {
                if (data.code == 0 && data.data) {
                    alert('修改集群限流客户端配置成功');
                    window.location.reload();
                } else {
                    if (data.code === UNSUPPORTED_CODE) {
                        alert('机器 ' + mac[0] + ':' + mac[1] + ' 的 Sentinel 客户端版本不支持集群限流客户端，请升级至 1.4.0 以上版本并引入相关依赖。');
                    } else {
                        alert('修改失败：' + data.msg);
                    }
                }
            }).error((data, header, config, status) => {
                alert('未知错误');
            });
        }

        function checkValidServerConfig(stateVO) {
            if (!stateVO.server || !stateVO.server.transport) {
                alert('不合法的配置');
                return false;
            }
            if (stateVO.server.namespaceSetStr === undefined || stateVO.server.namespaceSetStr == '') {
                alert('请输入有效的命名空间集合（多个 namespace 以 , 分隔）');
                return false;
            }
            let transportConfig = stateVO.server.transport;
            if (transportConfig.port === undefined || transportConfig.port <= 0 || transportConfig.port > 65535) {
                alert('请输入有效的 Token Server 端口');
                return false;
            }
            let flowConfig = stateVO.server.flow;
            if (flowConfig.maxAllowedQps === undefined || flowConfig.maxAllowedQps < 0) {
                alert('请输入有效的最大允许 QPS');
                return false;
            }
            // if (transportConfig.idleSeconds === undefined || transportConfig.idleSeconds <= 0) {
            //     alert('请输入有效的连接清理时长 (idleSeconds)');
            //     return false;
            // }
            return true;
        }

        function sendClusterServerRequest(stateVO) {
            if (!checkValidServerConfig(stateVO)) {
                return;
            }
            if (!$scope.macInputModel) {
                return;
            }
            let mac = $scope.macInputModel.split(':');
            let request = {
                app: $scope.app,
                ip: mac[0],
                port: mac[1],
            };
            request.mode = CLUSTER_MODE_SERVER;
            request.flowConfig = stateVO.server.flow;
            request.transportConfig = stateVO.server.transport;
            request.namespaceSet = convertStrToNamespaceSet(stateVO.server.namespaceSetStr);
            ClusterStateService.modifyClusterConfig(request).success(function (data) {
                if (data.code == 0 && data.data) {
                    alert('修改集群限流服务端配置成功');
                    window.location.reload();
                } else {
                    if (data.code === UNSUPPORTED_CODE) {
                        alert('机器 ' + mac[0] + ':' + mac[1] + ' 的 Sentinel 客户端版本不支持集群限流服务端，请升级至 1.4.0 以上版本并引入相关依赖。');
                    } else {
                        alert('修改失败：' + data.msg);
                    }
                }
            }).error((data, header, config, status) => {
                alert('未知错误');
            });
        }


        $scope.saveConfig = () => {
            let ok = confirm('是否确定修改集群限流配置？');
            if (!ok) {
                return;
            }
            let mode = $scope.stateVO.stateInfo.mode;
            if (mode != 1 && mode != 0) {
                alert('未知的集群限流模式');
                return;
            }
            if (mode == 0) {
                sendClusterClientRequest($scope.stateVO);
            } else {
                sendClusterServerRequest($scope.stateVO);
            }
        };

        function queryAppMachines() {
            MachineService.getAppMachines($scope.app).success(
                function (data) {
                    if (data.code === 0) {
                        // $scope.machines = data.data;
                        if (data.data) {
                            $scope.machines = [];
                            $scope.macsInputOptionsOrigin = [];
                            $scope.macsInputOptions = [];
                            data.data.forEach(function (item) {
                                if (item.healthy) {
                                    $scope.macsInputOptionsOrigin.push({
                                        text: item.ip + ':' + item.port,
                                        value: item.ip + ':' + item.port
                                    });
                                }
                            });
                            $scope.macsInputOptions = $scope.macsInputOptionsOrigin;
                        }
                        if ($scope.macsInputOptions.length > 0) {
                            $scope.macInputModel = $scope.macsInputOptions[0].value;
                        }
                    } else {
                        $scope.macsInputOptions = [];
                    }
                }
            );
        }
        queryAppMachines();

        $scope.$watch('searchKey', function () {
            if (!$scope.macsInputOptions) {
                return;
            }
            if ($scope.searchKey) {
                $scope.macsInputOptions = $scope.macsInputOptionsOrigin
                    .filter((e) => e.value.indexOf($scope.searchKey) !== -1);
            } else {
                $scope.macsInputOptions = $scope.macsInputOptionsOrigin;
            }
            if ($scope.macsInputOptions.length > 0) {
                $scope.macInputModel = $scope.macsInputOptions[0].value;
            } else {
                $scope.macInputModel = '';
            }
        });

        $scope.$watch('macInputModel', function () {
            if ($scope.macInputModel) {
                fetchMachineClusterState();
            }
        });
    }]);
