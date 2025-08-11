package interface_adapter.fuzzy_search;

import entity.Post;
import java.util.List;

/**
 * State for the fuzzy search feature.
 */
public class FuzzySearchState {
    private List<Post> searchResults;
    private String message;
    private boolean success;
    private String searchQuery;

    public FuzzySearchState() {
        this.searchResults = null;
        this.message = "";
        this.success = false;
        this.searchQuery = "";
    }

    public List<Post> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Post> searchResults) {
        this.searchResults = searchResults;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
