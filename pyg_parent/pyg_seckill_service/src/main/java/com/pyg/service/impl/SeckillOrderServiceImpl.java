package com.pyg.service.impl;

import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSeckillOrderMapper;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.pojo.TbSeckillOrderExample;
import com.pyg.pojo.TbSeckillOrderExample.Criteria;
import com.pyg.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }

        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 提交订单
     *
     * @param seckillId
     * @param userId
     */
    public void submitOrder(Long seckillId, String userId) {
        //缓存的库存-1
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").get(seckillId);
        if (seckillGoods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (seckillGoods.getStockCount() <= 0) {
            throw new RuntimeException("商品已抢完");
        }
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);//库存-1
        redisTemplate.boundHashOps("seckillGoodsList").put(seckillId, seckillGoods);
        //库存为0，更新数据库，删除缓存
        if (seckillGoods.getStockCount() == 0) {
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            redisTemplate.boundHashOps("seckillGoodsList").delete(seckillId);
        }
        //生成秒杀订单存入缓存
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());//id
        seckillOrder.setSeckillId(seckillGoods.getId());//秒杀商品ID
        seckillOrder.setMoney(seckillGoods.getCostPrice());//支付金额
        seckillOrder.setUserId(userId);//用户
        seckillOrder.setSellerId(seckillGoods.getSellerId());//商家
        seckillOrder.setCreateTime(new Date());//创建时间
        seckillOrder.setStatus("0");//状态
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
        System.out.println("seckillOrder ----->   redis");
    }

    /**
     * 在缓存中查找seckillOrder
     *
     * @param userId
     * @return
     */
    public TbSeckillOrder findSeckillOrderfromRedis(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    /**
     * 保存订单到数据库
     *
     * @param userId
     * @param orderId
     * @param transactionId
     */
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        //从redis获取当前用户的订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        //如果与传递过来的订单号不符
        if (seckillOrder.getId().longValue() != orderId.longValue()) {
            throw new RuntimeException("订单不相符");
        }
        //跟新订单信息
        seckillOrder.setPayTime(new Date());//支付时间
        seckillOrder.setStatus("1");//状态
        seckillOrder.setTransactionId(transactionId);//交易流水
        //保存到数据库
        seckillOrderMapper.insert(seckillOrder);
        System.out.println("seckillOrder ----->   db");
        //删除缓存
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }

    /**
     * 从缓存删除订单
     *
     * @param userId
     * @param orderId
     */
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //从redis获取当前用户的订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        //从redis删除当前用户的订单缓存
        if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }
        //从redis获取秒杀商品放入redis，数量+1
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").get(seckillOrder.getSeckillId());
        System.out.println(seckillGoods);
        if (seckillGoods == null) {
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
        }
        seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
        redisTemplate.boundHashOps("seckillGoodsList").put(seckillGoods.getId(), seckillGoods);
    }

}
