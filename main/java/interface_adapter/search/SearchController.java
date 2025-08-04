package interface_adapter.search;

import interface_adapter.ViewManagerModel;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInputData;
import java.util.List;

public class SearchController {
    private final SearchInputBoundary searchInteractor;
    private final ViewManagerModel viewManagerModel;

    public SearchController(SearchInputBoundary searchInteractor,
                            ViewManagerModel viewManagerModel) {
        this.searchInteractor = searchInteractor;
        this.viewManagerModel = viewManagerModel;
    }

    public void execute(String searchQuery, boolean isFuzzy) {
        SearchInputData searchInputData = new SearchInputData(searchQuery, isFuzzy);
        searchInteractor.execute(searchInputData);
    }

    public void executeAdvancedSearch(String title, String location,
                                      List<String> tags, Boolean isLost) {
        SearchInputData searchInputData = new SearchInputData(title, location, tags, isLost);
        searchInteractor.execute(searchInputData);
    }

    public void navigateBack() {
        viewManagerModel.popViewOrClose();
    }
}