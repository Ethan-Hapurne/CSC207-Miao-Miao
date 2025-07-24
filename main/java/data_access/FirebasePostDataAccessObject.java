package data_access;

import com.google.firebase.database.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import entity.Comment;
import entity.Post;
import use_case.dashboard.DashboardUserDataAccessInterface;
import use_case.search.SearchUserDataAccessInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

/**
 * Firebase implementation of data access for posts.
 */
public class FirebasePostDataAccessObject implements 
        DashboardUserDataAccessInterface, 
        SearchUserDataAccessInterface {
    
    private final DatabaseReference postsRef;
    private final DateTimeFormatter dateFormatter;
    
    public FirebasePostDataAccessObject() {
        this.postsRef = FirebaseConfig.getDatabase().getReference("posts");
        this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }
    
    @Override
    public List<Post> getAllPosts() {
        CompletableFuture<List<Post>> future = new CompletableFuture<>();
        postsRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Post> posts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        posts.add(post);
                    }
                }
                future.complete(posts);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to load posts: " + databaseError.getMessage()));
            }
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Error fetching posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Post> searchPosts(String query) {
        List<Post> allPosts = getAllPosts();
        List<Post> matchingPosts = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Post post : allPosts) {
            // Search only in title and content (description) for now — tag search will be added later
            if (post.getTitle().toLowerCase().contains(lowerQuery) ||
                post.getDescription().toLowerCase().contains(lowerQuery)) {
                matchingPosts.add(post);
            }
        }
        
        return matchingPosts;
    }
    
    @Override
    public Post getPostById(int postID) {
        CompletableFuture<Post> future = new CompletableFuture<>();
        postsRef.child(String.valueOf(postID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                future.complete(post);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to load post: " + databaseError.getMessage()));
            }
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Error fetching post: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Post addPost(String title, String content, List<String> tags, String location, boolean isLost, String author) {
        String postId = postsRef.push().getKey();
        Post newPost = new Post(
            postId.hashCode(),
            title,
            content,
            tags != null ? tags : new ArrayList<>(),
            LocalDateTime.now(),
            author,
            location,
            null, // image URL
            isLost,
            0, // likes
            new HashMap<>() // reactions
        );
        postsRef.child(postId).setValue(newPost, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.err.println("Error saving post: " + databaseError.getMessage());
                } else {
                    System.out.println("Post saved successfully!");
                }
            }
        });
        return newPost;
    }
    
    @Override
    public List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost) {
        // SESSION CHANGE: If all criteria are blank, return all posts sorted alphabetically by title
        List<Post> allPosts = getAllPosts();
        List<Post> matchingPosts = new ArrayList<>();

        boolean allBlank = (title == null || title.isEmpty()) &&
                           (location == null || location.isEmpty()) &&
                           (tags == null || tags.isEmpty()) &&
                           (isLost == null);

        if (allBlank) {
            allPosts.sort(Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER));
            return allPosts;
        }

        for (Post post : allPosts) {
            boolean matches = true;

            if (title != null && !title.isEmpty() &&
                !post.getTitle().toLowerCase().contains(title.toLowerCase())) {
                matches = false;
            }

            if (location != null && !location.isEmpty() &&
                !post.getLocation().toLowerCase().contains(location.toLowerCase())) {
                matches = false;
            }

            if (tags != null && !tags.isEmpty()) {
                boolean hasMatchingTag = false;
                for (String searchTag : tags) {
                    for (String postTag : post.getTags()) {
                        if (postTag.toLowerCase().contains(searchTag.toLowerCase())) {
                            hasMatchingTag = true;
                            break;
                        }
                    }
                    if (hasMatchingTag) break;
                }
                if (!hasMatchingTag) {
                    matches = false;
                }
            }

            if (isLost != null && post.isLost() != isLost) {
                matches = false;
            }

            if (matches) {
                matchingPosts.add(post);
            }
        }

        // Always sort the result alphabetically by title
        matchingPosts.sort(Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER));
        return matchingPosts;
    }

    // Fetch comments for a post from Firebase
    public List<Comment> getCommentsForPost(int postId) {
        try {
            DatabaseReference postRef = FirebaseConfig.getDatabase().getReference("posts").child(String.valueOf(postId));
            CompletableFuture<List<Comment>> future = new CompletableFuture<>();
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Comment> comments = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Comment comment = snapshot.getValue(Comment.class);
                        if (comment != null) {
                            comments.add(comment);
                        }
                    }
                    future.complete(comments);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    future.completeExceptionally(new RuntimeException("Failed to load comments: " + databaseError.getMessage()));
                }
            });
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // Add a top-level comment to a post in Firebase
    public void addCommentToPost(int postId, Comment comment) {
        try {
            DatabaseReference postRef = FirebaseConfig.getDatabase().getReference("posts").child(String.valueOf(postId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        List<Comment> comments = post.getComments();
                        if (comments == null) comments = new ArrayList<>();
                        comments.add(comment);
                        post.setComments(comments);
                        postRef.setValue(post, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    System.err.println("Error saving comment: " + databaseError.getMessage());
                                } else {
                                    System.out.println("Comment saved successfully!");
                                }
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    future.completeExceptionally(new RuntimeException("Failed to load post for comment: " + databaseError.getMessage()));
                }
            });
            future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    // Like a comment (top-level only for now)
    public void likeComment(int postId, String commentId) {
        try {
            DatabaseReference postRef = FirebaseConfig.getDatabase().getReference("posts").child(String.valueOf(postId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null && post.getComments() != null) {
                        for (Comment c : post.getComments()) {
                            if (c.getId().equals(commentId)) {
                                c.like();
                                break;
                            }
                        }
                        postRef.setValue(post, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    System.err.println("Error saving liked comment: " + databaseError.getMessage());
                                } else {
                                    System.out.println("Comment liked successfully!");
                                }
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    future.completeExceptionally(new RuntimeException("Failed to load post for like: " + databaseError.getMessage()));
                }
            });
            future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
} 