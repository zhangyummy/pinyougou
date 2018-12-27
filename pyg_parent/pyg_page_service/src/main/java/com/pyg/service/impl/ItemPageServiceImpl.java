package com.pyg.service.impl;

import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.*;
import com.pyg.service.ItemPageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pageDir}")
    private String pageDir;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 生成商品详细页
     */
    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        try {
            Template template = configuration.getTemplate("item.ftl");
            Map dataModel = new HashMap();
            //1.商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            //2.商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //3.商品分类
            TbItemCat itemCat_1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id());
            TbItemCat itemCat_2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id());
            TbItemCat itemCat_3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
            dataModel.put("itemCat_1", itemCat_1.getName());
            dataModel.put("itemCat_2", itemCat_2.getName());
            dataModel.put("itemCat_3", itemCat_3.getName());
            //4.SKU列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");//启用的
            example.setOrderByClause("is_default desc");//默认商品降序排列，第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);
            //输出
            Writer out = new FileWriter(pageDir + goodsId + ".html");
            template.process(dataModel, out);
            out.close();
            System.out.println("sheng cheng ye mian ------------------" + goodsId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File(pageDir + goodsId + ".html").delete();
            }
            System.out.println("shan chu ye mian -------------");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
