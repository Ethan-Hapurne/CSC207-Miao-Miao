package use_case.change_password;

import entity.User;
import entity.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ChangePasswordInteractor using Mockito (no extra fake/spy files).
 * Targets:
 * - Interactor ~100% line coverage
 * - Touch getters in InputData and OutputData so their files reach ≥70%
 */
class ChangePasswordInteractorTest {

    private ChangePasswordUserDataAccessInterface dao;
    private ChangePasswordOutputBoundary presenter;
    private UserFactory userFactory;
    private User user; // returned by factory
    private ChangePasswordInteractor interactor;

    @BeforeEach
    void setUp() {
        dao = mock(ChangePasswordUserDataAccessInterface.class);
        presenter = mock(ChangePasswordOutputBoundary.class);
        userFactory = mock(UserFactory.class);
        user = mock(User.class);
        interactor = new ChangePasswordInteractor(dao, presenter, userFactory);
    }

    @Test
    void execute_happyPath_callsDao_andSuccessPresenter_andTouchesGetters() {
        // Arrange
        when(userFactory.create("alice", "newPass123", false)).thenReturn(user);
        when(user.getName()).thenReturn("alice");
        when(user.getPassword()).thenReturn("newPass123");
        when(user.isAdmin()).thenReturn(false);

        ChangePasswordInputData in = new ChangePasswordInputData("newPass123", "alice", false);

        // Act
        interactor.execute(in);

        // Assert: DAO received the same user instance created by the factory
        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
        verify(dao, times(1)).changePassword(userCap.capture());
        assertSame(user, userCap.getValue());

        // Assert: presenter success path, capture output and touch its getters
        ArgumentCaptor<ChangePasswordOutputData> outCap =
                ArgumentCaptor.forClass(ChangePasswordOutputData.class);
        verify(presenter, times(1)).prepareSuccessView(outCap.capture());
        verify(presenter, never()).prepareFailView(anyString());

        ChangePasswordOutputData out = outCap.getValue();
        assertEquals("alice", out.getUsername());
        assertFalse(out.isUseCaseFailed());

        // Bump file coverage for InputData by touching all getters
        assertEquals("newPass123", in.getPassword());
        assertEquals("alice", in.getUsername());
        assertFalse(in.getAdmin());
    }

    @Test
    void execute_adminCase_factoryCalledWithExactArgs() {
        // Arrange
        when(userFactory.create("bob", "p@ss", true)).thenReturn(user);
        when(user.getName()).thenReturn("bob");
        when(user.isAdmin()).thenReturn(true);

        ChangePasswordInputData in = new ChangePasswordInputData("p@ss", "bob", true);

        // Act
        interactor.execute(in);

        // Assert
        verify(userFactory).create("bob", "p@ss", true);
        verify(presenter, times(1)).prepareSuccessView(any(ChangePasswordOutputData.class));
        verify(presenter, never()).prepareFailView(anyString());
    }
}