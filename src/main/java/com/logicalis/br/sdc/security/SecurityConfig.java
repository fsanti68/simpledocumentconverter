package com.logicalis.br.sdc.security;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer.UserDetailsBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.logicalis.br.sdc.model.User;

/**
 * Spring-based security.
 * 
 * @author Fabio De Santi
 *
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private UserRepository repo;

	// Authentication : User --> Roles
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> configurer = auth.inMemoryAuthentication();

		Map<String, User> users = repo.findAllUsers();
		if (users == null || users.isEmpty()) {
			// Se não houver usuários cadastrados, cria o apiuser e o admin
			logger.info("Creating default users: apiuser and admin");
			repo.saveUser(new User("apiuser", "L0g!cal!s@657$", User.Role.API));
			repo.saveUser(new User("admin", "secret1", User.Role.ADMIN, User.Role.API));
			users = repo.findAllUsers();

		} else
			logger.info("Found " + users.size() + " users.");

		@SuppressWarnings("rawtypes")
		UserDetailsBuilder udb = null;
		for (String username : users.keySet()) {
			User user = users.get(username);
			logger.info("\t" + user);
			String[] roles = user.getRoleAsString();

			udb = (udb == null ? configurer.withUser(username).password(user.getPassword()).roles(roles)
					: udb.and().withUser(username).password(user.getPassword()).roles(roles));
		}
	}

	// Authorization : Role -> Access
	// API role allows /api access
	// ADMIN role allows everything
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic().and().authorizeRequests()

				.antMatchers("/api/**").hasRole("API")

				.antMatchers("/user", "/user/**").hasRole("ADMIN")

				.anyRequest().hasRole("ADMIN")

				.and().csrf().disable();
	}
}
