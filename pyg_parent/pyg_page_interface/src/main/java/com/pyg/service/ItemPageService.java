package com.pyg.service;

public interface ItemPageService {

    /**
     * 生成商品详细页
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详情页
     *
     * @param goodsIds
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
