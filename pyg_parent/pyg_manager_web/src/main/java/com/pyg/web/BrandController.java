package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbBrand;
import com.pyg.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    //从远程获取接口的服务实现
    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(int pageNum, int pageSize) {
        return brandService.findPage(pageNum, pageSize);
    }

    //保存，业务层判断是更新还是添加
    @RequestMapping("/save")
    public Result save(@RequestBody TbBrand brand) {
        try {
            brandService.save(brand);
            return new Result(true, "保存成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败！");
        }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id) {
        return brandService.findOne(id);
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败！");
        }
    }

    //搜索+分页
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand, int pageNum, int pageSize) {
        return brandService.findPage(brand, pageNum, pageSize);
    }

    //查询品牌下拉框数据
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }

}
