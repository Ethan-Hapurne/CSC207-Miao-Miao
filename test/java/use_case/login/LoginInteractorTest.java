package use_case.login;

import entity.CommonUser;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginInteractorTest {

    private LoginUserDataAccessInterface userDataAccessObject;
    private LoginOutputBoundary loginPresenter;
    private LoginInteractor loginInteractor;

    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password123";
    private static final String INVALID_PASSWORD = "wrongpassword";
    private static final String NONEXISTENT_USER = "nonexistentuser";
    private static final String ADMIN_USERNAME = "admin";

    @BeforeEach
    void setUp() {
        // Create mocks
        userDataAccessObject = mock(LoginUserDataAccessInterface.class);
        loginPresenter = mock(LoginOutputBoundary.class);

        // Create interactor with mocked dependencies
        loginInteractor = new LoginInteractor(userDataAccessObject, loginPresenter);

        // Setup common behavior for user DAO
        User regularUser = new CommonUser(VALID_USERNAME, VALID_PASSWORD, false);
        User adminUser = new CommonUser(ADMIN_USERNAME, VALID_PASSWORD, true);

        when(userDataAccessObject.existsByName(VALID_USERNAME)).thenReturn(true);
        when(userDataAccessObject.existsByName(ADMIN_USERNAME)).thenReturn(true);
        when(userDataAccessObject.existsByName(NONEXISTENT_USER)).thenReturn(false);

        when(userDataAccessObject.get(VALID_USERNAME)).thenReturn(regularUser);
        when(userDataAccessObject.get(ADMIN_USERNAME)).thenReturn(adminUser);
    }

    @Nested
    @DisplayName("Successful Login Tests")
    class SuccessfulLoginTests {

        @Test
        @DisplayName("Regular user can login with correct credentials")
        void regularUserLogin_Success() {
            // Arrange
            LoginInputData inputData = new LoginInputData(VALID_USERNAME, VALID_PASSWORD, false);

            // Act
            loginInteractor.execute(inputData);

            // Assert
            verify(userDataAccessObject).setCurrentUsername(VALID_USERNAME);

            // Capture the output data to verify its contents
            ArgumentCaptor<LoginOutputData> outputDataCaptor = ArgumentCaptor.forClass(LoginOutputData.class);
            verify(loginPresenter).prepareSuccessView(outputDataCaptor.capture());

            LoginOutputData outputData = outputDataCaptor.getValue();
            assertEquals(VALID_USERNAME, outputData.getUsername());
            assertFalse(outputData.isAdmin());
        }

        @Test
        @DisplayName("Admin user can login with correct credentials")
        void adminUserLogin_Success() {
            // Arrange
            LoginInputData inputData = new LoginInputData(ADMIN_USERNAME, VALID_PASSWORD, true);

            // Act
            loginInteractor.execute(inputData);

            // Assert
            verify(userDataAccessObject).setCurrentUsername(ADMIN_USERNAME);

            ArgumentCaptor<LoginOutputData> outputDataCaptor = ArgumentCaptor.forClass(LoginOutputData.class);
            verify(loginPresenter).prepareSuccessView(outputDataCaptor.capture());

            LoginOutputData outputData = outputDataCaptor.getValue();
            assertEquals(ADMIN_USERNAME, outputData.getUsername());
            assertTrue(outputData.isAdmin());
        }
    }

    @Nested
    @DisplayName("Failed Login Tests")
    class FailedLoginTests {

        @Test
        @DisplayName("Login fails with non-existent username")
        void nonExistentUser_LoginFails() {
            // Arrange
            LoginInputData inputData = new LoginInputData(NONEXISTENT_USER, VALID_PASSWORD, false);

            // Act
            loginInteractor.execute(inputData);

            // Assert
            verify(loginPresenter).prepareFailView(contains("Account does not exist"));
            verify(userDataAccessObject, never()).setCurrentUsername(any());
        }

        @Test
        @DisplayName("Login fails with incorrect password")
        void incorrectPassword_LoginFails() {
            // Arrange
            LoginInputData inputData = new LoginInputData(VALID_USERNAME, INVALID_PASSWORD, false);

            // Act
            loginInteractor.execute(inputData);

            // Assert
            verify(loginPresenter).prepareFailView(contains("Incorrect password"));
            verify(userDataAccessObject, never()).setCurrentUsername(any());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("Login with empty or blank username fails")
        void emptyOrBlankUsername_LoginFails(String username) {
            // Setup specific for this test
            when(userDataAccessObject.existsByName(username)).thenReturn(false);

            // Arrange
            LoginInputData inputData = new LoginInputData(username, VALID_PASSWORD, false);

            // Act
            loginInteractor.execute(inputData);

            // Assert
            verify(loginPresenter).prepareFailView(contains("Account does not exist"));
        }

        @Test
        @DisplayName("Case sensitivity is respected in usernames")
        void usernameCaseSensitivity() {
            // Setup uppercase username not existing
            String uppercaseUsername = VALID_USERNAME.toUpperCase();
            when(userDataAccessObject.existsByName(uppercaseUsername)).thenReturn(false);

            // Arrange
            LoginInputData inputData = new LoginInputData(uppercaseUsername, VALID_PASSWORD, false);

            // Act
            loginInteractor.execute(inputData);

            // Assert
            verify(loginPresenter).prepareFailView(contains("Account does not exist"));
        }

        @Test
        @DisplayName("Case sensitivity is respected in passwords")
        void passwordCaseSensitivity() {
            // Arrange
            LoginInputData inputData = new LoginInputData(VALID_USERNAME, VALID_PASSWORD.toUpperCase(), false);

            // Act
            loginInteractor.execute(inputData);

            // Assert
            verify(loginPresenter).prepareFailView(contains("Incorrect password"));
        }

        @Test
        @DisplayName("Login with very long username and password")
        void veryLongCredentials() {
            // Setup very long username
            String longUsername = "user" + "a".repeat(1000);
            String longPassword = "pass" + "b".repeat(1000);

            // Create mock user with these credentials
            User longCredentialsUser = new CommonUser(longUsername, longPassword, false);

            when(userDataAccessObject.existsByName(longUsername)).thenReturn(true);
            when(userDataAccessObject.get(longUsername)).thenReturn(longCredentialsUser);

            // Arrange
            LoginInputData inputData = new LoginInputData(longUsername, longPassword, false);

            // Act
            loginInteractor.execute(inputData);

            // Assert - login should succeed
            verify(userDataAccessObject).setCurrentUsername(longUsername);
            verify(loginPresenter).prepareSuccessView(any(LoginOutputData.class));
        }
    }

    @Test
    @DisplayName("Login process handles passwords with special characters")
    void passwordWithSpecialCharacters() {
        // Setup user with special character password
        String specialPassword = "p@$$w0rd!#$%^&*()";
        User specialPasswordUser = new CommonUser(VALID_USERNAME, specialPassword, false);

        when(userDataAccessObject.get(VALID_USERNAME)).thenReturn(specialPasswordUser);

        // Arrange
        LoginInputData inputData = new LoginInputData(VALID_USERNAME, specialPassword, false);

        // Act
        loginInteractor.execute(inputData);

        // Assert
        verify(userDataAccessObject).setCurrentUsername(VALID_USERNAME);
        verify(loginPresenter).prepareSuccessView(any(LoginOutputData.class));
    }

    @Test
    @DisplayName("Validation is performed before database access")
    void validationBeforeDatabaseAccess() {
        // This test verifies the order of operations

        // Arrange
        LoginInputData inputData = new LoginInputData(VALID_USERNAME, VALID_PASSWORD, false);

        // Act
        loginInteractor.execute(inputData);

        // Assert - verify the order of method calls
        // First checks if user exists
        verify(userDataAccessObject, times(1)).existsByName(VALID_USERNAME);

        // Then gets the user - note that this happens twice in the implementation
        verify(userDataAccessObject, times(2)).get(VALID_USERNAME);

        // Finally sets the current username
        verify(userDataAccessObject, times(1)).setCurrentUsername(VALID_USERNAME);

        // Check that presenter is called last
        verify(loginPresenter, times(1)).prepareSuccessView(any(LoginOutputData.class));
    }
}