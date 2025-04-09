package fctreddit.impl.servers.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.api.java.Content;
import fctreddit.api.rest.RestContent;
import fctreddit.impl.servers.java.JavaContent;

public class ContentResource implements RestContent {

    private static Logger Log = Logger.getLogger(ContentResource.class.getName());

    final Content impl;
    
    public ContentResource() {
        impl = new JavaContent();
    }

    @Override
    public String createPost(Post post, String userPassword) {
        Log.info("createPost : " + post);
        
        Result<String> res = impl.createPost(post, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public List<String> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts : timestamp=" + timestamp + ", sortOrder=" + sortOrder);
        
        Result<List<String>> res = impl.getPosts(timestamp, sortOrder);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Post getPost(String postId) {
        Log.info("getPost : " + postId);
        
        Result<Post> res = impl.getPost(postId);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public List<String> getPostAnswers(String postId, long timeout) {
        Log.info("getPostAnswers : postId=" + postId + ", timeout=" + timeout);
        
        Result<List<String>> res = impl.getPostAnswers(postId, timeout);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Post updatePost(String postId, String userPassword, Post post) {
        Log.info("updatePost : postId=" + postId + ", pwd=" + userPassword);
        
        Result<Post> res = impl.updatePost(postId, userPassword, post);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public void deletePost(String postId, String userPassword) {
        Log.info("deletePost : postId=" + postId + ", pwd=" + userPassword);
        
        Result<Void> res = impl.deletePost(postId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void upVotePost(String postId, String userId, String userPassword) {
        Log.info("upVotePost : postId=" + postId + ", userId=" + userId);
        
        Result<Void> res = impl.upVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void removeUpVotePost(String postId, String userId, String userPassword) {
        Log.info("removeUpVotePost : postId=" + postId + ", userId=" + userId);
        
        Result<Void> res = impl.removeUpVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void downVotePost(String postId, String userId, String userPassword) {
        Log.info("downVotePost : postId=" + postId + ", userId=" + userId);
        
        Result<Void> res = impl.downVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void removeDownVotePost(String postId, String userId, String userPassword) {
        Log.info("removeDownVotePost : postId=" + postId + ", userId=" + userId);
        
        Result<Void> res = impl.removeDownVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public Integer getupVotes(String postId) {
        Log.info("getupVotes : " + postId);
        
        Result<Integer> res = impl.getupVotes(postId);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Integer getDownVotes(String postId) {
        Log.info("getDownVotes : " + postId);
        
        Result<Integer> res = impl.getDownVotes(postId);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    protected static Status errorCodeToStatus(Result.ErrorCode error) {
        return switch(error) {
            case NOT_FOUND -> Status.NOT_FOUND;
            case CONFLICT -> Status.CONFLICT;
            case FORBIDDEN -> Status.FORBIDDEN;
            case NOT_IMPLEMENTED -> Status.NOT_IMPLEMENTED;
            case BAD_REQUEST -> Status.BAD_REQUEST;
            default -> Status.INTERNAL_SERVER_ERROR;
        };
    }
}