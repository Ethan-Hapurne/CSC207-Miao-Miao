package use_case.fuzzy_search;

import entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FuzzySearchInteractor.
 * Mocks DAO and Presenter; verifies interactor behavior and output mapping.
 */
class FuzzySearchInteractorTest {

    private FuzzySearchUserDataAccessInterface dao;
    private FuzzySearchOutputBoundary presenter;
    private FuzzySearchInteractor interactor;

    @BeforeEach
    void setUp() {
        dao = mock(FuzzySearchUserDataAccessInterface.class);
        presenter = mock(FuzzySearchOutputBoundary.class);
        interactor = new FuzzySearchInteractor(dao, presenter);
    }

    // Build a minimal Post using setters to avoid constructor drift
    private Post makePost(int id, String title, String desc, List<String> tags, String location) {
        Post p = new Post();
        p.setPostID(id);
        p.setTitle(title);
        p.setDescription(desc);
        p.setTags(tags);
        p.setLocation(location);
        p.setAuthor("tester");
        // Your Post#setTimestamp expects a String, not LocalDateTime
        p.setTimestamp("2025-08-11T10:00:00");
        p.setLost(true);
        p.setReactions(new HashMap<>());
        return p;
    }

    @Test
    void execute_success_callsPrepareSuccessView_withResults() {
        String query = "phone";
        List<Post> all = Arrays.asList(
                makePost(1, "Lost Phone", "black iPhone", Collections.singletonList("electronics"), "Library"),
                makePost(2, "Wallet", "brown wallet", Collections.singletonList("accessory"), "Cafe")
        );
        when(dao.getAllPosts()).thenReturn(all);

        interactor.execute(new FuzzySearchInputData(query));

        ArgumentCaptor<FuzzySearchOutputData> cap = ArgumentCaptor.forClass(FuzzySearchOutputData.class);
        verify(presenter, times(1)).prepareSuccessView(cap.capture());
        verify(presenter, never()).prepareFailView(any());

        FuzzySearchOutputData out = cap.getValue();
        assertTrue(out.isSuccess());
        assertEquals(query, out.getSearchQuery());
        assertNotNull(out.getMessage());
        assertNotNull(out.getSearchResults());
        assertFalse(out.getSearchResults().isEmpty());
        assertTrue(out.getMessage().toLowerCase().contains("found"));
    }

    @Test
    void execute_emptyQuery_callsFailPresenter() {
        interactor.execute(new FuzzySearchInputData("   ")); // blank

        ArgumentCaptor<FuzzySearchOutputData> cap = ArgumentCaptor.forClass(FuzzySearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        verify(presenter, never()).prepareSuccessView(any());

        FuzzySearchOutputData out = cap.getValue();
        assertFalse(out.isSuccess());
        assertNotNull(out.getMessage());
        assertTrue(out.getMessage().toLowerCase().contains("cannot be empty"));
    }

    @Test
    void execute_noPostsAvailable_callsFailPresenter() {
        when(dao.getAllPosts()).thenReturn(Collections.emptyList());

        interactor.execute(new FuzzySearchInputData("anything"));

        ArgumentCaptor<FuzzySearchOutputData> cap = ArgumentCaptor.forClass(FuzzySearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        FuzzySearchOutputData out = cap.getValue();
        assertFalse(out.isSuccess());
        assertNotNull(out.getMessage());
        assertTrue(out.getMessage().toLowerCase().contains("no posts"));
        assertNull(out.getSearchResults());
    }

    @Test
    void execute_noResult_callsFailPresenter_withHelpfulMessage() {
        List<Post> all = Collections.singletonList(
                makePost(1, "Wallet", "brown wallet", Collections.singletonList("accessory"), "Cafe")
        );
        when(dao.getAllPosts()).thenReturn(all);

        interactor.execute(new FuzzySearchInputData("phone"));

        ArgumentCaptor<FuzzySearchOutputData> cap = ArgumentCaptor.forClass(FuzzySearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        FuzzySearchOutputData out = cap.getValue();
        assertFalse(out.isSuccess());
        assertNotNull(out.getMessage());
        assertTrue(out.getMessage().toLowerCase().contains("no results"));
        assertEquals("phone", out.getSearchQuery());
        assertNull(out.getSearchResults());
    }

    @Test
    void execute_daoThrows_callsFailPresenter_withErrorMessage() {
        when(dao.getAllPosts()).thenThrow(new RuntimeException("DAO down"));

        interactor.execute(new FuzzySearchInputData("phone"));

        ArgumentCaptor<FuzzySearchOutputData> cap = ArgumentCaptor.forClass(FuzzySearchOutputData.class);
        verify(presenter, times(1)).prepareFailView(cap.capture());
        verify(presenter, never()).prepareSuccessView(any());

        FuzzySearchOutputData out = cap.getValue();
        assertFalse(out.isSuccess());
        assertNotNull(out.getMessage());
        assertTrue(out.getMessage().toLowerCase().contains("error"));
        assertTrue(out.getMessage().contains("DAO down"));
    }
}
