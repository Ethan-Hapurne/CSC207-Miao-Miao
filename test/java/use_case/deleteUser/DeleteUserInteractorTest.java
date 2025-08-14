package use_case.deleteUser;

import data_access.FirebaseUserDataAccessObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteUserInteractorTest {

    private FirebaseUserDataAccessObject userDataAccessObject;
    private DeleteUserOutputBoundary deleteUserPresenter;
    private DeleteUserInteractor interactor;

    @BeforeEach
    void setUp() {
        userDataAccessObject = mock(FirebaseUserDataAccessObject.class);
        deleteUserPresenter = mock(DeleteUserOutputBoundary.class);
        interactor = new DeleteUserInteractor(userDataAccessObject, deleteUserPresenter);
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Successfully delete existing user")
        void deleteUser_Success() {
            // Arrange
            String username = "testUser";
            DeleteUserInputData inputData = new DeleteUserInputData(username);
            doNothing().when(userDataAccessObject).deleteUser(username);

            // Act
            interactor.execute(inputData);

            // Assert
            verify(userDataAccessObject).deleteUser(username);

            ArgumentCaptor<DeleteUserOutputData> outputDataCaptor = ArgumentCaptor.forClass(DeleteUserOutputData.class);
            verify(deleteUserPresenter).prepareSuccessView(outputDataCaptor.capture());

            DeleteUserOutputData outputData = outputDataCaptor.getValue();
            assertTrue(outputData.isSuccess());
            assertEquals("Successfully deleted user: testUser", outputData.getMessage());
        }

        @Test
        @DisplayName("Handle null username")
        void deleteUser_NullUsername() {
            // Arrange
            DeleteUserInputData inputData = new DeleteUserInputData(null);

            // Act
            interactor.execute(inputData);

            // Assert
            verify(userDataAccessObject, never()).deleteUser(any());
            verify(deleteUserPresenter).prepareFailView("Failed to delete user: Username cannot be null");
        }

        @Test
        @DisplayName("Handle empty username")
        void deleteUser_EmptyUsername() {
            // Arrange
            DeleteUserInputData inputData = new DeleteUserInputData("");

            // Act
            interactor.execute(inputData);

            // Assert
            verify(userDataAccessObject, never()).deleteUser(any());
            verify(deleteUserPresenter).prepareFailView("Failed to delete user: Username cannot be empty");
        }

        @Test
        @DisplayName("Handle database exception")
        void deleteUser_DatabaseException() {
            // Arrange
            String username = "testUser";
            DeleteUserInputData inputData = new DeleteUserInputData(username);
            doThrow(new RuntimeException("Database error")).when(userDataAccessObject).deleteUser(username);

            // Act
            interactor.execute(inputData);

            // Assert
            verify(userDataAccessObject).deleteUser(username);
            verify(deleteUserPresenter).prepareFailView("Failed to delete user: Database error");
        }
    }

    @Nested
    @DisplayName("Load Users Tests")
    class LoadUsersTests {

        @Test
        @DisplayName("Successfully load users list")
        void loadUsers_Success() {
            // Arrange
            List<String> usersList = Arrays.asList("user1", "user2", "user3");
            when(userDataAccessObject.getAllUsers()).thenReturn(usersList);

            // Act
            interactor.loadUsers();

            // Assert
            verify(userDataAccessObject).getAllUsers();
            verify(deleteUserPresenter).presentUsersList(usersList);
        }

        @Test
        @DisplayName("Handle empty users list")
        void loadUsers_EmptyList() {
            // Arrange
            List<String> emptyList = Arrays.asList();
            when(userDataAccessObject.getAllUsers()).thenReturn(emptyList);

            // Act
            interactor.loadUsers();

            // Assert
            verify(userDataAccessObject).getAllUsers();
            verify(deleteUserPresenter).presentUsersList(emptyList);
        }

        @Test
        @DisplayName("Handle database exception during load")
        void loadUsers_DatabaseException() {
            // Arrange
            when(userDataAccessObject.getAllUsers())
                    .thenThrow(new RuntimeException("Failed to fetch users"));

            // Act
            interactor.loadUsers();

            // Assert
            verify(userDataAccessObject).getAllUsers();
            verify(deleteUserPresenter).prepareFailView("Failed to load users: Failed to fetch users");
        }

        @Test
        @DisplayName("Handle null return from database")
        void loadUsers_NullResponse() {
            // Arrange
            when(userDataAccessObject.getAllUsers()).thenReturn(null);

            // Act
            interactor.loadUsers();

            // Assert
            verify(userDataAccessObject).getAllUsers();
            verify(deleteUserPresenter).presentUsersList(null);
            verify(deleteUserPresenter, never()).prepareFailView(any());
        }
    }

    @Test
    @DisplayName("Verify delete user triggers users list reload")
    void deleteUser_TriggersReload() {
        // Arrange
        String username = "testUser";
        DeleteUserInputData inputData = new DeleteUserInputData(username);
        List<String> updatedUsersList = Arrays.asList("user1", "user3");

        doNothing().when(userDataAccessObject).deleteUser(username);
        when(userDataAccessObject.getAllUsers()).thenReturn(updatedUsersList);

        // Act
        interactor.execute(inputData);

        // Assert
        verify(userDataAccessObject).deleteUser(username);
        verify(userDataAccessObject).getAllUsers();
        verify(deleteUserPresenter).prepareSuccessView(any(DeleteUserOutputData.class));
        verify(deleteUserPresenter).presentUsersList(updatedUsersList);
    }
}