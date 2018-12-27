package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbContent;
import com.pyg.service.ContentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;

    /**
     * 根据广告类别查找广告
     *
     * @param categoryId
     * @return
     */
    @RequestMapping("/findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId) {
        return contentService.findByCategoryId(categoryId);
    }


}
