package use_case.fuzzy_search;

/**
 * Input boundary for the fuzzy search use case.
 */
public interface FuzzySearchInputBoundary {
    /**
     * Executes fuzzy search on posts.
     * @param fuzzySearchInputData the input data containing the search query
     */
    void execute(FuzzySearchInputData fuzzySearchInputData);
}
