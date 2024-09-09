package com.vendor.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.vendor.model.Category;
import com.vendor.model.UserData;
import com.vendor.repository.CategoryRepository;
import com.vendor.service.CategoryService;

@Service
public class CategoryServiceImp implements CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Override
	public Category saveCategory(Category category) {
		
		return categoryRepository.save(category);
	}

	@Override
	public List<Category> getAllCategory() {
		
		return categoryRepository.findAll();
	}

	@Override
	public Boolean existCategory(String name) {
		
		return categoryRepository.existsByName(name);
	}

	@Override
	public Boolean deleteCategory(int id) {
	
		Category category=categoryRepository.findById(id).orElse(null);
		
		if (!ObjectUtils.isEmpty(category)) {
			
			categoryRepository.delete(category);
			return true;
		}
		
		
		return false;
	}

	@Override
	public Category getCategoryById(int id) {

	Category category=categoryRepository.findById(id).orElse(null);
	
		return category;
	}

	@Override
	public List<Category> getAllActiveCategory() {
	List<Category> categories=	categoryRepository.findByIsActiveTrue();
		return categories;
	}

	@Override
	public List<Category> getCategoriesByUser(UserData currentUser) {
		
		return categoryRepository.findByStoredBy(currentUser);
	}

}
