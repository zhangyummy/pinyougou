//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

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
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};
                    editor.html("");// 清空富文本编辑器
                    $scope.typeTemplate.specIds = [];//清空规格，再保存后点击启用规格不显示规格选项表
                    location.href = "goods.html";//跳转到商品列表页
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
            goodsService.dele($scope.selectIds).success(function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            });
        }
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
        $scope.selectIds = [];
    }


    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};

    //添加
    $scope.add = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(function (response) {
            if (response.success) {
                alert(response.message);
                $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};
                editor.html("");// 清空富文本编辑器
                $scope.typeTemplate.specIds = [];//清空规格
            } else {
                alert(response.message);
            }
        });
    }

    $scope.image_entity = {};

    //上传文件
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {
                $scope.image_entity.url = response.message;
            } else {
                alert(response.message);
            }
        });
    }

    //添加图片到列表中
    $scope.addImageToList = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    // 删除行
    $scope.delTableRow = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

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

    //添加到specificationItems
    $scope.updateSpecAttribute = function (event, name, value) {
        //判断attributeName属性中是否有name
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            if (event.target.checked) {
                object.attributeValue.push(value);
            } else {
                //移除选项
                var index = object.attributeValue.indexOf(value);
                object.attributeValue.splice(index, 1); // (移除参数索引,移除个数)
                //如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {
                    var index2 = $scope.entity.goodsDesc.specificationItems.indexOf(object);
                    $scope.entity.goodsDesc.specificationItems.splice(index2, 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push({attributeName: name, attributeValue: [value]});
        }
    }

    //创建SKU列表
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, status: "0", isDefault: "0"}];//初始化
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }

    //添加列值
    addColumn = function (list, columnName, columnValues) {
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    //商品状态列表
    $scope.auditStatusList = ["未提交", "已提交", "审核通过", "驳回"];

    //分类列表
    $scope.itemCatList = [];

    //初始化分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                $scope.itemCatList[response[i].id] = response[i].name;
                //$scope.itemCatList[response[i]["id"]] = response[i]["name"];
            }
        });
    }

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


});
