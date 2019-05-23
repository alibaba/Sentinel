var app = angular.module('sentinelDashboardApp');

app.controller('MetricCtl', ['$scope', '$stateParams', 'MetricService', '$interval', '$timeout',
  function ($scope, $stateParams, MetricService, $interval, $timeout) {

    $scope.endTime = new Date();
    $scope.startTime = new Date();
    $scope.startTime.setMinutes($scope.endTime.getMinutes() - 30);
    $scope.startTimeFmt = formatDate($scope.startTime);
    $scope.endTimeFmt = formatDate($scope.endTime);
    function formatDate(date) {
      return moment(date).format('YYYY/MM/DD HH:mm:ss');
    }
    $scope.changeStartTime = function (startTime) {
      $scope.startTime = new Date(startTime);
      $scope.startTimeFmt = formatDate(startTime);
    };
    $scope.changeEndTime = function (endTime) {
      $scope.endTime = new Date(endTime);
      $scope.endTimeFmt = formatDate(endTime);
    };

    $scope.app = $stateParams.app;
    // 数据自动刷新频率
    var DATA_REFRESH_INTERVAL = 1000 * 10;

    $scope.servicePageConfig = {
      pageSize: 6,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };
    $scope.servicesChartConfigs = [];

    $scope.pageChanged = function (newPageNumber) {
      $scope.servicePageConfig.currentPageIndex = newPageNumber;
      reInitIdentityDatas();
    };

    var searchT;
    $scope.searchService = function () {
      $timeout.cancel(searchT);
      searchT = $timeout(function () {
        reInitIdentityDatas();
      }, 600);
    }

    var intervalId;
    reInitIdentityDatas();
    function reInitIdentityDatas() {
      $interval.cancel(intervalId);
      queryIdentityDatas();
      intervalId = $interval(function () {
        queryIdentityDatas();
      }, DATA_REFRESH_INTERVAL);
    };

    $scope.$on('$destroy', function () {
      $interval.cancel(intervalId);
    });
    $scope.initAllChart = function () {
      $.each($scope.metrics, function (idx, metric) {
        if (idx == $scope.metrics.length - 1) {
          return;
        }
        const chart = new G2.Chart({
          container: 'chart' + idx,
          forceFit: true,
          width: 100,
          height: 250,
          padding: [10, 30, 70, 50]
        });
        var maxQps = 0;
        for (var i in metric.data) {
          var item = metric.data[i];
          if (item.passQps > maxQps) {
            maxQps = item.passQps;
          }
          if (item.blockQps > maxQps) {
            maxQps = item.blockQps;
          }
        }
        chart.source(metric.data);
        chart.scale('timestamp', {
          type: 'time',
          mask: 'YYYY-MM-DD HH:mm:ss'
        });
        chart.scale('passQps', {
          min: 0,
          max: maxQps,
          fine: true,
          alias: '通过 QPS'
          // max: 10
        });
        chart.scale('blockQps', {
          min: 0,
          max: maxQps,
          fine: true,
          alias: '拒绝 QPS',
        });
        chart.scale('rt', {
          min: 0,
          fine: true,
        });
        chart.axis('rt', {
          grid: null,
          label: null
        });
        chart.axis('blockQps', {
          grid: null,
          label: null
        });

        chart.axis('timestamp', {
          label: {
            textStyle: {
              textAlign: 'center', // 文本对齐方向，可取值为： start center end
              fill: '#404040', // 文本的颜色
              fontSize: '11', // 文本大小
              //textBaseline: 'top', // 文本基准线，可取 top middle bottom，默认为middle
            },
            autoRotate: false,
            formatter: function (text, item, index) {
              return text.substring(11, 11 + 5);
            }
          }
        });
        chart.legend({
          custom: true,
          position: 'bottom',
          allowAllCanceled: true,
          itemFormatter: function (val) {
            if ('passQps' === val) {
              return '通过 QPS';
            }
            if ('blockQps' === val) {
              return '拒绝 QPS';
            }
            return val;
          },
          items: [
            { value: 'passQps', marker: { symbol: 'hyphen', stroke: 'green', radius: 5, lineWidth: 2 } },
            { value: 'blockQps', marker: { symbol: 'hyphen', stroke: 'blue', radius: 5, lineWidth: 2 } },
            //{ value: 'rt', marker: {symbol: 'hyphen', stroke: 'gray', radius: 5, lineWidth: 2} },
          ],
          onClick: function (ev) {
            const item = ev.item;
            const value = item.value;
            const checked = ev.checked;
            const geoms = chart.getAllGeoms();
            for (var i = 0; i < geoms.length; i++) {
              const geom = geoms[i];
              if (geom.getYScale().field === value) {
                if (checked) {
                  geom.show();
                } else {
                  geom.hide();
                }
              }
            }
          }
        });
        chart.line().position('timestamp*passQps').size(1).color('green').shape('smooth');
        chart.line().position('timestamp*blockQps').size(1).color('blue').shape('smooth');
        //chart.line().position('timestamp*rt').size(1).color('gray').shape('smooth');
        G2.track(false);
        chart.render();
      });
    };

    $scope.metrics = [];
    $scope.emptyObjs = [];
    function queryIdentityDatas() {
      var params = {
        app: $scope.app,
        pageIndex: $scope.servicePageConfig.currentPageIndex,
        pageSize: $scope.servicePageConfig.pageSize,
        desc: $scope.isDescOrder,
        searchKey: $scope.serviceQuery
      };
      MetricService.queryAppSortedIdentities(params).success(function (data) {
        $scope.metrics = [];
        $scope.emptyObjs = [];
        if (data.code === 0 && data.data) {
          var metricsObj = data.data.metric;
          var identityNames = Object.keys(metricsObj);
          if (identityNames.length < 1) {
            $scope.emptyServices = true;
          } else {
            $scope.emptyServices = false;
          }
          $scope.servicePageConfig.totalPage = data.data.totalPage;
          $scope.servicePageConfig.pageSize = data.data.pageSize;
          var totalCount = data.data.totalCount;
          $scope.servicePageConfig.totalCount = totalCount;
          for (i = 0; i < totalCount; i++) {
            $scope.emptyObjs.push({});
          }
          $.each(identityNames, function (idx, identityName) {
            var identityDatas = metricsObj[identityName];
            var metrics = {};
            metrics.resource = identityName;
            // metrics.data = identityDatas;
            metrics.data = fillZeros(identityDatas);
            metrics.shortData = lastOfArray(identityDatas, 6);
            $scope.metrics.push(metrics);
          });
          // push an empty element in the last, for ng-init reasons.
          $scope.metrics.push([]);
        } else {
          $scope.emptyServices = true;
          console.log(data.msg);
        }
      });
    };
    function fillZeros(metricData) {
      if (!metricData || metricData.length == 0) {
        return [];
      }
      var filledData = [];
      filledData.push(metricData[0]);
      var lastTime = metricData[0].timestamp / 1000;
      for (var i = 1; i < metricData.length; i++) {
        var curTime = metricData[i].timestamp / 1000;
        if (curTime > lastTime + 1) {
          for (var j = lastTime + 1; j < curTime; j++) {
            filledData.push({
                "timestamp": j * 1000,
                "passQps": 0,
                "blockQps": 0,
                "successQps": 0,
                "exception": 0,
                "rt": 0,
                "count": 0
            })
          }
        }
        filledData.push(metricData[i]);
        lastTime = curTime;
      }
      return filledData;
    }
    function lastOfArray(arr, n) {
      if (!arr.length) {
        return [];
      }
      var rs = [];
      for (i = 0; i < n && i < arr.length; i++) {
        rs.push(arr[arr.length - 1 - i]);
      }
      return rs;
    }

    $scope.isDescOrder = true;
    $scope.setDescOrder = function () {
      $scope.isDescOrder = true;
      reInitIdentityDatas();
    }
    $scope.setAscOrder = function () {
      $scope.isDescOrder = false;
      reInitIdentityDatas();
    }
  }]);
