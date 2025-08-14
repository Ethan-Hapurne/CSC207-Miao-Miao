package use_case.search;

import entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SearchInteractor (compatible with single-arg SearchInputData).
 * Focus:
 *  - Query search path (searchPosts)
 *  - Criteria search path (searchPostsByCriteria)
 *  - Presenter success/fail flows
 */
class SearchInteractorTest {

    private SearchUserDataAccessInterface dao;
    private SearchOutputBoundary presenter;
    private SearchInteractor interactor;

    @BeforeEach
    void setUp() {
        dao = mock(SearchUserDataAccessInterface.class);
        presenter = mock(SearchOutputBoundary.class);
        interactor = new SearchInteractor(dao, presenter);
    }

    // Helper to create a minimal Post
    private Post makePost(int id, String title) {
        Post p = new Post();
        p.setPostID(id);
        p.setTitle(title);
        return p;
    }

    @Test
    void execute_query_success_callsSearchPosts_andPresenterSuccess() {
        String query = "wallet";
        List<Post> results = Arrays.asList(
                makePost(1, "Lost wallet at library"),
                makePost(2, "Wallet found near cafeteria")
        );
        when(dao.searchPosts(query)).thenReturn(results);

        SearchInputData input = new SearchInputData(query);

        interactor.execute(input);

        // DAO interaction
        verify(dao, times(1)).searchPosts(query);
        verify(dao, never()).searchPostsByCriteria(any(), any(), any(), any());

        // Presenter success
        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareSuccessView(cap.capture());
        verify(presenter, never()).prepareFailView(any());

        SearchOutputData out = cap.getValue();
        assertFalse(out.hasError());
        assertNotNull(out.getPosts());
        assertEquals(2, out.getPosts().size());
        assertEquals("Lost wallet at library", out.getPosts().get(0).getTitle());
    }

    @Test
    void execute_criteria_success_callsSearchPostsByCriteria_andPresenterSuccess() {
        String title = "phone";
        String location = "library";
        List<String> tags = Arrays.asList("electronics", "black");
        Boolean isLost = true;

        List<Post> results = Arrays.asList(
                makePost(10, "Lost phone at library"),
                makePost(11, "Black phone missing")
        );
        when(dao.searchPostsByCriteria(title, location, tags, isLost)).thenReturn(results);

        SearchInputData input = new SearchInputData(title, location, tags, isLost);

        interactor.execute(input);

        // DAO interaction
        verify(dao, times(1)).searchPostsByCriteria(title, location, tags, isLost);
        verify(dao, never()).searchPosts(anyString());

        // Presenter success
        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareSuccessView(cap.capture());
        verify(presenter, never()).prepareFailView(any());

        SearchOutputData out = cap.getValue();
        assertFalse(out.hasError());
        assertEquals(2, out.getPosts().size());
        assertEquals("Lost phone at library", out.getPosts().get(0).getTitle());
    }

    @Test
    void execute_query_empty_results_callsFailPresenter() {
        String query = "no-such-item";
        when(dao.searchPosts(query)).thenReturn(Collections.emptyList());

        SearchInputData input = new SearchInputData(query);

        interactor.execute(input);

        verify(dao, times(1)).searchPosts(query);

        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        verify(presenter, never()).prepareSuccessView(any());

        SearchOutputData out = cap.getValue();
        assertTrue(out.hasError());
        assertNull(out.getPosts());
        assertNotNull(out.getError());
    }

    @Test
    void execute_query_daoThrows_callsFailPresenter() {
        String query = "error-case";
        when(dao.searchPosts(query)).thenThrow(new RuntimeException("DAO down"));

        SearchInputData input = new SearchInputData(query);

        interactor.execute(input);

        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        verify(presenter, never()).prepareSuccessView(any());

        SearchOutputData out = cap.getValue();
        assertTrue(out.hasError());
        assertNotNull(out.getError());
        assertTrue(out.getError().contains("DAO down"));
    }
}
