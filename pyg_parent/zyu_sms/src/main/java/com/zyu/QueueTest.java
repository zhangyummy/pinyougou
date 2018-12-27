package com.zyu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class QueueTest {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @RequestMapping("/sendsms")
    public void sendSms() {
        Map map = new HashMap<>();
        map.put("mobile", "13207198468");
        map.put("template_code", "SMS_123737132");
        map.put("sign_name", "品优taotao");
        map.put("param", "{\"number\":\"888888\"}");
        jmsMessagingTemplate.convertAndSend("sms", map);
    }
}
