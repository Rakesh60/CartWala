package com.vendor.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.vendor.model.Category;
import com.vendor.model.Product;
import com.vendor.model.UserData;
import com.vendor.service.CartService;
import com.vendor.service.CategoryService;
import com.vendor.service.ProductService;
import com.vendor.service.UserService;
import com.vendor.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {

		if (p != null) {
			String email = p.getName();
			UserData userData = userService.getUserByEmail(email);
			Integer cartCount = cartService.getCartCount(userData.getId());
			m.addAttribute("user", userData);
			m.addAttribute("cartCount", cartCount);
		}

	}

	@GetMapping("/")
	public String index(Model m) {
		List<Category> categories = categoryService.getAllActiveCategory();
		List<Product> products = productService.getAllproducts();
		m.addAttribute("products", products);
		m.addAttribute("categories", categories);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,@RequestParam(name="pageNo",defaultValue = "0") Integer pageNo,@RequestParam(name="pageSize",defaultValue = "3") Integer pageSize ) {
		
		m.addAttribute("paramValue", category);

		
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);

		//List<Product> products = productService.getAllActiveProducts(category);
		//m.addAttribute("products", products);
		
		Page<Product> pageData = productService.getAllActiveProductPagination(pageNo,pageSize,category);
		List<Product> products = pageData.getContent();
		
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());
		m.addAttribute("pageNo", pageData.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", pageData.getNumberOfElements());
		m.addAttribute("totalPages", pageData.getTotalPages());
		m.addAttribute("isFirst", pageData.isFirst());
		m.addAttribute("isLast", pageData.isLast());
		return "products";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);

		m.addAttribute("product", productById);

		return "view_product";
	}

	/* search Products */

	/*
	 * @GetMapping("/search-product") public String searchProduct(@RequestParam
	 * String st, Model m) {
	 * 
	 * List<Product> products = productService.searchProduct(st);
	 * m.addAttribute("products", products); m.addAttribute("showBackButton", true);
	 * return "products"; }
	 */
	
	
	/* search Products with Pagination */
	@GetMapping("/search-product")
	public String searchProduct(
	        @RequestParam String st,
	        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	        @RequestParam(name = "pageSize", defaultValue = "6") Integer pageSize,
	        Model m) {

	    Page<Product> pageData = productService.searchProductWithPagination(st, pageNo, pageSize);
	    List<Product> products = pageData.getContent();

	    m.addAttribute("products", products);
	    m.addAttribute("showBackButton", true);
	    m.addAttribute("pageNo", pageData.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", pageData.getNumberOfElements());
	    m.addAttribute("totalPages", pageData.getTotalPages());
	    m.addAttribute("isFirst", pageData.isFirst());
	    m.addAttribute("isLast", pageData.isLast());
	    m.addAttribute("searchTerm", st); // To persist the search term
	    return "products";
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@PostMapping("/saveuser")
	public String saveUser(@ModelAttribute UserData user, @RequestParam("file") MultipartFile file, HttpSession session)
			throws IOException {

		// Check if the file is empty or not
		String imageName;
		if (file != null && !file.isEmpty()) {
			imageName = file.getOriginalFilename();
		} else {
			imageName = "default.jpg"; // Use a default image if none is uploaded
		}
		user.setImagename(imageName);
		System.out.println(user);

		// Save the user
		UserData savedUser = userService.saveUser(user);

		if (!ObjectUtils.isEmpty(savedUser)) {
			// Define the path where you want to save the file
			String uploadDir = "uploads/img/profile_img";
			File uploadDirectory = new File(uploadDir);

			// Create directories if they don't exist
			if (!uploadDirectory.exists()) {
				uploadDirectory.mkdirs();
			}

			// Save the file only if a file was uploaded
			if (!file.isEmpty()) {
				Path filePath = Paths.get(uploadDirectory.getAbsolutePath(), file.getOriginalFilename());
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File saved to: " + filePath);
			}

			session.setAttribute("successMsg", user.getRole() + " Registered Successfully");
		} else {
			session.setAttribute("errorMsg", "Registration Failed");
		}

		return "redirect:/register";
	}

	// Forgot Password Logic
	@GetMapping("/forgot")
	public String showforgotPassword() {

		return "forgot-password";
	}

	@PostMapping("/forgot-password")
	public String processforgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {
		UserData userByEmail = userService.getUserByEmail(email);
		// Extract the user's name from UserData
		String userName = userByEmail.getName();
		if (ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg", email + " Email is not Registerd");
		} else {

			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			// http://localhost:8080/reset?token=<generatedtoken>
			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

			Boolean sendMail = commonUtil.sendMail(url, email, userName);

			if (sendMail) {
				session.setAttribute("successMsg", "Reset link sent to " + userByEmail.getEmail());
			} else {
				session.setAttribute("errorMsg", "Failed to send Reset Link");
			}
		}

		return "redirect:/forgot";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, Model m) {
		UserData userByToken = userService.getUserByToken(token);

		if (userByToken == null) {
			m.addAttribute("errorMsg", "Your link is invalid or token expired !!");
			return "reset-error";
		}

		m.addAttribute("token", token);
		return "reset-password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
			Model m) {

		UserData userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("errorMsg", "Your link is invalid or expired !!");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			session.setAttribute("successMsg", "Pasword Changed Successfully");

			return "redirect:/forgot";
		}

	}
}
