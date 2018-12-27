package com.pyg.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.service.WeiXinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    @Value("${appId}")
    private String appId;

    @Value("${mchId}")
    private String mchId;

    @Value("${sign}")
    private String sign;

    /**
     * 生成微信支付二维码
     *
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map create2DCode(String out_trade_no, String total_fee) {
        //1.创建参数
        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("appid", appId);//公众账号ID
        mapParam.put("mch_id", mchId);//商户号
        mapParam.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        mapParam.put("body", "品优购");//商品描述
        mapParam.put("out_trade_no", out_trade_no);//商户订单号
        mapParam.put("total_fee", "1");//标价金额
        mapParam.put("spbill_create_ip", "127.0.0.1");//终端IP
        mapParam.put("notify_url", "https://www.baidu.com");//通知地址
        mapParam.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(mapParam, sign);
            //System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获取结果
            String xmlResult = client.getContent();
            //System.out.println(xmlResult);
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            Map<String, String> map = new HashMap<>();
            map.put("code_url", mapResult.get("code_url"));//支付地址
            map.put("out_trade_no", out_trade_no);//商户订单号
            map.put("total_fee", total_fee);//标价金额
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    /**
     * 查询订单状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1.创建参数
        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("appid", appId);//公众账号ID
        mapParam.put("mch_id", mchId);//商户号
        mapParam.put("out_trade_no", out_trade_no);//商户订单号
        mapParam.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(mapParam, sign);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获取结果
            String xmlResult = client.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println(mapResult.get("trade_state"));
            return mapResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    public Map closePay(String out_trade_no){
        //1.创建参数
        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("appid", appId);//公众账号ID
        mapParam.put("mch_id", mchId);//商户号
        mapParam.put("out_trade_no", out_trade_no);//商户订单号
        mapParam.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(mapParam, sign);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获取结果
            String xmlResult = client.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            return mapResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
