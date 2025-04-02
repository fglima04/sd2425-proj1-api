package fctreddit.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.discovery.Discovery;

public class GetUserClient {
	
	private static Logger Log = Logger.getLogger(GetUserClient.class.getName());


	public static void main(String[] args) throws IOException {
		
		if (args.length != 2) {
			System.err.println("Use: java " + GetUserClient.class.getCanonicalName() + " userId password");
			return;
		}

		String userId = args[0];
		String password = args[1];

		Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR);
		discovery.start();

		System.out.println("Discovering server...");
		URI[] uris = discovery.knownUrisOf("UsersService", 1);
		if (uris.length == 0) {
			System.err.println("No UsersService found.");
			return;
		}

		String serverUrl = uris[0].toString();
		System.out.println("Discovered server at: " + serverUrl);

		System.out.println("Sending request to server.");
		
		UsersClient client = null;
		
		if(serverUrl.endsWith("rest"))
			client = new RestUsersClient(discovery);
		else
			client = new GrpcUsersClient( discovery );
			
		Result<User> result = client.getUser(userId, password);
		if( result.isOK()  )
			Log.info("Get user:" + result.value() );
		else
			Log.info("Get user failed with error: " + result.error());
		
	}
	
}
