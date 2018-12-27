package com.pyg.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pyg.mapper.*;
import com.pyg.myPojo.GoodsPro;
import com.pyg.pojo.*;
import com.pyg.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(GoodsPro goodsPro) {
        TbGoods goods = goodsPro.getGoods();
        TbGoodsDesc goodsDesc = goodsPro.getGoodsDesc();
        List<TbItem> itemList = goodsPro.getItemList();
        goods.setAuditStatus("0");//0,未审核
        goodsMapper.insert(goods);
        //设置goodsDesc的goodId
        goodsDesc.setGoodsId(goods.getId());
        goodsDescMapper.insert(goodsDesc);
        //保存itemList数据
        saveItemList(itemList, goods, goodsDesc);
    }

    //保存itemList数据
    private void saveItemList(List<TbItem> itemList, TbGoods goods, TbGoodsDesc goodsDesc) {
        //判断是否启用规则
        if ("1".equals(goods.getIsEnableSpec())) {
            //q启用规则
            for (TbItem item : itemList) {
                String title = goods.getGoodsName();
                //"spec": { "网络": "移动3G", "机身内存": "32G" }
                Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                for (String str : map.keySet()) {
                    title += " " + map.get(str);
                }
                item.setTitle(title);//标题
                setItemValue(item, goods, goodsDesc);
                itemMapper.insert(item);
            }
        } else {
            TbItem item = new TbItem();
            item.setTitle(goods.getGoodsName());//标题
            item.setSpec("{}");//规格
            item.setPrice(goods.getPrice());//价格
            item.setNum(9999);//库存
            item.setStatus("0");//状态
            item.setIsDefault("1");
            setItemValue(item, goods, goodsDesc);
            itemMapper.insert(item);
        }
    }

    //设置item的值
    private void setItemValue(TbItem item, TbGoods goods, TbGoodsDesc goodsDesc) {
        //商品图片[{"color":"白色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVnGZfWAaX2hAAjlKdWCzvg173.jpg"}]
        List<Map> list = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
        if (list != null && list.size() > 0) {
            item.setImage((String) list.get(0).get("url"));
        }
        item.setCategoryid(goods.getCategory3Id());//所属分类
        item.setCreateTime(new Date());//创建时间
        item.setUpdateTime(new Date());//更新时间
        item.setGoodsId(goods.getId());//goodsId
        item.setSellerId(goods.getSellerId());//sellerId
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
        item.setCategory(itemCat.getName());
        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getBrandId());
        item.setBrand(brand.getName());
        //店铺名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getSellerId());
        item.setSeller(seller.getNickName());
    }


    /**
     * 修改
     */
    @Override
    public void update(GoodsPro goodsPro) {
        TbGoods goods = goodsPro.getGoods();
        TbGoodsDesc goodsDesc = goodsPro.getGoodsDesc();
        List<TbItem> itemList = goodsPro.getItemList();
        goods.setAuditStatus("0");//0,未审核  修改后设置状态为未审核
        goodsMapper.updateByPrimaryKey(goods);
        goodsDescMapper.updateByPrimaryKey(goodsDesc);
        //先删除再修改
        TbItemExample example = new TbItemExample();
        example.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemMapper.deleteByExample(example);
        //保存itemList数据
        saveItemList(itemList, goods, goodsDesc);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public GoodsPro findOne(Long id) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(id);
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        TbItemExample example = new TbItemExample();
        example.createCriteria().andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(example);
        GoodsPro goodsPro = new GoodsPro();
        goodsPro.setGoods(goods);
        goodsPro.setGoodsDesc(goodsDesc);
        goodsPro.setItemList(itemList);
        return goodsPro;
    }

    /**
     * 批量删除（真删）
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
            goodsDescMapper.deleteByPrimaryKey(id);
            TbItemExample example = new TbItemExample();
            example.createCriteria().andGoodsIdEqualTo(id);
            itemMapper.deleteByExample(example);
        }
    }

    /**
     * 批量删除（假删除 isDelete=1）
     */
    @Override
    public void delete_1(Long[] ids) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        TbGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andIsDeleteIsNull();//isDelete不为空才显示

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 修改商品状态
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

    /**
     * 根据SPU的goodId和status查找SKU集合
     */
    public List<TbItem> findByGoodsIdAndStatus(Long[] goodsIds, String status) {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        criteria.andStatusEqualTo(status);
        return itemMapper.selectByExample(example);
    }

}
