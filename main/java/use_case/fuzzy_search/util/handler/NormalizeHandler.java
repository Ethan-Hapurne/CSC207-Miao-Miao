package use_case.fuzzy_search.util.handler;

import entity.Post;
import java.util.List;

public class NormalizeHandler implements SearchHandler {
    private SearchHandler next;

    @Override public void setNext(SearchHandler next) { this.next = next; }

    @Override
    public List<Post> handle(List<Post> current, String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        return (next == null) ? current : next.handle(current, q);
    }
}
