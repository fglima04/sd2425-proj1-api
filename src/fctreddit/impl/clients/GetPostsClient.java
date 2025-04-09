package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.impl.clients.java.Content.ContentClient;
import fctreddit.impl.clients.rest.RestContentClient;
import fctreddit.impl.discovery.Discovery;

public class GetPostsClient {
    private static Logger Log = Logger.getLogger(GetPostsClient.class.getName());
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Use: java " + GetPostsClient.class.getCanonicalName() + 
                             " timestamp sortOrder");
            return;
        }
        
        long timestamp = Long.parseLong(args[0]);
        String sortOrder = args[1];
        
        Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR);
        discovery.start();

        URI[] uris = discovery.knownUrisOf("ContentService", 1);
        if (uris.length == 0) {
            throw new RuntimeException("No available servers for service: ContentService");
        }
        URI serverURI = uris[0];

        ContentClient client = null;
        
        if (serverURI.toString().endsWith("rest"))
            client = new RestContentClient(serverURI);
        else {
            Log.info("gRPC client not implemented yet, using REST client instead.");
            client = new RestContentClient(serverURI);
        }
        
        Result<List<String>> result = client.getPosts(timestamp, sortOrder);
        if (result.isOK())
            Log.info("Got posts: " + result.value());
        else
            Log.info("Get posts failed with error: " + result.error());
    }
}