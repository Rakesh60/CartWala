package com.vendor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class OrderAddress {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 Integer id;

	 String firstName;

	 String lastName;

	 String email;

	 String mobileNo;

	 String address;

	 String city;

	 String state;

	 String pincode;

	

}
