/**
 * Parameter flow control service.
 * 
 * @author Eric Zhao
 */
angular.module('sentinelDashboardApp').service('ParamFlowService', ['$http', function ($http) {
  this.queryMachineRules = function(app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: '/paramFlow/rules',
      params: param,
      method: 'GET'
    });
  };

  this.addNewRule = function(rule) {
    return $http({
      url: '/paramFlow/rule',
      data: rule,
      method: 'POST'
    });
  };

  this.saveRule = function (entity) {
    return $http({
      url: '/paramFlow/rule/' + entity.id,
      data: entity,
      method: 'PUT'
    });
  };

  this.deleteRule = function (entity) {
    return $http({
      url: '/paramFlow/rule/' + entity.id,
      method: 'DELETE'
    });
  };

    function isNumberClass(classType) {
        return classType === 'int' || classType === 'double' ||
            classType === 'float' || classType === 'long' || classType === 'short';
    }

    function isByteClass(classType) {
        return classType === 'byte';
    }

    function notNumberAtLeastZero(num) {
        return num === undefined || num === '' || isNaN(num) || num < 0;
    }

    function notGoodNumber(num) {
        return num === undefined || num === '' || isNaN(num);
    }

    function notGoodNumberBetweenExclusive(num, l ,r) {
        return num === undefined || num === '' || isNaN(num) || num < l || num > r;
    }

    function notValidParamItem(curExItem) {
        if (isNumberClass(curExItem.classType) && notGoodNumber(curExItem.object)) {
            return true;
        }
        if (isByteClass(curExItem.classType) && notGoodNumberBetweenExclusive(curExItem.object, -128, 127)) {
            return true;
        }
        return curExItem.object === undefined || curExItem.classType === undefined ||
            notNumberAtLeastZero(curExItem.count);
    }

  this.checkRuleValid = function (rule) {
      if (!rule.resource || rule.resource === '') {
          alert('Resource name cannot be empty');
          return false;
      }
      if (rule.grade != 1) {
          alert('Invalid mode');
          return false;
      }
      if (rule.count < 0) {
          alert('Threshold should be at least 0');
          return false;
      }
      if (rule.paramIdx === undefined || rule.paramIdx === '' || isNaN(rule.paramIdx) || rule.paramIdx < 0) {
          alert('Parameter index should be at least 0');
          return false;
      }
      if (rule.paramFlowItemList !== undefined) {
          for (var i = 0; i < rule.paramFlowItemList.length; i++) {
              var item = rule.paramFlowItemList[i];
              if (notValidParamItem(item)) {
                  alert('Invalid exception item, please check: param item value is ' + item.object + ', item type is ' +
                      item.classType + ', threshold is ' + item.count);
                  return false;
              }
          }
      }
      return true;
  };
}]);
