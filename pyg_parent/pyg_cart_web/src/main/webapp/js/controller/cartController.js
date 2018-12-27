//控制层
app.controller('cartController', function ($scope, cartService) {

    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            $scope.totalValue = cartService.sum($scope.cartList);//求合计数
            console.info($scope.cartList);
        });
    }

    //添加商品到购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(function (response) {
            if (response.success) {
                $scope.findCartList();//添加成功刷新购物车列表
            } else {
                alert(response.message);
            }
        });
    }

    //查找地址列表
    $scope.findAddressList = function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList = response;
            //设置默认地址
            for (var i = 0; i < $scope.addressList.length; i++) {
                if ($scope.addressList[i].isDefault == "1") {
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }
            console.info($scope.address)
        });
    }

    //选择收货地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }

    //判断收货地址是否是当前选择的收获地址
    $scope.isSelectedAddress = function (address) {
        if ($scope.address == address) {
            return true;
        } else {
            return false;
        }
    }

    $scope.order = {paymentType: '1'};

    //选择支付方式
    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    }

    //提交订单
    $scope.submitOrder = function (type) {
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver = $scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success) {
                if ($scope.order.paymentType == '1') {//如果是微信支付
                    location.href = "pay.html";
                } else {//如果是货到付款
                    location.href = "paysuccess.html";
                }
            } else {
                alert(response.message);
            }
        });
    }


});	
