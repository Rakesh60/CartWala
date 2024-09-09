package com.vendor.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.vendor.model.Product;

public interface ProductService {

	public Product saveProduct(Product product);

	public List<Product> getAllproducts();

	public Boolean deleteProduct(Integer id);

	public Product getProductById(Integer id);

	public List<Product> getAllActiveProducts(String category);
	
	public List<Product> searchProduct(String st);
	
	
	public Page<Product> searchProductWithPagination(String st, int pageNo, int pageSize) ;

	
	
	public Page<Product> getAllActiveProductPagination(Integer pageNo,Integer pageSize,String category);
	
	

}
