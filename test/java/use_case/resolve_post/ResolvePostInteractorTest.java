package use_case.resolve_post;

import entity.Post;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ResolvePostInteractor.
 * Notes:
 *  - Do NOT mock entity.Post (real instance avoids ByteBuddy/JDK compatibility issues).
 *  - Only mock interfaces: ResolvePostUserDataAccessInterface, ResolvePostOutputBoundary, User.
 */
class ResolvePostInteractorTest {

    private ResolvePostUserDataAccessInterface dao;
    private ResolvePostOutputBoundary presenter;
    private ResolvePostInteractor interactor;

    @BeforeEach
    void setUp() {
        dao = mock(ResolvePostUserDataAccessInterface.class);
        presenter = mock(ResolvePostOutputBoundary.class);
        interactor = new ResolvePostInteractor(dao, presenter);
    }

    // ---------- helpers ----------

    private Post newUnresolvedPost(String id) {
        Post p = new Post();
        p.setPostID(Integer.parseInt(id));
        p.setResolved(false);
        return p;
    }

    private ResolvePostInputData input(String postId, String credited, String resolvedBy) {
        return new ResolvePostInputData(postId, credited, resolvedBy);
    }

    // ---------- tests ----------

    @Test
    void execute_success_happyPath_updatesPostAndUser_andCallsSuccessPresenter() {
        // Arrange
        String postId = "101";
        String credited = "alice";
        String resolver = "bob";

        Post post = newUnresolvedPost(postId);

        User creditedUser = mock(User.class);
        when(creditedUser.getName()).thenReturn(credited);
        when(creditedUser.getCredibilityScore()).thenReturn(42 /* before add, we assert on output only */);

        User resolvingUser = mock(User.class);

        when(dao.getPostById(postId)).thenReturn(post);
        when(dao.getUserByUsername(credited)).thenReturn(creditedUser);
        when(dao.getUserByUsername(resolver)).thenReturn(resolvingUser);
        when(dao.updatePost(post)).thenReturn(true);
        when(dao.updateUser(creditedUser)).thenReturn(true);

        // Act
        interactor.execute(input(postId, credited, resolver));

        // Assert DAO interactions
        verify(dao).getPostById(postId);
        verify(dao).getUserByUsername(credited);
        verify(dao).getUserByUsername(resolver);
        verify(dao).updatePost(post);
        verify(dao).updateUser(creditedUser);
        verifyNoMoreInteractions(dao);

        // Assert user side-effects
        verify(creditedUser).addResolvedPost(postId);
        verify(creditedUser).addCredibilityPoints(10);

        // Assert post fields mutated
        assertTrue(post.isResolved());
        assertEquals(resolver, post.getResolvedBy());
        assertEquals(credited, post.getCreditedTo());

        // Assert presenter
        ArgumentCaptor<ResolvePostOutputData> cap = ArgumentCaptor.forClass(ResolvePostOutputData.class);
        verify(presenter).prepareSuccessView(cap.capture());
        ResolvePostOutputData out = cap.getValue();
        assertTrue(out.isSuccess());
        assertEquals(credited, out.getCreditedUsername());
        // message should mention credited user and points; keep it resilient
        assertTrue(out.getMessage().toLowerCase().contains("resolved"));
        assertTrue(out.getMessage().contains(credited));
        assertTrue(out.getMessage().contains("10"));
        verifyNoMoreInteractions(presenter);
    }

    @Test
    void execute_postNotFound_callsFailPresenter() {
        String postId = "404";
        when(dao.getPostById(postId)).thenReturn(null);

        interactor.execute(input(postId, "alice", "bob"));

        verify(dao).getPostById(postId);
        verifyNoMoreInteractions(dao);

        ArgumentCaptor<ResolvePostOutputData> cap = ArgumentCaptor.forClass(ResolvePostOutputData.class);
        verify(presenter).prepareFailView(cap.capture());
        ResolvePostOutputData out = cap.getValue();
        assertFalse(out.isSuccess());
        assertEquals("Post not found.", out.getMessage());
        verifyNoMoreInteractions(presenter);
    }

