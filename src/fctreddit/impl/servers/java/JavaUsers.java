package fctreddit.impl.servers.java;

import java.util.List;
import java.util.logging.Logger;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.servers.persistence.Hibernate;
import fctreddit.impl.clients.rest.RestImageClient;

public class JavaUsers implements Users {

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private Hibernate hibernate;
	private RestImageClient imageClient;
	
	public JavaUsers() {
		hibernate = Hibernate.getInstance();
	}

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null
				|| user.getEmail() == null) {
			Log.info("User object invalid.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		// Check if user already exists
		try {
			User existingUser = hibernate.get(User.class, user.getUserId());
			if (existingUser != null) {
				Log.info("User already exists with userId: " + user.getUserId());
				return Result.error(ErrorCode.CONFLICT);
			}

			// If we get here, user doesn't exist, so create it
			hibernate.persist(user);
			return Result.ok(user.getUserId());
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.severe("Error creating user: " + e.getMessage());
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		// Check if user is valid
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user = null;
		try {
			user = hibernate.get(User.class, userId);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}

		// Check if user exists
		if (user == null) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; userData = " + user);

		// Check if userId, password, or user data is null
		if (userId == null || password == null || user == null) {
			Log.info("Invalid input data.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User existingUser = getUser(userId, password).value();

		// Update user fields
		if (user.getEmail() != null) {
			existingUser.setEmail(user.getEmail());
		}
		if (user.getFullName() != null) {
			existingUser.setFullName(user.getFullName());
		}
		if (user.getPassword() != null) {
			existingUser.setPassword(user.getPassword());
		}


		try {
			 hibernate.update(existingUser); // Updated from merge to update
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok(existingUser);
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);

		// Check if userId or password is null
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		User user = getUser(userId, password).value();

		try {
			 hibernate.delete(user); // Updated from remove to delete
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok(user);
	}


	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		
		try {
			List<User> list = hibernate.jpql("SELECT u FROM User u WHERE u.userId LIKE '%" + pattern +"%'", User.class);
			return Result.ok(list);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}

}
