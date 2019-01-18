var app = angular.module('sentinelDashboardApp');

app.controller('SentinelClusterAppTokenClientListController', ['$scope', '$stateParams', 'ngDialog',
    'MachineService', 'ClusterStateService',
    function ($scope, $stateParams, ngDialog, MachineService, ClusterStateService) {
        $scope.app = $stateParams.app;

        const UNSUPPORTED_CODE = 4041;
        const CLUSTER_MODE_CLIENT = 0;
        const CLUSTER_MODE_SERVER = 1;

        function processClientData(clientVO) {

        }

        $scope.modifyClientConfigDialog = (clientVO) => {
            if (!clientVO) {
                return;
            }
            $scope.ccDialogData = {
                ip: clientVO.ip,
                commandPort: clientVO.commandPort,
                clientId: clientVO.id,
                serverHost: clientVO.state.clientConfig.serverHost,
                serverPort: clientVO.state.clientConfig.serverPort,
                requestTimeout: clientVO.state.clientConfig.requestTimeout,
            };
            $scope.ccDialog = ngDialog.open({
                template: '/app/views/dialog/cluster/cluster-client-config-dialog.html',
                width: 700,
                overlay: true,
                scope: $scope
            });
        };

        function checkValidClientConfig(config) {
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

        $scope.doModifyClientConfig = () => {
            if (!checkValidClientConfig($scope.ccDialogData)) {
                return;
            }
            let id = $scope.ccDialogData.id;
            let request = {
                app: $scope.app,
                ip: $scope.ccDialogData.ip,
                port: $scope.ccDialogData.commandPort,
                mode: CLUSTER_MODE_CLIENT,
                clientConfig: {
                    serverHost: $scope.ccDialogData.serverHost,
                    serverPort: $scope.ccDialogData.serverPort,
                    requestTimeout: $scope.ccDialogData.requestTimeout,
                }
            };
            ClusterStateService.modifyClusterConfig(request).success((data) => {
                if (data.code === 0 && data.data) {
                    alert('修改 Token Client 配置成功');
                    window.location.reload();
                } else {
                    if (data.code === UNSUPPORTED_CODE) {
                        alert('机器 ' + id + ' 的 Sentinel 没有引入集群限流客户端，请升级至 1.4.0 以上版本并引入相关依赖。');
                    } else {
                        alert('修改失败：' + data.msg);
                    }
                }
            }).error((data, header, config, status) => {
                alert('未知错误');
            });
        };

        function retrieveClusterTokenClientInfo() {
            ClusterStateService.fetchClusterClientStateOfApp($scope.app)
                .success((data) => {
                    if (data.code === 0 && data.data) {
                        $scope.loadError = undefined;
                        $scope.clientVOList = data.data;
                        $scope.clientVOList.forEach(processClientData);
                    } else {
                        $scope.clientVOList = [];
                        if (data.code === UNSUPPORTED_CODE) {
                            $scope.loadError = {message: '该应用的 Sentinel 客户端不支持集群限流，请升级至 1.4.0 以上版本并引入相关依赖。'}
                        } else {
                            $scope.loadError = {message: data.msg};
                        }
                    }
                })
                .error(() => {
                    $scope.loadError = {message: '未知错误'};
                });
        }

        retrieveClusterTokenClientInfo();

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
