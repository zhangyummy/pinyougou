//服务层
app.service('payService', function ($http) {

    //生成微信支付二维码
    this.create2DCode = function () {
        return $http.get('pay/create2DCode.do');
    }

    //检测支付状态
    this.queryPayStatus = function (out_trade_no) {
        return $http.get('pay/queryPayStatus.do?out_trade_no=' + out_trade_no);
    }

});
