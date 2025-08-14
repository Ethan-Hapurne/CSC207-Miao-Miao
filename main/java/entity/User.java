package entity;

import java.util.List;

/**
 * The representation of a user in our program.
 */
public interface User {

    /**
     * Returns the username of the user.
     * @return the username of the user.
     */
    String getName();

    /**
     * Returns the password of the user.
     * @return the password of the user.
     */
    String getPassword();

    /**
     * Returns whether the user is an admin. For backward compatibility, the default
     * implementation returns false so older test doubles that do not implement this
     * method will still compile and behave as non-admin users.
     * @return true if the user is an admin, false otherwise
     */
    default boolean isAdmin() { return false; }

    /**
     * Returns the credibility score of the user.
     * @return the credibility score.
     */
    int getCredibilityScore();

    /**
     * Returns the list of posts this user has resolved.
     * @return list of resolved post IDs.
     */
    List<String> getResolvedPosts();

    /**
     * Adds a resolved post to the user's list.
     * @param postId the ID of the resolved post.
     */
    void addResolvedPost(String postId);

    /**
     * Increases the user's credibility score.
     * @param points the number of points to add.
     */
    void addCredibilityPoints(int points);
}
