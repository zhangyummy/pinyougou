package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbPayLog;
import com.pyg.service.OrderService;
import com.pyg.service.WeiXinPayService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeiXinPayService weiXinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/create2DCode")
    public Map create2DCode() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.findPayLogFromRedis(userId);
        if (payLog != null) {
            return weiXinPayService.create2DCode(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
        } else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        System.out.println(out_trade_no + "-----");
        Result result = null;
        int x = 0;
        while (true) {
            Map map = weiXinPayService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false, "支付出错");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")) {
                result = new Result(true, "支付成功");
                //修改订单状态
                orderService.updateOrderStatus((String) map.get("out_trade_no"), (String) map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x > 20) {//一分钟未支付跳出循环
                result = new Result(false, "二维码超时");
                break;
            }
        }
        return result;
    }

}
