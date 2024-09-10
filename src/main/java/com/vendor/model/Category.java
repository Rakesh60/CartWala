package com.vendor.model;

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
@Getter @Setter
@Entity
public class Category {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 int id;
	
	 String name;
	
	 String imagename;
	
	 Boolean isActive;

	 // Track the user who added/stored the category
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
     UserData storedBy;

}
