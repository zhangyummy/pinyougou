//服务层
app.service('seckillGoodsService', function ($http) {

    //查找当前时间段内的秒杀商品
    this.findList = function () {
        return $http.get('seckillGoods/findList.do');
    }

    //从redis中查找秒杀对象
    this.findOneFromRedis = function (id) {
        return $http.get('seckillGoods/findOneFromRedis.do?id=' + id);
    }

    //提交订单
    this.submitOrder = function (id) {
        return $http.get('seckillOrder/submitOrder.do?seckillId=' + id);
    }

});
