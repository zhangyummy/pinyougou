package com.pyg.web;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pyg.myPojo.GoodsPro;
import com.pyg.pojo.TbItem;
import com.pyg.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbGoods;

import entity.PageResult;
import entity.Result;

import javax.jms.*;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    //@Reference
    //private ItemSearchService itemSearchService;

    //@Reference
    //private ItemPageService itemPageService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination queueSolrDestination;//用于发送 solr 导入的消息

    @Autowired
    private Destination queueSolrDeleteDestination;//用户在索引库中删除记录

    @Autowired
    private Destination topicPageDestination;//生成页面和删除页面消息

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }


    /**
     * 修改
     *
     * @param goodsPro
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsPro goodsPro) {
        try {
            goodsService.update(goodsPro);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public GoodsPro findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除（假删除）
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete_1(ids);
            //从索引库删除
            //itemSearchService.deleteByGoodsId(ids);
            //发送solr删除记录的文本消息
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            //发送删除页面的消息
            jmsTemplate.send(topicPageDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage(ids);
                    objectMessage.setJMSType("DELETE");
                    return objectMessage;
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 修改商品状态
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            //审核通过，更新索引库
            if ("2".equals(status)) {
                List<TbItem> itemList = goodsService.findByGoodsIdAndStatus(ids, "1");
                if (itemList.size() > 0) {
                    //itemSearchService.importListToSolr(itemList);
                    //发送导入索引库的文本消息
                    String jsonString = JSON.toJSONString(itemList);
                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            System.out.println("fa song shu ju -----------");
                            return session.createTextMessage(jsonString);
                        }
                    });
                }
                //静态页生成
                for (Long id : ids) {
                    //itemPageService.genItemHtml(id);
                    jmsTemplate.send(topicPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            ObjectMessage objectMessage = session.createObjectMessage(id);
                            objectMessage.setJMSType("CREATE");
                            return objectMessage;
                        }
                    });
                }
            }
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 生成商品详情页
     */
    /*@RequestMapping("/genHtml")
    public void updateStatus(Long goodsId) {
        if (itemPageService.genItemHtml(goodsId)) {
            System.out.println("ye mian cheng gong -------------");
        } else {
            System.out.println("shi bai --------------");
        }
    }*/
}
