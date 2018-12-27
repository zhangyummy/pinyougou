package com.pyg.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.mapper.TbItemMapper;
import com.pyg.myPojo.Cart;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import com.pyg.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据itemId查询SKU
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品状态异常");
        }
        //2.根据SKU获取商家id
        String sellerId = item.getSellerId();
        //3.根据商家id查询购物车列表
        Cart cart = findCartBySellerId(cartList, sellerId);

        if (cart == null) { //4.如果商家购物车不存在
            //4.1 创建新cart
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 添加到cartList中
            cartList.add(cart);

        } else {//5.如果商家购物车存在
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //  判断SKU是否存在
            TbOrderItem orderItem = findOrderItemByItemId(orderItemList, itemId);

            if (orderItem == null) { //5.1 如果SKU不存在，创建新的购物车明细项
                orderItem = createOrderItem(item, num);
                orderItemList.add(orderItem);
            } else { //5.2 如果SKU存在，增加商品数量 和 总金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum()));
                //如果购物车明细项数量为0，移除此项
                if (orderItem.getNum() == 0) {
                    orderItemList.remove(orderItem);
                }
                //如果商家购物车的购物车明细列表长度为0，移除此购物车
                if (orderItemList.size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 从redis中查询购物车列表
     *
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("redis  ----->    cartList");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 将购物车列表存入redis
     *
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("cartList  ----->    redis");
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * 合并购物车
     *
     * @param cartList1
     * @param cartList2
     */
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("merge cart  --------------");
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家id在购物车列表中查询商家购物车
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart findCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据itemId在购物车明细列表中查询明细项
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车明细项
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }
}
