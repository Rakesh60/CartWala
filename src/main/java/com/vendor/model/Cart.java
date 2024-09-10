package com.vendor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 Integer id;

	@ManyToOne
	 UserData user;

	@ManyToOne
	 Product product;

	 Integer quantity;
	
	@Transient
	 Double totalPrice;
	
	@Transient
	 Double totalOrderdAmount;
}
