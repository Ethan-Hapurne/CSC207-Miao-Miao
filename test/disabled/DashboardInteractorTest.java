package app.use_case.dashboard;

import entity.Post;
import use_case.dashboard.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardInteractorTest {

    @Test
    void testLoadPostsSuccess() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        DashboardInteractor interactor = new DashboardInteractor(dao, presenter);

        interactor.execute(new DashboardInputData("load_posts"));

        assertTrue(presenter.successCalled);
        assertEquals(2, presenter.lastOutput.getPosts().size());
    }

    @Test
    void testSearchPostsSuccess() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        DashboardInteractor interactor = new DashboardInteractor(dao, presenter);

        interactor.execute(new DashboardInputData("search_posts", "Java"));

        assertTrue(presenter.successCalled);
        assertEquals(1, presenter.lastOutput.getPosts().size());
        assertEquals("Java Basics", presenter.lastOutput.getPosts().get(0).getTitle());
    }

    @Test
    void testSearchPostsWithEmptyQueryReturnsSortedAll() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        DashboardInteractor interactor = new DashboardInteractor(dao, presenter);

        interactor.execute(new DashboardInputData("search_posts", ""));

        assertTrue(presenter.successCalled);
        List<Post> sorted = presenter.lastOutput.getPosts();
        assertEquals("Algorithms", sorted.get(0).getTitle());
        assertEquals("Java Basics", sorted.get(1).getTitle());
    }

    @Test
    void testAddPostMissingTitleFails() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        DashboardInteractor interactor = new DashboardInteractor(dao, presenter);

        interactor.execute(new DashboardInputData("add_post", "", "Some content",
                new ArrayList<>(), "Library", true));

        assertTrue(presenter.failCalled);
        assertEquals("Post title and content are required.", presenter.lastOutput.getError());
    }

    @Test
    void testInvalidActionFails() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        DashboardInteractor interactor = new DashboardInteractor(dao, presenter);

        interactor.execute(new DashboardInputData("invalid_action"));

        assertTrue(presenter.failCalled);
        assertEquals("Invalid action.", presenter.lastOutput.getError());
    }

    // ----------------------
    // Fake classes for test
    // ----------------------

    private static class FakeDAO implements DashboardUserDataAccessInterface {
        @Override
        public List<Post> getAllPosts() {
            List<Post> posts = new ArrayList<>();
            posts.add(new Post(1, "Java Basics", "Learn Java", new ArrayList<>(),
                    LocalDateTime.now(), "Alice", "Room A", "", true, 0, new HashMap<>()));
            posts.add(new Post(2, "Algorithms", "Sorting stuff", new ArrayList<>(),
                    LocalDateTime.now(), "Bob", "Room B", "", false, 0, new HashMap<>()));
            return posts;
        }

        @Override
        public List<Post> searchPosts(String query) {
            List<Post> result = new ArrayList<>();
            if (query.toLowerCase().contains("java")) {
                result.add(new Post(1, "Java Basics", "Learn Java", new ArrayList<>(),
                        LocalDateTime.now(), "Alice", "Room A", "", true, 0, new HashMap<>()));
            }
            return result;
        }

        @Override
        public Post getPostById(int postID) {
            return null;
        }

        @Override
        public Post addPost(String title, String content, List<String> tags, String location,
                            boolean isLost, String author) {
            return new Post(3, title, content, tags, LocalDateTime.now(), author,
                    location, "", isLost, 0, new HashMap<>());
        }
    }

    private static class FakePresenter implements DashboardOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        DashboardOutputData lastOutput;

        @Override
        public void prepareSuccessView(DashboardOutputData dashboardOutputData) {
            successCalled = true;
            lastOutput = dashboardOutputData;
        }

        @Override
        public void prepareFailView(DashboardOutputData dashboardOutputData) {
            failCalled = true;
            lastOutput = dashboardOutputData;
        }
    }
}
