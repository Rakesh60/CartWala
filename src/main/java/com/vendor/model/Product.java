package com.vendor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@AllArgsConstructor @NoArgsConstructor
@Getter
@Setter
@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 int id;

	@Column(length = 500)
	 String title;

	@Column(length = 5000)
	 String description;

	 String category;

	 Double price;
	
	 int discount;
	
	 Double discountedPrice;
	
	 Boolean isActive;

	
	 int stock;

	 String imageName;
	
	 // Track the user who added/stored the category
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
     UserData storedBy;
}
