package app.use_case.login;

import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.login.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class LoginInteractorTest {

    private FakeDAO dao;
    private FakePresenter presenter;
    private LoginInteractor interactor;

    @BeforeEach
    void setUp() {
        dao = new FakeDAO();
        presenter = new FakePresenter();
        interactor = new LoginInteractor(dao, presenter);
    }

    @Test
    void testLoginSuccess() {
        dao.save(new FakeUser("alice", "password123"));

        LoginInputData input = new LoginInputData("alice", "password123");
        interactor.execute(input);

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("alice", presenter.outputData.getUsername());
        assertEquals("alice", dao.currentUser);
    }

    @Test
    void testLoginNonexistentUserFails() {
        LoginInputData input = new LoginInputData("bob", "doesntmatter");
        interactor.execute(input);

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("bob: Account does not exist.", presenter.failMessage);
    }

    @Test
    void testIncorrectPasswordFails() {
        dao.save(new FakeUser("charlie", "securepass"));

        LoginInputData input = new LoginInputData("charlie", "wrongpass");
        interactor.execute(input);

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Incorrect password for \"charlie\".", presenter.failMessage);
    }

    // ---------- Fake classes ----------

    static class FakeUser implements User {
        private final String name;
        private final String password;

        FakeUser(String name, String password) {
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

    static class FakeDAO implements LoginUserDataAccessInterface {
        Map<String, User> users = new HashMap<>();
        String currentUser = null;

        @Override
        public boolean existsByName(String username) {
            return users.containsKey(username);
        }

        @Override
        public void save(User user) {
            users.put(user.getName(), user);
        }

        @Override
        public User get(String username) {
            return users.get(username);
        }

        @Override
        public String getCurrentUsername() {
            return currentUser;
        }

        @Override
        public void setCurrentUsername(String username) {
            currentUser = username;
        }
    }

    static class FakePresenter implements LoginOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        LoginOutputData outputData;
        String failMessage;

        @Override
        public void prepareSuccessView(LoginOutputData outputData) {
            successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            failCalled = true;
            this.failMessage = errorMessage;
        }
    }
}
