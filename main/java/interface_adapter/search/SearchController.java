package interface_adapter.search;

import interface_adapter.ViewManagerModel;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInputData;

import java.util.List;

/**
 * The controller for the Search View.
 */
public class SearchController {

    private final SearchInputBoundary searchInteractor;
    private final ViewManagerModel viewManagerModel;

    public SearchController(SearchInputBoundary searchInteractor, ViewManagerModel viewManagerModel) {
        this.searchInteractor = searchInteractor;
        this.viewManagerModel = viewManagerModel;
    }

    /**
     * Executes a basic search with query.
     * @param searchQuery the search query entered by the user
     */
    public void execute(String searchQuery) {
        SearchInputData searchInputData = new SearchInputData(searchQuery);
        searchInteractor.execute(searchInputData);
    }

    /**
     * Executes advanced search with specific criteria.
     * @param title title to search for
     * @param location location to search for
     * @param tags tags to search for
     * @param isLost whether to search for lost or found items
     */
    public void executeAdvancedSearch(String title, String location, List<String> tags, Boolean isLost) {
        SearchInputData searchInputData = new SearchInputData(title, location, tags, isLost);
        searchInteractor.execute(searchInputData);
    }

    /**
     * Navigates back to the previous view.
     */
    public void navigateBack() {
        viewManagerModel.popViewOrClose();
    }
}
