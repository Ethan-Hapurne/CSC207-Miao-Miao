package use_case.logout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutInteractorTest {

    private LogoutUserDataAccessInterface userDataAccessObject;
    private LogoutOutputBoundary logoutPresenter;
    private LogoutInteractor logoutInteractor;

    private static final String CURRENT_USERNAME = "testUser";
    private static final String EMPTY_USERNAME = "";
    private static final String NULL_USERNAME = null;

    @BeforeEach
    void setUp() {
        // Create mocks
        userDataAccessObject = mock(LogoutUserDataAccessInterface.class);
        logoutPresenter = mock(LogoutOutputBoundary.class);

        // Create the interactor with mocked dependencies
        logoutInteractor = new LogoutInteractor(userDataAccessObject, logoutPresenter);
    }

    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("Should log out user successfully with valid username")
        void logoutSuccess_ValidUsername() {
            // Arrange
            LogoutInputData inputData = new LogoutInputData(CURRENT_USERNAME);

            // Act
            logoutInteractor.execute(inputData);

            // Assert
            // Verify that setCurrentUsername was called with null
            verify(userDataAccessObject).setCurrentUsername(null);

            // Verify presenter was called with correct output data
            ArgumentCaptor<LogoutOutputData> outputDataCaptor = ArgumentCaptor.forClass(LogoutOutputData.class);
            verify(logoutPresenter).prepareSuccessView(outputDataCaptor.capture());

            LogoutOutputData outputData = outputDataCaptor.getValue();
            assertEquals(CURRENT_USERNAME, outputData.getUsername());
            assertFalse(outputData.isUseCaseFailed());
        }

        @Test
        @DisplayName("Should log out user successfully with empty username")
        void logoutSuccess_EmptyUsername() {
            // Arrange
            LogoutInputData inputData = new LogoutInputData(EMPTY_USERNAME);

            // Act
            logoutInteractor.execute(inputData);

            // Assert
            verify(userDataAccessObject).setCurrentUsername(null);

            ArgumentCaptor<LogoutOutputData> outputDataCaptor = ArgumentCaptor.forClass(LogoutOutputData.class);
            verify(logoutPresenter).prepareSuccessView(outputDataCaptor.capture());

            LogoutOutputData outputData = outputDataCaptor.getValue();
            assertEquals(EMPTY_USERNAME, outputData.getUsername());
            assertFalse(outputData.isUseCaseFailed());
        }

        @Test
        @DisplayName("Should log out user successfully with null username")
        void logoutSuccess_NullUsername() {
            // Arrange
            LogoutInputData inputData = new LogoutInputData(NULL_USERNAME);

            // Act
            logoutInteractor.execute(inputData);

            // Assert
            verify(userDataAccessObject).setCurrentUsername(null);

            ArgumentCaptor<LogoutOutputData> outputDataCaptor = ArgumentCaptor.forClass(LogoutOutputData.class);
            verify(logoutPresenter).prepareSuccessView(outputDataCaptor.capture());

            LogoutOutputData outputData = outputDataCaptor.getValue();
            assertNull(outputData.getUsername());
            assertFalse(outputData.isUseCaseFailed());
        }
    }

    @Nested
    @DisplayName("Interaction Verification Tests")
    class InteractionVerificationTests {

        @Test
        @DisplayName("Should follow correct execution order")
        void correctExecutionOrder() {
            // Arrange
            LogoutInputData inputData = new LogoutInputData(CURRENT_USERNAME);

            // Act
            logoutInteractor.execute(inputData);

            // Assert - verify the correct order of method calls
            InOrder inOrder = inOrder(userDataAccessObject, logoutPresenter);

            // First, the username should be set to null in the DAO
            inOrder.verify(userDataAccessObject).setCurrentUsername(null);

            // Then, the presenter should be told to prepare the success view
            inOrder.verify(logoutPresenter).prepareSuccessView(any(LogoutOutputData.class));

            // No more interactions
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("Should call dependencies exactly once")
        void callDependenciesExactlyOnce() {
            // Arrange
            LogoutInputData inputData = new LogoutInputData(CURRENT_USERNAME);

            // Act
            logoutInteractor.execute(inputData);

            // Assert - verify each method is called exactly once
            verify(userDataAccessObject, times(1)).setCurrentUsername(null);
            verify(logoutPresenter, times(1)).prepareSuccessView(any(LogoutOutputData.class));

            // Verify no other methods were called on the mocks
            verifyNoMoreInteractions(userDataAccessObject);
            verifyNoMoreInteractions(logoutPresenter);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle exceptions from userDataAccessObject")
        void handleExceptionsFromUserDAO() {
            // Arrange
            LogoutInputData inputData = new LogoutInputData(CURRENT_USERNAME);

            // Set up mock to throw exception
            doThrow(new RuntimeException("Database error")).when(userDataAccessObject).setCurrentUsername(null);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                logoutInteractor.execute(inputData);
            });

            assertEquals("Database error", exception.getMessage());

            // Verify presenter was never called
            verify(logoutPresenter, never()).prepareSuccessView(any(LogoutOutputData.class));
        }

        @Test
        @DisplayName("Should handle exceptions from logoutPresenter")
        void handleExceptionsFromPresenter() {
            // Arrange
            LogoutInputData inputData = new LogoutInputData(CURRENT_USERNAME);

            // Set up mock to throw exception
            doThrow(new RuntimeException("Presenter error")).when(logoutPresenter).prepareSuccessView(any(LogoutOutputData.class));

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                logoutInteractor.execute(inputData);
            });

            assertEquals("Presenter error", exception.getMessage());

            // Verify DAO was still called
            verify(userDataAccessObject).setCurrentUsername(null);
        }
    }

    @Nested
    @DisplayName("Multiple Calls Tests")
    class MultipleCallsTests {

        @Test
        @DisplayName("Should handle multiple logout calls")
        void handleMultipleLogoutCalls() {
            // Arrange
            LogoutInputData inputData1 = new LogoutInputData("user1");
            LogoutInputData inputData2 = new LogoutInputData("user2");

            // Act
            logoutInteractor.execute(inputData1);
            logoutInteractor.execute(inputData2);

            // Assert
            // Verify setCurrentUsername was called twice with null
            verify(userDataAccessObject, times(2)).setCurrentUsername(null);

            // Verify presenter was called with correct output data
            ArgumentCaptor<LogoutOutputData> outputDataCaptor = ArgumentCaptor.forClass(LogoutOutputData.class);
            verify(logoutPresenter, times(2)).prepareSuccessView(outputDataCaptor.capture());

            // Get all captured values
            List<LogoutOutputData> capturedOutputs = outputDataCaptor.getAllValues();
            assertEquals(2, capturedOutputs.size());

            assertEquals("user1", capturedOutputs.get(0).getUsername());
            assertEquals("user2", capturedOutputs.get(1).getUsername());
        }
    }

    @Test
    @DisplayName("Should preserve username in output data")
    void preserveUsernameInOutputData() {
        // This test is for regression purposes, to ensure that even if implementation changes,
        // the original username passed in is preserved in the output data

        // Arrange
        String specialUsername = "user@123-456_SPECIAL";
        LogoutInputData inputData = new LogoutInputData(specialUsername);

        // Act
        logoutInteractor.execute(inputData);

        // Assert
        ArgumentCaptor<LogoutOutputData> outputDataCaptor = ArgumentCaptor.forClass(LogoutOutputData.class);
        verify(logoutPresenter).prepareSuccessView(outputDataCaptor.capture());

        LogoutOutputData outputData = outputDataCaptor.getValue();
        assertEquals(specialUsername, outputData.getUsername());
    }
}