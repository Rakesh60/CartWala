package com.vendor.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.vendor.model.Cart;
import com.vendor.model.Product;
import com.vendor.model.UserData;
import com.vendor.repository.CartRepository;
import com.vendor.repository.ProductRepository;
import com.vendor.repository.UserRepository;
import com.vendor.service.CartService;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public Cart saveCart(Integer productId, Integer userId) {

		UserData userData = userRepository.findById(userId).get();
		Product product = productRepository.findById(productId).get();

		Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

		Cart cart = null;

		if (ObjectUtils.isEmpty(cartStatus)) {
			cart = new Cart();
			cart.setUser(userData);
			cart.setProduct(product);
			cart.setQuantity(1);
			cart.setTotalPrice(1 * product.getDiscountedPrice());
		} else {
			cart = cartStatus;
			cart.setQuantity(cart.getQuantity() + 1);
			cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountedPrice());
		}

		Cart saveCart = cartRepository.save(cart);

		return saveCart;
	}

	@Override
	public List<Cart> getCartByUser(Integer userId) {
		List<Cart> cartItems = cartRepository.findByUserId(userId);

		Double totalOrderdAmount = 0.0;
		List<Cart> updatedCart = new ArrayList<>();

		for (Cart c : cartItems) {
			// Calculate total price based on discounted price and quantity
			Double totalPrice = c.getProduct().getDiscountedPrice() * c.getQuantity();
			c.setTotalPrice(totalPrice);
			// Optionally accumulate the total ordered amount
			totalOrderdAmount += totalPrice;
			c.setTotalOrderdAmount(totalOrderdAmount);
			// Add the updated cart item to the list
			updatedCart.add(c);

		}

		// Return the updated list of cart items
		return updatedCart;
	}

	@Override
	public Integer getCartCount(Integer userId) {
		Integer countByUserId = cartRepository.countByUserId(userId);
		return countByUserId;
	}

	@Override
	public void updateQty(String sy, Integer cid) {

		// Find the cart item by ID, or throw an exception if not found
		Cart cart = cartRepository.findById(cid).orElseThrow(() -> new RuntimeException("Cart not found"));

		if (sy.equalsIgnoreCase("del")) {
			// Delete the item if "del" is passed
			cartRepository.deleteById(cid);
			return;
		}

		// Get the current quantity of the cart item
		int updatedQty = cart.getQuantity();

		// Handle the increase or decrease of quantity
		if (sy.equalsIgnoreCase("mi")) {
			updatedQty -= 1;

			// If the updated quantity is less than or equal to 0, delete the item from the
			// cart
			if (updatedQty <= 0) {
				cartRepository.deleteById(cid);
				return;
			}
		} else {
			updatedQty += 1;
		}

		// Update the quantity and save the cart item
		cart.setQuantity(updatedQty);
		cartRepository.save(cart);
	}

	@Override
	@Transactional
	public void emptyCartByUser(Integer userId) {
		cartRepository.deleteByUserId(userId);
	}

}
