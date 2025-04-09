package fctreddit.impl.clients.java.Content;

import java.util.List;

import fctreddit.api.java.Result;
import fctreddit.api.java.Content;
import fctreddit.api.Post;

public abstract class ContentClient implements Content {
	
	protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

	protected static final int MAX_RETRIES = 10;
	protected static final int RETRY_SLEEP = 5000;
	
	abstract public Result<String> createPost(Post post, String userPassword);

	abstract public Result<List<String>> getPosts(long timestamp, String sortOrder);

	abstract public Result<Post> getPost(String postId);

	abstract public Result<List<String>> getPostAnswers(String postId, long maxTimeout);

	abstract public Result<Post> updatePost(String postId, String userPassword, Post post);

	abstract public Result<Void> deletePost(String postId, String userPassword);

	abstract public Result<Void> upVotePost(String postId, String userId, String userPassword);

	abstract public Result<Void> removeUpVotePost(String postId, String userId, String userPassword);

	abstract public Result<Void> downVotePost(String postId, String userId, String userPassword);

	abstract public Result<Void> removeDownVotePost(String postId, String userId, String userPassword);

	abstract public Result<Integer> getupVotes(String postId);

	abstract public Result<Integer> getDownVotes(String postId);

}
