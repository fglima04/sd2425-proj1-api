package fctreddit.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.discovery.Discovery;
import static fctreddit.discovery.Discovery.DISCOVERY_ADDR;

public class CreateUserClient {

	private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());
	
	public static void main(String[] args) throws IOException {
		
		if (args.length != 4) {
			System.err.println("Use: java " + CreateUserClient.class.getCanonicalName() + " userId fullName email password");
			return;
		}

		String userId = args[0];
		String fullName = args[1];
		String email = args[2];
		String password = args[3];

		Discovery discovery = new Discovery(DISCOVERY_ADDR);
		discovery.start();

		System.out.println("Discovering server...");
		URI[] uris = discovery.knownUrisOf("UsersService", 1);
		if (uris.length == 0) {
			System.err.println("No UsersService found.");
			return;
		}

		String serverUrl = uris[0].toString();
		System.out.println("Discovered server at: " + serverUrl);

		User usr = new User(userId, fullName, email, password);

		UsersClient client = null;
		
		if(serverUrl.endsWith("rest"))
			client = new RestUsersClient(discovery);
		else
			client = new GrpcUsersClient(discovery);
		
		Result<String> result = client.createUser(usr);
		if(result.isOK())
			Log.info("Created user:" + result.value());
		else
			Log.info("Create user failed with error: " + result.error());
	}
	
}
