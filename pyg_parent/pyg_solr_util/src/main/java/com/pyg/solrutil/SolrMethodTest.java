package com.pyg.solrutil;

import com.pyg.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-solr.xml")
public class SolrMethodTest {

    @Autowired
    private SolrTemplate solrTemplate;

    //条件+分页
    @Test
    public void testPageQuery() {
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_title").contains("2");
        //criteria = criteria.and("item_title").contains("三星");
        query.addCriteria(criteria);
        query.setOffset(0);//开始索引（默认0）
        query.setRows(100);//每页记录数（默认10）
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数：" + scoredPage.getTotalElements());
        List<TbItem> itemList = scoredPage.getContent();
        for (TbItem item : itemList) {
            System.out.println(item.getTitle() + "   " + item.getPrice());
        }
    }

    //删除全部数据
    @Test
    public void testDeleteAll() {
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
