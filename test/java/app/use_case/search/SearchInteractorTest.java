package app.use_case.search;

import entity.Post;
import org.junit.jupiter.api.Test;
import use_case.search.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchInteractorTest {

    @Test
    void testQuerySearchReturnsResults() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        SearchInteractor interactor = new SearchInteractor(dao, presenter);

        interactor.execute(new SearchInputData("Java"));

        assertTrue(presenter.successCalled);
        assertEquals(1, presenter.lastOutput.getPosts().size());
        assertEquals("Java Book", presenter.lastOutput.getPosts().get(0).getTitle());
    }

    @Test
    void testQuerySearchNoResults() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        SearchInteractor interactor = new SearchInteractor(dao, presenter);

        interactor.execute(new SearchInputData("Nonexistent"));

        assertTrue(presenter.failCalled);
        assertEquals("No posts found matching your search criteria.", presenter.lastOutput.getError());
    }

    @Test
    void testCriteriaSearchReturnsResults() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        SearchInteractor interactor = new SearchInteractor(dao, presenter);

        interactor.execute(new SearchInputData("Math Notes", "Library", Arrays.asList("notes"), true));

        assertTrue(presenter.successCalled);
        assertEquals(1, presenter.lastOutput.getPosts().size());
        assertEquals("Math Notes", presenter.lastOutput.getPosts().get(0).getTitle());
    }

    @Test
    void testCriteriaSearchNoResults() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();
        SearchInteractor interactor = new SearchInteractor(dao, presenter);

        interactor.execute(new SearchInputData("X", "Nowhere", Arrays.asList("random"), false));

        assertTrue(presenter.failCalled);
        assertEquals("No posts found matching your search criteria.", presenter.lastOutput.getError());
    }

    // ==== Fake classes ====

    private static class FakeDAO implements SearchUserDataAccessInterface {
        private final List<Post> samplePosts;

        public FakeDAO() {
            LocalDateTime now = LocalDateTime.now();
            samplePosts = new ArrayList<>();
            samplePosts.add(new Post(
                    1, "Java Book", "Intro to Java", null,
                    now, "Alice", "Room A", null,
                    true, 0, null
            ));
            samplePosts.add(new Post(
                    2, "Math Notes", "Lost notes", Arrays.asList("notes", "math"),
                    now, "Bob", "Library", null,
                    true, 0, null
            ));
        }

        @Override
        public List<Post> searchPosts(String query) {
            List<Post> result = new ArrayList<>();
            for (Post p : samplePosts) {
                if (p.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    result.add(p);
                }
            }
            return result;
        }

        @Override
        public List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost) {
            List<Post> result = new ArrayList<>();
            for (Post p : samplePosts) {
                boolean matches = (title == null || p.getTitle().equalsIgnoreCase(title)) &&
                        (location == null || p.getLocation().equalsIgnoreCase(location)) &&
                        (tags == null || (p.getTags() != null && p.getTags().containsAll(tags))) &&
                        (isLost == null || p.isLost() == isLost);
                if (matches) result.add(p);
            }
            return result;
        }


    }

    private static class FakePresenter implements SearchOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        SearchOutputData lastOutput;

        @Override
        public void prepareSuccessView(SearchOutputData searchOutputData) {
            successCalled = true;
            lastOutput = searchOutputData;
        }

        @Override
        public void prepareFailView(SearchOutputData searchOutputData) {
            failCalled = true;
            lastOutput = searchOutputData;
        }
    }
}
