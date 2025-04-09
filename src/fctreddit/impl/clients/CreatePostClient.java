package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.Post;
import fctreddit.impl.clients.java.Content.ContentClient;
import fctreddit.impl.clients.rest.RestContentClient;
import fctreddit.impl.discovery.Discovery;

public class CreatePostClient {
    
    private static Logger Log = Logger.getLogger(CreatePostClient.class.getName());
    
    public static void main(String[] args) throws IOException {
        
        if (args.length != 4) {
            System.err.println("Use: java " + CreatePostClient.class.getCanonicalName() + 
                             " authorId content postId password");
            return;
        }
        
        String authorId = args[0];
        String content = args[1];
        String postId = args[2];
        String password = args[3];
        
        Post post = new Post(postId, authorId, content);
        
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
            //client = new GrpcContentClient(serverURI);
            Log.info("gRPC client not implemented yet, using REST client instead.");
            client = new RestContentClient(serverURI);
        }
        
        Result<String> result = client.createPost(post, password);
        if (result.isOK())
            Log.info("Created post: " + result.value());
        else
            Log.info("Create post failed with error: " + result.error());
    }
}