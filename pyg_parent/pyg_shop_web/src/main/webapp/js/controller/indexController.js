app.controller("indexController", function ($scope, loginService) {

    // 获取登录名
    $scope.getName = function () {
        loginService.getName().success(function (response) {
            $scope.loginName = response.loginName;
        });
    }


});