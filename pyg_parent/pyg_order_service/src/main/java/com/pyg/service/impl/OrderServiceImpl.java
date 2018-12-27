package com.pyg.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.myPojo.Cart;
import com.pyg.pojo.TbOrderExample;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbOrderMapper;
import com.pyg.pojo.TbOrder;
import com.pyg.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        List<String> orderIdList = new ArrayList<>();//支付记录中的订单列表
        double totalFee = 0;
        //每个商家的购物车创建一个订单
        for (Cart cart : cartList) {
            TbOrder tbOrder = new TbOrder();
            tbOrder.setOrderId(idWorker.nextId());//雪花算法生成id
            tbOrder.setPaymentType(order.getPaymentType());//支付类型
            tbOrder.setStatus("1");//状态
            tbOrder.setCreateTime(new Date());//创建时间
            tbOrder.setUpdateTime(new Date());//更新时间
            tbOrder.setUserId(order.getUserId());//用户id
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
            tbOrder.setReceiverMobile(order.getReceiverMobile());//收货人手机
            tbOrder.setReceiver(order.getReceiver());//收货人姓名
            tbOrder.setSourceType(order.getSourceType());//订单来源
            tbOrder.setSellerId(cart.getSellerId());//商家id
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId());//雪花算法生成id
                orderItem.setOrderId(tbOrder.getOrderId());//订单id
                money += orderItem.getTotalFee().doubleValue();//每个订单实付金额
                orderItemMapper.insert(orderItem);
            }
            tbOrder.setPayment(new BigDecimal(money));//实付金额
            orderMapper.insert(tbOrder);
            orderIdList.add(tbOrder.getOrderId() + "");//支付记录中包括订单的id
            totalFee += money;//支付记录中的总金额
        }
        if ("1".equals(order.getPaymentType())) {//如果为微信支付，添加支付记录
            TbPayLog payLog = new TbPayLog();
            payLog.setOutTradeNo(idWorker.nextId() + "");//支付订单号
            payLog.setCreateTime(new Date());//创建时间
            payLog.setTotalFee((long) (totalFee * 100));//支付金额（分）
            payLog.setUserId(order.getUserId());//用户ID
            payLog.setTradeState("0");//交易状态
            String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");//订单表 ID 串，用逗号 分隔
            payLog.setOrderList(ids);
            payLog.setPayType("1");//支付类型
            payLogMapper.insert(payLog);
            redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存
        }
        //清空缓存
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        TbOrderExample.Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 在缓存中查询payLog
     *
     * @param userId
     * @return
     */
    @Override
    public TbPayLog findPayLogFromRedis(String userId) {
        TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
        return payLog;
    }

    /**
     * 修改订单状态
     *
     * @param out_trade_no
     * @param transaction_id
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //1.修改支付记录状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());//支付时间
        payLog.setTradeState("1");//支付状态
        payLog.setTransactionId(transaction_id);//交易号
        payLogMapper.updateByPrimaryKey(payLog);
        //2.修改订单状态
        String[] orderIds = payLog.getOrderList().split(",");
        for (String orderId : orderIds) {
            TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (order != null) {
                order.setStatus("2");//已付款
                order.setPaymentTime(new Date());//支付时间
                orderMapper.updateByPrimaryKey(order);
            }
        }
        //3.清楚缓存数据
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}
