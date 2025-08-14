package use_case.fuzzy_search.util.handler;

import entity.Post;
import java.util.List;

public interface SearchHandler {
    void setNext(SearchHandler next);
    List<Post> handle(List<Post> current, String query);
}
