package com.logicalis.br.sdc.security;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import com.logicalis.br.sdc.model.User;

/**
 * Repository (or data access object) for {@link User} persisted objects.
 * 
 * @author Fabio De Santi
 *
 */
@Component
public class UserRepository {

	private final Logger logger = LoggerFactory.getLogger(UserRepository.class);

	private static String KEY = User.class.getSimpleName().toUpperCase();

	@Resource(name = "redisTemplate")
	private HashOperations<String, String, User> hashOps;

	/**
	 * Persist a {@link User} object.
	 * 
	 * @param user
	 */
	public void saveUser(User user) {
		hashOps.put(KEY, user.getUsername(), user);
		logger.info("redis.save " + user);
	}

	/**
	 * Find a {@link User} by its name.
	 * 
	 * @param username
	 * @return
	 */
	public User findUser(String username) {
		User u = hashOps.get(KEY, username);
		logger.info("redis.find " + username + ": " + u);
		return u;
	}

	/**
	 * List all users.
	 * 
	 * @return
	 */
	public Map<String, User> findAllUsers() {
		Map<String, User> m = hashOps.entries(KEY);
		logger.info("redis.findAll: " + m.size());
		return m;
	}

	/**
	 * Delete a persisted {@link User} by its name.
	 * 
	 * @param username
	 */
	public void deleteUser(String username) {
		Long l = hashOps.delete(KEY, username);
		logger.info("redis.delete " + username + ": " + l);
	}
}
