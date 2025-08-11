package use_case.fuzzy_search;

import entity.Post;
import use_case.fuzzy_search.util.FuzzyMatchHelper;

import java.util.List;

/**
 * Interactor for the fuzzy search use case.
 * Implements the business logic for fuzzy searching posts.
 */
public class FuzzySearchInteractor implements FuzzySearchInputBoundary {
    private final FuzzySearchUserDataAccessInterface fuzzySearchDataAccessObject;
    private final FuzzySearchOutputBoundary fuzzySearchOutputBoundary;

    public FuzzySearchInteractor(FuzzySearchUserDataAccessInterface fuzzySearchDataAccessObject,
                                FuzzySearchOutputBoundary fuzzySearchOutputBoundary) {
        this.fuzzySearchDataAccessObject = fuzzySearchDataAccessObject;
        this.fuzzySearchOutputBoundary = fuzzySearchOutputBoundary;
    }

    @Override
    public void execute(FuzzySearchInputData fuzzySearchInputData) {
        try {
            String searchQuery = fuzzySearchInputData.getSearchQuery();
            
            // Validate input
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                fuzzySearchOutputBoundary.prepareFailView(
                    new FuzzySearchOutputData("Search query cannot be empty.", false, searchQuery)
                );
                return;
            }

            // Get all posts from data access
            List<Post> allPosts = fuzzySearchDataAccessObject.getAllPosts();
            System.out.println("DEBUG: FuzzySearchInteractor - Retrieved " + (allPosts != null ? allPosts.size() : "null") + " posts from data access");
            
            if (allPosts == null || allPosts.isEmpty()) {
                System.out.println("DEBUG: FuzzySearchInteractor - No posts available for search");
                fuzzySearchOutputBoundary.prepareFailView(
                    new FuzzySearchOutputData("No posts available for search.", false, searchQuery)
                );
                return;
            }

            // Perform fuzzy search using existing utility
            List<Post> searchResults = FuzzyMatchHelper.fuzzyMatchPosts(allPosts, searchQuery);
            System.out.println("DEBUG: FuzzySearchInteractor - Fuzzy search returned " + searchResults.size() + " results");
            
            if (searchResults.isEmpty()) {
                String message = String.format("No results found for '%s'. Try different keywords or check spelling.", searchQuery);
                System.out.println("DEBUG: FuzzySearchInteractor - No results found, calling prepareFailView");
                fuzzySearchOutputBoundary.prepareFailView(
                    new FuzzySearchOutputData(message, false, searchQuery)
                );
            } else {
                String message = String.format("Found %d results for '%s'", searchResults.size(), searchQuery);
                System.out.println("DEBUG: FuzzySearchInteractor - Results found, calling prepareSuccessView with message: " + message);
                fuzzySearchOutputBoundary.prepareSuccessView(
                    new FuzzySearchOutputData(searchResults, message, true, searchQuery)
                );
            }

        } catch (Exception e) {
            fuzzySearchOutputBoundary.prepareFailView(
                new FuzzySearchOutputData("An error occurred during search: " + e.getMessage(), false, fuzzySearchInputData.getSearchQuery())
            );
        }
    }
}
