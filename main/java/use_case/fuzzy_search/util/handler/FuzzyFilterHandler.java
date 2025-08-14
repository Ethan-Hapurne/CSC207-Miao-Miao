package use_case.fuzzy_search.util.handler;

import entity.Post;
import use_case.fuzzy_search.util.FuzzyMatcher;
import java.util.ArrayList;
import java.util.List;

public class FuzzyFilterHandler implements SearchHandler {
    private final int threshold;
    private SearchHandler next;

    public FuzzyFilterHandler(int threshold) { this.threshold = threshold; }

    @Override public void setNext(SearchHandler next) { this.next = next; }

    @Override
    public List<Post> handle(List<Post> current, String query) {
        List<Post> filtered = new ArrayList<>();
        for (Post p : current) {
            String hay = ((p.getTitle() == null ? "" : p.getTitle()) + " " +
                    (p.getDescription() == null ? "" : p.getDescription())).toLowerCase();
            if (FuzzyMatcher.isFuzzyMatch(hay, query, threshold)) filtered.add(p);
        }
        return (next == null) ? filtered : next.handle(filtered, query);
    }
}
