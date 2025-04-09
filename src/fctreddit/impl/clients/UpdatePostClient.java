package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.Post;
import fctreddit.impl.clients.java.Content.ContentClient;
import fctreddit.impl.clients.rest.RestContentClient;
import fctreddit.impl.discovery.Discovery;

public class UpdatePostClient {
    private static Logger Log = Logger.getLogger(UpdatePostClient.class.getName());
    
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println("Use: java " + UpdatePostClient.class.getCanonicalName() + 
                             " postId userPassword newContent authorId");
            return;
        }
        
        String postId = args[0];
        String userPassword = args[1];
        String newContent = args[2];
        String authorId = args[3];
        
        Post updatedPost = new Post(postId, authorId, newContent);
        
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
        
        Result<Post> result = client.updatePost(postId, userPassword, updatedPost);
        if (result.isOK())
            Log.info("Updated post: " + result.value());
        else
            Log.info("Update post failed with error: " + result.error());
    }
}