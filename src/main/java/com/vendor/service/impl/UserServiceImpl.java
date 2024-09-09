package com.vendor.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vendor.model.UserData;
import com.vendor.repository.UserRepository;
import com.vendor.service.UserService;
import com.vendor.util.AppConstant;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserData saveUser(UserData user) {
		/* user.setRole("ROLE_USER"); */

		user.setIsEnabled(true);
		user.setAccountNotLocked(true);
		user.setFailedAttempt(0);

		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);

		UserData saveUser = userRepository.save(user);
		return saveUser;
	}

	@Override
	public UserData getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserData> getUsers(String role) {
		return userRepository.findByRole(role);

	}

	@Override
	public Boolean updateAccountStatus(Boolean status, Integer id) {

		Optional<UserData> userbyId = userRepository.findById(id);

		if (userbyId.isPresent()) {
			UserData userData = userbyId.get();
			userData.setIsEnabled(status);
			userRepository.save(userData);
			return true;
		}

		return false;

	}

	@Override
	public void increaseFailedAttempt(UserData user) {
		int attempt = (user.getFailedAttempt() == null) ? 1 : user.getFailedAttempt() + 1;
		user.setFailedAttempt(attempt);
		userRepository.save(user);
	}

	@Override
	public void userAccountLock(UserData user) {

		user.setAccountNotLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
	}

	@Override
	public boolean unlockAccountTimeExpired(UserData user) {

		Long lockTime = user.getLockTime().getTime();

		long unLockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;

		long currentTime = System.currentTimeMillis();

		if (unLockTime < currentTime) {
			user.setAccountNotLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}

		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		UserData findByEmail = userRepository.findByEmail(email);
		findByEmail.setResetToken(resetToken);
		userRepository.save(findByEmail);

	}

	@Override
	public UserData getUserByToken(String token) {

		return userRepository.findByResetToken(token);
	}

	@Override
	public UserData updateUser(UserData user) {

		return userRepository.save(user);
	}

	@Override
	public UserData updateUserProfile(UserData user) {
		 // Proceed with the update logic as before
		
	
		
		
		return userRepository.save(user);
	}

	@Override
	public List<UserData> getUsersByRoles(List<String> roles) {
	    return userRepository.findByRoleIn(roles);
	}




}
