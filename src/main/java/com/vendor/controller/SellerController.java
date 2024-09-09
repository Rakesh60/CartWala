package com.vendor.controller;

import java.io.File;
import java.io.IOException;
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
@RequestMapping("/seller")
public class SellerController {

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
        return "seller/index";
    }
    
	@GetMapping("/getusers")
	public String getAllUsers(Model m) {

		List<UserData> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users", users);

		return "seller/users";
	}

    @GetMapping("/category")
    public String category(Model m, Principal principal) {
        // Retrieve the logged-in user's email (assuming email is the username)
        String userEmail = principal.getName();
        
        // Fetch the current user from the database
        UserData currentUser = userService.getUserByEmail(userEmail);
        
        // Fetch only the categories entered by this user
        List<Category> userCategories = categoryService.getCategoriesByUser(currentUser);
        
        m.addAttribute("categories", userCategories);
        
        return "seller/add-category";
    }

    @PostMapping("/savecategory")
    public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
            HttpSession session, Principal principal) throws IOException {

        // Retrieve the logged-in user from the principal (or session if necessary)
        String userEmail = principal.getName();
        UserData currentUser = userService.getUserByEmail(userEmail);

        // Set the user who is saving the category
        category.setStoredBy(currentUser);

        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
        category.setImagename(imageName);

        Boolean existCategory = categoryService.existCategory(category.getName());
        if (existCategory) {
            session.setAttribute("errorMsg", "Category Already exists");
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

        return "redirect:/seller/category";
    }

    /* Delete Category by user */

    @GetMapping("/deletecategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session, Principal principal) {
        // Retrieve the logged-in user's email
        String userEmail = principal.getName();
        
        // Fetch the current user from the database
        UserData currentUser = userService.getUserByEmail(userEmail);

        // Check if the category belongs to the current user before attempting to delete
        Category category = categoryService.getCategoryById(id);
        
        if (category == null) {
            session.setAttribute("errorMsg", "Category not found.");
            return "redirect:/seller/category";
        }

        // Ensure the category was stored by the current user
        if (!category.getStoredBy().getId().equals(currentUser.getId())) {
            session.setAttribute("errorMsg", "You are not authorized to delete this category.");
            return "redirect:/seller/category";
        }

        // Attempt to delete the category
        Boolean deleteCategory = categoryService.deleteCategory(id);
        if (deleteCategory) {
            session.setAttribute("successMsg", "Category Deleted Successfully");
        } else {
            session.setAttribute("errorMsg", "Unable to Delete Category");
        }

        return "redirect:/seller/category";
    }


    /* Fetch data for Edit Category */
    @GetMapping("/loadeditcategory/{id}")
    public String loadEditCategory(@PathVariable int id, Model m) {
        m.addAttribute("category", categoryService.getCategoryById(id));
        return "seller/edit-category";
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

        return "redirect:/seller/loadeditcategory/" + category.getId();
    }

    // Get All categories in Dropdown
    @GetMapping("/loadProducts")
    public String addProducts(Model m) {
        List<Category> categories = categoryService.getAllCategory();
        m.addAttribute("categories", categories);
        return "seller/add-products";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
            HttpSession session, Principal principal) throws IOException {

        // Retrieve the logged-in user's email
        String userEmail = principal.getName();
        
        // Fetch the current user from the database
        UserData currentUser = userService.getUserByEmail(userEmail);

        // Set the user who is adding the product
        product.setStoredBy(currentUser);
        // Check if a file is provided
        if (!file.isEmpty()) {
            // Set the file name as the image name
            String imageName = file.getOriginalFilename();
            product.setImageName(imageName);

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
            product.setImageName("default.jpg");
        }

        // DISCOUNT LOGIC while saving
        Double discount = product.getPrice() * (product.getDiscount() / 100.0);
        Double discountedPrice = product.getPrice() - discount;
        product.setDiscountedPrice(discountedPrice);

        // Save the product
        Product savedProduct = productService.saveProduct(product);

        if (!ObjectUtils.isEmpty(savedProduct)) {
            session.setAttribute("successMsg", "Product Added Successfully");
        } else {
            session.setAttribute("errorMsg", "Unable to add product");
        }

        return "redirect:/seller/loadProducts";
    }

    @GetMapping("/viewproducts")
    public String viewProducts(Model m, Principal principal) {
        // Retrieve the logged-in user's email
        String userEmail = principal.getName();
        
        // Fetch the current user from the database
        UserData currentUser = userService.getUserByEmail(userEmail);
        
        // Fetch only the products added by this user (seller)
        List<Product> userProducts = productService.getProductsByUser(currentUser);
        
        // Add the user's products to the model
        m.addAttribute("products", userProducts);
        
        return "seller/products";
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
        return "redirect:/seller/viewproducts";
    }

    // load edit page
    @GetMapping("/loadeditproduct/{id}")
    public String loadEditproduct(@PathVariable int id, Model m) {
        m.addAttribute("product", productService.getProductById(id));
        m.addAttribute("category", categoryService.getAllCategory());
        return "seller/edit-product";
    }

    // Updating product
    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
            HttpSession session) throws IOException {

        // Retrieve the old product based on the ID
        Product oldProduct = productService.getProductById(product.getId());

        if (!ObjectUtils.isEmpty(oldProduct)) {
            // Update the fields of the old product with the new data
            oldProduct.setTitle(product.getTitle());
            oldProduct.setDescription(product.getDescription());
           
            oldProduct.setPrice(product.getPrice());
            oldProduct.setDiscount(product.getDiscount());
            oldProduct.setStock(product.getStock());
            oldProduct.setIsActive(product.getIsActive());
            oldProduct.setCategory(product.getCategory());
            

            // Update the image name if a new file was uploaded
            if (!file.isEmpty()) {
                String imageName = file.getOriginalFilename();
                oldProduct.setImageName(imageName);

                // Save the new image file
                String uploadDir = "uploads/img/product_img";
                File uploadDirectory = new File(uploadDir);

                // Create directories if they don't exist
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdirs();
                }

                Path filePath = Paths.get(uploadDirectory.getAbsolutePath(), file.getOriginalFilename());
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // DISCOUNT LOGIC while updating
            Double discount = oldProduct.getPrice() * (oldProduct.getDiscount() / 100.0);
            Double discountedPrice = oldProduct.getPrice() - discount;
            oldProduct.setDiscountedPrice(discountedPrice);

            // Save the updated product
            Product updatedProduct = productService.saveProduct(oldProduct);

            if (!ObjectUtils.isEmpty(updatedProduct)) {
                session.setAttribute("successMsg", "Product updated successfully");
            } else {
                session.setAttribute("errorMsg", "Failed to update the product. Please try again.");
            }
        } else {
            session.setAttribute("errorMsg", "Product not found.");
        }

        return "redirect:/seller/loadeditproduct/" + product.getId();
    }

	/*
	 * // Order History
	 * 
	 * @GetMapping("/orderHistory") public String orderHistory(Model m, Principal
	 * principal) { String email = principal.getName(); UserData user =
	 * userService.getUserByEmail(email);
	 * 
	 * List<ProductOrder> orderList = orderService.getOrdersBySeller(user);
	 * m.addAttribute("orders", orderList); m.addAttribute("statuses",
	 * Arrays.asList(OrderStatus.values()));
	 * 
	 * return "seller/order-history"; }
	 * 
	 * // Update Order Status
	 * 
	 * @GetMapping("/updateOrderStatus/{orderId}/{status}") public String
	 * updateOrderStatus(@PathVariable Long orderId, @PathVariable int status,
	 * HttpSession session) { boolean isUpdated =
	 * orderService.updateOrderStatus(orderId, status);
	 * 
	 * if (isUpdated) { session.setAttribute("successMsg",
	 * "Order status updated successfully"); } else {
	 * session.setAttribute("errorMsg", "Failed to update order status"); }
	 * 
	 * return "redirect:/seller/orderHistory"; }
	 */

}
