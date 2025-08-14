package use_case.resolve_post;

import entity.Post;
import entity.User;

/**
 * Data access interface for the resolve post use case.
 */
public interface ResolvePostUserDataAccessInterface {
    /**
     * Gets a user by username.
     * @param username the username to search for
     * @return the user if found, null otherwise
     */
    User getUserByUsername(String username);

    /**
     * Gets a post by ID.
     * @param postId the post ID to search for
     * @return the post if found, null otherwise
     */
    Post getPostById(String postId);

    /**
     * Updates a user's information.
     * @param user the user to update
     * @return true if update was successful, false otherwise
     */
    boolean updateUser(User user);

    /**
     * Updates a post's information.
     * @param post the post to update
     * @return true if update was successful, false otherwise
     */
    boolean updatePost(Post post);
}

