package com.logicalis.br.sdc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.logicalis.br.sdc.model.User;
import com.logicalis.br.sdc.security.UserRepository;

import io.swagger.annotations.ApiOperation;

/**
 * Users API.
 * 
 * @author Fabio De Santi
 */
@RestController
public class UserService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserRepository repo;

	/**
	 * List all registered users.
	 * 
	 * @return
	 */
	@ApiOperation(value = "List registered users")
	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody ResponseEntity<List<User>> list() {

		try {
			Map<String, User> map = repo.findAllUsers();
			List<User> l = new ArrayList<>(map.values());
			return ResponseEntity.ok(l);

		} catch (Exception e) {
			logger.error("Error listing users", e);
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Save a user object.
	 * 
	 * @param user
	 *            a {@link User} object
	 * @return
	 */
	@ApiOperation(value = "Create or update a user")
	@RequestMapping(value = "/user", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody ResponseEntity<String> save(@RequestBody User user) {

		try {
			repo.saveUser(user);
			return ResponseEntity.ok("Done");

		} catch (Exception e) {
			logger.error("Error saving " + user, e);
			return new ResponseEntity<>("Fail", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Get a {@link User} object by its username.
	 * 
	 * @param username
	 * @return
	 */
	@ApiOperation(value = "Find user by username")
	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<User> get(@PathVariable String username) {

		try {
			User user = repo.findUser(username);
			return new ResponseEntity<>(user, user != null ? HttpStatus.FOUND : HttpStatus.NOT_FOUND);

		} catch (Exception e) {
			logger.error("Error searching " + username, e);
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Delete a {@link User} by its username.
	 * 
	 * @param username
	 * @return
	 */
	@ApiOperation(value = "Delete a user by its username")
	@RequestMapping(value = "/user/{username}/delete", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<String> delete(@PathVariable String username) {

		try {
			repo.deleteUser(username);
			return ResponseEntity.ok("Deleted");

		} catch (Exception e) {
			logger.error("Error deleting " + username, e);
			return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
		}
	}
}
