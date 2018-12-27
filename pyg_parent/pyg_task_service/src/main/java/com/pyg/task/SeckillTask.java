package com.pyg.task;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("refresh seckillGoods ------------------------------------   " + new Date());
        //查询所有的秒杀商品集合
        List ids = new ArrayList(redisTemplate.boundHashOps("seckillGoodsList").keys());
        System.out.println(ids);
        //查询正在秒杀的商品列表
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已审核
        criteria.andStockCountGreaterThan(0);//库存大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
        if (ids.size() > 0) {
            criteria.andIdNotIn(ids);//排除已有商品
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //存入缓存
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoodsList").put(seckillGoods.getId(), seckillGoods);
        }
        System.out.println(seckillGoodsList.size() + "  tiao shang pin cun ru huan cun ----------------------");
    }

    /**
     * 移除秒杀商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        System.out.println("                       remove seckillGoods    " + new Date());
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoodsList").values();
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            //商品秒杀时间过期，删除商品
            if (seckillGoods.getEndTime().getTime() < new Date().getTime()) {
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//更新到数据库
                redisTemplate.boundHashOps("seckillGoodsList").delete(seckillGoods.getId());
                System.out.println("                       remove seckillGoods  " + seckillGoods.getId() + "    ===========");
            }
        }
        System.out.println("                       remove seckillGoods   end");
    }
}
