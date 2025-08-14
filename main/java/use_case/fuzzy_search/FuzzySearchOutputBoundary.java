package use_case.fuzzy_search;

/**
 * Output boundary for the fuzzy search use case.
 */
public interface FuzzySearchOutputBoundary {
    /**
     * Prepares the success view when fuzzy search is successful.
     * @param fuzzySearchOutputData the output data containing search results
     */
    void prepareSuccessView(FuzzySearchOutputData fuzzySearchOutputData);

    /**
     * Prepares the fail view when fuzzy search fails.
     * @param fuzzySearchOutputData the output data containing error information
     */
    void prepareFailView(FuzzySearchOutputData fuzzySearchOutputData);
}
