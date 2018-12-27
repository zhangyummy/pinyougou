app.controller("brandController", function ($scope, $controller, brandService) {

    $controller('baseController', {$scope: $scope});//继承


    // 查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(function (response) {
            $scope.list = response;
        });
    }


    // 分页查询品牌列表
    $scope.findPage = function (pageNum, pageSize) {
        brandService.findPage().success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;
        });
    }

    // 保存品牌
    $scope.save = function () {
        brandService.save($scope.entity).success(function (response) {
            if (response.success) {
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        });
    }

    // 查找一个
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        });
    }


    // 删除品牌
    $scope.del = function () {
        if (confirm("确认删除？" + $scope.selectIds)) {
            brandService.del($scope.selectIds).success(function (response) {
                if (response.success) {
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            });
            $scope.selectIds = []; // 删除后清空集合
        }
    }

    $scope.searchEntity = {}; // 初始化

    // 条件搜索+分页
    $scope.search = function (pageNum, pageSize) {
        brandService.search(pageNum, pageSize, $scope.searchEntity).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;
        });
        $scope.selectIds = []; // 翻页后清空集合
    }


});