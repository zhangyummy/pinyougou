package com.pyg.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     */
    public Map<String, Object> search(Map searchMap);

    /**
     * 导入数据到索引库
     */
    public void importListToSolr(List list);

    /**
     * 从索引库删除数据
     */
    public void deleteByGoodsId(Long[] goodsIds);
}
