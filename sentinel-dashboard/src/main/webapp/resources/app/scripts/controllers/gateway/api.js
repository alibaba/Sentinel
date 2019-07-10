var app = angular.module('sentinelDashboardApp');

app.controller('GatewayApiCtl', ['$scope', '$stateParams', 'GatewayApiService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, GatewayApiService, ngDialog, MachineService) {
    $scope.app = $stateParams.app;

    $scope.apisPageConfig = {
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

    getApis();
    function getApis() {
      if (!$scope.macInputModel) {
        return;
      }

      var mac = $scope.macInputModel.split(':');
      GatewayApiService.queryApis($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            // To merge rows for api who has more than one predicateItems, here we build data manually
            $scope.apis = [];

            data.data.forEach(function(api) {
              api["predicateItems"].forEach(function (item, index) {
                var newItem = {};
                newItem["id"] = api["id"];
                newItem["app"] = api["app"];
                newItem["ip"] = api["ip"];
                newItem["port"] = api["port"];
                newItem["apiName"] = api["apiName"];
                newItem["pattern"] = item["pattern"];
                newItem["matchStrategy"] = item["matchStrategy"];
                // The itemSize indicates how many rows to merge, by using rowspan="{{api.itemSize}}" in <td> tag
                newItem["itemSize"] = api["predicateItems"].length;
                // Mark the flag of first item to zero, indicates the start row to merge
                newItem["firstFlag"] = index == 0 ? 0 : 1;
                // Still hold the data of predicateItems, in order to bind data in edit dialog html
                newItem["predicateItems"] = api["predicateItems"];
                $scope.apis.push(newItem);
              });
            });

            $scope.apisPageConfig.totalCount = data.data.length;
          } else {
            $scope.apis = [];
            $scope.apisPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getApis = getApis;

    var gatewayApiDialog;
    $scope.editApi = function (api) {
      $scope.currentApi = angular.copy(api);
      $scope.gatewayApiDialog = {
        title: '编辑自定义 API',
        type: 'edit',
        confirmBtnText: '保存'
      };
      gatewayApiDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/api-dialog.html',
        width: 900,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewApi = function () {
      var mac = $scope.macInputModel.split(':');
      $scope.currentApi = {
        grade: 0,
        app: $scope.app,
        ip: mac[0],
        port: mac[1],
        predicateItems: [{matchStrategy: 0, pattern: ''}]
      };
      $scope.gatewayApiDialog = {
        title: '新增自定义 API',
        type: 'add',
        confirmBtnText: '新增'
      };
      gatewayApiDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/api-dialog.html',
        width: 900,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveApi = function () {
      var apiNames = [];
      if ($scope.gatewayApiDialog.type === 'add') {
        apiNames = $scope.apis.map(function (item, index, array) {
          return item["apiName"];
        }).filter(function (item, index, array) {
          return array.indexOf(item) === index;
        });
      }

      if (!GatewayApiService.checkApiValid($scope.currentApi, apiNames)) {
        return;
      }

      if ($scope.gatewayApiDialog.type === 'add') {
        addNewApi($scope.currentApi);
      } else if ($scope.gatewayApiDialog.type === 'edit') {
        saveApi($scope.currentApi, true);
      }
    };

    function addNewApi(api) {
      GatewayApiService.newApi(api).success(function (data) {
        if (data.code == 0) {
          getApis();
          gatewayApiDialog.close();
        } else {
          alert('新增自定义API失败!' + data.msg);
        }
      });
    };

    function saveApi(api, edit) {
      GatewayApiService.saveApi(api).success(function (data) {
        if (data.code == 0) {
          getApis();
          if (edit) {
            gatewayApiDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('修改自定义API失败!' + data.msg);
        }
      });
    };

    var confirmDialog;
    $scope.deleteApi = function (api) {
      $scope.currentApi = api;
      $scope.confirmDialog = {
        title: '删除自定义API',
        type: 'delete_api',
        attentionTitle: '请确认是否删除如下自定义API',
        attention: 'API名称: ' + api.apiName,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_api') {
        deleteApi($scope.currentApi);
      } else {
        console.error('error');
      }
    };

    function deleteApi(api) {
      GatewayApiService.deleteApi(api).success(function (data) {
        if (data.code == 0) {
          getApis();
          confirmDialog.close();
        } else {
          alert('删除自定义API失败!' + data.msg);
        }
      });
    };

    $scope.addNewMatchPattern = function() {
      var total;
      if ($scope.currentApi.predicateItems == null) {
        $scope.currentApi.predicateItems = [];
        total = 0;
      } else {
        total = $scope.currentApi.predicateItems.length;
      }
      $scope.currentApi.predicateItems.splice(total + 1, 0, {matchStrategy: 0, pattern: ''});
    };

    $scope.removeMatchPattern = function($index) {
      if ($scope.currentApi.predicateItems.length <= 1) {
        // Should never happen since no remove button will display when only one predicateItem.
        alert('至少有一个匹配规则');
        return;
      }
      $scope.currentApi.predicateItems.splice($index, 1);
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
        getApis();
      }
    });
  }]
);
