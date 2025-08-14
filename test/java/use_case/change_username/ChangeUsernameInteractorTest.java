package use_case.change_username;

import org.junit.jupiter.api.Test;
import use_case.change_username.*;

import static org.junit.jupiter.api.Assertions.*;

class ChangeUsernameInteractorTest {

    private boolean usernameChanged = false;
    private boolean presenterSuccessCalled = false;
    private boolean presenterFailCalled = false;
    private String capturedNewUsername = null;
    private String capturedFailMessage = null;

    class FakeDAO implements ChangeUsernameUserDataAccessInterface {
        @Override
        public boolean existsByName(String username) {
            return "takenUsername".equals(username);
        }

        @Override
        public boolean changeUsername(String oldUsername, String newUsername) {
            usernameChanged = true;
            capturedNewUsername = newUsername;
            return !"failOld".equals(oldUsername); // 如果 oldUsername 是 "failOld"，模拟失败
        }
    }

    class FakePresenter implements ChangeUsernameOutputBoundary {
        @Override
        public void prepareSuccessView(ChangeUsernameOutputData outputData) {
            presenterSuccessCalled = true;
            capturedNewUsername = outputData.getNewUsername();
        }

        @Override
        public void prepareFailView(String error) {
            presenterFailCalled = true;
            capturedFailMessage = error;
        }
    }

    @Test
    void successChangeUsername() {
        ChangeUsernameInteractor interactor = new ChangeUsernameInteractor(new FakeDAO(), new FakePresenter());
        ChangeUsernameInputData input = new ChangeUsernameInputData("oldUser", "newUser");

        interactor.execute(input);

        assertTrue(usernameChanged);
        assertTrue(presenterSuccessCalled);
        assertEquals("newUser", capturedNewUsername);
        assertFalse(presenterFailCalled);
    }

    @Test
    void usernameAlreadyExists() {
        ChangeUsernameInteractor interactor = new ChangeUsernameInteractor(new FakeDAO(), new FakePresenter());
        ChangeUsernameInputData input = new ChangeUsernameInputData("oldUser", "takenUsername");

        interactor.execute(input);

        assertFalse(usernameChanged);
        assertFalse(presenterSuccessCalled);
        assertTrue(presenterFailCalled);
        assertEquals("Username already exists.", capturedFailMessage);
    }

    @Test
    void changeUsernameFailsInDAO() {
        ChangeUsernameInteractor interactor = new ChangeUsernameInteractor(new FakeDAO(), new FakePresenter());
        ChangeUsernameInputData input = new ChangeUsernameInputData("failOld", "newUser");

        interactor.execute(input);

        assertTrue(usernameChanged);
        assertFalse(presenterSuccessCalled);
        assertTrue(presenterFailCalled);
        assertEquals("Failed to change username.", capturedFailMessage);
    }
}
