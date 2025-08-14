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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Firebase implementation of data access for posts.
 */
public class FirebasePostDataAccessObject implements
        DashboardUserDataAccessInterface,
        SearchUserDataAccessInterface,
        use_case.fuzzy_search.FuzzySearchUserDataAccessInterface,
        use_case.admin.AdminUserDataAccessInterface {

    // Constants for magic numbers
    private static final int TIMEOUT_SECONDS = 5;
    private static final int INITIAL_POST_COUNT = 0;
    private static final int MAX_POST_ID = 0;
    private static final int INCREMENT_VALUE = 1;
    private static final int DEFAULT_LIKES = 0;
    private static final int CREDIBILITY_POINTS = 1;
    private static final String POSTS_REFERENCE = "posts";
    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String POST_ID_FIELD = "postID";
    private static final String ANONYMOUS_AUTHOR = "anonymous";
    private static final String DEBUG_PREFIX = "DEBUG: ";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String FIREBASE_DAO_PREFIX = "FirebaseDAO: ";

    private final DatabaseReference postsRef;
    private final DateTimeFormatter dateFormatter;
    private final FirebaseDatabase database;

    /**
     * Creates a new FirebasePostDataAccessObject.
     */
    public FirebasePostDataAccessObject() {
        this.postsRef = FirebaseConfig.getDatabase().getReference(POSTS_REFERENCE);
        this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.database = FirebaseConfig.getDatabase();
    }

    @Override
    public List<Post> getAllPosts() {
        System.out.println("\n=== DEBUG: getAllPosts() called ===");
        CompletableFuture<List<Post>> future = new CompletableFuture<>();

        System.out.println(DEBUG_PREFIX + "Setting up Firebase listener...");
        postsRef.orderByChild(TIMESTAMP_FIELD)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    System.out.println(DEBUG_PREFIX + "Firebase onDataChange called");
                    List<Post> posts = new ArrayList<>();
                    int postCount = INITIAL_POST_COUNT;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        if (post != null) {
                            posts.add(post);
                            postCount++;
                        } else {
                            System.out.println(DEBUG_PREFIX 
                                + "Warning - null post found in snapshot");
                        }
                    }
                    System.out.println(DEBUG_PREFIX + "Retrieved " + postCount 
                        + " posts from Firebase");
                    future.complete(posts);
                }

                @Override
                public void onCancelled(final DatabaseError databaseError) {
                    System.out.println(DEBUG_PREFIX + "Firebase onCancelled called with error: " 
                        + databaseError.getMessage());
                    future.completeExceptionally(new RuntimeException("Failed to load posts: " 
                        + databaseError.getMessage()));
                }
            });

        try {
            System.out.println(DEBUG_PREFIX + "Waiting for Firebase response (timeout: " 
                + TIMEOUT_SECONDS + " seconds)...");
            List<Post> result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println(DEBUG_PREFIX + "getAllPosts() returning " + result.size() 
                + " posts");
            return result;
        } catch (InterruptedException e) {
            System.out.println(DEBUG_PREFIX + "InterruptedException in getAllPosts(): " 
                + e.getMessage());
            e.printStackTrace();
            System.err.println(ERROR_PREFIX + "fetching posts: " + e.getMessage());
            return new ArrayList<>();
        } catch (ExecutionException e) {
            System.out.println(DEBUG_PREFIX + "ExecutionException in getAllPosts(): " 
                + e.getMessage());
            System.out.println(DEBUG_PREFIX + "ExecutionException cause: " 
                + (e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null"));
            e.printStackTrace();
            System.err.println(ERROR_PREFIX + "fetching posts: " + e.getMessage());
            return new ArrayList<>();
        } catch (TimeoutException e) {
            System.out.println(DEBUG_PREFIX + "TimeoutException in getAllPosts(): " 
                + e.getMessage());
            e.printStackTrace();
            System.err.println(ERROR_PREFIX + "fetching posts: " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.out.println(DEBUG_PREFIX + "Unexpected Exception in getAllPosts(): " 
                + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            System.err.println(ERROR_PREFIX + "fetching posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Post> searchPosts(final String query) {
        List<Post> allPosts = getAllPosts();
        List<Post> matchingPosts = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Post post : allPosts) {
            // Search only in title and content (description) for now 
            // tag search will be added later
            if (post.getTitle().toLowerCase().contains(lowerQuery)
                    || post.getDescription().toLowerCase().contains(lowerQuery)) {
                matchingPosts.add(post);
            }
        }

        return matchingPosts;
    }

    @Override
    public Post getPostById(final String postID) {
        // Try to find post by hash code (which is what we're passing from the UI)
        try {
            int hashCode = Integer.parseInt(postID);
            return findPostByHashCode(hashCode);
        } catch (NumberFormatException e) {
            // If it's not a number, try to find it as a Firebase key
            CompletableFuture<Post> future = new CompletableFuture<>();

            postsRef.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    Post post = dataSnapshot.getValue(Post.class);
                    future.complete(post);
                }

                @Override
                public void onCancelled(final DatabaseError databaseError) {
                    future.completeExceptionally(new RuntimeException("Failed to load post: " 
                        + databaseError.getMessage()));
                }
            });

            try {
                return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                System.err.println(ERROR_PREFIX + "fetching post: " + ex.getMessage());
                return null;
            }
        }
    }

    /**
     * Finds a post by its hash code.
     * @param hashCode the hash code to search for
     * @return the post if found, null otherwise
     */
    private Post findPostByHashCode(final int hashCode) {
        CompletableFuture<Post> future = new CompletableFuture<>();

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Post foundPost = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null && post.getPostID() == hashCode) {
                        foundPost = post;
                        break;
                    }
                }
                future.complete(foundPost);
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to search posts: " 
                    + databaseError.getMessage()));
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Error searching for post by hash code: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Post addPost(String title, String content, List<String> tags, String location, boolean isLost, String author) {
        // Get a new Firebase key for the post
        String firebaseKey = postsRef.push().getKey();
        if (firebaseKey == null) {
            System.err.println("Error: Failed to generate Firebase key");
            return null;
        }

        // Get all posts to find the highest ID
        List<Post> allPosts = getAllPosts();
        int maxId = 0;
        for (Post post : allPosts) {
            if (post.getPostID() > maxId) {
                maxId = post.getPostID();
            }
        }
        int newPostId = maxId + 1;

        // Create the new post with sequential ID
        Post newPost = new Post(
                newPostId,
                title,
                content,
                tags != null ? tags : new ArrayList<>(),
                LocalDateTime.now(),
                author,
                location,
                null,
                isLost,
                0,
                new HashMap<>()
        );

        // Save the post and wait for completion
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        postsRef.child(firebaseKey).setValue(newPost, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Error saving post: " + databaseError.getMessage());
                future.complete(false);
            } else {
                System.out.println("Post saved successfully with ID: " + newPostId);
                future.complete(true);
            }
        });

        try {
            boolean success = future.get(5, TimeUnit.SECONDS);
            if (success) {
                return newPost;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error waiting for post save: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost) {
        System.out.println("\n=== DEBUG: searchPostsByCriteria() called ===");
        System.out.println("DEBUG: Search criteria:");
        System.out.println("  - Title: '" + title + "'");
        System.out.println("  - Location: '" + location + "'");
        System.out.println("  - Tags: " + (tags != null ? tags.toString() : "null"));
        System.out.println("  - IsLost: " + isLost);

        // SESSION CHANGE: If all criteria are blank, return all posts sorted alphabetically by title
        List<Post> allPosts = getAllPosts();
        System.out.println("DEBUG: Retrieved " + allPosts.size() + " total posts from database");

        List<Post> matchingPosts = new ArrayList<>();

        boolean allBlank = (title == null || title.isEmpty()) &&
                (location == null || location.isEmpty()) &&
                (tags == null || tags.isEmpty()) &&
                (isLost == null);

        System.out.println("DEBUG: All criteria blank? " + allBlank);

        if (allBlank) {
            // Return all posts sorted alphabetically by title
            System.out.println("DEBUG: All criteria blank, returning all posts sorted by title");
            allPosts.sort(Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER));
            return allPosts;
        }

        System.out.println("DEBUG: Starting to filter posts...");
        int postIndex = 0;
        for (Post post : allPosts) {
            postIndex++;
            System.out.println("\nDEBUG: Checking post " + postIndex + ": '" + post.getTitle() + "'");
            boolean matches = true;

            if (title != null && !title.isEmpty()) {
                boolean titleMatches = post.getTitle().toLowerCase().contains(title.toLowerCase());
                System.out.println("DEBUG:   Title check - Query: '" + title + "' vs Post: '" + post.getTitle() + "' -> " + titleMatches);
                if (!titleMatches) {
                matches = false;
                }
            }

            if (location != null && !location.isEmpty()) {
                boolean locationMatches = post.getLocation().toLowerCase().contains(location.toLowerCase());
                System.out.println("DEBUG:   Location check - Query: '" + location + "' vs Post: '" + post.getLocation() + "' -> " + locationMatches);
                if (!locationMatches) {
                matches = false;
                }
            }

            if (tags != null && !tags.isEmpty()) {
                System.out.println("DEBUG:   Tags check - Query tags: " + tags + " vs Post tags: " + post.getTags());
                boolean hasMatchingTag = false;
                List<String> postTags = post.getTags();
                if (postTags != null && !postTags.isEmpty()) {
                for (String searchTag : tags) {
                        for (String postTag : postTags) {
                        if (postTag.toLowerCase().contains(searchTag.toLowerCase())) {
                            hasMatchingTag = true;
                                System.out.println("DEBUG:     Found matching tag: '" + searchTag + "' in post tag: '" + postTag + "'");
                            break;
                        }
                    }
                    if (hasMatchingTag) break;
                    }
                } else {
                    System.out.println("DEBUG:     Post has no tags to check against");
                }
                if (!hasMatchingTag) {
                    System.out.println("DEBUG:     No matching tags found");
                    matches = false;
                }
            }

            if (isLost != null) {
                boolean lostStatusMatches = post.isLost() == isLost;
                System.out.println("DEBUG:   Lost status check - Query: " + isLost + " vs Post: " + post.isLost() + " -> " + lostStatusMatches);
                if (!lostStatusMatches) {
                matches = false;
                }
            }

            System.out.println("DEBUG:   Final match result: " + matches);
            if (matches) {
                matchingPosts.add(post);
                System.out.println("DEBUG:   ✓ Post added to results");
            } else {
                System.out.println("DEBUG:   ✗ Post excluded from results");
            }
        }

        System.out.println("\nDEBUG: Search completed. Found " + matchingPosts.size() + " matching posts out of " + allPosts.size() + " total posts");

        // Always sort the result alphabetically by title
        matchingPosts.sort(Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER));
        System.out.println("DEBUG: Results sorted alphabetically by title");

        System.out.println("DEBUG: Returning " + matchingPosts.size() + " posts");
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

