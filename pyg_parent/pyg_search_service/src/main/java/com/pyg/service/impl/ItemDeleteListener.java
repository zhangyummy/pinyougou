package com.pyg.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("jie shou dao xiao xi -------------------");
        try {
            ObjectMessage obj = (ObjectMessage) message;
            Long[] goodsIds = (Long[]) obj.getObject();
            itemSearchService.deleteByGoodsId(goodsIds);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("shan chu suo yin ku ji lu -----------");
    }
}
