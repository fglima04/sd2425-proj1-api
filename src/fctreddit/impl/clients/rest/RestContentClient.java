package fctreddit.impl.clients.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.Post;
import fctreddit.api.rest.RestContent;
import fctreddit.impl.clients.java.Content.ContentClient;

public class RestContentClient extends ContentClient {
    private static Logger Log = Logger.getLogger(RestContentClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    private URI serverURI;
    final Client client;
    final ClientConfig config;
    WebTarget target;

    public RestContentClient(URI serverURI) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();
        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);
        this.target = client.target(serverURI).path(RestContent.PATH);
    }

    private void updateTarget() {
        this.target = client.target(serverURI).path(RestContent.PATH);
    }

    @Override
    public Result<String> createPost(Post post, String userPassword) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.queryParam("password", userPassword)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(post, MediaType.APPLICATION_JSON));

                int status = r.getStatus();
                if (status == Status.OK.getStatusCode() && r.hasEntity()) {
                    return Result.ok(r.readEntity(String.class));
                } else {
                    return Result.error(getErrorCodeFrom(status));
                }
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                updateTarget();
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // nothing to do
                }
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Response r = target.queryParam("timestamp", timestamp)
                         .queryParam("order", sortOrder)
                         .request()
                         .accept(MediaType.APPLICATION_JSON)
                         .get();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode() && r.hasEntity()) {
            List<String> posts = r.readEntity(new GenericType<List<String>>() {});
            return Result.ok(posts);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Post> getPost(String postId) {
        Response r = target.path(postId)
                         .request()
                         .accept(MediaType.APPLICATION_JSON)
                         .get();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode() && r.hasEntity()) {
            Post post = r.readEntity(Post.class);
            return Result.ok(post);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long timeout) {
        Response r = target.path(postId + "/answers")
                         .queryParam("timeout", timeout)
                         .request()
                         .accept(MediaType.APPLICATION_JSON)
                         .get();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode() && r.hasEntity()) {
            List<String> answers = r.readEntity(new GenericType<List<String>>() {});
            return Result.ok(answers);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Response r = target.path(postId)
                         .queryParam("password", userPassword)
                         .request()
                         .accept(MediaType.APPLICATION_JSON)
                         .put(Entity.entity(post, MediaType.APPLICATION_JSON));

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode() && r.hasEntity()) {
            Post updatedPost = r.readEntity(Post.class);
            return Result.ok(updatedPost);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        Response r = target.path(postId)
                         .queryParam("password", userPassword)
                         .request()
                         .delete();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId + "/votes/" + userId + "/up")
                         .queryParam("password", userPassword)
                         .request()
                         .post(null);

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId + "/votes/" + userId + "/up")
                         .queryParam("password", userPassword)
                         .request()
                         .delete();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId + "/votes/" + userId + "/down")
                         .queryParam("password", userPassword)
                         .request()
                         .post(null);

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId + "/votes/" + userId + "/down")
                         .queryParam("password", userPassword)
                         .request()
                         .delete();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        Response r = target.path(postId + "/votes/up")
                         .request()
                         .accept(MediaType.APPLICATION_JSON)
                         .get();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode() && r.hasEntity()) {
            Integer votes = r.readEntity(Integer.class);
            return Result.ok(votes);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        Response r = target.path(postId + "/votes/down")
                         .request()
                         .accept(MediaType.APPLICATION_JSON)
                         .get();

        int status = r.getStatus();
        if (status == Status.OK.getStatusCode() && r.hasEntity()) {
            Integer votes = r.readEntity(Integer.class);
            return Result.ok(votes);
        } else {
            return Result.error(getErrorCodeFrom(status));
        }
    }

    private static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
