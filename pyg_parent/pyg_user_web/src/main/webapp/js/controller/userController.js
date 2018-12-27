//控制层
app.controller('userController', function ($scope, userService) {

    $scope.entity = {};

    //注册
    $scope.register = function () {
        if ($scope.entity.password != $scope.password2) {
            $scope.errorMsg = "两次密码不一致！";
            return;
        } else {
            $scope.errorMsg = "";
        }
        userService.add($scope.entity, $scope.code).success(function (response) {
            if (response.success) {
                location.href = "http://localhost:8080/cas/login";
            } else {
                alert(response.message);
            }
        });
        $scope.code = "";
    }

    //发送验证码
    $scope.sendSmsCode = function () {
        if ($scope.entity.phone == null) {
            alert("请输入手机号！")
            return;
        }
        userService.sendSmsCode($scope.entity.phone).success(function (response) {
            if (!response.success) {
                alert(response.message)
            }
        });
    }

});	
