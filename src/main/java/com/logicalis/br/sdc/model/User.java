package com.logicalis.br.sdc.model;

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple user representation. Used for persistence and spring-based
 * authentication.
 * 
 * @author Fabio De Santi
 *
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Role {
		API, ADMIN
	}

	private String username;
	private String password;
	private Role[] role;

	public User() {
	}

	public User(String username, String password, Role... role) {
		this.username = username;
		this.password = password;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role[] getRole() {
		return role;
	}

	public void setRole(Role[] role) {
		this.role = role;
	}

	@JsonIgnore
	public String[] getRoleAsString() {
		String[] a = new String[role.length];
		int idx = 0;
		for (Role r : role) {
			a[idx++] = r.name();
		}
		return a;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", role=" + Arrays.toString(role) + "]";
	}
}
