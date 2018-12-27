//控制层
app.controller('seckillGoodsController', function ($scope, $location, $interval, seckillGoodsService) {

    //查找当前时间段内的秒杀商品
    $scope.findList = function () {
        seckillGoodsService.findList().success(function (response) {
            console.info(response);
            $scope.list = response;
        });
    }

    //从redis中查找秒杀对象
    $scope.findOneFromRedis = function () {
        var id = $location.search().id;
        if (id == null) {
            location.href = "seckill-index.html";
            return;
        }
        seckillGoodsService.findOneFromRedis(id).success(function (response) {
            $scope.seckillGoods = response;
            //剩余时间 秒
            var allSeconds = Math.floor((new Date(response.endTime).getTime() - new Date().getTime()) / 1000);
            //倒计时
            $interval(function () {
                if (allSeconds > 0) {
                    allSeconds--;
                    //console.info(allSeconds);
                    $scope.timeString = convertDateToString(allSeconds);
                }
            }, 1000);
        });
    }

    //将秒数转换为倒计时字符串
    convertDateToString = function (allSeconds) {
        var days = Math.floor(allSeconds / (24 * 60 * 60));// 天
        var hours = Math.floor((allSeconds - 24 * 60 * 60 * days) / (60 * 60));// 时
        var minutes = Math.floor((allSeconds - 24 * 60 * 60 * days - 60 * 60 * hours) / 60);// 分
        var seconds = allSeconds - 24 * 60 * 60 * days - 60 * 60 * hours - 60 * minutes;// 秒
        var timeString = "";
        if (days > 0) {
            timeString += days + "天 ";
        }
        if ((hours + "").length == 1) {
            hours = "0" + hours;
        }
        if ((minutes + "").length == 1) {
            minutes = "0" + minutes;
        }
        if ((seconds + "").length == 1) {
            seconds = "0" + seconds;
        }
        return timeString + hours + ":" + minutes + ":" + seconds;
    }

    //提交订单
    $scope.submitOrder = function (id) {
        seckillGoodsService.submitOrder(id).success(function (response) {
            if (response.success) {
                alert("下单成功，请在 1 分钟内完成支付");
                location.href = "pay.html";
            } else {
                alert(response.message);
            }
        });
    }


});
