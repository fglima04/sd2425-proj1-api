package fctreddit.impl.clients.java.Content;

import java.util.List;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.api.java.Post;

public abstract class UsersClient implements Posts {
	
	protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

	protected static final int MAX_RETRIES = 10;
	protected static final int RETRY_SLEEP = 5000;
	
	abstract public Result<String> createEntry(Post post, String userId, String password);

	abstract public Result<Post> deleteEntry(Post post, String userId, String password);
	
	abstract public Result<List<Post>> getAnswers(Post post);

	abstract public Result<List<Post>> getEntries(Post post);

	abstract public Result<List<Post>> getFullEntry(Post post);

    abstract public Result<String> upVote(Post post, String userId, String password);

    abstract public Result<String> removeUpVote(Post post, String userId, String password);

    abstract public Result<String> downVote(Post post, String userId, String password);

    abstract public Result<String> removeDownVote(Post post, String userId, String password);

}