    @Test
    void execute_alreadyResolved_callsFailPresenter() {
        String postId = "1";
        Post post = newUnresolvedPost(postId);
        post.setResolved(true); // already resolved

        when(dao.getPostById(postId)).thenReturn(post);

        interactor.execute(input(postId, "alice", "bob"));

        verify(dao).getPostById(postId);
        verifyNoMoreInteractions(dao);

        ArgumentCaptor<ResolvePostOutputData> cap = ArgumentCaptor.forClass(ResolvePostOutputData.class);
        verify(presenter).prepareFailView(cap.capture());
        assertEquals("Post is already resolved.", cap.getValue().getMessage());
        assertFalse(cap.getValue().isSuccess());
        verifyNoMoreInteractions(presenter);
    }

    @Test
    void execute_creditedUserNotFound_callsFailPresenter() {
        String postId = "2";
        Post post = newUnresolvedPost(postId);

        when(dao.getPostById(postId)).thenReturn(post);
        when(dao.getUserByUsername("alice")).thenReturn(null);

        interactor.execute(input(postId, "alice", "bob"));

        verify(dao).getPostById(postId);
        verify(dao).getUserByUsername("alice");
        verifyNoMoreInteractions(dao);

        ArgumentCaptor<ResolvePostOutputData> cap = ArgumentCaptor.forClass(ResolvePostOutputData.class);
        verify(presenter).prepareFailView(cap.capture());
        assertEquals("Credited user not found.", cap.getValue().getMessage());
        assertFalse(cap.getValue().isSuccess());
        verifyNoMoreInteractions(presenter);
    }

    @Test
    void execute_resolvingUserNotFound_callsFailPresenter() {
        String postId = "3";
        Post post = newUnresolvedPost(postId);

        User creditedUser = mock(User.class);

        when(dao.getPostById(postId)).thenReturn(post);
        when(dao.getUserByUsername("alice")).thenReturn(creditedUser);
        when(dao.getUserByUsername("bob")).thenReturn(null);

        interactor.execute(input(postId, "alice", "bob"));

        verify(dao).getPostById(postId);
        verify(dao).getUserByUsername("alice");
        verify(dao).getUserByUsername("bob");
        verifyNoMoreInteractions(dao);

        ArgumentCaptor<ResolvePostOutputData> cap = ArgumentCaptor.forClass(ResolvePostOutputData.class);
        verify(presenter).prepareFailView(cap.capture());
        assertEquals("Resolving user not found.", cap.getValue().getMessage());
        assertFalse(cap.getValue().isSuccess());
        verifyNoMoreInteractions(presenter);
    }

    @Test
    void execute_updateFailure_callsFailPresenter() {
        String postId = "5";
        String credited = "alice";
        String resolver = "bob";

        Post post = newUnresolvedPost(postId);
        User creditedUser = mock(User.class);
        User resolvingUser = mock(User.class);

        when(dao.getPostById(postId)).thenReturn(post);
        when(dao.getUserByUsername(credited)).thenReturn(creditedUser);
        when(dao.getUserByUsername(resolver)).thenReturn(resolvingUser);

        // Simulate DB failure on either post or user update
        when(dao.updatePost(post)).thenReturn(true);
        when(dao.updateUser(creditedUser)).thenReturn(false);

        interactor.execute(input(postId, credited, resolver));

        verify(dao).getPostById(postId);
        verify(dao).getUserByUsername(credited);
        verify(dao).getUserByUsername(resolver);
        verify(dao).updatePost(post);
        verify(dao).updateUser(creditedUser);
        verifyNoMoreInteractions(dao);

        ArgumentCaptor<ResolvePostOutputData> cap = ArgumentCaptor.forClass(ResolvePostOutputData.class);
        verify(presenter).prepareFailView(cap.capture());
        assertEquals("Failed to update post or user in database.", cap.getValue().getMessage());
        assertFalse(cap.getValue().isSuccess());
        verifyNoMoreInteractions(presenter);
    }

    @Test
    void execute_daoThrows_callsFailPresenterWithErrorMessage() {
        String postId = "9";
        when(dao.getPostById(postId)).thenThrow(new RuntimeException("boom"));

        interactor.execute(input(postId, "alice", "bob"));

        verify(dao).getPostById(postId);
        verifyNoMoreInteractions(dao);

        ArgumentCaptor<ResolvePostOutputData> cap = ArgumentCaptor.forClass(ResolvePostOutputData.class);
        verify(presenter).prepareFailView(cap.capture());
        ResolvePostOutputData out = cap.getValue();
        assertFalse(out.isSuccess());
        assertTrue(out.getMessage().startsWith("An error occurred: "));
        assertTrue(out.getMessage().contains("boom"));
        verifyNoMoreInteractions(presenter);
    }
}
