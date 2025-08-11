package interface_adapter.fuzzy_search;

import use_case.fuzzy_search.FuzzySearchInputBoundary;
import use_case.fuzzy_search.FuzzySearchInputData;

/**
 * Controller for the fuzzy search feature.
 */
public class FuzzySearchController {
    private final FuzzySearchInputBoundary fuzzySearchInteractor;

    public FuzzySearchController(FuzzySearchInputBoundary fuzzySearchInteractor) {
        this.fuzzySearchInteractor = fuzzySearchInteractor;
    }

    /**
     * Executes fuzzy search with the given query.
     * @param searchQuery the search query to execute
     */
    public void executeFuzzySearch(String searchQuery) {
        FuzzySearchInputData inputData = new FuzzySearchInputData(searchQuery);
        fuzzySearchInteractor.execute(inputData);
    }
}
