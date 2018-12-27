//服务层
app.service('cartService', function ($http) {

    //查询购物车列表
    this.findCartList = function () {
        return $http.get('cart/findCartList.do');
    }

    //添加商品到购物车
    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('cart/addGoodsToCartList.do?itemId=' + itemId + "&num=" + num);
    }

    //购物车总件数，总金额
    this.sum = function (cartList) {
        var totalValue = {totalNum: 0, totalMoney: 0};
        for (var i = 0; i < cartList.length; i++) {
            var orderItemList = cartList[i].orderItemList;
            for (var j = 0; j < orderItemList.length; j++) {
                totalValue.totalNum += orderItemList[j].num;
                totalValue.totalMoney += orderItemList[j].totalFee;
            }
        }
        return totalValue;
    }

    //查找地址列表
    this.findAddressList = function () {
        return $http.get('address/findListByLoginName.do');
    }

    //提交订单
    this.submitOrder = function (order) {
        return $http.post('order/add.do', order);
    }

});
