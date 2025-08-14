package use_case.signup;

import entity.CommonUser;
import entity.User;
import entity.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SignupInteractorTest {

    private SignupUserDataAccessInterface userDataAccessObject;
    private SignupOutputBoundary userPresenter;
    private UserFactory userFactory;
    private SignupInteractor signupInteractor;

    private static final String VALID_USERNAME = "newuser";
    private static final String EXISTING_USERNAME = "existinguser";
    private static final String VALID_PASSWORD = "password123";
    private static final String DIFFERENT_PASSWORD = "differentpassword";

    @BeforeEach
    void setUp() {
        // Create mocks
        userDataAccessObject = mock(SignupUserDataAccessInterface.class);
        userPresenter = mock(SignupOutputBoundary.class);
        userFactory = mock(UserFactory.class);

        // Configure mock behavior
        when(userDataAccessObject.existsByName(VALID_USERNAME)).thenReturn(false);
        when(userDataAccessObject.existsByName(EXISTING_USERNAME)).thenReturn(true);

        // Create the interactor with mocked dependencies
        signupInteractor = new SignupInteractor(userDataAccessObject, userPresenter, userFactory);
    }

    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("Successful signup with regular user")
        void successfulSignup_RegularUser() {
            // Arrange
            SignupInputData inputData = new SignupInputData(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert
            verify(userDataAccessObject).save(any(User.class));

            // Verify presenter was called with correct output data
            ArgumentCaptor<SignupOutputData> outputDataCaptor = ArgumentCaptor.forClass(SignupOutputData.class);
            verify(userPresenter).prepareSuccessView(outputDataCaptor.capture());

            SignupOutputData outputData = outputDataCaptor.getValue();
            assertEquals(VALID_USERNAME, outputData.getUsername());
            assertFalse(outputData.isUseCaseFailed());
        }

        @Test
        @DisplayName("Successful signup with admin user")
        void successfulSignup_AdminUser() {
            // Arrange
            SignupInputData inputData = new SignupInputData(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, true);

            // Act
            signupInteractor.execute(inputData);

            // Assert
            // Capture the user that was saved
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDataAccessObject).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals(VALID_USERNAME, savedUser.getName());
            assertEquals(VALID_PASSWORD, savedUser.getPassword());
            assertTrue(savedUser.isAdmin());

            // Verify presenter was called
            verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
        }
    }

    @Nested
    @DisplayName("Validation Failure Tests")
    class ValidationFailureTests {

        @Test
        @DisplayName("Signup fails with existing username")
        void signupFails_UserAlreadyExists() {
            // Arrange
            SignupInputData inputData = new SignupInputData(EXISTING_USERNAME, VALID_PASSWORD, VALID_PASSWORD, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert
            verify(userPresenter).prepareFailView("User already exists.");

            // Verify that save was never called
            verify(userDataAccessObject, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Signup fails with mismatched passwords")
        void signupFails_PasswordsDontMatch() {
            // Arrange
            SignupInputData inputData = new SignupInputData(VALID_USERNAME, VALID_PASSWORD, DIFFERENT_PASSWORD, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert
            verify(userPresenter).prepareFailView("Passwords don't match.");

            // Verify that save was never called
            verify(userDataAccessObject, never()).save(any(User.class));
        }

        @ParameterizedTest
        @CsvSource({
                "user1, password1, password1, false, false, success",
                "user2, password2, different, false, false, passwordMismatch",
                "existinguser, password3, password3, false, true, userExists"
        })
        @DisplayName("Various signup scenarios")
        void variousSignupScenarios(String username, String password, String repeatPassword,
                                    boolean isAdmin, boolean userExists, String expectedResult) {
            // Arrange
            when(userDataAccessObject.existsByName(username)).thenReturn(userExists);

            SignupInputData inputData = new SignupInputData(username, password, repeatPassword, isAdmin);

            // Act
            signupInteractor.execute(inputData);

            // Assert
            if ("success".equals(expectedResult)) {
                verify(userDataAccessObject).save(any(User.class));
                verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
            } else if ("passwordMismatch".equals(expectedResult)) {
                verify(userPresenter).prepareFailView("Passwords don't match.");
                verify(userDataAccessObject, never()).save(any(User.class));
            } else if ("userExists".equals(expectedResult)) {
                verify(userPresenter).prepareFailView("User already exists.");
                verify(userDataAccessObject, never()).save(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Signup with empty username")
        void signupWithEmptyUsername() {
            // For this test, we need to reset the mock behavior for empty username
            String emptyUsername = "";
            when(userDataAccessObject.existsByName(emptyUsername)).thenReturn(false);

            // Arrange
            SignupInputData inputData = new SignupInputData(emptyUsername, VALID_PASSWORD, VALID_PASSWORD, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert - Empty username is allowed as long as it doesn't exist
            verify(userDataAccessObject).save(any(User.class));
            verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
        }

        @Test
        @DisplayName("Signup with blank username")
        void signupWithBlankUsername() {
            // For this test, we need to reset the mock behavior for blank username
            String blankUsername = "   ";
            when(userDataAccessObject.existsByName(blankUsername)).thenReturn(false);

            // Arrange
            SignupInputData inputData = new SignupInputData(blankUsername, VALID_PASSWORD, VALID_PASSWORD, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert - Blank username is allowed as long as it doesn't exist
            verify(userDataAccessObject).save(any(User.class));
            verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
        }

        @Test
        @DisplayName("Signup with empty password")
        void signupWithEmptyPassword() {
            // Arrange
            String emptyPassword = "";
            SignupInputData inputData = new SignupInputData(VALID_USERNAME, emptyPassword, emptyPassword, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert - Empty password is allowed as long as both passwords match
            verify(userDataAccessObject).save(any(User.class));
            verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
        }

        @Test
        @DisplayName("Signup with blank password")
        void signupWithBlankPassword() {
            // Arrange
            String blankPassword = "   ";
            SignupInputData inputData = new SignupInputData(VALID_USERNAME, blankPassword, blankPassword, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert - Blank password is allowed as long as both passwords match
            verify(userDataAccessObject).save(any(User.class));
            verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
        }

        @Test
        @DisplayName("Signup with very long username and password")
        void signupWithVeryLongCredentials() {
            // Arrange
            String longUsername = "user" + "a".repeat(1000);
            String longPassword = "pass" + "b".repeat(1000);

            when(userDataAccessObject.existsByName(longUsername)).thenReturn(false);

            SignupInputData inputData = new SignupInputData(longUsername, longPassword, longPassword, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert
            verify(userDataAccessObject).save(any(User.class));

            ArgumentCaptor<SignupOutputData> outputDataCaptor = ArgumentCaptor.forClass(SignupOutputData.class);
            verify(userPresenter).prepareSuccessView(outputDataCaptor.capture());

            SignupOutputData outputData = outputDataCaptor.getValue();
            assertEquals(longUsername, outputData.getUsername());
        }

        @Test
        @DisplayName("Signup with special characters in credentials")
        void signupWithSpecialCharacters() {
            // Arrange
            String specialUsername = "user@123!#$%";
            String specialPassword = "p@$$w0rd!#$%^&*()";

            when(userDataAccessObject.existsByName(specialUsername)).thenReturn(false);

            SignupInputData inputData = new SignupInputData(specialUsername, specialPassword, specialPassword, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert
            verify(userDataAccessObject).save(any(User.class));
            verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
        }
    }

    @Nested
    @DisplayName("Interaction Verification Tests")
    class InteractionVerificationTests {

        @Test
        @DisplayName("Check order of operations in success case")
        void checkOrderOfOperations_Success() {
            // Arrange
            SignupInputData inputData = new SignupInputData(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert - verify the correct order of method calls
            InOrder inOrder = inOrder(userDataAccessObject, userPresenter);

            // First checks if user exists
            inOrder.verify(userDataAccessObject).existsByName(VALID_USERNAME);

            // Then saves the user
            inOrder.verify(userDataAccessObject).save(any(User.class));

            // Finally calls the presenter
            inOrder.verify(userPresenter).prepareSuccessView(any(SignupOutputData.class));
        }

        @Test
        @DisplayName("Check order of operations when user exists")
        void checkOrderOfOperations_UserExists() {
            // Arrange
            SignupInputData inputData = new SignupInputData(EXISTING_USERNAME, VALID_PASSWORD, VALID_PASSWORD, false);

            // Act
            signupInteractor.execute(inputData);

            // Assert - verify the correct order of method calls
            InOrder inOrder = inOrder(userDataAccessObject, userPresenter);

            // First checks if user exists
            inOrder.verify(userDataAccessObject).existsByName(EXISTING_USERNAME);

            // Then calls the presenter with fail view
            inOrder.verify(userPresenter).prepareFailView(anyString());

            // Verify no more interactions
            verifyNoMoreInteractions(userDataAccessObject);
        }
    }

    @Test
    @DisplayName("Test switchToLoginView")
    void testSwitchToLoginView() {
        // Act
        signupInteractor.switchToLoginView();

        // Assert
        verify(userPresenter).switchToLoginView();
        verifyNoMoreInteractions(userPresenter);
        verifyNoInteractions(userDataAccessObject);
        verifyNoInteractions(userFactory);
    }

    @Test
    @DisplayName("CommonUser is created and used instead of using UserFactory")
    void commonUserIsUsedInsteadOfFactory() {
        // This test confirms that CommonUser is directly instantiated instead of using the factory

        // Arrange
        SignupInputData inputData = new SignupInputData(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, true);

        // Act
        signupInteractor.execute(inputData);

        // Assert
        // Verify that UserFactory was never used
        verifyNoInteractions(userFactory);

        // Instead, CommonUser should have been directly created and saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDataAccessObject).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertTrue(savedUser instanceof CommonUser);
        assertEquals(VALID_USERNAME, savedUser.getName());
        assertEquals(VALID_PASSWORD, savedUser.getPassword());
        assertTrue(savedUser.isAdmin());
    }

    @Test
    @DisplayName("Exception handling during user creation and saving")
    void exceptionHandlingDuringUserCreationAndSaving() {
        // Arrange
        SignupInputData inputData = new SignupInputData(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, false);

        // Make the DAO throw an exception when save is called
        doThrow(new RuntimeException("Database error")).when(userDataAccessObject).save(any(User.class));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            signupInteractor.execute(inputData);
        });

        assertEquals("Database error", exception.getMessage());

        // Verify that the presenter's fail view was not called (exception propagated instead)
        verify(userPresenter, never()).prepareFailView(anyString());
        verify(userPresenter, never()).prepareSuccessView(any(SignupOutputData.class));
    }
}