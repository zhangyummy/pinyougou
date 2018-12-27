package com.pyg.service.impl;

import com.pyg.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        System.out.println("ye mian jian ting dao xiao xi ------------");
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            if ("CREATE".equals(objectMessage.getJMSType())) {
                Long goodsId = (Long) objectMessage.getObject();
                itemPageService.genItemHtml(goodsId);
            }
            if ("DELETE".equals(objectMessage.getJMSType())) {
                Long[] ids = (Long[]) objectMessage.getObject();
                itemPageService.deleteItemHtml(ids);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
