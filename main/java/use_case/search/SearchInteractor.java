package use_case.search;

import entity.Post;
import use_case.search.util.FuzzyMatchHelper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SearchInteractor implements SearchInputBoundary {
    private final SearchUserDataAccessInterface searchDataAccessObject;
    private final SearchOutputBoundary searchOutputBoundary;

    public SearchInteractor(SearchUserDataAccessInterface searchDataAccessObject,
                            SearchOutputBoundary searchOutputBoundary) {
        this.searchDataAccessObject = Objects.requireNonNull(
                searchDataAccessObject, "SearchDataAccessObject must not be null"
        );
        this.searchOutputBoundary = Objects.requireNonNull(
                searchOutputBoundary, "SearchOutputBoundary must not be null"
        );
    }

    @Override
    public void execute(SearchInputData searchInputData) {
        try {
            if (searchInputData == null) {
                throw new IllegalArgumentException("SearchInputData cannot be null");
            }

            List<Post> posts;
            String rawQuery = searchInputData.getQuery();
            boolean hasQuery = rawQuery != null && !rawQuery.trim().isEmpty();

            if (hasQuery) {
                String query = rawQuery.trim();
                posts = performQuerySearch(searchInputData, query);
            } else {
                posts = performCriteriaSearch(searchInputData);
            }

            handleSearchResults(posts);
        } catch (Exception e) {
            handleSearchError(e);
        }
    }

    private List<Post> performQuerySearch(SearchInputData searchInputData, String query) {
        if (searchInputData.isFuzzy()) {
            List<Post> allPosts = searchDataAccessObject.getAllPosts();
            return FuzzyMatchHelper.fuzzyMatchPosts(
                    allPosts != null ? allPosts : Collections.emptyList(),
                    query
            );
        } else {
            return searchDataAccessObject.searchPosts(query);
        }
    }

    private List<Post> performCriteriaSearch(SearchInputData searchInputData) {
        return searchDataAccessObject.searchPostsByCriteria(
                searchInputData.getTitle(),
                searchInputData.getLocation(),
                searchInputData.getTags(),
                searchInputData.getIsLost()
        );
    }

    private void handleSearchResults(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            searchOutputBoundary.prepareFailView(
                    new SearchOutputData("No matching posts found. Try different search terms.")
            );
        } else {
            searchOutputBoundary.prepareSuccessView(new SearchOutputData(posts));
        }
    }

    private void handleSearchError(Exception e) {
        String errorMessage = "Search failed: " + e.getMessage();
        System.err.println(errorMessage);
        searchOutputBoundary.prepareFailView(new SearchOutputData(errorMessage));
    }
}