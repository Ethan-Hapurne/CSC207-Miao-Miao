package interface_adapter.search;

import entity.Post;
import java.util.List;

/**
 * The state for the Search View Model.
 */
public class SearchState {
    private String searchQuery = "";
    private String searchError = "";
    private List<Post> searchResults = null;
    private boolean isLoading = false;

    public String getSearchQuery() {
        return searchQuery;
    }

    public String getSearchError() {
        return searchError;
    }

    public List<Post> getSearchResults() {
        return searchResults;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setSearchError(String searchError) {
        this.searchError = searchError;
    }

    public void setSearchResults(List<Post> searchResults) {
        this.searchResults = searchResults;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
    }

    // Add these to support SearchPresenter
    public void setPosts(List<Post> posts) {
        this.setSearchResults(posts);
    }

    public void setError(String error) {
        this.setSearchError(error);
    }
}
