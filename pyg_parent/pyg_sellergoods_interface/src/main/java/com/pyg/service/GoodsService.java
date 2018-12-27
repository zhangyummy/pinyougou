package com.pyg.service;
import java.util.List;

import com.pyg.myPojo.GoodsPro;
import com.pyg.pojo.TbGoods;

import com.pyg.pojo.TbItem;
import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();


	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(GoodsPro goodsPro);


	/**
	 * 修改
	 */
	public void update(GoodsPro goodsPro);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public GoodsPro findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

    /**
     * 批量删除（假删除）
     * @param ids
     */
    public void delete_1(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	/**
	 * 修改商品状态
	 */
    public void updateStatus(Long[] ids, String status);

	/**
	 * 根据SPU的goodId和status查找SKU集合
	 */
	public List<TbItem> findByGoodsIdAndStatus(Long[] goodsIds, String status);
}
