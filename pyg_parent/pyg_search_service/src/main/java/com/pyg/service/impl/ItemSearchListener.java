package com.pyg.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("jie shou dao xiao xi -------------------");
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            for (TbItem item : itemList) {
                //spec字段字符串转为map集合
                Map specMap = JSON.parseObject(item.getSpec(), Map.class);
                item.setSpecMap(specMap);
            }
            itemSearchService.importListToSolr(itemList);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("dao ru suo yin ku -----------");
    }
}
