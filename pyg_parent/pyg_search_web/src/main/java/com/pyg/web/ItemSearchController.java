package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {

    @Reference
    private ItemSearchService itemSearchService;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @RequestMapping("/search")
    public Map<String, Object> search(@RequestBody Map searchMap) {
        return itemSearchService.search(searchMap);
    }
}
