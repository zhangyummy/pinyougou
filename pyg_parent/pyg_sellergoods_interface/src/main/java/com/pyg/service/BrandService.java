package com.pyg.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    List<TbBrand> findAll();

    //分页查询
    PageResult findPage(int pageNum, int pageSize);

    void save(TbBrand brand);

    TbBrand findOne(Long id);

    //批量删除
    void delete(Long[] ids);

    //品牌条件查询
    PageResult findPage(TbBrand brand, int pageNum, int pageSize);

    //查询品牌下拉框数据
    List<Map> selectOptionList();

}
