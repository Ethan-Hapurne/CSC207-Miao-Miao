package use_case.search;

import entity.Post;
import java.util.List;

/**
 * Interface for data access operations related to search functionality.
 */
public interface SearchUserDataAccessInterface {
    /**
     * Searches posts by query string.
     * @param query the search query
     * @return List of matching posts
     */
    List<Post> searchPosts(String query);

    /**
     * Searches posts by specific criteria.
     * @param title title to search for
     * @param location location to search for
     * @param tags tags to search for
     * @param isLost whether to search for lost or found items
     * @return List of matching posts
     */
    List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost);


}
