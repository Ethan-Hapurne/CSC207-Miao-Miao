package data_access;

import entity.Post;
import use_case.search.SearchUserDataAccessInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import use_case.search.util.FuzzyMatchHelper;

/**
 * In-memory implementation of SearchUserDataAccessInterface for testing.
 */
public class SearchDataAccessMemory implements SearchUserDataAccessInterface {
    private final List<Post> posts;

    public SearchDataAccessMemory(List<Post> initialPosts) {
        this.posts = new ArrayList<>(initialPosts);
    }

    @Override
    public List<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }

    @Override
    public List<Post> searchPosts(String query) {
        return posts.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                        p.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> fuzzySearch(String query) {
        return FuzzyMatchHelper.fuzzyMatchPosts(posts, query);
    }

    @Override
    public List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost) {
        return posts.stream()
                .filter(p -> (title == null || p.getTitle().equalsIgnoreCase(title)) &&
                        (location == null || p.getLocation().equalsIgnoreCase(location)) &&
                        (tags == null || p.getTags().containsAll(tags)) &&
                        (isLost == null || p.isLost() == isLost))
                .collect(Collectors.toList());
    }
}