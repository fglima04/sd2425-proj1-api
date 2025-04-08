package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.impl.clients.java.Users.UsersClient;
import fctreddit.impl.clients.rest.RestUsersClient;
import fctreddit.impl.discovery.Discovery;

public class SearchUsersClient {
    private static Logger Log = Logger.getLogger(SearchUsersClient.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Use: java " + SearchUsersClient.class.getCanonicalName() + " pattern");
            return;
        }

        String pattern = args[0];

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

        Result<List<User>> result = client.searchUsers(pattern);
        if (result.isOK())
            Log.info("Found users: " + result.value());
        else
            Log.info("Search users failed with error: " + result.error());
    }
}
