package app.use_case.signup;

import entity.User;
import entity.UserFactory;
import org.junit.jupiter.api.Test;
import use_case.signup.*;

import static org.junit.jupiter.api.Assertions.*;

class SignupInteractorTest {

    @Test
    void testSignupSuccess() {
        FakeUserFactory userFactory = new FakeUserFactory();
        FakeUserDataAccess dao = new FakeUserDataAccess();
        FakePresenter presenter = new FakePresenter();

        SignupInteractor interactor = new SignupInteractor(dao, presenter, userFactory);
        interactor.execute(new SignupInputData("alice", "123", "123"));

        assertTrue(presenter.successCalled);
        assertEquals("alice", presenter.lastOutput.getUsername());
        assertFalse(presenter.lastOutput.isUseCaseFailed());
        assertTrue(dao.saved);
    }

    @Test
    void testSignupFailsUserExists() {
        FakeUserFactory userFactory = new FakeUserFactory();
        FakeUserDataAccess dao = new FakeUserDataAccess();
        dao.existingUser = "bob";
        FakePresenter presenter = new FakePresenter();

        SignupInteractor interactor = new SignupInteractor(dao, presenter, userFactory);
        interactor.execute(new SignupInputData("bob", "abc", "abc"));

        assertTrue(presenter.failCalled);
        assertEquals("User already exists.", presenter.lastError);
    }

    @Test
    void testSignupFailsPasswordMismatch() {
        FakeUserFactory userFactory = new FakeUserFactory();
        FakeUserDataAccess dao = new FakeUserDataAccess();
        FakePresenter presenter = new FakePresenter();

        SignupInteractor interactor = new SignupInteractor(dao, presenter, userFactory);
        interactor.execute(new SignupInputData("charlie", "pass1", "pass2"));

        assertTrue(presenter.failCalled);
        assertEquals("Passwords don't match.", presenter.lastError);
    }

    @Test
    void testSwitchToLoginView() {
        FakeUserFactory userFactory = new FakeUserFactory();
        FakeUserDataAccess dao = new FakeUserDataAccess();
        FakePresenter presenter = new FakePresenter();

        SignupInteractor interactor = new SignupInteractor(dao, presenter, userFactory);
        interactor.switchToLoginView();

        assertTrue(presenter.switchCalled);
    }

    // === Fake dependencies ===

    private static class FakeUser implements User {
        private final String name;
        private final String password;

        public FakeUser(String name, String password) {
            this.name = name;
            this.password = password;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getPassword() {
            return password;
        }
    }

    private static class FakeUserFactory implements UserFactory {
        @Override
        public User create(String name, String password) {
            return new FakeUser(name, password);
        }
    }

    private static class FakeUserDataAccess implements SignupUserDataAccessInterface {
        String existingUser = null;
        boolean saved = false;

        @Override
        public boolean existsByName(String username) {
            return username.equals(existingUser);
        }

        @Override
        public void save(User user) {
            saved = true;
        }
    }

    private static class FakePresenter implements SignupOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        boolean switchCalled = false;
        SignupOutputData lastOutput;
        String lastError;

        @Override
        public void prepareSuccessView(SignupOutputData outputData) {
            successCalled = true;
            lastOutput = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            failCalled = true;
            lastError = errorMessage;
        }

        @Override
        public void switchToLoginView() {
            switchCalled = true;
        }
    }
}
