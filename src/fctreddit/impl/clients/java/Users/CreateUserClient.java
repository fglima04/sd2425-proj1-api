package fctreddit.impl.clients.java.Users;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.impl.clients.rest.RestUsersClient;
import fctreddit.impl.discovery.Discovery;


public class CreateUserClient {

	private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());
	
	public static void main(String[] args) throws IOException {
		
		if( args.length != 4) {
			System.err.println( "Use: java " + CreateUserClient.class.getCanonicalName() + " userId fullName email password");
			return;
		}
		
		String userId = args[0];
		String fullName = args[1];
		String email = args[2];
		String password = args[3];
		
		User usr = new User( userId, fullName, email, password);
		
		Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR);
		discovery.start();
		
		RestUsersClient client = new RestUsersClient(discovery);
		
		Result<String> result = client.createUser( usr );
		if( result.isOK()  )
			Log.info("Created user:" + result.value() );
		else
			Log.info("Create user failed with error: " + result.error());

	}
	
}
