var app = angular.module('sentinelDashboardApp');

app.filter('range', [function () {
  return function (input, length) {
    if (isNaN(length) || length <= 0) {
      return [];
    }

    input = [];
    for (var index = 1; index <= length; index++) {
      input.push(index);
    }

    return input;
  };
  
}]);
