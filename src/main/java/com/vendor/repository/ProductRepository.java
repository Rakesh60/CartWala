package com.vendor.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vendor.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	List<Product> findByIsActiveTrue();

	Page<Product> findByIsActiveTrue(Pageable pagable);
	
	List<Product> findByCategory(String category);

	// Search by title or description and isActive status
	List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String title, String category);

	// Search by title or category with pagination
	Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String title, String category, Pageable pageable);


	Page<Product> findByCategory(Pageable pagable,String category);

	

}
