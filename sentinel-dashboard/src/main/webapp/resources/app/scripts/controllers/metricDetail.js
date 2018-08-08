var app = angular.module('sentinelDashboardApp');

app.controller('MetricDetailCtl', ['$scope', '$stateParams', 'MetricService', '$interval', '$timeout',
  function ($scope, $stateParams, MetricService, $interval, $timeout) {
    // set app name first
    $scope.app = $stateParams.app;
    $scope.url = $stateParams.url;
    // defalt search time diff
    const SEARCH_TIME_DIFF = 3 * 60;
    const INIT_TIME_DIFF = 20;

    $scope.endTime = new Date();
    $scope.startTime = new Date();
    $scope.startTime.setMinutes($scope.endTime.getMinutes() - INIT_TIME_DIFF);
    $scope.startTimeFmt = formatDate($scope.startTime);
    $scope.endTimeFmt = formatDate($scope.endTime);
    function formatDate(date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss');
    }
    $scope.changeStartTime = function (startTime) {
      $scope.startTime = new Date(startTime);
      $scope.startTimeFmt = formatDate(startTime);
    };
    $scope.changeEndTime = function (endTime) {
      $scope.endTime = new Date(endTime);
      $scope.endTimeFmt = formatDate(endTime);
    };

    $('#datetimepicker-start').datetimepicker({
      date: $scope.startTimeFmt,
    }).on('dp.change', function (e) {
      const start = new moment(e.date);
      const end = new moment($scope.endTimeFmt);
      if (start.isBefore(end)) {
        const result = start.format('YYYY-MM-DD  HH:mm:ss');
        $scope.startTimeFmt = result;
        queryIdentityDatas();
      } else {
        alert('开始时间不能大于结束时间');
      }
    });

    $('#datetimepicker-end').datetimepicker({
      date: $scope.endTimeFmt,
    }).on('dp.change', function (e) {
      const start = new moment($scope.startTimeFmt);
      const end = new moment(e.date);
      if (start.isBefore(end)) {
        const result = end.format('YYYY-MM-DD  HH:mm:ss');
        $scope.endTimeFmt = result;
        queryIdentityDatas();
      } else {
        alert('结束时间不能小于开始时间');
      }
    });

    function checkDiffTime() {
      const startSec = new moment($scope.startTimeFmt).valueOf();
      const endSec = new moment($scope.endTimeFmt).valueOf();
      if (((endSec - startSec) / 1000 / 60) > SEARCH_TIME_DIFF) {
        alert('开始时间与结束时间差不能超过' + SEARCH_TIME_DIFF + '分');
        return false;
      }

      return true;
    }

    // query data first
    queryIdentityDatas();

    let chart = null;
    function initChart() {
      $.each($scope.metrics, function (idx, metric) {
        chart = new G2.Chart({
          container: 'detail-chart',
          forceFit: true,
          width: 600,
          height: 300,
          padding: [10, 30, 70, 50]
        });
        chart.source(metric.data);
        chart.scale('timestamp', {
          type: 'time',
          mask: 'YYYY-MM-DD HH:mm:ss'
        });
        chart.scale('passedQps', {
          min: 0,
          fine: true,
          alias: 'p_qps'
          // max: 10
        });
        chart.scale('blockedQps', {
          min: 0,
          fine: true,
          alias: 'b_qps',
        });
        chart.scale('rt', {
          min: 0,
          fine: true,
        });
        chart.axis('rt', {
          grid: null,
          label: null
        });
        chart.axis('blockedQps', {
          grid: null,
          label: null
        });

        chart.axis('timestamp', {
          label: {
            textStyle: {
              textAlign: 'center', // 文本对齐方向，可取值为： start center end
              fill: '#404040', // 文本的颜色
              fontSize: '11', // 文本大小
              textBaseline: 'top', // 文本基准线，可取 top middle bottom，默认为middle
            },
            autoRotate: false,
            formatter: function (text, item, index) {
              return text.substring(11, 11 + 5);
            }
          }
        });
        chart.legend({
          custom: true,
          allowAllCanceled: true,
          itemFormatter: function (val) {
            if ('passedQps' === val) {
              return 'p_qps';
            }
            if ('blockedQps' === val) {
              return 'b_qps';
            }
            return val;
          },
          items: [
            { value: 'passedQps', marker: { symbol: 'hyphen', stroke: 'green', radius: 5, lineWidth: 2 } },
            { value: 'blockedQps', marker: { symbol: 'hyphen', stroke: 'blue', radius: 5, lineWidth: 2 } },
            // { value: 'rt', marker: {symbol: 'hyphen', stroke: 'gray', radius: 5, lineWidth: 2} },
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
        chart.line().position('timestamp*passedQps').size(1).color('green').shape('smooth');
        chart.line().position('timestamp*blockedQps').size(1).color('blue').shape('smooth');
        // chart.line().position('timestamp*rt').size(1).color('gray');
        G2.track(false);
        chart.render();
      });
    }

    function refreshChart() {
      $.each($scope.metrics, function (idx, metric) {
        chart.source(metric.data);
        chart.render();
      });
    }

    function queryIdentityDatas() {
      if ($scope.url.trim() === '') {
        alert('需传递接口参数');
        return;
      }
      if (checkDiffTime()) {
        MetricService.queryTopResourceMetricDetail({
          app: $scope.app,
          startTime: $scope.startTimeFmt,
          endTime: $scope.endTimeFmt,
          resourceKey: $scope.url,
        }).success(function(data) {
          $scope.metrics = [];
          $scope.emptyObjs = [];
          if (data.code == 0 && data.data) {
            var metricsObj = data.data.metric;
            var identityNames = Object.keys(metricsObj);
            if (identityNames.length < 1) {
              $scope.emptyServices = true;
            } else {
              $scope.emptyServices = false;
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
          } else {
            $scope.emptyServices = true;
  
            if (data.msg) {
              alert(data.msg);
            }
          }

          if (!chart) {
            initChart();
          } else {
            refreshChart();
          }
        });
      }
    }

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
                "passedQps": 0,
                "blockedQps": 0,
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
  }]);
