package com.vendor.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.vendor.model.Cart;
import com.vendor.model.OrderRequest;
import com.vendor.model.ProductOrder;
import com.vendor.model.UserData;
import com.vendor.repository.UserRepository;
import com.vendor.service.CartService;
import com.vendor.service.OrderService;
import com.vendor.service.UserService;
import com.vendor.util.CommonUtil;
import com.vendor.util.OrderStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
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
	public String home() {

		return "user/home";
	}

	@GetMapping("/carts")
	public String cart() {

		return "user/cart";
	}

	@GetMapping("/addtocart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {

		Cart saveCart = cartService.saveCart(pid, uid);

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "unable to add product to your cart");
		} else {
			session.setAttribute("successMsg", "Item Saved to Cart Successfully");
		}
		return "redirect:/product/" + pid;
	}

	private UserData getLoggedInUserData(Principal p) {
		String email = p.getName();
		UserData userByEmail = userService.getUserByEmail(email);
		return userByEmail;
	}

	@GetMapping("/cart")
	public String ShowCart(Principal p, Model m) {
		UserData user = getLoggedInUserData(p);
		List<Cart> cartItems = cartService.getCartByUser(user.getId());

		// Add cart items to the model
		m.addAttribute("cartItems", cartItems);
		if (cartItems.size() > 0) {

			// Calculate the total ordered amount
			Double totalOrderdAmount = cartItems.get(cartItems.size() - 1).getTotalOrderdAmount();
			m.addAttribute("totalOrderdAmount", totalOrderdAmount);

			// Calculate and add the total number of items to the model
			int totalItems = cartItems.size();
			m.addAttribute("totalItems", totalItems);
		} else {
			m.addAttribute("msg", "No items in your cart ");

		}
		return "user/cart";

	}

	@GetMapping("/cartQtyUpdate")
	public String updateCartQty(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQty(sy, cid);
		return "redirect:/user/cart";
	}

	@GetMapping("/order")
	public String orderUi(Principal p, Model m) {
		UserData user = getLoggedInUserData(p);
		List<Cart> cartItems = cartService.getCartByUser(user.getId());

		Double subtotal = 0.0;
		Double deliveryPrice = 0.0;
		Double taxAmount = 0.0;
		Double totalOrderAmount = 0.0;
		Double taxPercent = 0.10;

		if (!cartItems.isEmpty()) {
			// Calculate the subtotal
			subtotal = cartItems.get(cartItems.size() - 1).getTotalOrderdAmount();

			// Add delivery price if subtotal is less than 99
			if (subtotal < 99) {
				deliveryPrice = 49.0;
			}

			// Calculate 10% tax

			taxAmount = subtotal * taxPercent;

			// Calculate the total order amount
			totalOrderAmount = subtotal + deliveryPrice + taxAmount;

			m.addAttribute("subtotal", subtotal);
			m.addAttribute("deliveryPrice", deliveryPrice);
			m.addAttribute("taxAmount", taxAmount);
			m.addAttribute("totalOrderAmount", totalOrderAmount);
			m.addAttribute("taxPercent", taxPercent);
		}

		return "user/order";
	}

	@PostMapping("/saveorder")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) throws Exception {

		UserData user = getLoggedInUserData(p);
		orderService.saveOrder(user.getId(), request);

		return "/user/paymentSuccess";
	}

	@GetMapping("/myorders")
	public String myOrder(Principal p, Model m) {
		try {
			UserData loggedInUserData = getLoggedInUserData(p);
			List<ProductOrder> orders = orderService.getOredrByUser(loggedInUserData.getId());
			m.addAttribute("orders", orders);
			System.out.println(orders);
			return "user/myOrders";
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception for debugging
			return "error"; // Return an error page or a specific error message
		}
	}

	@GetMapping("/updateStatus")
	public String updateOrderStatus(@RequestParam(required = false) Integer id, @RequestParam Integer st,
			HttpServletRequest request, HttpSession session) {
		// Redirect to the same page if id is null
		if (id == null) {
			String currentUrl = request.getRequestURL().toString();
			return "redirect:" + currentUrl;
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
			session.setAttribute("successMsg", status);
		}
		else {
			
				session.setAttribute("errorMsg", status);
			
		}

		return "redirect:/user/myorders";
	}
	
	@GetMapping("/profile")
	public String Profile(Model m,Principal p) {
		
		return "user/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateUserProfile(@ModelAttribute UserData user, @RequestParam("file") MultipartFile file,
	                                HttpSession session) throws IOException {

	    // Retrieve the old user profile based on the ID
	    UserData oldUser = userRepository.findById(user.getId()).orElse(null);

	    if (oldUser == null) {
	        session.setAttribute("errorMsg", "User not found.");
	        return "redirect:/user/profile";
	    }

	    // Determine the image name to use (either the old one or the new one if a file was uploaded)
	    String imageName = file.isEmpty() ? oldUser.getImagename() : file.getOriginalFilename();

	    // Update the fields of the old user with the new data
	    oldUser.setName(user.getName());
	    oldUser.setMobileNumber(user.getMobileNumber());
	    oldUser.setEmail(user.getEmail());
	    oldUser.setAddress(user.getAddress());
	    oldUser.setCity(user.getCity());
	    oldUser.setState(user.getState());
	    oldUser.setPincode(user.getPincode());
	    oldUser.setImagename(imageName);
	    oldUser.setIsEnabled(true);

	    // Save the updated user before handling the image
	    UserData updatedUser = userRepository.save(oldUser);

	    // If a new file was uploaded, delete the old profile image and save the new one
	    if (!file.isEmpty()) {
	        String uploadDir = "uploads/img/profile_img";
	        File uploadDirectory = new File(uploadDir);

	        // Create directories if they don't exist
	        if (!uploadDirectory.exists()) {
	            boolean dirsCreated = uploadDirectory.mkdirs();
	            if (!dirsCreated) {
	                session.setAttribute("errorMsg", "Failed to create upload directory.");
	                return "redirect:/user/profile";
	            }
	        }

	        // Delete the old profile image if it exists and is not the default image
	        if (oldUser.getImagename() != null && !oldUser.getImagename().isEmpty()) {
	            File oldImage = new File(uploadDirectory, oldUser.getImagename());
	            if (oldImage.exists()) {
	                oldImage.delete();
	            }
	        }

	        // Save the new image file
	        try {
	            Path filePath = Paths.get(uploadDirectory.getAbsolutePath(), file.getOriginalFilename());
	            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
	            session.setAttribute("successMsg", "Profile updated successfully, and new image saved.");
	        } catch (IOException e) {
	            e.printStackTrace();
	            session.setAttribute("errorMsg", "Failed to save the new image.");
	            return "redirect:/user/profile";
	        }
	    }

	    return "redirect:/user/profile";
	}

	//Change Password
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,HttpSession session) {
		UserData loggedInUserData = getLoggedInUserData(p);
		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserData.getPassword());
		
		 if (matches) {
			 String encode = passwordEncoder.encode(newPassword);
			 loggedInUserData.setPassword(encode);
			 UserData savedUser = userService.updateUser(loggedInUserData);
			 if (ObjectUtils.isEmpty(savedUser)) {
					session.setAttribute("errorMsg","Password not updated");

			} else {
				session.setAttribute("successMsg","Password saved successfullly");

			}
			
		} else {
			session.setAttribute("errorMsg",currentPassword+" is Incorrect Password");
		}
		
		return "redirect:/user/profile";
	}


	

}
