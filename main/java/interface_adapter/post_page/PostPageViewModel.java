package interface_adapter.post_page;

import interface_adapter.ViewModel;

public class PostPageViewModel extends ViewModel<PostPageState> {
    public PostPageViewModel() {
        super("post page");
        setState(new PostPageState());
    }
}
