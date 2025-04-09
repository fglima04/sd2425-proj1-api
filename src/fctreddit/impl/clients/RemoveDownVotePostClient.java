package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.impl.clients.java.Content.ContentClient;
import fctreddit.impl.clients.rest.RestContentClient;
import fctreddit.impl.discovery.Discovery;

public class RemoveDownVotePostClient {
    private static Logger Log = Logger.getLogger(RemoveDownVotePostClient.class.getName());
    
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Use: java " + RemoveDownVotePostClient.class.getCanonicalName() + 
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
        
        Result<Void> result = client.removeDownVotePost(postId, userId, userPassword);
        if (result.isOK())
            Log.info("Removed downvote successfully");
        else
            Log.info("Remove downvote failed with error: " + result.error());
    }
}