package com.vendor.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.vendor.model.Product;
import com.vendor.model.UserData;
import com.vendor.repository.ProductRepository;
import com.vendor.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Override
	public Product saveProduct(Product product) {

		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllproducts() {

		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		if (!ObjectUtils.isEmpty(product)) {
			productRepository.delete(product);
			return true;
		}
		return false;
	}

	@Override
	public Product getProductById(Integer id) {

		Product product = productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		List<Product> products=null;
		if (ObjectUtils.isEmpty(category)) {
			products= productRepository.findByIsActiveTrue();
		} else {
			products= productRepository.findByCategory(category);

		}
		return products;
	}

	
	
	
	//product search
	@Override
	public List<Product> searchProduct(String st) {


		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(st, st);
	}

	
	//Pagination
	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize,String category) {
		
		Pageable pagable= PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct=null;
		
		if (ObjectUtils.isEmpty(category)) {
			pageProduct= productRepository.findByIsActiveTrue(pagable);
		} else {
			pageProduct= productRepository.findByCategory(pagable,category);

		}
		return pageProduct;
	}

	@Override
	public Page<Product> searchProductWithPagination(String st, int pageNo, int pageSize) {
	    Pageable pageable = PageRequest.of(pageNo, pageSize);
	    return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(st, st, pageable);
	}

	@Override
	public List<Product> getProductsByUser(UserData currentUser) {
		 return productRepository.findByStoredBy(currentUser);
	}


}
