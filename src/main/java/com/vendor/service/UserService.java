package com.vendor.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.vendor.model.UserData;

public interface UserService {

	public UserData saveUser(UserData user);

	public UserData getUserByEmail(String email);

	public List<UserData> getUsers(String role);

	public Boolean updateAccountStatus(Boolean status, Integer id);

	public void increaseFailedAttempt(UserData user);

	public void userAccountLock(UserData user);

	public boolean unlockAccountTimeExpired(UserData user);

	public void resetAttempt(int userId);

	public void updateUserResetToken(String email, String resetToken);

	public UserData getUserByToken(String token);

	public UserData updateUser(UserData user);

	public UserData updateUserProfile(UserData user);

}
