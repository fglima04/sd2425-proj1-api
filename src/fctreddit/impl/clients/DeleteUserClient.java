package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.impl.clients.java.Users.UsersClient;
import fctreddit.impl.clients.rest.RestUsersClient;
import fctreddit.impl.discovery.Discovery;

public class DeleteUserClient {
    private static Logger Log = Logger.getLogger(DeleteUserClient.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Use: java " + DeleteUserClient.class.getCanonicalName() + " userId password");
            return;
        }

        String userId = args[0];
        String password = args[1];

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

        Result<User> result = client.deleteUser(userId,password);
        if (result.isOK())
            Log.info("User deleted successfully");
        else
            Log.info("Delete user failed with error: " + result.error());
    }
}
