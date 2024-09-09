package com.vendor.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vendor.model.Cart;
import com.vendor.model.OrderAddress;
import com.vendor.model.OrderRequest;
import com.vendor.model.ProductOrder;
import com.vendor.repository.CartRepository;
import com.vendor.repository.ProductOrderRepository;
import com.vendor.service.OrderService;
import com.vendor.util.CommonUtil;
import com.vendor.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductOrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private CommonUtil commonUtil;

	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest) throws Exception {
		// Retrieve the list of cart items for the given user ID
		List<Cart> cartItems = cartRepository.findByUserId(userId);

		// Iterate through each cart item and create an order for each product
		for (Cart cart : cartItems) {
			ProductOrder order = new ProductOrder();

			// Generate a unique order ID using UUID
			order.setOrderId(UUID.randomUUID().toString());

			// Set the current date and time as the order date
			order.setOrderDate(new Date());

			// Set the product details from the cart item
			order.setProduct(cart.getProduct());

			// Set the price based on the discounted price of the product
			order.setPrice(cart.getProduct().getDiscountedPrice());

			// Set the quantity of the product ordered
			order.setQuantity(cart.getQuantity());

			// Set the user who placed the order
			order.setUser(cart.getUser());

			// Set the order status as "IN_PROGRESS"
			order.setStatus(OrderStatus.IN_PROGRESS.getName());

			// Set the payment type from the order request
			order.setPaymentType(orderRequest.getPaymentType());

			// Create and set the order address from the order request
			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());

			order.setOrderAddress(address);

			// Save the order to the repository
			ProductOrder saveOrder = orderRepository.save(order);
			commonUtil.sendMailForProductOrder(saveOrder, "Placed");
		}

		// The method currently returns null, indicating no specific order is returned
		// This could be modified to return a specific order or a summary of the orders
		// created

	}

	@Override
	public List<ProductOrder> getOredrByUser(Integer userId) {
		List<ProductOrder> byUserId = orderRepository.findByUserId(userId);
		return byUserId;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
	    Optional<ProductOrder> byId = orderRepository.findById(id);

	    // Early exit if the order does not exist
	    if (!byId.isPresent()) {
	        return null;
	    }

	    // Update the status and save the order
	    ProductOrder order = byId.get();
	    order.setStatus(status);
	    ProductOrder updateOrder = orderRepository.save(order);

	    return updateOrder;
	}

	
	// get all orders
	@Override
	public List<ProductOrder> getAllOrders() {
		
		return orderRepository.findAll();
	}


}
