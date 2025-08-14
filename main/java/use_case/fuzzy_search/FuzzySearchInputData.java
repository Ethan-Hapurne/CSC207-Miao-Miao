package use_case.fuzzy_search;

/**
 * Input data for the fuzzy search use case.
 */
public class FuzzySearchInputData {
    private final String searchQuery;

    public FuzzySearchInputData(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
