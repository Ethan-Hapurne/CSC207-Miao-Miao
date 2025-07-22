package interface_adapter.signup;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import interface_adapter.dashboard.DashboardController;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

// SESSION CHANGE: Sets current user in DashboardController after signup. See also: FirebasePostDataAccessObject, DashboardInteractor, DashboardView, LoginPresenter, LoggedInView, AppBuilder.

/**
 * The Presenter for the Signup Use Case.
 */
public class SignupPresenter implements SignupOutputBoundary {

    private final SignupViewModel signupViewModel;
    private final LoginViewModel loginViewModel;
    private final ViewManagerModel viewManagerModel;
    private final DashboardController dashboardController;

    public SignupPresenter(ViewManagerModel viewManagerModel,
                           SignupViewModel signupViewModel,
                           LoginViewModel loginViewModel,
                           DashboardController dashboardController) {
        this.viewManagerModel = viewManagerModel;
        this.signupViewModel = signupViewModel;
        this.loginViewModel = loginViewModel;
        this.dashboardController = dashboardController;
    }

    @Override
    public void prepareSuccessView(SignupOutputData response) {
        // SESSION CHANGE: Set current user in DashboardController after signup
        dashboardController.setCurrentUser(response.getUsername());
        final LoginState loginState = loginViewModel.getState();
        loginState.setUsername(response.getUsername());
        this.loginViewModel.setState(loginState);
        loginViewModel.firePropertyChanged();
        viewManagerModel.pushView(loginViewModel.getViewName());
    }

    @Override
    public void prepareFailView(String error) {
        final SignupState signupState = signupViewModel.getState();
        signupState.setUsernameError(error);
        signupViewModel.firePropertyChanged();
    }

    @Override
    public void switchToLoginView() {
        viewManagerModel.pushView(loginViewModel.getViewName());
    }
}
