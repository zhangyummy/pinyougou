package com.pyg.service;

import com.pyg.pojo.TbSeller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {
        TbSeller seller = sellerService.findOne(sellerId);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //不为空且状态为1才能登录
        if (seller != null && seller.getStatus().equals("1")) {
            return new User(seller.getSellerId(), seller.getPassword(), authorities);
        } else {
            return null;
        }
    }
}
