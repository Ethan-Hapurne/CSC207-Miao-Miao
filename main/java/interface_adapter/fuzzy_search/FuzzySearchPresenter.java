package interface_adapter.fuzzy_search;

import use_case.fuzzy_search.FuzzySearchOutputBoundary;
import use_case.fuzzy_search.FuzzySearchOutputData;

/**
 * Presenter for the fuzzy search feature.
 */
public class FuzzySearchPresenter implements FuzzySearchOutputBoundary {
    private final FuzzySearchViewModel fuzzySearchViewModel;

    public FuzzySearchPresenter(FuzzySearchViewModel fuzzySearchViewModel) {
        this.fuzzySearchViewModel = fuzzySearchViewModel;
    }

    @Override
    public void prepareSuccessView(FuzzySearchOutputData outputData) {
        FuzzySearchState state = new FuzzySearchState();
        state.setSearchResults(outputData.getSearchResults());
        state.setMessage(outputData.getMessage());
        state.setSuccess(true);
        state.setSearchQuery(outputData.getSearchQuery());
        
        fuzzySearchViewModel.setState(state);
        fuzzySearchViewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(FuzzySearchOutputData outputData) {
        FuzzySearchState state = new FuzzySearchState();
        state.setSearchResults(null);
        state.setMessage(outputData.getMessage());
        state.setSuccess(false);
        state.setSearchQuery(outputData.getSearchQuery());
        
        fuzzySearchViewModel.setState(state);
        fuzzySearchViewModel.firePropertyChanged();
    }
}
