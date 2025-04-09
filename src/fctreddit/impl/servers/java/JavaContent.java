package fctreddit.impl.servers.java;

import java.util.List;
import java.util.logging.Logger;
import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.servers.persistence.Hibernate;
import fctreddit.impl.clients.rest.RestImageClient;

public class JavaContent implements Content {

    private static Logger Log = Logger.getLogger(JavaContent.class.getName());

    private Hibernate hibernate;
    private RestImageClient imageClient;
    
    public JavaContent() {
        hibernate = Hibernate.getInstance();
    }

    @Override
    public Result<String> createPost(Post post, String userPassword) {
        Log.info("createPost : " + post);

        if (post == null || post.getAuthorId() == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            // Verify if user exists and password is correct
            User user = hibernate.get(User.class, post.getAuthorId());
            if (user == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            if (!user.getPassword().equals(userPassword)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            hibernate.persist(post);
            return Result.ok(post.getPostId());
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts : timestamp=" + timestamp + ", sortOrder=" + sortOrder);
        
        try {
            List<Post> posts = hibernate.jpql(
                "FROM Post WHERE timestamp > " + timestamp + " ORDER BY timestamp " + sortOrder,
                Post.class
            );
            // Convert Post objects to post IDs
            List<String> postIds = posts.stream()
                                      .map(Post::getPostId)
                                      .toList();
            return Result.ok(postIds);
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Post> getPost(String postId) {
        Log.info("getPost : " + postId);

        if (postId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            return Result.ok(post);
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long timeout) {
        Log.info("getPostAnswers : postId=" + postId + ", timeout=" + timeout);
        
        if (postId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            List<Post> answers = hibernate.jpql(
                "FROM Post WHERE replyTo = '" + postId + "'",
                Post.class
            );
            // Convert Post objects to post IDs
            List<String> answerIds = answers.stream()
                                          .map(Post::getPostId)
                                          .toList();
            return Result.ok(answerIds);
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Log.info("updatePost : postId=" + postId);
        
        if (postId == null || post == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post existingPost = hibernate.get(Post.class, postId);
            if (existingPost == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }

            User user = hibernate.get(User.class, existingPost.getAuthorId());
            if (!user.getPassword().equals(userPassword)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            existingPost.setContent(post.getContent());
            hibernate.update(existingPost);
            
            return Result.ok(existingPost);
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        Log.info("deletePost : postId=" + postId);
        
        if (postId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }

            User user = hibernate.get(User.class, post.getAuthorId());
            if (!user.getPassword().equals(userPassword)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            hibernate.delete(post);
            return Result.ok();
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        Log.info("upVotePost : postId=" + postId + ", userId=" + userId);
        
        if (postId == null || userId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            User user = hibernate.get(User.class, userId);
            
            if (post == null || user == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            
            if (!user.getPassword().equals(userPassword)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            post.setUpVote(post.getUpVote() + 1);
            hibernate.update(post);
            return Result.ok();
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        Log.info("removeUpVotePost : postId=" + postId + ", userId=" + userId);
        
        if (postId == null || userId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            User user = hibernate.get(User.class, userId);
            
            if (post == null || user == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            
            if (!user.getPassword().equals(userPassword)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            post.setUpVote(post.getUpVote() - 1);
            hibernate.update(post);
            return Result.ok();
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        Log.info("downVotePost : postId=" + postId + ", userId=" + userId);
        
        if (postId == null || userId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            User user = hibernate.get(User.class, userId);
            
            if (post == null || user == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            
            if (!user.getPassword().equals(userPassword)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            post.setDownVote(post.getDownVote() + 1);
            hibernate.update(post);
            return Result.ok();
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        Log.info("removeDownVotePost : postId=" + postId + ", userId=" + userId);
        
        if (postId == null || userId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            User user = hibernate.get(User.class, userId);
            
            if (post == null || user == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            
            if (!user.getPassword().equals(userPassword)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            post.setDownVote(post.getDownVote() - 1);
            hibernate.update(post);
            return Result.ok();
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        Log.info("getupVotes : postId=" + postId);
        
        if (postId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            return Result.ok(post.getUpVote());
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        Log.info("getDownVotes : postId=" + postId);
        
        if (postId == null) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            return Result.ok(post.getDownVote());
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }
}
