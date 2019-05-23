/**
 * Authority rule controller.
 */
angular.module('sentinelDashboardApp').controller('AuthorityRuleController', ['$scope', '$stateParams', 'AuthorityRuleService', 'ngDialog',
    'MachineService',
    function ($scope, $stateParams, AuthorityRuleService, ngDialog,
              MachineService) {
        $scope.app = $stateParams.app;

        $scope.rulesPageConfig = {
            pageSize: 10,
            currentPageIndex: 1,
            totalPage: 1,
            totalCount: 0,
        };
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

        function getMachineRules() {
            if (!$scope.macInputModel) {
                return;
            }
            let mac = $scope.macInputModel.split(':');
            AuthorityRuleService.queryMachineRules($scope.app, mac[0], mac[1])
                .success(function (data) {
                    if (data.code === 0 && data.data) {
                        $scope.loadError = undefined;
                        $scope.rules = data.data;
                        $scope.rulesPageConfig.totalCount = $scope.rules.length;
                    } else {
                        $scope.rules = [];
                        $scope.rulesPageConfig.totalCount = 0;
                        $scope.loadError = {message: data.msg};
                    }
                })
                .error((data, header, config, status) => {
                    $scope.loadError = {message: "未知错误"};
                });
        };
        $scope.getMachineRules = getMachineRules;
        getMachineRules();

        var authorityRuleDialog;

        $scope.editRule = function (rule) {
            $scope.currentRule = angular.copy(rule);
            $scope.authorityRuleDialog = {
                title: '编辑授权规则',
                type: 'edit',
                confirmBtnText: '保存',
            };
            authorityRuleDialog = ngDialog.open({
                template: '/app/views/dialog/authority-rule-dialog.html',
                width: 680,
                overlay: true,
                scope: $scope
            });
        };

        $scope.addNewRule = function () {
            var mac = $scope.macInputModel.split(':');
            $scope.currentRule = {
                app: $scope.app,
                ip: mac[0],
                port: mac[1],
                rule: {
                    strategy: 0,
                    limitApp: '',
                }
            };
            $scope.authorityRuleDialog = {
                title: '新增授权规则',
                type: 'add',
                confirmBtnText: '新增',
                showAdvanceButton: true,
            };
            authorityRuleDialog = ngDialog.open({
                template: '/app/views/dialog/authority-rule-dialog.html',
                width: 680,
                overlay: true,
                scope: $scope
            });
        };

        $scope.saveRule = function () {
            if (!AuthorityRuleService.checkRuleValid($scope.currentRule.rule)) {
                return;
            }
            if ($scope.authorityRuleDialog.type === 'add') {
                addNewRuleAndPush($scope.currentRule);
            } else if ($scope.authorityRuleDialog.type === 'edit') {
                saveRuleAndPush($scope.currentRule, true);
            }
        };

        function addNewRuleAndPush(rule) {
            AuthorityRuleService.addNewRule(rule).success((data) => {
                if (data.success) {
                    getMachineRules();
                    authorityRuleDialog.close();
                } else {
                    alert('添加规则失败：' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('添加规则失败：' + data.msg);
                } else {
                    alert("添加规则失败：未知错误");
                }
            });
        }

        function saveRuleAndPush(rule, edit) {
            AuthorityRuleService.saveRule(rule).success(function (data) {
                if (data.success) {
                    alert("修改规则成功");
                    getMachineRules();
                    if (edit) {
                        authorityRuleDialog.close();
                    } else {
                        confirmDialog.close();
                    }
                } else {
                    alert('修改规则失败：' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('修改规则失败：' + data.msg);
                } else {
                    alert("修改规则失败：未知错误");
                }
            });
        }

        function deleteRuleAndPush(entity) {
            if (entity.id === undefined || isNaN(entity.id)) {
                alert('规则 ID 不合法！');
                return;
            }
            AuthorityRuleService.deleteRule(entity).success((data) => {
                if (data.code == 0) {
                    getMachineRules();
                    confirmDialog.close();
                } else {
                    alert('删除规则失败：' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('删除规则失败：' + data.msg);
                } else {
                    alert("删除规则失败：未知错误");
                }
            });
        };

        var confirmDialog;
        $scope.deleteRule = function (ruleEntity) {
            $scope.currentRule = ruleEntity;
            $scope.confirmDialog = {
                title: '删除授权规则',
                type: 'delete_rule',
                attentionTitle: '请确认是否删除如下授权限流规则',
                attention: '资源名: ' + ruleEntity.rule.resource + ', 流控应用: ' + ruleEntity.rule.limitApp +
                    ', 类型: ' + (ruleEntity.rule.strategy === 0 ? '白名单' : '黑名单'),
                confirmBtnText: '删除',
            };
            confirmDialog = ngDialog.open({
                template: '/app/views/dialog/confirm-dialog.html',
                scope: $scope,
                overlay: true
            });
        };

        $scope.confirm = function () {
            if ($scope.confirmDialog.type === 'delete_rule') {
                deleteRuleAndPush($scope.currentRule);
            } else {
                console.error('error');
            }
        };

        queryAppMachines();

        function queryAppMachines() {
            MachineService.getAppMachines($scope.app).success(
                function (data) {
                    if (data.code == 0) {
                        // $scope.machines = data.data;
                        if (data.data) {
                            $scope.machines = [];
                            $scope.macsInputOptions = [];
                            data.data.forEach(function (item) {
                                if (item.healthy) {
                                    $scope.macsInputOptions.push({
                                        text: item.ip + ':' + item.port,
                                        value: item.ip + ':' + item.port
                                    });
                                }
                            });
                        }
                        if ($scope.macsInputOptions.length > 0) {
                            $scope.macInputModel = $scope.macsInputOptions[0].value;
                        }
                    } else {
                        $scope.macsInputOptions = [];
                    }
                }
            );
        };
        $scope.$watch('macInputModel', function () {
            if ($scope.macInputModel) {
                getMachineRules();
            }
        });
    }]);