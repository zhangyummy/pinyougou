//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        var id = $location.search().id;//获取id的参数值
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(function (response) {
            $scope.entity = response;
            editor.html($scope.entity.goodsDesc.introduction);//富文本编辑器添加内容
            $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);//显示图片列表
            $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);//扩展属性
            $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);//规格
            //SKU规格列表
            for (var i = 0; i < $scope.entity.itemList.length; i++) {
                $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
            }
        });
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //提交商品申请,修改商品状态
    $scope.updateStatus = function (status) {
        goodsService.updateStatus($scope.selectIds, status).success(function (response) {
            if (response.success) {
                $scope.reloadList();//刷新列表
                $scope.selectIds = [];
            } else {
                alert(response.message);
            }
        });
    }

    //商品状态列表
    $scope.auditStatusList = ["未提交", "未审核", "审核通过", "驳回"];


    // ---------------------------------------------------------------------------------------------------------------------

/*

    //分类列表
    $scope.itemCatList = [];

    //初始化分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                $scope.itemCatList[response[i].id] = response[i].name;
            }
        });
    }

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};

    //查询一级分类列表
    $scope.selectItemCatListOne = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCatListOne = response;
        });
    }

    //查询二级分类列表
    $scope.$watch("entity.goods.category1Id", function () {
        itemCatService.findByParentId($scope.entity.goods.category1Id).success(function (response) {
            $scope.itemCatListTwo = response;
        });
    });

    //查询三级分类列表
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCatListThree = response;
        });
    });

    //获取模板ID，获取brand品牌下拉列表数据
    $scope.$watch("entity.goods.category3Id", function (newValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;
        });
    });

    //获取brand品牌下拉列表数据，获取扩展属性，获取规格选项
    $scope.$watch("entity.goods.typeTemplateId", function (newValue) {
        typeTemplateService.findOnePro(newValue).success(function (response) {
            $scope.typeTemplate = response; //获取模板
            $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
            if ($location.search().id == null) {//如果没有ID
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
            }
            $scope.typeTemplate.specIds = JSON.parse($scope.typeTemplate.specIds);

        });
    });

    //根据entity.goodsDesc.specificationItems控制复选框勾选
    $scope.checkAttribute = function (name, value) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, "attributeName", name);
        if (object == null) {
            return false;
        } else {
            if (object.attributeValue.indexOf(value) >= 0) {
                return true;
            } else {
                return false;
            }
        }
    }
*/

});