//    // Add a top-level comment to a post in Firebase
//    public void addCommentToPost(int postId, Comment comment) {
//        try {
//            DatabaseReference postRef = FirebaseConfig.getDatabase().getReference("posts").child(String.valueOf(postId));
//            CompletableFuture<Void> future = new CompletableFuture<>();
//            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Post post = dataSnapshot.getValue(Post.class);
//                    if (post != null) {
//                        List<Comment> comments = post.getComments();
//                        if (comments == null) comments = new ArrayList<>();
//                        comments.add(comment);
//                        post.setComments(comments);
//                        postRef.setValue(post, new DatabaseReference.CompletionListener() {
//                            @Override
//                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                if (databaseError != null) {
//                                    System.err.println("Error saving comment: " + databaseError.getMessage());
//                                } else {
//                                    System.out.println("Comment saved successfully!");
//                                }
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    future.completeExceptionally(new RuntimeException("Failed to load post for comment: " + databaseError.getMessage()));
//                }
//            });
//            future.get(5, TimeUnit.SECONDS);
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Like a comment (top-level only for now)
//    public void likeComment(int postId, String commentId) {
//        try {
//            DatabaseReference postRef = FirebaseConfig.getDatabase().getReference("posts").child(String.valueOf(postId));
//            CompletableFuture<Void> future = new CompletableFuture<>();
//            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Post post = dataSnapshot.getValue(Post.class);
//                    if (post != null && post.getComments() != null) {
//                        for (Comment c : post.getComments()) {
//                            if (c.getId().equals(commentId)) {
//                                c.like();
//                                break;
//                            }
//                        }
//                        postRef.setValue(post, new DatabaseReference.CompletionListener() {
//                            @Override
//                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                if (databaseError != null) {
//                                    System.err.println("Error saving liked comment: " + databaseError.getMessage());
//                                } else {
//                                    System.out.println("Comment liked successfully!");
//                                }
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    future.completeExceptionally(new RuntimeException("Failed to load post for like: " + databaseError.getMessage()));
//                }
//            });
//            future.get(5, TimeUnit.SECONDS);
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Updates an existing post in Firebase.
     *
     * @param post the post to update
     * @return true if update was successful, false otherwise
     */
    public boolean updatePost(Post post) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Find the Firebase key for this post by searching through all posts
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firebaseKey = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post existingPost = snapshot.getValue(Post.class);
                    if (existingPost != null && existingPost.getPostID() == post.getPostID()) {
                        firebaseKey = snapshot.getKey();
                        break;
                    }
                }

                if (firebaseKey != null) {
                    // Update the post at the correct Firebase key
                    final String finalFirebaseKey = firebaseKey;
                    DatabaseReference postRef = postsRef.child(finalFirebaseKey);
                    postRef.setValue(post, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                System.err.println("Error updating post: " + databaseError.getMessage());
                                future.complete(false);
                            } else {
                                System.out.println("Post updated successfully at key: " + finalFirebaseKey);
                                future.complete(true);
                            }
                        }
                    });
                } else {
                    System.err.println("Could not find Firebase key for post with ID: " + post.getPostID());
                    future.complete(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error searching for post to update: " + databaseError.getMessage());
                future.complete(false);
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Error updating post: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a post from Firebase.
     *
     * @param postId the ID of the post to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deletePost(int postId) {
        System.out.println("\n=== Firebase Delete Operation ===");
        System.out.println("FirebaseDAO: Starting delete operation for post ID: " + postId);

        // First verify the post exists
        List<Post> allPosts = getAllPosts();
        boolean postExists = false;
        for (Post post : allPosts) {
            if (post.getPostID() == postId) {
                postExists = true;
                break;
            }
        }

        if (!postExists) {
            System.err.println("FirebaseDAO: Post with ID " + postId + " does not exist");
            return false;
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CountDownLatch deleteLatch = new CountDownLatch(1);

        // Query for posts with matching postID
        System.out.println("FirebaseDAO: Querying for post with ID: " + postId);
        postsRef.orderByChild("postID").equalTo(postId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("FirebaseDAO: Query returned " + dataSnapshot.getChildrenCount() + " matches");

                    if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                        System.err.println("FirebaseDAO: No matching post found in Firebase");
                        future.complete(false);
                        deleteLatch.countDown();
                        return;
                    }

                    // Should only be one post with this ID
                    DataSnapshot postSnapshot = dataSnapshot.getChildren().iterator().next();
                    String firebaseKey = postSnapshot.getKey();
                    Post post = postSnapshot.getValue(Post.class);
                    System.out.println("FirebaseDAO: Found post with Firebase key: " + firebaseKey);
                    System.out.println("FirebaseDAO: Post details - Title: " + (post != null ? post.getTitle() : "null") + 
                                     ", ID: " + (post != null ? post.getPostID() : "null"));

                    postsRef.child(firebaseKey).removeValue((error, ref) -> {
                        if (error != null) {
                            System.err.println("FirebaseDAO: Error deleting post: " + error.getMessage());
                            future.complete(false);
                        } else {
                            System.out.println("FirebaseDAO: Post successfully deleted");
                            future.complete(true);
                        }
                        deleteLatch.countDown();
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("FirebaseDAO: Delete operation cancelled: " + databaseError.getMessage());
                    System.err.println("FirebaseDAO: Error Code: " + databaseError.getCode());
                    System.err.println("FirebaseDAO: Error Details: " + databaseError.getDetails());
                    future.complete(false);
                    deleteLatch.countDown();
                }
            });

        try {
            // Wait for the operation to complete
            boolean completed = deleteLatch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("FirebaseDAO: Delete operation timed out");
                return false;
            }
            boolean result = future.get(5, TimeUnit.SECONDS);
            System.out.println("FirebaseDAO: Delete operation completed with result: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("FirebaseDAO: Error during delete operation: " + e.getMessage());
            return false;
        }
    }

    // Admin methods
    @Override
    public boolean editPost(String postId, String newTitle, String description,
                            String location, List<String> tags, boolean isLost) {
        System.out.println("FirebasePostDataAccessObject: Editing post: " + postId);

        // Convert string ID to int for lookup
        int numericId;
        try {
            numericId = Integer.parseInt(postId);
        } catch (NumberFormatException e) {
            System.err.println("Invalid post ID format: " + postId);
            return false;
        }

        // Find the post
        CompletableFuture<Post> future = new CompletableFuture<>();
        Query query = postsRef.orderByChild("postID").equalTo((double) numericId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    System.err.println("Post not found: " + postId);
                    future.complete(null);
                    return;
                }

                // Get the first matching post
                DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                Post existingPost = firstChild.getValue(Post.class);

                if (existingPost == null) {
                    System.err.println("Failed to deserialize post: " + postId);
                    future.complete(null);
                    return;
                }

                // Update fields only if new values are provided (not null)
                if (newTitle != null) {
                    existingPost.setTitle(newTitle);
                }
                if (description != null) {
                    existingPost.setDescription(description);
                }
                if (location != null) {
                    existingPost.setLocation(location);
                }
                if (tags != null) {
                    existingPost.setTags(tags);
                }
                existingPost.setLost(isLost);  // Boolean is always set

                // Update the post in the database
                String key = firstChild.getKey();
                postsRef.child(key).setValue(existingPost, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            System.err.println("Error updating post: " + databaseError.getMessage());
                            future.complete(null);
                        } else {
                            System.out.println("Post updated successfully!");
                            future.complete(existingPost);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
                future.complete(null);
            }
        });

        try {
            Post result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return result != null;
        } catch (Exception e) {
            System.err.println("Error waiting for post update: " + e.getMessage());
            return false;
        }
    }

    // Delete post methods
    @Override
    public void deletePost(String postId) {
    System.out.println("\n=== Firebase Delete Operation ===");
    System.out.println("FirebaseDAO: Starting delete operation for post ID: " + postId);

    CountDownLatch deleteLatch = new CountDownLatch(1);
    final DatabaseError[] errorHolder = new DatabaseError[1];

    try {
        System.out.println("FirebaseDAO: Converting post ID to integer: " + postId);
        int intPostId = Integer.parseInt(postId);

        System.out.println("FirebaseDAO: Querying for post with ID: " + intPostId);
        Query query = postsRef.orderByChild("postID").equalTo(intPostId);
        
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("FirebaseDAO: Query returned " +
                        dataSnapshot.getChildrenCount() + " matches");

                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    System.err.println("FirebaseDAO: No matching post found");
                    errorHolder[0] = DatabaseError.fromException(
                        new DatabaseException("No matching post found")
                    );
                    deleteLatch.countDown();
                    return;
                }

                DataSnapshot postSnapshot = dataSnapshot.getChildren().iterator().next();
                String firebaseKey = postSnapshot.getKey();
                System.out.println("FirebaseDAO: Found post with Firebase key: " + firebaseKey);

                postsRef.child(firebaseKey).removeValue((error, ref) -> {
                    if (error != null) {
                        System.err.println("FirebaseDAO: Error deleting post: " + error.getMessage());
                        errorHolder[0] = error;
                    }
                    deleteLatch.countDown();
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("FirebaseDAO: Delete operation cancelled: " + databaseError.getMessage());
                errorHolder[0] = databaseError;
                deleteLatch.countDown();
            }
        });

        if (!deleteLatch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Delete operation timed out");
        }
        
        if (errorHolder[0] != null) {
            throw new RuntimeException("Failed to delete post: " + errorHolder[0].getMessage());
        }

    } catch (NumberFormatException e) {
        throw new RuntimeException("Invalid post ID format: " + postId);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Operation interrupted while deleting post");
    }
}

    @Override
    public boolean existsPost(String postId) {
        System.out.println("\nChecking existence for postId: " + postId);
        DatabaseReference postsRef = database.getReference("posts");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean exists = new AtomicBoolean(false);

        postsRef.orderByChild("postID").equalTo(Integer.parseInt(postId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        exists.set(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0);
                        System.out.println("DataSnapshot exists: " + exists.get());
                        if (exists.get()) {
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                System.out.println("Found post with ID: " + child.child("postID").getValue());
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Database error: " + databaseError.getMessage());
                        latch.countDown();
                    }
                });

        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("Database operation timed out");
                return false;
            }
        } catch (InterruptedException e) {
            System.err.println("Operation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }

        boolean result = exists.get();
        System.out.println("Final result: post exists = " + result);
        return result;
    }



    @Override
    public entity.User getUserByUsername(String username) {
        // Use FirebaseUserDataAccessObject to get user data
        FirebaseUserDataAccessObject userDAO = new FirebaseUserDataAccessObject();
        return userDAO.get(username);
    }

    @Override
    public boolean updateUser(entity.User user) {
        // Use FirebaseUserDataAccessObject to update user data
        FirebaseUserDataAccessObject userDAO = new FirebaseUserDataAccessObject();
        try {
            userDAO.save(user);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    // Method for AdminUserDataAccessInterface compatibility
    public Post getPostById(int postID) {
        return getPostById(String.valueOf(postID));
    }
}