package com.pyg.service;
import java.util.List;
import java.util.Map;

import com.pyg.myPojo.SpecificationPro;
import com.pyg.pojo.TbSpecification;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SpecificationService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSpecification> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(SpecificationPro specificationPro);
	
	
	/**
	 * 修改
	 */
	public void update(SpecificationPro specificationPro);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public SpecificationPro findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize);

	//查询规格下拉框数据
	List<Map> selectOptionList();
	
}
