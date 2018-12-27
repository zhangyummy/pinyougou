package com.pyg.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pyg.mapper.TbSpecificationOptionMapper;
import com.pyg.pojo.TbSpecificationOption;
import com.pyg.pojo.TbSpecificationOptionExample;
import com.pyg.pojo.TbTypeTemplateExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbTypeTemplateMapper;
import com.pyg.pojo.TbTypeTemplate;
import com.pyg.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 执行此方法时，存品牌/规格到redis
     *
     * @param typeTemplate
     * @param pageNum      当前页 码
     * @param pageSize     每页记录数
     * @return
     */
    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        //存品牌/规格到redis
        saveToRedis();

        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        TbTypeTemplateExample.Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //存品牌/规格到redis
    private void saveToRedis() {
        List<TbTypeTemplate> typeTemplateList = findAll();
        for (TbTypeTemplate typeTemplate : typeTemplateList) {
            TbTypeTemplate typeTemplatePro = findOnePro(typeTemplate.getId());
            //保存品牌
            String brandIds = typeTemplatePro.getBrandIds();
            List<Map> brandList = JSON.parseArray(brandIds, Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);
            //保存规格
            String specIds = typeTemplatePro.getSpecIds();
            List<Map> specList = JSON.parseArray(specIds, Map.class);
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }
        System.out.println("jiang pin pai / gui ge cun ru cache ------------------");
    }


    @Override
    public TbTypeTemplate findOnePro(Long id) {
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds();//[{"id":34,"text":"分辨率"},{"id":38,"text":"CPU"}]
        List<Map> list = JSON.parseArray(specIds, Map.class);
        //遍历，添加specOption
        for (Map map : list) {
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(new Long((Integer) map.get("id")));
            List<TbSpecificationOption> specOptionList = specificationOptionMapper.selectByExample(example);
            map.put("specOptionList", specOptionList);//[{"id":34,"text":"分辨率","specOptionList":...},{"id":38,"text":"CPU","specOptionList":...}]
        }
        specIds = JSON.toJSONString(list);
        typeTemplate.setSpecIds(specIds);
        return typeTemplate;
    }

}
