package com.vendor.service;

import java.util.List;

import com.vendor.model.OrderRequest;
import com.vendor.model.ProductOrder;

public interface OrderService {
	
	public void saveOrder(Integer userId,OrderRequest orderRequest) throws Exception;

	public List<ProductOrder> getOredrByUser(Integer userId);
	
	public ProductOrder updateOrderStatus(Integer id,String status);
	
	
	public List<ProductOrder> getAllOrders();
}
