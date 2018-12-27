//控制层
app.controller('payController', function ($scope, $location, payService) {

    //生成微信支付二维码
    $scope.create2DCode = function () {
        payService.create2DCode().success(function (response) {
            console.info(response.out_trade_no);
            $scope.out_trade_no = response.out_trade_no;//商户订单号
            $scope.money = (response.total_fee / 100).toFixed(2);//标价金额
            //生成二维码
            var qr = new QRious({
                element: document.getElementById("qrious"),
                size: 300,
                level: 'H',
                value: response.code_url
            });
            queryPayStatus(response.out_trade_no);
        });
    }

    //检测支付状态
    queryPayStatus = function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            if (response.success) {
                location.href = "paysuccess.html#?money=" + $scope.money;
            } else {
                if (response.message == '二维码超时') {
                    var qr = new QRious({
                        element: document.getElementById("qrious"),
                        size: 300,
                        level: 'H',
                        value: '二维码超时'
                    });
                } else {
                    location.href = "payfail.html";
                }
            }
        });
    }

    //获取支付金额
    $scope.getMoney = function () {
        return $location.search().money;
    }

});
