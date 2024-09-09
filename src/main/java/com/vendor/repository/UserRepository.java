package com.vendor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendor.model.UserData;

public interface UserRepository extends JpaRepository<UserData, Integer>{
	
	public UserData findByEmail(String email);

	public List<UserData> findByRole(String role);
	
	public UserData  findByResetToken(String resetToken);
}
