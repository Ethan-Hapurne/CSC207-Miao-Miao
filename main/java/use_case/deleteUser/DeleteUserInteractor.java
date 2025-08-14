package use_case.deleteUser;

import data_access.FirebaseUserDataAccessObject;

public class DeleteUserInteractor implements DeleteUserInputBoundary {
    final FirebaseUserDataAccessObject userDataAccessObject;
    final DeleteUserOutputBoundary deleteUserOutputBoundary;

    public DeleteUserInteractor(FirebaseUserDataAccessObject userDataAccessObject,
                                DeleteUserOutputBoundary deleteUserOutputBoundary) {
        this.userDataAccessObject = userDataAccessObject;
        this.deleteUserOutputBoundary = deleteUserOutputBoundary;
    }

    @Override
    public void execute(DeleteUserInputData deleteUserInputData) {
        String username = deleteUserInputData.getUsername();

        // Validate username
        if (username == null) {
            deleteUserOutputBoundary.prepareFailView("Failed to delete user: Username cannot be null");
            return;
        }
        if (username.trim().isEmpty()) {
            deleteUserOutputBoundary.prepareFailView("Failed to delete user: Username cannot be empty");
            return;
        }

        try {
            userDataAccessObject.deleteUser(username);
            DeleteUserOutputData deleteUserOutputData = new DeleteUserOutputData(true,
                    "Successfully deleted user: " + username);
            deleteUserOutputBoundary.prepareSuccessView(deleteUserOutputData);

            // Reload users list after successful deletion
            loadUsers();
        } catch (Exception e) {
            deleteUserOutputBoundary.prepareFailView("Failed to delete user: " + e.getMessage());
        }
    }

    @Override
    public void loadUsers() {
        System.out.println("DEBUG: DeleteUserInteractor.loadUsers called");
        try {
            var users = userDataAccessObject.getAllUsers();
            System.out.println("DEBUG: Retrieved users from DAO: " + users);
            deleteUserOutputBoundary.presentUsersList(users);
        } catch (Exception e) {
            System.err.println("DEBUG: Error in loadUsers: " + e.getMessage());
            deleteUserOutputBoundary.prepareFailView("Failed to load users: " + e.getMessage());
        }
    }
}