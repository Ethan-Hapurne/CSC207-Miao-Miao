package use_case.change_username;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ChangeUsernameInteractor using Mockito.
 * Targets: Interactor ≥90%, and touch getters on Input/Output data to lift file coverage ≥70%.
 */
class ChangeUsernameInteractorTest {

    private ChangeUsernameUserDataAccessInterface dao;
    private ChangeUsernameOutputBoundary presenter;
    private ChangeUsernameInteractor interactor;

    @BeforeEach
    void setUp() {
        dao = mock(ChangeUsernameUserDataAccessInterface.class);
        presenter = mock(ChangeUsernameOutputBoundary.class);
        interactor = new ChangeUsernameInteractor(dao, presenter);
    }

    @Test
    void execute_success_flow_callsDao_andSuccessPresenter() {
        ChangeUsernameInputData in = new ChangeUsernameInputData("oldAlice", "newAlice");

        when(dao.existsByName("newAlice")).thenReturn(false);
        when(dao.changeUsername("oldAlice", "newAlice")).thenReturn(true);

        interactor.execute(in);

        // DAO should be queried then updated
        verify(dao, times(1)).existsByName("newAlice");
        verify(dao, times(1)).changeUsername("oldAlice", "newAlice");

        // Presenter success should be called; capture output and touch getters
        ArgumentCaptor<ChangeUsernameOutputData> outCap =
                ArgumentCaptor.forClass(ChangeUsernameOutputData.class);
        verify(presenter, times(1)).prepareSuccessView(outCap.capture());
        verify(presenter, never()).prepareFailView(anyString());

        ChangeUsernameOutputData out = outCap.getValue();
        assertEquals("newAlice", out.getNewUsername());
        assertFalse(out.isUseCaseFailed());

        // Touch InputData getters to bump its file coverage
        assertEquals("oldAlice", in.getOldUsername());
        assertEquals("newAlice", in.getNewUsername());
    }

    @Test
    void execute_usernameAlreadyExists_callsFailPresenter_andSkipsUpdate() {
        ChangeUsernameInputData in = new ChangeUsernameInputData("any", "taken");

        when(dao.existsByName("taken")).thenReturn(true);

        interactor.execute(in);

        verify(dao, times(1)).existsByName("taken");
        verify(dao, never()).changeUsername(anyString(), anyString());

        verify(presenter, times(1)).prepareFailView("Username already exists.");
        verify(presenter, never()).prepareSuccessView(any());
    }

    @Test
    void execute_updateReturnsFalse_callsFailPresenter() {
        ChangeUsernameInputData in = new ChangeUsernameInputData("bob", "bobby");

        when(dao.existsByName("bobby")).thenReturn(false);
        when(dao.changeUsername("bob", "bobby")).thenReturn(false);

        interactor.execute(in);

        verify(dao).existsByName("bobby");
        verify(dao).changeUsername("bob", "bobby");

        verify(presenter, times(1)).prepareFailView("Failed to change username.");
        verify(presenter, never()).prepareSuccessView(any());
    }
}
