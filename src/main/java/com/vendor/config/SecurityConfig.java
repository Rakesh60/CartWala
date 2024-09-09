package com.vendor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	@Lazy
	private AuthFailureHandlerImpl authenticationFailureHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService());
		authenticationProvider.setPasswordEncoder(passwordEncoder());
		return authenticationProvider;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.disable())
				.authorizeHttpRequests(req -> req
						.requestMatchers("/admin/**").hasRole("ADMIN") // Only ADMIN can access /admin/** URLs
		                .requestMatchers("/seller/**").hasRole("SELLER") // Only SELLER can access /seller/** URLs

						.requestMatchers("/user/**").hasRole("USER") // Only USER can access /user/** URLs

						.requestMatchers("/", "/products", "/product/**").hasAnyRole("USER", "ANONYMOUS") // Restrict
																											// ADMIN
																											// from
																											// accessing
																											// "/", but
																											// allow
																											// USER and
																											// others
						.requestMatchers("/**").permitAll() // Permit all other requests
				)
				.formLogin(form -> form
						.loginPage("/signin")
						.loginProcessingUrl("/login")
						.failureHandler(authenticationFailureHandler)
						.successHandler(authenticationSuccessHandler))
				.logout(logout -> logout.permitAll())
				.exceptionHandling(exception -> exception
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							// Redirect to custom error page
							response.sendRedirect("/access-denied");
						}));

		return http.build();
	}
}
