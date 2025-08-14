package use_case.admin;

import data_access.FirebasePostDataAccessObject;
import entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.admin.AdminInputData;
import use_case.admin.AdminInteractor;
import use_case.admin.AdminOutputBoundary;
import use_case.admin.AdminOutputData;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class AdminInteractorTest {
    private FakeAdminDAO adminDataAccessObject;
    private FakeAdminPresenter adminOutputBoundary;
    private AdminInteractor adminInteractor;

    @BeforeEach
    void setUp() {
        adminDataAccessObject = new FakeAdminDAO();
        adminOutputBoundary = new FakeAdminPresenter();
        adminInteractor = new AdminInteractor(adminDataAccessObject, adminOutputBoundary);
    }

    @Test
    void testLoadPosts() {
        AdminInputData inputData = new AdminInputData("load_posts");
        adminInteractor.execute(inputData);

        assertTrue(adminOutputBoundary.isSuccessViewCalled());
        assertFalse(adminOutputBoundary.isFailViewCalled());
        assertEquals(2, adminOutputBoundary.getLastOutputData().getPosts().size());
    }

    @Test
    void testSearchPostsWithValidQuery() {
        AdminInputData inputData = new AdminInputData("search_posts", "Java");
        adminInteractor.execute(inputData);

        assertTrue(adminOutputBoundary.isSuccessViewCalled());
        assertFalse(adminOutputBoundary.isFailViewCalled());
        assertEquals(1, adminOutputBoundary.getLastOutputData().getPosts().size());
        assertEquals("Java Book", adminOutputBoundary.getLastOutputData().getPosts().get(0).getTitle());
    }

    @Test
    void testSearchPostsWithEmptyQuery() {
        AdminInputData inputData = new AdminInputData("search_posts", "");
        adminInteractor.execute(inputData);

        assertTrue(adminOutputBoundary.isSuccessViewCalled());
        assertFalse(adminOutputBoundary.isFailViewCalled());
        assertEquals(2, adminOutputBoundary.getLastOutputData().getPosts().size());
    }

    @Test
    void testAddPostWithValidData() {
        AdminInputData inputData = new AdminInputData("add_post", "New Post", "Content",
                Arrays.asList("tag1", "tag2"), "Location", true, "author1");
        adminInteractor.execute(inputData);

        assertTrue(adminOutputBoundary.isSuccessViewCalled());
        assertFalse(adminOutputBoundary.isFailViewCalled());
    }

    @Test
    void testAddPostWithInvalidData() {
        AdminInputData inputData = new AdminInputData("add_post", "", "", null, null, true, null);
        adminInteractor.execute(inputData);

        assertFalse(adminOutputBoundary.isSuccessViewCalled());
        assertTrue(adminOutputBoundary.isFailViewCalled());
    }

    @Test
    void testEditPostSuccess() {
        AdminInputData inputData = new AdminInputData("edit_post", "1", "Updated Title",
                "Updated Content", Arrays.asList("tag1"), "New Location", true, "author1");
        adminInteractor.execute(inputData);

        assertTrue(adminOutputBoundary.isSuccessViewCalled());
        assertFalse(adminOutputBoundary.isFailViewCalled());
    }

    @Test
    void testDeletePostSuccess() {
        AdminInputData inputData = new AdminInputData("delete_post", "1", true);
        adminInteractor.execute(inputData);

        assertTrue(adminOutputBoundary.isSuccessViewCalled());
        assertFalse(adminOutputBoundary.isFailViewCalled());
    }

    @Test
    void testDeletePostWithInvalidId() {
        AdminInputData inputData = new AdminInputData("delete_post", "", true);
        adminInteractor.execute(inputData);

        assertFalse(adminOutputBoundary.isSuccessViewCalled());
        assertTrue(adminOutputBoundary.isFailViewCalled());
    }

    @Test
    void testInvalidAction() {
        AdminInputData inputData = new AdminInputData("invalid_action");
        adminInteractor.execute(inputData);

        assertFalse(adminOutputBoundary.isSuccessViewCalled());
        assertTrue(adminOutputBoundary.isFailViewCalled());
    }

    // Fake classes for testing
    private static class FakeAdminDAO extends FirebasePostDataAccessObject {
        private final List<Post> posts = new ArrayList<>();

        public FakeAdminDAO() {
            posts.add(new Post(1, "Java Book", "Description", null, null, "author1", "Location1", null, true, 0, null));
            posts.add(new Post(2, "Python Book", "Description", null, null, "author2", "Location2", null, false, 0, null));
        }

        @Override
        public List<Post> getAllPosts() {
            return new ArrayList<>(posts);
        }

        @Override
        public List<Post> searchPosts(String query) {
            return posts.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(query.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public Post addPost(String title, String content, List<String> tags, String location, boolean isLost, String author) {
            Post post = new Post(posts.size() + 1, title, content, tags, null, author, location, null, isLost, 0, null);
            posts.add(post);
            return post;
        }

        @Override
        public boolean editPost(String postId, String title, String content, String location, List<String> tags, boolean isLost) {
            return true;
        }

        @Override
        public void deletePost(String postId) {
            posts.removeIf(p -> p.getPostID() == Integer.parseInt(postId));
        }

        @Override
        public boolean existsPost(String postId) {
            return postId != null && !postId.trim().isEmpty() &&
                    posts.stream().anyMatch(p -> p.getPostID() == Integer.parseInt(postId));
        }

        @Override
        public Post getPostById(int postId) {
            return posts.stream()
                    .filter(p -> p.getPostID() == postId)
                    .findFirst()
                    .orElse(null);
        }
    }

    private static class FakeAdminPresenter implements AdminOutputBoundary {
        private boolean successViewCalled = false;
        private boolean failViewCalled = false;
        private AdminOutputData lastOutputData;

        @Override
        public void prepareSuccessView(AdminOutputData outputData) {
            successViewCalled = true;
            lastOutputData = outputData;
        }

        @Override
        public void prepareFailView(AdminOutputData outputData) {
            failViewCalled = true;
            lastOutputData = outputData;
        }

        public boolean isSuccessViewCalled() {
            return successViewCalled;
        }

        public boolean isFailViewCalled() {
            return failViewCalled;
        }

        public AdminOutputData getLastOutputData() {
            return lastOutputData;
        }
    }
}
