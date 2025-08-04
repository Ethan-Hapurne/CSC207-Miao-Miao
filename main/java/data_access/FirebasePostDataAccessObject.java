package data_access;

import com.google.firebase.database.*;
import entity.Post;
import use_case.dashboard.DashboardUserDataAccessInterface;
import use_case.search.SearchUserDataAccessInterface;
import use_case.search.util.FuzzyMatchHelper;

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

        matchingPosts.sort(Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER));
        return matchingPosts;
    }

    @Override
    public List<Post> fuzzySearch(String query) {
        List<Post> allPosts = getAllPosts();
        return FuzzyMatchHelper.fuzzyMatchPosts(allPosts, query);
    }
}