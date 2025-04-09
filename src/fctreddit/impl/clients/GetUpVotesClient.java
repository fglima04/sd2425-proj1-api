package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.impl.clients.java.Content.ContentClient;
import fctreddit.impl.clients.rest.RestContentClient;
import fctreddit.impl.discovery.Discovery;

public class GetUpVotesClient {
    private static Logger Log = Logger.getLogger(GetUpVotesClient.class.getName());
    
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Use: java " + GetUpVotesClient.class.getCanonicalName() + 
                             " postId");
            return;
        }
        
        String postId = args[0];
        
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
        
        Result<Integer> result = client.getupVotes(postId);
        if (result.isOK())
            Log.info("Post has " + result.value() + " upvotes");
        else
            Log.info("Get upvotes failed with error: " + result.error());
    }
}