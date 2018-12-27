package com.pyg.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 8000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //去掉空格
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));
        Map<String, Object> map = new HashMap<>();
        //高亮查询
        map.putAll(searchHighlightList(searchMap));
        //商品分类查询
        List itemCatList = searchItemCatList(searchMap);
        map.put("itemCatList", itemCatList);
        //查询品牌和规格列表
        if ("".equals(searchMap.get("category"))) {//如果未选择分类
            if (itemCatList.size() > 0) {
                map.putAll(searchBrandAndSpecList((String) itemCatList.get(0)));
            }
        } else {//如果选择分类
            map.putAll(searchBrandAndSpecList((String) searchMap.get("category")));
        }

        return map;
    }

    //高亮查询
    private Map<String, Object> searchHighlightList(Map searchMap) {
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions();
        //添加显示高亮的域
        highlightOptions.addField("item_title");
        //添加前缀后缀
        highlightOptions.setSimplePrefix("<em style='color:red'>").setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        //1.查询条件，关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //2.分类筛选
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //3.品牌筛选
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //4.规格筛选
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //5.价格筛选
        if (!"".equals(searchMap.get("price"))) {
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("-");
            if (!price[0].equals("0")) {
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);//大于等于
                FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);//小于等于
                FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //6.分页查询
        Integer pageNum = (Integer) searchMap.get("pageNum");
        if (pageNum == null) {
            pageNum = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            pageSize = 20;
        }
        query.setOffset((pageNum - 1) * pageSize);//开始索引
        query.setRows(pageSize);//每页记录数

        //7.排序
        String sortField = (String) searchMap.get("sortField");
        String sortValue = (String) searchMap.get("sort");
        if (sortValue != null && !"".equals(sortValue)) {
            if ("ASC".equals(sortValue)) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if ("DESC".equals(sortValue)) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }

        //高亮查询
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮入口集合
        List<HighlightEntry<TbItem>> highlightEntryList = highlightPage.getHighlighted();
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            if (highlightEntry.getHighlights().size() > 0 && highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                //获取原实体类
                TbItem item = highlightEntry.getEntity();
                //            多个高亮的域的集合                     一个域有多列（复制域）
                item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
                //打印高亮
                //System.out.println(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("rows", highlightPage.getContent());
        map.put("totalPage", highlightPage.getTotalPages());//总页数
        map.put("totalCount", highlightPage.getTotalElements());//总记录数
        return map;
    }

    //查询商品分类名称列表
    private List searchItemCatList(Map searchMap) {
        List list = new ArrayList();
        Query query = new SimpleQuery();
        //添加分组选项
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //分组查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //分组结果集
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //分组结果入口页
        Page<GroupEntry<TbItem>> groupEntryPage = groupResult.getGroupEntries();
        //分组结果入口集合
        List<GroupEntry<TbItem>> groupEntryList = groupEntryPage.getContent();
        for (GroupEntry<TbItem> groupEntry : groupEntryList) {
            list.add(groupEntry.getGroupValue());
            //打印分组
            //System.out.println(groupEntry.getGroupValue());
        }
        return list;
    }

    //根据 种类名称 查询品牌和规格列表
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCatList").get(category);
        if (typeId != null) {
            //查询品牌
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            //查询规格
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("brandList", brandList);
            map.put("specList", specList);
            System.out.println("cha xu pin pai / gui ge redis-----------------");
        }
        return map;
    }

    /**
     * 导入数据到索引库
     */
    public void importListToSolr(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 从索引库删除数据
     */
    public void deleteByGoodsId(Long[] goodsIds) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(Arrays.asList(goodsIds));
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

}
