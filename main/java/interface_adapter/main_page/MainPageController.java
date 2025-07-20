package interface_adapter.main_page;

import interface_adapter.ViewManagerModel;
import interface_adapter.post_page.PostPageViewModel;

/**
 * The controller for the Login Use Case.
 */
public class MainPageController {
    private final PostPageViewModel postPageViewModel;
    private final ViewManagerModel viewManagerModel;

    public MainPageController(PostPageViewModel postPageViewModel, ViewManagerModel viewManagerModel) {
        this.postPageViewModel = postPageViewModel;
        this.viewManagerModel = viewManagerModel;
    }

    public void navigateToPostsPage() {
        this.postPageViewModel.firePropertyChanged();
        this.viewManagerModel.setState(postPageViewModel.getViewName());
        this.viewManagerModel.firePropertyChanged();
    }
}
