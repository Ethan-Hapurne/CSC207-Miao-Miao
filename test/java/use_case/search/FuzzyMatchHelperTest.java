package use_case.search;

import entity.Post;
import org.junit.jupiter.api.Test;
import use_case.search.util.FuzzyMatchHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzyMatchHelperTest {

    private Post createPost(String title, String description, List<String> tags, String location) {
        return new Post(
                1,
                title,
                description,
                tags,
                LocalDateTime.now(),
                "test_author",
                location,
                "",
                true,
                0,
                new HashMap<>()
        );
    }

    @Test
    void testExactMatch() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Lost Phone", "Black iPhone", List.of("electronics"), "Library"));
        posts.add(createPost("Wallet", "Brown leather wallet", List.of("accessory"), "Cafeteria"));

        List<Post> results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "phone");
        assertEquals(1, results.size());
        assertEquals("Lost Phone", results.get(0).getTitle());
    }

    @Test
    void testSynonymMatch() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Cellphone", "Android device", List.of("electronics"), "Gym"));
        posts.add(createPost("Keys", "Set of house keys", List.of("key", "metal"), "Dorm"));

        List<Post> results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "phone");
        assertEquals(1, results.size());
        assertEquals("Cellphone", results.get(0).getTitle());
    }

    @Test
    void testSpellingTolerance() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Computer", "MacBook Pro", List.of("electronics"), "Library"));
        posts.add(createPost("Book", "Calculus Textbook", List.of("education"), "Classroom"));

        List<Post> results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "computor");
        assertEquals(1, results.size());
        assertEquals("Computer", results.get(0).getTitle());
    }

    @Test
    void testNoMatch() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Watch", "Apple Watch", List.of("electronics"), "Gym"));
        posts.add(createPost("Umbrella", "Red umbrella", List.of("weather"), "Cafe"));

        List<Post> results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "notebook");
        assertTrue(results.isEmpty());
    }

    @Test
    void testMatchInDescription() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Bag", "This contains a phone", List.of("accessory"), "Library"));

        List<Post> results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "phone");
        assertEquals(1, results.size());
        assertEquals("Bag", results.get(0).getTitle());
    }

    @Test
    void testMatchInTags() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("USB Drive", "Storage device", List.of("computer", "storage"), "Lab"));

        List<Post> results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "computor");
        assertEquals(1, results.size());
        assertEquals("USB Drive", results.get(0).getTitle());
    }

    @Test
    void testMatchInLocation() {
        List<Post> posts = new ArrayList<>();
        posts.add(createPost("Charger", "Laptop charger", List.of("electronics"), "Computer Lab"));

        List<Post> results = FuzzyMatchHelper.fuzzyMatchPosts(posts, "computor");
        assertEquals(1, results.size());
        assertEquals("Charger", results.get(0).getTitle());
    }
}