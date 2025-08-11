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
        use_case.admin.AdminUserDataAccessInterface{

    private final DatabaseReference postsRef;
    private final DateTimeFormatter dateFormatter;
    private final FirebaseDatabase database;

    public FirebasePostDataAccessObject() {
        this.postsRef = FirebaseConfig.getDatabase().getReference("posts");
        this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.database = FirebaseConfig.getDatabase();
    }

    @Override
    public List<Post> getAllPosts() {
        System.out.println("\n=== DEBUG: getAllPosts() called ===");
        CompletableFuture<List<Post>> future = new CompletableFuture<>();

        System.out.println("DEBUG: Setting up Firebase listener...");
        postsRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("DEBUG: Firebase onDataChange called");
                List<Post> posts = new ArrayList<>();
                int postCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        posts.add(post);
                        postCount++;
                    } else {
                        System.out.println("DEBUG: Warning - null post found in snapshot");
                    }
                }
                System.out.println("DEBUG: Retrieved " + postCount + " posts from Firebase");
                future.complete(posts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: Firebase onCancelled called with error: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to load posts: " + databaseError.getMessage()));
            }
        });

        try {
            System.out.println("DEBUG: Waiting for Firebase response (timeout: 5 seconds)...");
            List<Post> result = future.get(5, TimeUnit.SECONDS);
            System.out.println("DEBUG: getAllPosts() returning " + result.size() + " posts");
            return result;
        } catch (InterruptedException e) {
            System.out.println("DEBUG: InterruptedException in getAllPosts(): " + e.getMessage());
            e.printStackTrace();
            System.err.println("Error fetching posts: " + e.getMessage());
            return new ArrayList<>();
        } catch (ExecutionException e) {
            System.out.println("DEBUG: ExecutionException in getAllPosts(): " + e.getMessage());
            System.out.println("DEBUG: ExecutionException cause: " + (e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null"));
            e.printStackTrace();
            System.err.println("Error fetching posts: " + e.getMessage());
            return new ArrayList<>();
        } catch (TimeoutException e) {
            System.out.println("DEBUG: TimeoutException in getAllPosts(): " + e.getMessage());
            e.printStackTrace();
            System.err.println("Error fetching posts: " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.out.println("DEBUG: Unexpected Exception in getAllPosts(): " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
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
            // Search only in title and content (description) for now — tag search will be added later
            if (post.getTitle().toLowerCase().contains(lowerQuery) ||
                    post.getDescription().toLowerCase().contains(lowerQuery)) {
                matchingPosts.add(post);
            }
        }

        return matchingPosts;
    }

    @Override
    public Post getPostById(String postID) {
        // Try to find post by hash code (which is what we're passing from the UI)
        try {
            int hashCode = Integer.parseInt(postID);
            return findPostByHashCode(hashCode);
        } catch (NumberFormatException e) {
            // If it's not a number, try to find it as a Firebase key
            CompletableFuture<Post> future = new CompletableFuture<>();

            postsRef.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
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
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                System.err.println("Error fetching post: " + ex.getMessage());
                return null;
            }
        }
    }

    private Post findPostByHashCode(int hashCode) {
        CompletableFuture<Post> future = new CompletableFuture<>();

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to search posts: " + databaseError.getMessage()));
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
        String postId = postsRef.push().getKey();

        Post newPost = new Post(
                postId.hashCode(),
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

    // Admin methods
    @Override
    public boolean editPost(String postId, String newTitle, String description,
                            String location, List<String> tags, boolean isLost) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        int intPostId = Integer.parseInt(postId);
        // Query for the post with matching title
        postsRef.orderByChild("postID").equalTo(intPostId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Should only be one post with this title
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post existingPost = postSnapshot.getValue(Post.class);
                            if (existingPost != null) {
                                // Create updated post
                                Post updatedPost = new Post(
                                        existingPost.getPostID(),
                                        newTitle,
                                        description,
                                        tags != null ? tags : existingPost.getTags(),
                                        LocalDateTime.parse(existingPost.getTimestamp(), dateFormatter),
                                        existingPost.getAuthor(),
                                        location,
                                        existingPost.getImageURL(),
                                        isLost,
                                        existingPost.getNumberOfLikes(),
                                        existingPost.getReactions()
                                );

                                // Update in Firebase using the snapshot's key
                                postsRef.child(postSnapshot.getKey())
                                        .setValue(updatedPost, (databaseError, databaseReference) -> {
                                            if (databaseError != null) {
                                                System.err.println("Error updating post: " +
                                                        databaseError.getMessage());
                                                future.complete(false);
                                            } else {
                                                System.out.println("Post updated successfully!");
                                                future.complete(true);
                                            }
                                        });
                                return;  // Exit after finding and updating the post
                            }
                        }
                        // If we get here, no post was found
                        System.err.println("Post not found with postid: " + postId);
                        future.complete(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Error fetching post for update: " +
                                databaseError.getMessage());
                        future.complete(false);
                    }
                });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Error during post update: " + e.getMessage());
            return false;
        }
    }

    // Delete post methods
    @Override
    public void deletePost(String postId) {
        System.out.println("\n=== Firebase Delete Operation ===");
        System.out.println("FirebaseDAO: Starting delete operation for post ID: " + postId);

        DatabaseReference postsRef = database.getReference("posts");
        CountDownLatch deleteLatch = new CountDownLatch(1);

        try {
            System.out.println("FirebaseDAO: Converting post ID to integer: " + postId);
            int intPostId = Integer.parseInt(postId);

            System.out.println("FirebaseDAO: Querying for post with ID: " + intPostId);
            postsRef.orderByChild("postID").equalTo(intPostId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        System.out.println("FirebaseDAO: Query returned " +
                                         dataSnapshot.getChildrenCount() + " matches");

                        if (!dataSnapshot.exists()) {
                            System.err.println("FirebaseDAO: No matching post found");
                            deleteLatch.countDown();
                            return;
                        }

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String firebaseKey = postSnapshot.getKey();
                            System.out.println("FirebaseDAO: Found post with Firebase key: " + firebaseKey);

                            postsRef.child(firebaseKey).removeValue((error, ref) -> {
                                if (error != null) {
                                    System.err.println("FirebaseDAO: Error deleting post: " +
                                                     error.getMessage());
                                } else {
                                    System.out.println("FirebaseDAO: Post successfully deleted");
                                }
                                deleteLatch.countDown();
                            });
                            return;
                        }

                        System.out.println("FirebaseDAO: No posts to delete");
                        deleteLatch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("FirebaseDAO: Delete operation cancelled: " +
                                         databaseError.getMessage());
                        System.err.println("FirebaseDAO: Error Code: " + databaseError.getCode());
                        System.err.println("FirebaseDAO: Error Details: " + databaseError.getDetails());
                        deleteLatch.countDown();
                    }
                });

        System.out.println("FirebaseDAO: Waiting for delete operation to complete...");
        boolean completed = deleteLatch.await(5, TimeUnit.SECONDS);
        System.out.println("FirebaseDAO: Operation completed: " + completed);

        if (!completed) {
            System.err.println("FirebaseDAO: Delete operation timed out");
            throw new RuntimeException("Delete operation timed out");
        }
    } catch (NumberFormatException e) {
        System.err.println("FirebaseDAO: Invalid post ID format: " + e.getMessage());
        throw new RuntimeException("Invalid post ID format: " + postId);
    } catch (InterruptedException e) {
        System.err.println("FirebaseDAO: Operation interrupted: " + e.getMessage());
        Thread.currentThread().interrupt();
        throw new RuntimeException("Operation interrupted while deleting post");
    } catch (Exception e) {
        System.err.println("FirebaseDAO: Unexpected error: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Unexpected error during delete: " + e.getMessage());
    }
    System.out.println("=== Firebase Delete Operation Completed ===\n");
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
    public boolean deletePost(int postId) {
        // Convert int to String and call the existing deletePost method
        deletePost(String.valueOf(postId));
        return true; // Assuming success if no exception is thrown
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