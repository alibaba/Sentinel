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
                    $scope.loadError = {message: "Unknown error"};
                });
        };
        $scope.getMachineRules = getMachineRules;
        getMachineRules();

        var authorityRuleDialog;

        $scope.editRule = function (rule) {
            $scope.currentRule = rule;
            $scope.authorityRuleDialog = {
                title: 'Edit Authority Rule',
                type: 'edit',
                confirmBtnText: 'Save',
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
                title: 'Add New Authority Rule',
                type: 'add',
                confirmBtnText: 'Add',
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
                    alert('Failed to add new rule: ' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('Failed to add new rule: ' + data.msg);
                } else {
                    alert('Failed to add new rule: Unknown error');
                }
            });
        }

        function saveRuleAndPush(rule, edit) {
            AuthorityRuleService.saveRule(rule).success(function (data) {
                if (data.success) {
                    alert("Rule successfully edited");
                    getMachineRules();
                    if (edit) {
                        authorityRuleDialog.close();
                    } else {
                        confirmDialog.close();
                    }
                } else {
                    alert('Failed to edit rule: ' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('Failed to edit rule: ' + data.msg);
                } else {
                    alert('Failed to edit rule: Unknown error');
                }
            });
        }

        function deleteRuleAndPush(entity) {
            if (entity.id === undefined || isNaN(entity.id)) {
                alert('Invalid rule ID');
                return;
            }
            AuthorityRuleService.deleteRule(entity).success((data) => {
                if (data.code == 0) {
                    getMachineRules();
                    confirmDialog.close();
                } else {
                    alert('Failed to delete the rule: ' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('Failed to delete the rule: ' + data.msg);
                } else {
                    alert('Failed to delete the rule: Unknown error');
                }
            });
        };

        var confirmDialog;
        $scope.deleteRule = function (ruleEntity) {
            $scope.currentRule = ruleEntity;
            $scope.confirmDialog = {
                title: 'Delete Authority Rule',
                type: 'delete_rule',
                attentionTitle: 'Please confirm the following rule to be deleted',
                attention: 'Resource name: ' + ruleEntity.rule.resource + ', limit Origin: ' + ruleEntity.rule.limitApp +
                    ', type: ' + (ruleEntity.rule.strategy === 0 ? 'Whitelist' : 'Blacklist'),
                confirmBtnText: 'Delete',
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
                                if (item.health) {
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