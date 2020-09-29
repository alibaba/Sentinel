angular.module('sentinelDashboardApp')
    .directive('hamSelectSearch', hamSelectOptionsDirective);
function hamSelectOptionsDirective() {
    return {
        restrict: 'AE', //attribute or element
        scope: {
            datas: '=',
            value: '='
        },
        template: '<input type = "test" style="width:100%" class="form-control" ng-change="changeKeyValue(value)"' +
        ' ng-model="value" style = "display:block;" ' +
        'ng-click = "hidden=!hidden" value="{{value}}"/></input>' +
        '<div ng-hide="hidden" style="z-index:-1">' +
        ' <select style = "width:100%" ng-change="change(x)" ng-model="x" multiple>' +
        '  <option ng-repeat="data in datas" >{{data}}</option>' +
        ' </select>' +
        '</div>',
        // replace: true,
        link: function ($scope, elem, attr, ctrl) {
            
            $scope.tempdatas = $scope.datas; //a temp array to store the datas
            $scope.hidden = true;//show ot hide the select
            $scope.value = '';//the data user input
            //set the value of selected value to input
            $scope.change = function (x) {
                console.log("x",x);
                $scope.value = x[0];
                $scope.hidden = true;
            }
            //compare the input value to data array.If the temp array contains the input value . put it to newData and set it to $scope.datas
            //If no compare , will set the $scope.datas = $scope.tempdatas
            $scope.changeKeyValue = function (v) {
                var newData = []; //temp array
                angular.forEach($scope.tempdatas, function (data, index, array) {
                    if (data.indexOf(v) >= 0) {
                        newData.unshift(data);
                    }
                });
                //replace the temp to resource array
                $scope.datas = newData;
                //下拉选展示
                $scope.hidden = false;
                //if input value is null , reset the $scope.datas
                if ('' == v) {
                    $scope.datas = $scope.tempdatas;
                }
                console.log($scope.datas);
            }

        }
    };
}