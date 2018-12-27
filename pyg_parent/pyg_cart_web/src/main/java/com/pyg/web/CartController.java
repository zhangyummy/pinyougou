package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.myPojo.Cart;
import com.pyg.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);

        //读取cookie中的购物车
        String cartList_str = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if (cartList_str == null || cartList_str.equals("") || cartList_str.equals("null")) {
            cartList_str = "[]"; //不存在设为空集合
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartList_str, Cart.class);

        if (username.equals("anonymousUser")) { //如果未登录
            return cartList_cookie;
        } else { //如果已经登录
            //读取redis中的购物车
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            //如果redis中有购物车
            if (cartList_cookie.size() > 0) {
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
            }
            //清空cookie数据
            CookieUtil.deleteCookie(request, response, "cartList");
            //保存到redis
            cartService.saveCartListToRedis(username, cartList_redis);
            return cartList_redis;
        }

    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")//注解方式解决跨域请求
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //解决跨域请求
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        //response.setHeader("Access-Control-Allow-Credentials", "true");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) { //如果未登录，保存到cookie
                String cartList_str = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartList_str, 3600 * 24, "utf-8");
                System.out.println("cartList  ----->    cookie");
            } else { //如果已经登录，保存到redis
                cartService.saveCartListToRedis(username, cartList);
                System.out.println("redis  ----->    cartList");
            }
            return new Result(true, "添加成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }


}
