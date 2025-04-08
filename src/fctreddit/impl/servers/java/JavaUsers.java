package fctreddit.impl.servers.java;

import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.User;
import fctreddit.api.rest.RestUsers;
import fctreddit.impl.servers.persistence.Hibernate;
import fctreddit.impl.clients.rest.RestImageClient;
import fctreddit.impl.discovery.Discovery;

public class JavaUsers implements RestUsers {

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private Hibernate hibernate;
	private RestImageClient imageClient;
	
	public JavaUsers() {
		hibernate = Hibernate.getInstance();
		try {
			imageClient = new RestImageClient(new Discovery(Discovery.DISCOVERY_ADDR));
		} catch (IOException e) {
			Log.severe("Failed to initialize RestImageClient: " + e.getMessage());
			throw new RuntimeException("Initialization error", e);
		}
	}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null
				|| user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// Handle avatar if provided
		if (user.getAvatarUrl() != null) {
			try {
				String avatarUri = imageClient.uploadImage(user.getAvatarUrl().getBytes());
				user.setAvatarUrl(URI.create(avatarUri).toString());
			} catch (Exception e) {
				Log.warning("Failed to upload avatar: " + e.getMessage());
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}
		}

		try {
			 hibernate.persist(user);
		} catch (Exception e) {
			e.printStackTrace(); //Most likely the exception is due to the user already existing...
			Log.info("User already exists.");
			throw new WebApplicationException(Status.CONFLICT);
		}
		
		return user.getUserId();
	}

	@Override
	public User getUser(String userId, String password) {
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
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		// Check if user exists
		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		// Check if the password is correct
		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		return user;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; userData = " + user);

		// Check if userId, password, or user data is null
		if (userId == null || password == null || user == null) {
			Log.info("Invalid input data.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User existingUser = getUser(userId, password);

		// Update user fields
		if (user.getFullName() != null) {
			existingUser.setFullName(user.getFullName());
		}
		if (user.getEmail() != null) {
			existingUser.setEmail(user.getEmail());
		}
		if (user.getPassword() != null) {
			existingUser.setPassword(user.getPassword());
		}
		if (user.getAvatarUrl() != null) {
			try {
				String avatarUri = imageClient.uploadImage(user.getAvatarUrl().getBytes());
				existingUser.setAvatarUrl(URI.create(avatarUri).toString());
			} catch (Exception e) {
				Log.warning("Failed to upload avatar: " + e.getMessage());
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}
		}

		try {
			 hibernate.update(existingUser); // Updated from merge to update
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		return existingUser;
	}

	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);

		// Check if userId or password is null
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user = getUser(userId, password);

		try {
			 hibernate.delete(user); // Updated from remove to delete
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		return user;
	}


	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		
		try {
			List<User> list = hibernate.jpql("SELECT u FROM User u WHERE u.userId LIKE '%" + pattern +"%'", User.class);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

}
