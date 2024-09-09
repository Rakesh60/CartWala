package com.vendor.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.vendor.model.Category;
import com.vendor.model.Product;
import com.vendor.model.ProductOrder;
import com.vendor.model.UserData;
import com.vendor.service.CategoryService;
import com.vendor.service.OrderService;
import com.vendor.service.ProductService;
import com.vendor.service.UserService;
import com.vendor.util.CommonUtil;
import com.vendor.util.OrderStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommonUtil commonUtil;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {

		if (p != null) {
			String email = p.getName();
			UserData userData = userService.getUserByEmail(email);
			m.addAttribute("user", userData);
		}

	}

	@GetMapping("/")
	public String index() {

		return "admin/index";
	}

	@GetMapping("/category")
	public String category(Model m,Principal principal) {
		 // Retrieve the logged-in user's email (assuming email is the username)
	    String userEmail = principal.getName();
	    
	    // Fetch the current user from the database
	    UserData currentUser = userService.getUserByEmail(userEmail);  // Assuming you have th
		
	    // Fetch only the categories entered by this user
	    List<Category> userCategories = categoryService.getAllActiveCategory();
		
		//m.addAttribute("categories", categoryService.getAllCategory());
		m.addAttribute("categories", userCategories);
		
		return "admin/add-category";
	}

	@PostMapping("/savecategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session,Principal principal) throws IOException {
		

	    // Retrieve the logged-in user from the principal (or session if necessary)
	    //String userEmail = principal.getName();  // Assuming email is used as the username
	    //UserData currentUser = userService.getUserByEmail(userEmail);  // Fetch the current user from your UserService

	    // Set the user who is saving the category
	   // category.setStoredBy(currentUser);
		
		
		
		
		
		
		
		
		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImagename(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());
		if (existCategory) {
			session.setAttribute("errorMsg", "Categry Already exist");
		} else {
			Category savedCategory = categoryService.saveCategory(category);
			if (ObjectUtils.isEmpty(savedCategory)) {
				session.setAttribute("errorMsg", "Category not saved. Internal server error");
			} else {
				// Define the path where you want to save the file
				String uploadDir = "uploads/img/category_img";
				File uploadDirectory = new File(uploadDir);

				// Create directories if they don't exist
				if (!uploadDirectory.exists()) {
					uploadDirectory.mkdirs();
				}

				// Save the file
				Path filePath = Paths.get(uploadDirectory.getAbsolutePath(), file.getOriginalFilename());
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("File saved to: " + filePath);

				session.setAttribute("successMsg", "Category Saved Successfully");
			}
		}

		return "redirect:/admin/category";
	}

	/* Delete Category */

	@GetMapping("/deletecategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {

		System.out.println(id);
		Boolean deleteCategory = categoryService.deleteCategory(id);
		if (deleteCategory) {

			session.setAttribute("successMsg", "Category Deleted Successfully");

		} else {
			session.setAttribute("errorMsg", "Unable to Delete Category");

		}

		return "redirect:/admin/category";
	}

	/* Fetch data for Edit Category */
	@GetMapping("/loadeditcategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {

		m.addAttribute("category", categoryService.getCategoryById(id));
		
		return "admin/edit-category";
	}

	/* Update Category */

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		// Retrieve the old category based on the ID
		Category oldCategory = categoryService.getCategoryById(category.getId());

		// Determine the image name to use (either the old one or the new one if a file
		// was uploaded)
		String imageName = file.isEmpty() ? oldCategory.getImagename() : file.getOriginalFilename();
		category.setImagename(imageName);

		if (!ObjectUtils.isEmpty(oldCategory)) {
			// Update the fields of the old category with the new data
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImagename(imageName);

			// Save the updated category
			Category updatedCategory = categoryService.saveCategory(oldCategory);

			// If a new file was uploaded, save it
			if (!file.isEmpty()) {
				String uploadDir = "uploads/img/category_img";
				File uploadDirectory = new File(uploadDir);

				// Create directories if they don't exist
				if (!uploadDirectory.exists()) {
					uploadDirectory.mkdirs();
				}

				// Save the new image file
				Path filePath = Paths.get(uploadDirectory.getAbsolutePath(), file.getOriginalFilename());
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			}

			if (!ObjectUtils.isEmpty(updatedCategory)) {
				session.setAttribute("successMsg", "Category updated successfully");
			} else {
				session.setAttribute("errorMsg", "Failed to update the category. Please try again.");
			}
		} else {
			session.setAttribute("errorMsg", "Category not found.");
		}

		return "redirect:/admin/loadeditcategory/" + category.getId();
	}

	// Get All categories in Dropdown
	@GetMapping("/loadProducts")
	public String addProducts(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
	
		return "admin/add-products";
	}

	// Save Product through Form
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		// Check if a file is provided
		if (!file.isEmpty()) {
			// Set the file name as the image name
			String imageName = file.getOriginalFilename();
			product.setImageName(imageName); // Assuming you have an imageName field in your Product entity

			// Define the path where you want to save the file
			String uploadDir = "uploads/img/product_img";
			File uploadDirectory = new File(uploadDir);

			// Create directories if they don't exist
			if (!uploadDirectory.exists()) {
				uploadDirectory.mkdirs();
			}

			// Save the file
			Path filePath = Paths.get(uploadDirectory.getAbsolutePath(), file.getOriginalFilename());
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			System.out.println("File saved to: " + filePath);
		} else {
			product.setImageName("default.jpg"); // Handle the case where no file is uploaded
		}

		// DISCOUNT LOGIC while saving

		Double discount = product.getPrice() * (product.getDiscount() / 100.0);
		Double discountedPrice = product.getPrice() - discount;
		System.out.println(product.getDiscount());
		product.setDiscountedPrice(discountedPrice);
		// Save the product
		Product savedProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(savedProduct)) {
			session.setAttribute("successMsg", "Product Added Successfully");
		} else {
			session.setAttribute("errorMsg", "Unable to add product");
		}

		return "redirect:/admin/loadProducts";
	}

	// View All products in Admin page
	@GetMapping("/viewproducts")
	public String viewProducts(Model m) {
		m.addAttribute("products", productService.getAllproducts());
		return "admin/products";
	}

	// deleting a product
	@GetMapping("/deleteproduct/{id}")
	public String deleteproduct(@PathVariable int id, HttpSession session) {
		Boolean deleteproduct = productService.deleteProduct(id);
		if (deleteproduct) {
			session.setAttribute("successMsg", "Product Delete Successfully");
		} else {
			session.setAttribute("errorMsg", "Unable to delete product");

		}
		return "redirect:/admin/viewproducts";
	}

	// load edit page

	@GetMapping("/loadeditproduct/{id}")
	public String loadEditproduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
	

		Product product = productService.getProductById(id);

		// Use reflection to get all the fields of the Product class
		for (Field field : product.getClass().getDeclaredFields()) {
		    field.setAccessible(true);  // Ensure private fields can be accessed
		    try {
		        // Print the field name and its value
		        System.out.println(field.getName() + " = " + field.get(product));
		    } catch (IllegalAccessException e) {
		        e.printStackTrace();
		    }
		}
		m.addAttribute("category", categoryService.getAllCategory());
		return "admin/edit-product";
	}

	// Updating product
	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam MultipartFile file, HttpSession session)
			throws IOException {

		// Retrieve the old product based on the ID
		Product oldProduct = productService.getProductById(product.getId());

		// Determine the image name to use (either the old one or the new one if a file
		// was uploaded)
		String imageName = file.isEmpty() ? oldProduct.getImageName() : file.getOriginalFilename();
		product.setImageName(imageName);

		if (!ObjectUtils.isEmpty(oldProduct)) {
			// Update the fields of the old product with the new data
			oldProduct.setTitle(product.getTitle());
			oldProduct.setDescription(product.getDescription());
			oldProduct.setPrice(product.getPrice());
			oldProduct.setStock(product.getStock());
			oldProduct.setIsActive(product.getIsActive());
			oldProduct.setCategory(product.getCategory());
			oldProduct.setImageName(imageName);

			// Set the discount value on the oldProduct
			oldProduct.setDiscount(product.getDiscount()); // <-- This is important

			// DISCOUNT LOGIC while updating
			Double discount = product.getPrice() * (product.getDiscount() / 100.0);
			Double discountedPrice = product.getPrice() - discount;
			oldProduct.setDiscountedPrice(discountedPrice);

			// Save the updated product
			Product updatedProduct = productService.saveProduct(oldProduct);

			// If a new file was uploaded, save it
			if (!file.isEmpty()) {
				String uploadDir = "uploads/img/product_img";
				File uploadDirectory = new File(uploadDir);

				// Create directories if they don't exist
				if (!uploadDirectory.exists()) {
					uploadDirectory.mkdirs();
				}

				// Save the new image file
				Path filePath = Paths.get(uploadDirectory.getAbsolutePath(), file.getOriginalFilename());
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			}

			if (!ObjectUtils.isEmpty(updatedProduct)) {
				session.setAttribute("successMsg", "Product updated successfully");
			} else {
				session.setAttribute("errorMsg", "Failed to update the product. Please try again.");
			}
		} else {
			session.setAttribute("errorMsg", "Product not found.");
		}

		return "redirect:/admin/viewproducts";
	}

	// user details

	@GetMapping("/getusers")
	public String getAllUsers(Model m) {
		
		List<String> roles = Arrays.asList("ROLE_USER", "ROLE_SELLER");
		List<UserData> users = userService.getUsersByRoles(roles);

		//List<UserData> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users", users);

		return "admin/users";
	}

	@GetMapping("/updatestatus")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {

		Boolean f = userService.updateAccountStatus(status, id);
		if (f) {
			session.setAttribute("successMsg", "Status changed of id:" + id);
		} else {
			session.setAttribute("errorMsg", "Something went wrong");
		}

		return "redirect:/admin/getusers";
	}

	// order related apis
	@GetMapping("/orders")
	public String getAllOrders(Model m) {
		List<ProductOrder> allOrders = orderService.getAllOrders();
		List<OrderStatus> orderStatuses = Arrays.asList(OrderStatus.values());
		m.addAttribute("orderStatuses", orderStatuses);

		m.addAttribute("orders", allOrders);
		return "admin/orders";
	}
	
	// Update the order status 
	
	@PostMapping("/update-order-status")
	public String updateOrderStatus(
	        @RequestParam(required = false) Integer id,
	        @RequestParam Integer st,
	        HttpServletRequest request,
	        HttpSession session) {
	    
	    // Redirect to the same page if id is null
	    if (id == null) {
	        session.setAttribute("errorMsg", "Order ID is missing.");
	        return "redirect:" + request.getRequestURL().toString();
	    }

	    // Find the status name by its id
	    OrderStatus[] values = OrderStatus.values();
	    String status = null;

	    for (OrderStatus ordSt : values) {
	        if (ordSt.getId().equals(st)) {
	            status = ordSt.getName();
	            break;
	        }
	    }
	    ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// Update the order status if the id is valid
		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("successMsg", "Order is Updated :"+status);
		}
		else {
			
				session.setAttribute("errorMsg","Unable Updated :"+ status);
			
		}

	    return "redirect:/admin/orders";
	}

}
