package com.pyg.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import com.pyg.service.SeckillGoodsService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillGoods> findAll() {
        return seckillGoodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillGoods seckillGoods) {
        seckillGoodsMapper.insert(seckillGoods);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillGoods seckillGoods) {
        seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillGoods findOne(Long id) {
        return seckillGoodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillGoodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();

        if (seckillGoods != null) {
            if (seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0) {
                criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
            }
            if (seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
            }
            if (seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
            }
            if (seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
            }
            if (seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0) {
                criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
            }

        }

        Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 查找当前时间段内的秒杀商品
     *
     * @return
     */
    public List<TbSeckillGoods> findList() {
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoodsList").values();
        if (seckillGoodsList == null || seckillGoodsList.size() == 0) {//缓存中没有
            TbSeckillGoodsExample example = new TbSeckillGoodsExample();
            TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//已审核
            criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于当前时间
            criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
            criteria.andStockCountGreaterThan(0);//库存大于0
            seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            System.out.println("search database -----------");
            for (TbSeckillGoods goods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoodsList").put(goods.getId(), goods);
            }
        } else {
            System.out.println("search redis -------------");
        }
        return seckillGoodsList;
    }

    /**
     * 从redis中查找秒杀对象
     *
     * @param id
     * @return
     */
    public TbSeckillGoods findOneFromRedis(Long id) {
        return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").get(id);
    }



}
