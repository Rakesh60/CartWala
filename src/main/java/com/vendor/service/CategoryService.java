package com.vendor.service;

import java.util.List;

import com.vendor.model.Category;
import com.vendor.model.UserData;


public interface CategoryService {
	
	public Category saveCategory(Category category);
	
	public Boolean existCategory(String name);
	
	public List<Category> getAllCategory();
	
	public Boolean deleteCategory(int id);

	public Category getCategoryById(int id);
	
	public List<Category> getAllActiveCategory();

	public List<Category> getCategoriesByUser(UserData currentUser);

}
