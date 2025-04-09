package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.impl.clients.java.Content.ContentClient;
import fctreddit.impl.clients.rest.RestContentClient;
import fctreddit.impl.discovery.Discovery;

public class DownVotePostClient {
    private static Logger Log = Logger.getLogger(DownVotePostClient.class.getName());
    
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Use: java " + DownVotePostClient.class.getCanonicalName() + 
                             " postId userId userPassword");
            return;
        }
        
        String postId = args[0];
        String userId = args[1];
        String userPassword = args[2];
        
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
        
        Result<Void> result = client.downVotePost(postId, userId, userPassword);
        if (result.isOK())
            Log.info("Downvoted post successfully");
        else
            Log.info("Downvote post failed with error: " + result.error());
    }
}