// use_case/fuzzy_search/util/handler/RankHandler.java
package use_case.fuzzy_search.util.handler;

import entity.Post;
import java.util.Comparator;
import java.util.List;

public class RankHandler implements SearchHandler {
    private SearchHandler next;

    @Override public void setNext(SearchHandler next) { this.next = next; }

    @Override
    public List<Post> handle(List<Post> current, String query) {
        current.sort(Comparator.comparingInt(Post::getNumberOfLikes).reversed());
        return (next == null) ? current : next.handle(current, query);
    }
}
