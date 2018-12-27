package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.myPojo.GoodsPro;
import com.pyg.pojo.TbGoods;
import com.pyg.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goodsPro
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsPro goodsPro) {
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goodsPro.getGoods().setSellerId(sellerId);
        try {
            goodsService.add(goodsPro);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goodsPro
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsPro goodsPro) {
        //校验当前商品是否属于商家
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        GoodsPro goodsPro1 = goodsService.findOne(goodsPro.getGoods().getId());
        //改goodsId       改sellerId
        if (!sellerId.equals(goodsPro1.getGoods().getSellerId()) || !sellerId.equals(goodsPro.getGoods().getSellerId())) {
            return new Result(false, "改你马呢！");
        }
        try {
            goodsService.update(goodsPro);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public GoodsPro findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 修改商品状态
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

}
