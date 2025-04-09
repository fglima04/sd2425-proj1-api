package fctreddit.impl.clients;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.impl.clients.java.Content.ContentClient;
import fctreddit.impl.clients.rest.RestContentClient;
import fctreddit.impl.discovery.Discovery;

public class GetPostAnswersClient {
    private static Logger Log = Logger.getLogger(GetPostAnswersClient.class.getName());
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Use: java " + GetPostAnswersClient.class.getCanonicalName() + 
                             " postId timeout");
            return;
        }
        
        String postId = args[0];
        long timeout = Long.parseLong(args[1]);
        
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
        
        Result<List<String>> result = client.getPostAnswers(postId, timeout);
        if (result.isOK())
            Log.info("Got answers: " + result.value());
        else
            Log.info("Get answers failed with error: " + result.error());
    }
}