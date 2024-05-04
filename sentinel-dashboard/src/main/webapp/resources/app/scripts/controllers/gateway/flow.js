var app = angular.module('sentinelDashboardApp');

app.controller('GatewayFlowCtl', ['$scope', '$stateParams', 'GatewayFlowService', 'GatewayApiService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, GatewayFlowService, GatewayApiService, ngDialog, MachineService) {
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
    function extractIPAndPort(input) {
    // 假设输入字符串是 9 组的格式，并且我们想要前 8 组作为 IPv6 地址，最后一组作为端口号
    const parts = input.split(':');
    if (parts.length === 9) {
        // 提取前 8 组作为 IPv6 地址
        const ip = parts.slice(0, 8).join(':');
        // 尝试将最后一组转换为端口号（这里假设它是有效的端口号）
        const portStr = parts[8];
        const port = parseInt(portStr, 10);

        // 验证端口号是否在有效范围内
        if (port >= 1 && port <= 65535) {
            // 返回包含 IPv6 地址和端口号的数组
            return [ip, port];
        } else {
            // 端口号无效，返回包含 IPv6 地址和 null 的数组
            return [ip, null];
        }
    } else if(parts.length === 2){
    	const ip = parts[0];
        const port = parseInt(parts[1], 10);
      	return [ip, port];
    }else {
        // 输入字符串格式不正确，返回包含两个 null 的数组
        return [null, null];
    }
};
    getMachineRules();
    function getMachineRules() {
      if (!$scope.macInputModel) {
        return;
      }

      var mac = extractIPAndPort($scope.macInputModel);
      GatewayFlowService.queryRules($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.rules = data.data;
            $scope.rulesPageConfig.totalCount = $scope.rules.length;
          } else {
            $scope.rules = [];
            $scope.rulesPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getMachineRules = getMachineRules;

    getApiNames();
    function getApiNames() {
      if (!$scope.macInputModel) {
        return;
      }

      var mac = extractIPAndPort($scope.macInputModel);
      GatewayApiService.queryApis($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.apiNames = [];

            data.data.forEach(function (api) {
              $scope.apiNames.push(api["apiName"]);
            });
          }
        });
    }

    $scope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}, {val: 2, desc: '时'}, {val: 3, desc: '天'}];

    var gatewayFlowRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = angular.copy(rule);
      $scope.gatewayFlowRuleDialog = {
        title: '编辑网关流控规则',
        type: 'edit',
        confirmBtnText: '保存'
      };
      gatewayFlowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/flow-rule-dialog.html',
        width: 780,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      var mac = extractIPAndPort($scope.macInputModel);
      $scope.currentRule = {
        grade: 1,
        app: $scope.app,
        ip: mac[0],
        port: mac[1],
        resourceMode: 0,
        interval: 1,
        intervalUnit: 0,
        controlBehavior: 0,
        burst: 0,
        maxQueueingTimeoutMs: 0
      };

      $scope.gatewayFlowRuleDialog = {
        title: '新增网关流控规则',
        type: 'add',
        confirmBtnText: '新增'
      };

      gatewayFlowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/flow-rule-dialog.html',
        width: 780,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if (!GatewayFlowService.checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.gatewayFlowRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.gatewayFlowRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    $scope.useRouteID = function() {
      $scope.currentRule.resource = '';
    };

    $scope.useCustormAPI = function() {
      $scope.currentRule.resource = '';
    };

    $scope.useParamItem = function () {
      $scope.currentRule.paramItem = {
        parseStrategy: 0,
        matchStrategy: 0
      };
    };

    $scope.notUseParamItem = function () {
      $scope.currentRule.paramItem = null;
    };

    $scope.useParamItemVal = function() {
      $scope.currentRule.paramItem.pattern = "";
      $scope.currentRule.paramItem.matchStrategy = 0;
    };

    $scope.notUseParamItemVal = function() {
      $scope.currentRule.paramItem.pattern = null;
      $scope.currentRule.paramItem.matchStrategy = null;
    };

    function addNewRule(rule) {
      GatewayFlowService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          gatewayFlowRuleDialog.close();
        } else {
          alert('新增网关流控规则失败!' + data.msg);
        }
      });
    };

    function saveRule(rule, edit) {
      GatewayFlowService.saveRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          if (edit) {
            gatewayFlowRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('修改网关流控规则失败!' + data.msg);
        }
      });
    };

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: '删除网关流控规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下规则',
        attention: 'API名称: ' + rule.resource + ', ' + (rule.grade == 1 ? 'QPS阈值' : '线程数') + ': ' + rule.count,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_rule') {
        deleteRule($scope.currentRule);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      GatewayFlowService.deleteRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          confirmDialog.close();
        } else {
          alert('删除网关流控规则失败!' + data.msg);
        }
      });
    };

    queryAppMachines();

    function queryAppMachines() {
      MachineService.getAppMachines($scope.app).success(
          function (data) {
            if (data.code == 0) {
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
        getApiNames();
      }
    });
  }]
);
