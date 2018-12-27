package com.pyg.service;

import java.util.Map;

public interface WeiXinPayService {

    /**
     * 生成微信支付二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    public Map create2DCode(String out_trade_no, String total_fee);

    /**
     * 查询订单状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    public Map closePay(String out_trade_no);
}
