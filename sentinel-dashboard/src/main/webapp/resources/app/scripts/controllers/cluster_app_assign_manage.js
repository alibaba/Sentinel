var app = angular.module('sentinelDashboardApp');

app.controller('SentinelClusterAppAssignManageController', ['$scope', '$stateParams', 'ngDialog',
    'MachineService', 'ClusterStateService',
    function ($scope, $stateParams, ngDialog, MachineService, ClusterStateService) {
        $scope.app = $stateParams.app;
        const UNSUPPORTED_CODE = 4041;

        const CLUSTER_MODE_CLIENT = 0;
        const CLUSTER_MODE_SERVER = 1;
        const DEFAULT_CLUSTER_SERVER_PORT = 18730;

        $scope.tmp = {
            curClientChosen: [],
            curRemainingClientChosen: [],
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

        function processAppSingleData(data) {
            if (data.state.server && data.state.server.namespaceSet) {
                data.state.server.namespaceSetStr = convertSetToString(data.state.server.namespaceSet);
                data.mode = data.state.stateInfo.mode;
            }
        }

        function removeFromArr(arr, v) {
            for (let i = 0; i < arr.length; i++) {
                if (arr[i] === v) {
                    arr.splice(i, 1);
                    break;
                }
            }
        }

        function resetChosen() {
            $scope.tmp.curClientChosen = [];
            $scope.tmp.curRemainingClientChosen = [];
        }

        function generateMachineId(e) {
            return e.ip + '@' + e.commandPort;
        }

        function applyClusterMap(appClusterMachineList) {
            if (!appClusterMachineList) {
                return;
            }
            let tmpMap = new Map();
            $scope.clusterMap = [];
            $scope.remainingClientAddressList = [];
            let tmpServerList = [];
            let tmpClientList = [];
            appClusterMachineList.forEach((e) => {
                if (e.mode === CLUSTER_MODE_CLIENT) {
                    tmpClientList.push(e);
                } else if (e.mode === CLUSTER_MODE_SERVER) {
                    tmpServerList.push(e);
                } else {
                    $scope.remainingClientAddressList.push(generateMachineId(e));
                }
            });
            tmpServerList.forEach((e) => {
                let ip = e.ip;
                let machineId = ip + '@' + e.commandPort;
                let group = {
                    ip: ip,
                    machineId: machineId,
                    port: e.state.server.port,
                    clientSet: [],
                    namespaceSetStr: e.state.server.namespaceSetStr,
                    belongToApp: true,
                };
                if (!tmpMap.has(ip)) {
                    tmpMap.set(ip, group);
                }
            });
            tmpClientList.forEach((e) => {
                let ip = e.ip;
                let machineId = ip + '@' + e.commandPort;

                let targetServer = e.state.client.clientConfig.serverHost;
                let targetPort = e.state.client.clientConfig.serverPort;
                if (targetServer === undefined || targetServer === '' ||
                    targetPort === undefined || targetPort <= 0) {
                    $scope.remainingClientAddressList.push(generateMachineId(e));
                    return;
                }

                if (!tmpMap.has(targetServer)) {
                    let group = {
                        ip: targetServer,
                        machineId: targetServer,
                        port: targetPort,
                        clientSet: [machineId],
                        belongToApp: false,
                    };
                    tmpMap.set(targetServer, group);
                } else {
                    let g = tmpMap.get(targetServer);
                    g.clientSet.push(machineId);
                }
            });
            tmpMap.forEach((v) => {
                if (v !== undefined) {
                    $scope.clusterMap.push(v);
                }
            });
        }

        $scope.onCurrentServerChange = () => {
            resetChosen();
        };

        $scope.remainingClientAddressList = [];

        $scope.moveToServerGroup = () => {
            let chosenServer = $scope.tmp.curChosenServer;
            if (!chosenServer || !chosenServer.machineId) {
                return;
            }
            $scope.tmp.curRemainingClientChosen.forEach(e => {
                chosenServer.clientSet.push(e);
                removeFromArr($scope.remainingClientAddressList, e);
            });
            resetChosen();
        };

        $scope.moveToRemainingSharePool = () => {
            $scope.tmp.curClientChosen.forEach(e => {
                $scope.remainingClientAddressList.push(e);
                removeFromArr($scope.tmp.curChosenServer.clientSet, e);
            });
            resetChosen();
        };

        function parseIpFromMachineId(machineId) {
            if (machineId.indexOf('@') === -1) {
                return machineId;
            }
            let arr = machineId.split('@');
            return arr[0];
        }

        $scope.addToServerList = () => {
            let group;
            $scope.tmp.curRemainingClientChosen.forEach(e => {
                group = {
                    machineId: e,
                    ip: parseIpFromMachineId(e),
                    port: DEFAULT_CLUSTER_SERVER_PORT,
                    clientSet: [],
                    namespaceSetStr: 'default,' + $scope.app,
                    belongToApp: true,
                };
                $scope.clusterMap.push(group);
                removeFromArr($scope.remainingClientAddressList, e);
                $scope.tmp.curChosenServer = group;
            });
            resetChosen();
        };

        $scope.removeFromServerList = () => {
            let chosenServer = $scope.tmp.curChosenServer;
            if (!chosenServer || !chosenServer.machineId) {
                return;
            }
            chosenServer.clientSet.forEach((e) => {
                if (e !== undefined) {
                    $scope.remainingClientAddressList.push(e);
                }
            });

            if (chosenServer.belongToApp || chosenServer.machineId.indexOf('@') !== -1) {
                $scope.remainingClientAddressList.push(chosenServer.machineId);
            } else {
                alert('提示：非本应用内机器将不会置回空闲列表中');
            }

            removeFromArr($scope.clusterMap, chosenServer);

            resetChosen();

            if ($scope.clusterMap.length > 0) {
                $scope.tmp.curChosenServer = $scope.clusterMap[0];
                $scope.onCurrentServerChange();
            } else {
                $scope.tmp.curChosenServer = {};
            }
        };

        function retrieveClusterAppInfo() {
            ClusterStateService.fetchClusterUniversalStateOfApp($scope.app).success(function (data) {
                if (data.code === 0 && data.data) {
                    $scope.loadError = undefined;
                    $scope.appClusterMachineList = data.data;
                    $scope.appClusterMachineList.forEach(processAppSingleData);
                    applyClusterMap($scope.appClusterMachineList);
                    if ($scope.clusterMap.length > 0) {
                        $scope.tmp.curChosenServer = $scope.clusterMap[0];
                        $scope.onCurrentServerChange();
                    }
                } else {
                    $scope.appClusterMachineList = {};
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

        retrieveClusterAppInfo();

        $scope.saveAndApplyAssign = () => {
            let ok = confirm('是否确认执行变更？');
            if (!ok) {
                return;
            }
            let cm = $scope.clusterMap;
            if (!cm) {
                cm = [];
            }
            cm.forEach((e) => {
                e.namespaceSet = convertStrToNamespaceSet(e.namespaceSetStr);
            });
            cm.namespaceSet = convertStrToNamespaceSet(cm.namespaceSetStr);
            let request = {
                clusterMap: cm,
                remainingList: $scope.remainingClientAddressList,
            };
            ClusterStateService.applyClusterFullAssignOfApp($scope.app, request).success((data) => {
                if (data.code === 0 && data.data) {
                    let failedServerSet = data.data.failedServerSet;
                    let failedClientSet = data.data.failedClientSet;
                    if (failedClientSet.length === 0 && failedServerSet.length === 0) {
                        alert('全部推送成功');
                    } else {
                        alert('推送完毕。token server 失败列表：' + JSON.stringify(failedServerSet) +
                            '; token client 失败列表：' + JSON.stringify(failedClientSet));
                    }

                    retrieveClusterAppInfo();
                } else {
                    if (data.code === UNSUPPORTED_CODE) {
                        alert('该应用的 Sentinel 客户端不支持集群限流，请升级至 1.4.0 以上版本并引入相关依赖。');
                    } else {
                        alert('推送失败：' + data.msg);
                    }
                }
            }).error(() => {
                alert('未知错误');
            });
        };
    }]);
