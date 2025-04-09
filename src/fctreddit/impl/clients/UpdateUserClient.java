package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.impl.clients.java.Users.UsersClient;
import fctreddit.impl.clients.rest.RestUsersClient;
import fctreddit.impl.discovery.Discovery;

public class UpdateUserClient {
    private static Logger Log = Logger.getLogger(UpdateUserClient.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.err.println("Use: java " + UpdateUserClient.class.getCanonicalName() + " userId oldPassword fullName email password");
            return;
        }

        String userId = args[0];
        String oldPwd = args[1];
        String fullName = args[2];
        String email = args[3];
        String password = args[4];

        User usr = new User(userId, fullName, email, password);

        Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR);
        discovery.start();

        URI[] uris = discovery.knownUrisOf("UsersService", 1);
        if (uris.length == 0) {
            throw new RuntimeException("No available servers for service: UsersService");
        }
        URI serverURI = uris[0];

        UsersClient client = null;
        
        if(serverURI.toString().endsWith("rest"))
            client = new RestUsersClient(serverURI);
        else
            Log.info("gRPC client not implemented yet, using REST client instead.");

        Result<User> result = client.updateUser(userId,oldPwd,usr);
        if (result.isOK())
            Log.info("Updated user: " + result.value());
        else
            Log.info("Update user failed with error: " + result.error());
    }
}
