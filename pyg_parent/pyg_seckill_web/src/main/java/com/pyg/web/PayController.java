package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.service.SeckillGoodsService;
import com.pyg.service.SeckillOrderService;
import com.pyg.service.WeiXinPayService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 10000)
    private WeiXinPayService weiXinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/create2DCode")
    public Map create2DCode() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.findSeckillOrderfromRedis(userId);
        if (seckillOrder != null) {
            long fen = (long) (seckillOrder.getMoney().doubleValue() * 100);
            return weiXinPayService.create2DCode(seckillOrder.getId() + "", fen + "");
        } else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        System.out.println(out_trade_no + "-----");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
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
                //保存订单到数据库
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), (String) map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x > 100) {//五分钟未支付跳出循环
                result = new Result(false, "二维码超时");
                Map map1 = weiXinPayService.closePay(out_trade_no);
                if (!"SUCCESS".equals(map1.get("result_code"))) {//如果返回结果是正常关闭
                    if ("ORDERPAID".equals(map1.get("err_code"))) {
                        result = new Result(true, "支付成功");
                        seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), (String) map.get("transaction_id"));
                    }
                }
                if (result.isSuccess() == false) {
                    System.out.println("qu xiao ding dan -----");
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }

}
