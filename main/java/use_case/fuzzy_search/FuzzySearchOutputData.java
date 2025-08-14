package use_case.fuzzy_search;

import entity.Post;
import java.util.List;

/**
 * Output data for the fuzzy search use case.
 */
public class FuzzySearchOutputData {
    private final List<Post> searchResults;
    private final String message;
    private final boolean success;
    private final String searchQuery;

    public FuzzySearchOutputData(List<Post> searchResults, String message, boolean success, String searchQuery) {
        this.searchResults = searchResults;
        this.message = message;
        this.success = success;
        this.searchQuery = searchQuery;
    }

    public FuzzySearchOutputData(String message, boolean success, String searchQuery) {
        this.searchResults = null;
        this.message = message;
        this.success = success;
        this.searchQuery = searchQuery;
    }

    public List<Post> getSearchResults() {
        return searchResults;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
