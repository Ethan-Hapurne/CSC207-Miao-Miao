package use_case.fuzzy_search.util;

import entity.Post;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FuzzyMatchHelper (independent of the Search use case).
 */
class FuzzyMatchHelperTest {

    private Post createPost(String title, String description, List<String> tags, String location) {
        Post p = new Post();
        p.setPostID(1);
        p.setTitle(title);
        p.setDescription(description);
        p.setTags(tags);
        p.setLocation(location);
        p.setAuthor("test_author");
        // setTimestamp expects String in your Post
        p.setTimestamp("2025-08-11T10:00:00");
        // remove: p.setImageUrl(""); // method does not exist
        p.setLost(true);
        p.setReactions(new HashMap<>());
        return p;
    }

    @Test
    void exactMatch_onTitle() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Lost Phone", "Black iPhone", List.of("electronics"), "Library"));
        posts.add(createPost("Wallet", "Brown leather wallet", List.of("accessory"), "Cafeteria"));

        var results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "phone");
        assertEquals(1, results.size());
        assertEquals("Lost Phone", results.get(0).getTitle());
    }

    @Test
    void synonymMatch_onTitle() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Cellphone", "Android device", List.of("electronics"), "Gym"));
        posts.add(createPost("Keys", "House keys", List.of("key", "metal"), "Dorm"));

        var results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "phone");
        assertEquals(1, results.size());
        assertEquals("Cellphone", results.get(0).getTitle());
    }

    @Test
    void spellingTolerance_onTitle() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Computer", "MacBook Pro", List.of("electronics"), "Library"));
        posts.add(createPost("Book", "Calculus Textbook", List.of("education"), "Classroom"));

        var results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "computor"); // intentional typo
        assertEquals(1, results.size());
        assertEquals("Computer", results.get(0).getTitle());
    }

    @Test
    void noMatch_returnsEmptyList() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Watch", "Apple Watch", List.of("electronics"), "Gym"));
        posts.add(createPost("Umbrella", "Red umbrella", List.of("weather"), "Cafe"));

        var results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "notebook");
        assertTrue(results.isEmpty());
    }

    @Test
    void matchInDescription() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Bag", "This contains a phone", List.of("accessory"), "Library"));

        var results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "phone");
        assertEquals(1, results.size());
        assertEquals("Bag", results.get(0).getTitle());
    }

    @Test
    void matchInTags_orLocation() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("USB Drive", "Storage device", List.of("computer", "storage"), "Lab"));
        posts.add(createPost("Charger", "Laptop charger", List.of("electronics"), "Computer Lab"));

        var r1 = FuzzyMatchHelper.fuzzyMatchPosts(posts, "computor"); // intentional typo
        assertEquals(2, r1.size());
        assertTrue(r1.stream().anyMatch(p -> p.getTitle().equals("USB Drive")));
        assertTrue(r1.stream().anyMatch(p -> p.getTitle().equals("Charger")));
    }
}
