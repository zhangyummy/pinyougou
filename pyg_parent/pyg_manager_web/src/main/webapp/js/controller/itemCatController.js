//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = itemCatService.update($scope.entity); //修改
        } else {
            $scope.entity.parentId = $scope.parentId;//给上级id赋值
            serviceObject = itemCatService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    $scope.findByParentId($scope.parentId);//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        if (confirm("确认删除？" + $scope.selectIds)) {
            //获取选中的复选框
            itemCatService.dele($scope.selectIds).success(function (response) {
                if (response.success) {
                    $scope.findByParentId($scope.parentId);//重新加载
                } else {
                    alert(response.message);
                }
            });
            $scope.selectIds = [];// 删除后清空集合
        }
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //定义变量记录上级id
    $scope.parentId = 0;

    //查询下级
    $scope.findByParentId = function (parentId) {
        $scope.parentId = parentId; //查询前记录上级id
        itemCatService.findByParentId(parentId).success(function (response) {
            $scope.list = response;
        });
    }

    //分类级别
    $scope.rank = 1;

    $scope.addRank = function (value) {
        $scope.rank = value;
    }

    //面包屑 导航
    $scope.selectList = function (x) {
        if ($scope.rank == 1) {
            $scope.two = null;
            $scope.three = null;
        }
        if ($scope.rank == 2) {
            $scope.two = x;
            $scope.three = null;
        }
        if ($scope.rank == 3) {
            $scope.three = x;
        }
        $scope.findByParentId(x.id);
        $scope.selectIds = [];
    }

    //模板下拉框数据
    $scope.typeList = [];

    //查找下拉框模板数据
    $scope.findTypeList = function () {
        typeTemplateService.findAll().success(function (response) {
            $scope.typeList = response;
        });
    }

});	
