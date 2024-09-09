package com.vendor.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.vendor.model.UserData;
import com.vendor.repository.UserRepository;
import com.vendor.service.UserService;
import com.vendor.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");

        UserData userData = userRepository.findByEmail(email);

        if (userData == null) {
            exception = new BadCredentialsException("User does not exist or bad credentials");
        } else if (userData.getIsEnabled()) {

            if (userData.getAccountNotLocked()) {

                if (userData.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
                    userService.increaseFailedAttempt(userData);
                } else {
                    userService.userAccountLock(userData);
                    exception = new LockedException("Your account is locked! Too many attempts");
                }
            } else {

                if (userService.unlockAccountTimeExpired(userData)) {
                    exception = new LockedException("Your account is unlocked! Please try to login");
                } else {
                    exception = new LockedException("Your account is temporarily locked");
                }
            }

        } else {
            exception = new LockedException("Your account is inactive");
        }

        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);
    }
}
