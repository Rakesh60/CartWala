package com.vendor.model;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ProductOrder {
	
	 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use appropriate strategy
     Long id;

	 String orderId;

	 Date orderDate;

	@ManyToOne
	 Product product;

	 Double price;

	 Integer quantity;

	@ManyToOne
	 UserData user;

	 String status;

	 String paymentType;
	
	@OneToOne(cascade = CascadeType.ALL)
	 OrderAddress orderAddress;

	

}