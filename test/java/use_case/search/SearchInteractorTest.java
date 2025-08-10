package use_case.search;

import entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SearchInteractor.
 * Targets:
 *  - Exercise both paths: query search (regular/fuzzy) and criteria search
 *  - Verify which DAO method is called for each input shape
 *  - Verify presenter receives success/fail with expected content
 *  - Cover error handling and empty-result branches
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

    // Helper: create a simple Post with minimal fields through setters
    private Post makePost(int id, String title) {
        Post p = new Post(); // no-arg constructor exists in your entity
        p.setPostID(id);
        p.setTitle(title);
        return p;
    }

    @Test
    void execute_regularQuery_success_callsSearchPosts_andPresenterSuccess() {
        // Arrange
        String query = "wallet";
        List<Post> results = Arrays.asList(
                makePost(1, "Lost wallet at library"),
                makePost(2, "Wallet found near cafeteria")
        );
        when(dao.searchPosts(query)).thenReturn(results);

        SearchInputData input = new SearchInputData(query, /*isFuzzy=*/false);

        // Act
        interactor.execute(input);

        // Assert DAO
        verify(dao, times(1)).searchPosts(query);
        verify(dao, never()).fuzzySearch(anyString());
        verify(dao, never()).searchPostsByCriteria(any(), any(), any(), any());

        // Assert Presenter
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
    void execute_fuzzyQuery_success_callsFuzzySearch_only_andPresenterSuccess() {
        // Arrange
        String query = "walet"; // typo to simulate fuzzy
        List<Post> results = Collections.singletonList(makePost(7, "Wallet near gym"));
        when(dao.fuzzySearch(query)).thenReturn(results);

        SearchInputData input = new SearchInputData(query, /*isFuzzy=*/true);

        // Act
        interactor.execute(input);

        // Assert DAO called correctly
        verify(dao, times(1)).fuzzySearch(query);
        verify(dao, never()).searchPosts(anyString());
        verify(dao, never()).searchPostsByCriteria(any(), any(), any(), any());

        // Assert Presenter success
        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareSuccessView(cap.capture());
        verify(presenter, never()).prepareFailView(any());

        assertFalse(cap.getValue().hasError());
        assertEquals(1, cap.getValue().getPosts().size());
        assertEquals("Wallet near gym", cap.getValue().getPosts().get(0).getTitle());
    }

    @Test
    void execute_criteriaSearch_success_callsSearchPostsByCriteria_only_andPresenterSuccess() {
        // Arrange
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

        // Act
        interactor.execute(input);

        // Assert DAO
        verify(dao, times(1)).searchPostsByCriteria(title, location, tags, isLost);
        verify(dao, never()).searchPosts(anyString());
        verify(dao, never()).fuzzySearch(anyString());

        // Assert Presenter
        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareSuccessView(cap.capture());
        verify(presenter, never()).prepareFailView(any());

        assertFalse(cap.getValue().hasError());
        assertEquals(2, cap.getValue().getPosts().size());
        assertEquals("Lost phone at library", cap.getValue().getPosts().get(0).getTitle());
    }

    @Test
    void execute_regularQuery_emptyResults_callsFailPresenter_withGenericMessage() {
        // Arrange
        String query = "something-no-one-has";
        when(dao.searchPosts(query)).thenReturn(new ArrayList<>());

        SearchInputData input = new SearchInputData(query, /*isFuzzy=*/false);

        // Act
        interactor.execute(input);

        // Assert
        verify(dao, times(1)).searchPosts(query);

        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        verify(presenter, never()).prepareSuccessView(any());

        SearchOutputData out = cap.getValue();
        assertTrue(out.hasError());
        assertNull(out.getPosts());
        assertNotNull(out.getError());
        assertTrue(out.getError().contains("No posts found"));
        assertFalse(out.getError().toLowerCase().contains("fuzzy"));
    }

    @Test
    void execute_fuzzyQuery_emptyResults_callsFailPresenter_withFuzzyMessage() {
        // Arrange
        String query = "fzyy-typo";
        when(dao.fuzzySearch(query)).thenReturn(Collections.emptyList());

        SearchInputData input = new SearchInputData(query, /*isFuzzy=*/true);

        // Act
        interactor.execute(input);

        // Assert
        verify(dao, times(1)).fuzzySearch(query);

        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        verify(presenter, never()).prepareSuccessView(any());

        SearchOutputData out = cap.getValue();
        assertTrue(out.hasError());
        assertNull(out.getPosts());
        assertNotNull(out.getError());
        assertTrue(out.getError().toLowerCase().contains("fuzzy"));
    }

    @Test
    void execute_daoThrows_callsFailPresenter_withErrorMessage() {
        // Arrange
        String query = "error-case";
        when(dao.searchPosts(query)).thenThrow(new RuntimeException("DAO down"));

        SearchInputData input = new SearchInputData(query, /*isFuzzy=*/false);

        // Act
        interactor.execute(input);

        // Assert
        ArgumentCaptor<SearchOutputData> cap = ArgumentCaptor.forClass(SearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        verify(presenter, never()).prepareSuccessView(any());

        SearchOutputData out = cap.getValue();
        assertTrue(out.hasError());
        assertNotNull(out.getError());
        assertTrue(out.getError().contains("DAO down"));
    }

    @Test
    void execute_queryWithSpaces_isTrimmed_beforeCallingDao() {
        // Arrange
        String raw = "   wallet   ";
        String trimmed = "wallet";
        when(dao.searchPosts(trimmed)).thenReturn(Collections.singletonList(makePost(3, "wallet")));

        SearchInputData input = new SearchInputData(raw, /*isFuzzy=*/false);

        // Act
        interactor.execute(input);

        // Assert
        verify(dao, times(1)).searchPosts(trimmed);
        verify(dao, never()).searchPosts(raw); // ensure trim is used
        verify(presenter, times(1)).prepareSuccessView(any());
    }
}
