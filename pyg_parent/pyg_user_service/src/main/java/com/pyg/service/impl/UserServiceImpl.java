package com.pyg.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbUserExample;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbUserMapper;
import com.pyg.pojo.TbUser;
import com.pyg.service.UserService;

import entity.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination smsDestination;

    @Value("${template_code}")
    private String template_code;

    @Value("${sign_name}")
    private String sign_name;

    /**
     * 查询全部
     */
    @Override
    public List<TbUser> findAll() {
        return userMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbUser user) {
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));
        user.setCreated(new Date());
        user.setUpdated(new Date());
        user.setSourceType("1");//用户来源
        userMapper.insert(user);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbUser user) {
        userMapper.updateByPrimaryKey(user);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbUser findOne(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            userMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbUser user, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();

        if (user != null) {
            if (user.getUsername() != null && user.getUsername().length() > 0) {
                criteria.andUsernameLike("%" + user.getUsername() + "%");
            }
            if (user.getPassword() != null && user.getPassword().length() > 0) {
                criteria.andPasswordLike("%" + user.getPassword() + "%");
            }
            if (user.getPhone() != null && user.getPhone().length() > 0) {
                criteria.andPhoneLike("%" + user.getPhone() + "%");
            }
            if (user.getEmail() != null && user.getEmail().length() > 0) {
                criteria.andEmailLike("%" + user.getEmail() + "%");
            }
            if (user.getSourceType() != null && user.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + user.getSourceType() + "%");
            }
            if (user.getNickName() != null && user.getNickName().length() > 0) {
                criteria.andNickNameLike("%" + user.getNickName() + "%");
            }
            if (user.getName() != null && user.getName().length() > 0) {
                criteria.andNameLike("%" + user.getName() + "%");
            }
            if (user.getStatus() != null && user.getStatus().length() > 0) {
                criteria.andStatusLike("%" + user.getStatus() + "%");
            }
            if (user.getHeadPic() != null && user.getHeadPic().length() > 0) {
                criteria.andHeadPicLike("%" + user.getHeadPic() + "%");
            }
            if (user.getQq() != null && user.getQq().length() > 0) {
                criteria.andQqLike("%" + user.getQq() + "%");
            }
            if (user.getIsMobileCheck() != null && user.getIsMobileCheck().length() > 0) {
                criteria.andIsMobileCheckLike("%" + user.getIsMobileCheck() + "%");
            }
            if (user.getIsEmailCheck() != null && user.getIsEmailCheck().length() > 0) {
                criteria.andIsEmailCheckLike("%" + user.getIsEmailCheck() + "%");
            }
            if (user.getSex() != null && user.getSex().length() > 0) {
                criteria.andSexLike("%" + user.getSex() + "%");
            }

        }

        Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 生成短信验证码
     */
    @Override
    public void createSmsCode(String phone) {
        //生成6位验证码
        String smsCode = "";
        while (smsCode.length() < 6) {
            smsCode = (long) (Math.random() * 1000000) + "";
        }
        final String smsCode1 = smsCode;
        System.out.println("yan zheng ma : " + smsCode1);
        //存入缓存
        redisTemplate.boundHashOps("smsCode").put(phone, smsCode1);
        //发送到activeMQ
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", phone);
                mapMessage.setString("template_code", template_code);
                mapMessage.setString("sign_name", sign_name);
                Map map = new HashMap();
                map.put("number", smsCode1);
                mapMessage.setString("param", JSON.toJSONString(map));
                return mapMessage;
            }
        });
    }

    /**
     * 判断验证码是否正确
     */
    @Override
    public boolean checkSmsCode(String phone, String code) {
        String smsCode = (String) redisTemplate.boundHashOps("smsCode").get(phone);
        if (smsCode == null || !smsCode.equals(code)) {
            return false;
        }
        return true;
    }
}
