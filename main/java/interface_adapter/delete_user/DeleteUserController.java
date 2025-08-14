package interface_adapter.delete_user;

import use_case.deleteUser.DeleteUserInputBoundary;
import use_case.deleteUser.DeleteUserInputData;

public class DeleteUserController {
    final DeleteUserInputBoundary deleteUserInputBoundary;

    public DeleteUserController(DeleteUserInputBoundary deleteUserUseCaseInteractor) {
        System.out.println("DEBUG: Creating DeleteUserController with interactor: " + (deleteUserUseCaseInteractor != null));
        this.deleteUserInputBoundary = deleteUserUseCaseInteractor;
    }


    public void execute(String username) {
        DeleteUserInputData deleteUserInputData = new DeleteUserInputData(username);
        deleteUserInputBoundary.execute(deleteUserInputData);
    }

    public void loadUsers() {
        System.out.println("DEBUG: loadUsers called, interactor is: " + (deleteUserInputBoundary != null));
        if (deleteUserInputBoundary == null) {
            throw new IllegalStateException("DeleteUserInputBoundary not initialized");
        }
        deleteUserInputBoundary.loadUsers();
    }

}