package interface_adapter.fuzzy_search;

import interface_adapter.ViewModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * ViewModel for the fuzzy search feature.
 */
public class FuzzySearchViewModel extends ViewModel {
    public static final String TITLE_LABEL = "Fuzzy Search";
    public static final String SEARCH_LABEL = "Search Query";
    public static final String SEARCH_BUTTON_LABEL = "Search";
    public static final String BACK_BUTTON_LABEL = "Back";

    private FuzzySearchState state = new FuzzySearchState();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public FuzzySearchViewModel() {
        super("fuzzy search");
    }

    public void setState(FuzzySearchState state) {
        this.state = state;
    }

    public FuzzySearchState getState() {
        return state;
    }

    public void firePropertyChanged() {
        support.firePropertyChange("state", null, this.state);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
