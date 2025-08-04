package interface_adapter.search;

import use_case.search.SearchOutputBoundary;
import use_case.search.SearchOutputData;

public class SearchPresenter implements SearchOutputBoundary {
    private final SearchViewModel searchViewModel;

    public SearchPresenter(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
    }

    @Override
    public void prepareSuccessView(SearchOutputData searchOutputData) {
        SearchState state = searchViewModel.getState();
        state.setPosts(searchOutputData.getPosts());
        state.setError(null);
        searchViewModel.setState(state);
        searchViewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(SearchOutputData searchOutputData) {
        SearchState state = searchViewModel.getState();
        state.setPosts(null);
        state.setError(searchOutputData.getError());
        searchViewModel.setState(state);
        searchViewModel.firePropertyChanged();
    }
}
