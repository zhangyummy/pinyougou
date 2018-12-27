package com.pyg.service.impl;

import java.util.List;
import java.util.Map;

import com.pyg.mapper.TbSpecificationOptionMapper;
import com.pyg.myPojo.SpecificationPro;
import com.pyg.pojo.TbSpecificationExample;
import com.pyg.pojo.TbSpecificationOption;
import com.pyg.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSpecificationMapper;
import com.pyg.pojo.TbSpecification;
import com.pyg.service.SpecificationService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(SpecificationPro specificationPro) {
        //添加规格
        TbSpecification specification = specificationPro.getSpecification();
        specificationMapper.insert(specification);
        //添加规格项
        List<TbSpecificationOption> specificationOptionList = specificationPro.getSpecificationOptionList();
        for (TbSpecificationOption specificationOption : specificationOptionList) {
            //设置关联的spec_id值
            specificationOption.setSpecId(specification.getId());
            specificationOptionMapper.insert(specificationOption);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(SpecificationPro specificationPro) {
        //修改规格
        specificationMapper.updateByPrimaryKey(specificationPro.getSpecification());
        //删除规格项
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(specificationPro.getSpecification().getId());
        specificationOptionMapper.deleteByExample(example);
        //添加规格项
        for (TbSpecificationOption specificationOption : specificationPro.getSpecificationOptionList()) {
            specificationOption.setSpecId(specificationPro.getSpecification().getId());
            specificationOptionMapper.insert(specificationOption);
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public SpecificationPro findOne(Long id) {
        //查找规格
        TbSpecification specification = specificationMapper.selectByPrimaryKey(id);
        //查找规格项
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);

        SpecificationPro specificationPro = new SpecificationPro();
        specificationPro.setSpecification(specification);
        specificationPro.setSpecificationOptionList(specificationOptionList);
        return specificationPro;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            specificationMapper.deleteByPrimaryKey(id);
            //删除规格项
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        TbSpecificationExample.Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //查询品牌下拉框数据
    @Override
    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
