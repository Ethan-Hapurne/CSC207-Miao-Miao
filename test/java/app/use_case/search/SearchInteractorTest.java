package app.use_case.search;

import entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.search.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SearchInteractorTest {

    private SearchUserDataAccessInterface fakeDAO;
    private DummySearchPresenter presenter;

    @BeforeEach
    public void setUp() {
        this.fakeDAO = new FakeSearchDAO();
        this.presenter = new DummySearchPresenter();
    }

    @Test
    public void testExactSearchFound() {
        SearchInputData input = new SearchInputData("computer", false);
        SearchInteractor interactor = new SearchInteractor(fakeDAO, presenter);
        interactor.execute(input);

        assertNull(presenter.outputData.getError());
        assertEquals(1, presenter.outputData.getPosts().size());
        assertEquals("Lost Computer", presenter.outputData.getPosts().get(0).getTitle());
    }

    @Test
    public void testExactSearchNotFound() {
        SearchInputData input = new SearchInputData("banana", false);
        SearchInteractor interactor = new SearchInteractor(fakeDAO, presenter);
        interactor.execute(input);

        assertTrue(presenter.outputData.hasError());
        assertEquals("No matching posts found. Try different search terms.", presenter.outputData.getError());
    }

    @Test
    public void testFuzzySearchFound() {
        SearchInputData input = new SearchInputData("computor", true);
        SearchInteractor interactor = new SearchInteractor(fakeDAO, presenter);
        interactor.execute(input);

        assertNull(presenter.outputData.getError());
        assertEquals(1, presenter.outputData.getPosts().size());
        assertEquals("Lost Computer", presenter.outputData.getPosts().get(0).getTitle());
    }

    // Dummy Presenter for capturing output
    private static class DummySearchPresenter implements SearchOutputBoundary {
        SearchOutputData outputData;

        @Override
        public void prepareSuccessView(SearchOutputData searchOutputData) {
            this.outputData = searchOutputData;
        }

        @Override
        public void prepareFailView(SearchOutputData searchOutputData) {
            this.outputData = searchOutputData;
        }
    }

    // Fake DAO with hardcoded post list
    private static class FakeSearchDAO implements SearchUserDataAccessInterface {

        private final List<Post> fakePosts;

        public FakeSearchDAO() {
            Post post1 = new Post(
                    1,
                    "Lost Computer",
                    "Black Dell laptop with stickers",
                    Arrays.asList("electronics", "laptop"),
                    LocalDateTime.now(),
                    "Alice",
                    "Library",
                    null,
                    true,
                    0,
                    new HashMap<>()
            );
            this.fakePosts = Collections.singletonList(post1);
        }

        @Override
        public List<Post> getAllPosts() {
            return fakePosts;
        }

        @Override
        public List<Post> searchPosts(String query) {
            List<Post> result = new ArrayList<>();
            for (Post post : fakePosts) {
                if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        post.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    result.add(post);
                }
            }
            return result;
        }

        @Override
        public List<Post> fuzzySearch(String query) {
            // Simulate fuzzy match for "computor" → "computer"
            if ("computor".equalsIgnoreCase(query)) {
                return fakePosts;
            }
            return new ArrayList<>();
        }

        @Override
        public List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost) {
            return new ArrayList<>();
        }
    }
}
