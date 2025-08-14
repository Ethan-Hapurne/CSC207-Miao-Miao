package use_case.dashboard;

import entity.Post;
import java.util.List;

/**
 * Interface for data access operations related to dashboard functionality.
 */
public interface DashboardUserDataAccessInterface {
    /**
     * Gets all posts for the dashboard.
     * @return List of all posts
     */
    List<Post> getAllPosts();

    /**
     * Searches posts by query string.
     * @param query the search query
     * @return List of matching posts
     */
    List<Post> searchPosts(String query);

    /**
     * Performs fuzzy search on posts.
     * @param query the search query
     * @return List of matching posts using fuzzy logic
     */
    default List<Post> fuzzySearch(String query) { return java.util.Collections.emptyList(); }

    /**
     * Searches posts by specific criteria.
     * @param title title to search for (can be null/empty)
     * @param location location to search for (can be null/empty)
     * @param tags tags to search for (can be null/empty)
     * @param isLost filter by lost (true), found (false), or all (null)
     * @return List of matching posts
     */
    default List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost) {
        return java.util.Collections.emptyList();
    }

    /**
     * Gets a specific post by ID.
     * @param postID the ID of the post
     * @return the post, or null if not found
     */
    Post getPostById(String postID);

    /**
     * Adds a new post.
     * @param title the post title
     * @param content the post content
     * @param tags the post tags
     * @param location the post location
     * @param isLost whether it's a lost item
     * @param author the post author
     * @return the created post
     */
    Post addPost(String title, String content, List<String> tags, String location, boolean isLost, String author);
    
    /**
     * Updates an existing post. Default returns true for backward compatibility
     * in tests that don't care about update behavior on fake DAOs.
     * @param post the post to update
     * @return true if update was successful, false otherwise
     */
    default boolean updatePost(Post post) { return true; }
    
    /**
     * Deletes a post. Default returns false for backward compatibility so
     * older in-memory test doubles that don't implement this method still
     * compile. Real implementations should override.
     * @param postId the ID of the post to delete
     * @return true if deletion was successful, false otherwise
     */
    default boolean deletePost(int postId) { return false; }
    
    /**
     * Gets a user by username.
     * @param username the username to search for
     * @return the user if found, null otherwise
     */
    default entity.User getUserByUsername(String username) { return null; }
    
    /**
     * Updates a user's information.
     * @param user the user to update
     * @return true if update was successful, false otherwise
     */
    default boolean updateUser(entity.User user) { return false; }
}