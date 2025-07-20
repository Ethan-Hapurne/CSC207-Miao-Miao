package interface_adapter.main_page;

import interface_adapter.ViewModel;

public class MainPageViewModel extends ViewModel<MainPageState> {
    public MainPageViewModel() {
        super("main page");
        setState(new MainPageState());
    }
}
