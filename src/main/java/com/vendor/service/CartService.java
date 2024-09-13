package com.vendor.service;

import java.util.List;

import com.vendor.model.Cart;

public interface CartService {
	
	public Cart saveCart(Integer productId,Integer userId);
	
	public List<Cart> getCartByUser(Integer userId);

	public Integer getCartCount(Integer userId);

	public void updateQty(String sy, Integer cid);
	
	public void emptyCartByUser(Integer userId);

}
