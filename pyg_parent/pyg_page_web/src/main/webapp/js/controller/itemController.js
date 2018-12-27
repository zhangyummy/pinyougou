app.controller("itemController", function ($scope) {

    //商品数目增加
    $scope.addNum = function (x) {
        $scope.num += x;
        if ($scope.num <= 0) {
            $scope.num = 1;
        }
    }

    //记录用户选择的规格
    $scope.specificationItems = {};

    $scope.selectSpec = function (name, value) {
        $scope.specificationItems[name] = value;
        $scope.searchSKU();//读取当前sku
    }

    //判断规格是否被选中
    $scope.isSelected = function (name, value) {
        if ($scope.specificationItems[name] == value) {
            return true;
        } else {
            return false;
        }
    }

    //加载默认sku
    $scope.loadDefaultSKU = function () {
        $scope.sku = skuList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //匹配两个对象
    $scope.matchObject = function (map1, map2) {
        if (map1.length != map2.length) {
            return false;
        }
        for (var key in map1) {
            if (map1[key] != map2[key]) {
                return false;
            }
        }
        return true;
    }

    //匹配选择的sku
    $scope.searchSKU = function () {
        for (var i = 0; i < skuList.length; i++) {
            if ($scope.matchObject($scope.specificationItems, skuList[i].spec)) {
                $scope.sku = skuList[i];
                return;
            }
        }
        $scope.sku = {id: 0, title: "------------", price: 0};
    }

    //添加商品到购物车
    $scope.addToCart = function () {
        alert("skuId" + $scope.sku.id);
    }

});