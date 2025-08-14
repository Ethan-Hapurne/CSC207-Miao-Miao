package app;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import data_access.FirebaseConfig;
import data_access.FirebasePostDataAccessObject;
import data_access.FirebaseUserDataAccessObject;
import entity.CommonUserFactory;
import entity.UserFactory;
import interface_adapter.ViewManagerModel;
import interface_adapter.adminloggedIn.AdminLoggedInViewModel;
import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.change_password.ChangePasswordPresenter;
import interface_adapter.change_password.LoggedInViewModel;
import interface_adapter.delete_user.DeleteUserController;
import interface_adapter.delete_user.DeleteUserPresenter;
import interface_adapter.delete_user.DeleteUserViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchPresenter;
import interface_adapter.search.SearchViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
import interface_adapter.dashboard.DashboardController;
import interface_adapter.dashboard.DashboardPresenter;
import interface_adapter.dashboard.DashboardViewModel;
import interface_adapter.change_username.ChangeUsernameController;
import interface_adapter.change_username.ChangeUsernamePresenter;
import interface_adapter.change_username.ChangeUsernameViewModel;
import interface_adapter.admin.AdminController;
import interface_adapter.admin.AdminPresenter;
import interface_adapter.admin.AdminViewModel;
import use_case.admin.AdminInputBoundary;
import use_case.admin.AdminInteractor;
import use_case.admin.AdminOutputBoundary;
import use_case.admin.AdminUserDataAccessInterface;
import use_case.change_password.ChangePasswordOutputBoundary;
import use_case.change_password.ChangePasswordInputBoundary;
import use_case.change_password.ChangePasswordInteractor;
import use_case.change_password.ChangePasswordOutputBoundary;
import use_case.deleteUser.DeleteUserInputBoundary;
import use_case.deleteUser.DeleteUserInteractor;
import use_case.deleteUser.DeleteUserOutputBoundary;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.logout.LogoutInputBoundary;
import use_case.logout.LogoutInteractor;
import use_case.logout.LogoutOutputBoundary;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInteractor;
import use_case.search.SearchOutputBoundary;
import use_case.search.SearchUserDataAccessInterface;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputBoundary;
import use_case.dashboard.DashboardInputBoundary;
import use_case.dashboard.DashboardInteractor;
import use_case.dashboard.DashboardOutputBoundary;
import use_case.dashboard.DashboardUserDataAccessInterface;
import view.DashboardView;
import view.AccountView;
import interface_adapter.change_username.ChangeUsernameController;
import interface_adapter.change_username.ChangeUsernamePresenter;
import interface_adapter.change_username.ChangeUsernameViewModel;
import use_case.change_username.ChangeUsernameInputBoundary;
import use_case.change_username.ChangeUsernameInteractor;
import use_case.change_username.ChangeUsernameOutputBoundary;
import use_case.change_username.ChangeUsernameUserDataAccessInterface;
import view.DMsView;
import interface_adapter.dms.DMsController;
import interface_adapter.dms.DMsPresenter;
import interface_adapter.dms.DMsViewModel;
import use_case.dms.DMsInputBoundary;
import use_case.dms.DMsInteractor;
import use_case.dms.DMsOutputBoundary;
import use_case.dms.DMsUserDataAccessInterface;
import interface_adapter.fuzzy_search.FuzzySearchController;
import interface_adapter.fuzzy_search.FuzzySearchPresenter;
import interface_adapter.fuzzy_search.FuzzySearchState;
import interface_adapter.fuzzy_search.FuzzySearchViewModel;
import use_case.fuzzy_search.FuzzySearchInputBoundary;
import use_case.fuzzy_search.FuzzySearchInteractor;
import use_case.fuzzy_search.FuzzySearchOutputBoundary;
import use_case.fuzzy_search.FuzzySearchUserDataAccessInterface;
import data_access.FirebaseChatDataAccessObject;

import view.*;
import java.awt.Component;

/**
 * The AppBuilder class is responsible for putting together the pieces of
 * our CA architecture; piece by piece.
 * <p/>
 * This is done by adding each View and then adding related Use Cases.
 */
// Checkstyle note: you can ignore the "Class Data Abstraction Coupling"
//                  and the "Class Fan-Out Complexity" issues for this lab; we encourage
//                  your team to think about ways to refactor the code to resolve these
//                  if your team decides to work with this as your starter code
//                  for your final project this term.
public class AppBuilder {
    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    // thought question: is the hard dependency below a problem?
    private final UserFactory userFactory = new CommonUserFactory();
    private final ViewManagerModel viewManagerModel = new ViewManagerModel();
    private final ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    // thought question: is the hard dependency below a problem?
    private final FirebasePostDataAccessObject firebasePostDataAccessObject = new FirebasePostDataAccessObject();
    // private final DBUserDataAccessObject userDataAccessObject = new DBUserDataAccessObject(userFactory);
    // private final InMemoryUserDataAccessObject userDataAccessObject = new InMemoryUserDataAccessObject();
    private final FirebaseUserDataAccessObject userDataAccessObject = new FirebaseUserDataAccessObject();
    private final SearchUserDataAccessInterface postDataAccessObject = new FirebasePostDataAccessObject();
    private final DashboardUserDataAccessInterface dashboardDataAccessObject = new FirebasePostDataAccessObject();
    private final FirebasePostDataAccessObject adminDataAccessObject = new FirebasePostDataAccessObject();
    private final FirebasePostDataAccessObject deletePostDataAccessObject = new FirebasePostDataAccessObject();
    private final DMsUserDataAccessInterface dmsDataAccessObject = new FirebaseChatDataAccessObject();

    private SignupView signupView;
    private SignupViewModel signupViewModel;
    private LoginViewModel loginViewModel;
    private LoggedInViewModel loggedInViewModel;
    private LoggedInView loggedInView;
    private LoginView loginView;
    private SearchView searchView;
    private SearchViewModel searchViewModel;
    private AdvancedSearchView advancedSearchView;
    private DashboardView dashboardView;
    private DashboardViewModel dashboardViewModel;
    private AccountView accountView;
    private ChangeUsernameController changeUsernameController;
    private ChangeUsernameViewModel changeUsernameViewModel;
    private DMsView dmsView;
    private DashboardController dashboardController;
    private AdminView adminView;
    private AdminViewModel adminViewModel;
    private AdminLoggedInView adminloggedInView;
    private AdminLoggedInViewModel adminloggedInViewModel;
    private DeleteUserViewModel deleteUserViewModel;
    private DeleteUserView deleteUserView;
    private DeleteUserController deleteUserController;
    private DeleteUserInputBoundary deleteUserUseCaseInteractor;
    private DMsViewModel dmsViewModel;
    private FuzzySearchViewModel fuzzySearchViewModel;
    private FuzzySearchController fuzzySearchController;
    private FuzzySearchInputBoundary fuzzySearchUseCaseInteractor;

    public AppBuilder() {
        // Initialize Firebase
        FirebaseConfig.initializeFirebase();

        cardPanel.setLayout(cardLayout);
    }

    /**
     * Adds the Signup View to the application.
     * @return this builder
     */
    public AppBuilder addSignupView() {
        signupViewModel = new SignupViewModel();
        signupView = new SignupView(signupViewModel, viewManagerModel);
        
        // Set the component name to match the viewName
        signupView.setName("sign up");
        
        cardPanel.add(signupView, signupView.getViewName());
        return this;
    }

    /**
     * Adds the Login View to the application.
     * @return this builder
     */
    public AppBuilder addLoginView() {
        loginViewModel = new LoginViewModel();
        loginView = new LoginView(loginViewModel, viewManagerModel);
        
        // Set the component name to match the viewName
        loginView.setName("log in");
        
        cardPanel.add(loginView, loginView.getViewName());
        return this;
    }

    /**
     * Adds the LoggedIn View to the application.
     * @return this builder
     */
    public AppBuilder addLoggedInView() {
        loggedInViewModel = new LoggedInViewModel();
        loggedInView = new LoggedInView(loggedInViewModel, viewManagerModel);
        cardPanel.add(loggedInView, loggedInView.getViewName());
        // Wire dashboardController if it exists
        if (dashboardController != null) {
            loggedInView.setDashboardController(dashboardController);
        }
        return this;
    }

    public AppBuilder addAdminLoggedInView() {
        adminloggedInViewModel = new AdminLoggedInViewModel();
        adminloggedInView = new AdminLoggedInView(adminloggedInViewModel, viewManagerModel);
        
        // Set the component name to match the viewName
        adminloggedInView.setName("admin logged in");
        
        cardPanel.add(adminloggedInView, adminloggedInView.getViewName());
        return this;
    }

    /**
     * Adds the Search View to the application.
     * @return this builder
     */
    public AppBuilder addSearchView() {
        searchViewModel = new SearchViewModel();
        searchView = new SearchView(searchViewModel);
        
        // Set the component name to match the viewName
        searchView.setName("search");
        
        cardPanel.add(searchView, searchView.getViewName());
        return this;
    }

    /**
     * Adds the Advanced Search View to the application.
     * @return this builder
     */
    public AppBuilder addAdvancedSearchView() {
        if (searchViewModel == null) {
            searchViewModel = new SearchViewModel();
        }
        advancedSearchView = new AdvancedSearchView(searchViewModel);
        
        // Set the component name to match the viewName
        advancedSearchView.setName("advanced search");
        
        cardPanel.add(advancedSearchView, advancedSearchView.getViewName());
        return this;
    }

    /**
     * Adds the Dashboard View to the application.
     * @return this builder
     */
    public AppBuilder addDashboardView() {
        dashboardViewModel = new DashboardViewModel();
        dashboardView = new DashboardView(dashboardViewModel);
        
        // Set the component name to match the viewName
        dashboardView.setName("dashboard");
        
        cardPanel.add(dashboardView, dashboardView.getViewName());
        return this;
    }

    public AppBuilder addAdminView() {
        adminViewModel = new AdminViewModel();
        adminView = new AdminView(adminViewModel);
        
        // Make sure the view component itself has a name
        adminView.setName("admin");
        
        // Add the view to the card panel with the view name from getViewName()
        cardPanel.add(adminView, adminView.getViewName());
        return this;
    }

    /**
     * Adds the Account View to the application.
     * @return this builder
     */
    public AppBuilder addAccountView() {
        accountView = new AccountView(viewManagerModel);
        accountView.setLoggedInViewModel(loggedInViewModel);
        cardPanel.add(accountView, accountView.getViewName());
        return this;
    }

    /**
     * Adds the DMs View to the application.
     * @return this builder
     */
    public AppBuilder addDMsView() {
        dmsViewModel = new DMsViewModel();
        dmsView = new DMsView(viewManagerModel, dmsViewModel);
        
        // Set the component name to match the viewName
        dmsView.setName("dms");
        
        cardPanel.add(dmsView, dmsView.getViewName());
        return this;
    }

    public AppBuilder addDeleteUserView() {
        deleteUserViewModel = new DeleteUserViewModel();
        DeleteUserInputBoundary deleteUserUseCaseInteractor = new DeleteUserInteractor(userDataAccessObject, new DeleteUserPresenter(deleteUserViewModel));
        DeleteUserController deleteUserController = new DeleteUserController(deleteUserUseCaseInteractor);
        // Initially create view with null controller
        deleteUserView = new DeleteUserView(deleteUserViewModel, deleteUserController, viewManagerModel);
        cardPanel.add(deleteUserView, deleteUserView.getViewName());
        return this;
    }

    public AppBuilder addFuzzySearchView() {
        fuzzySearchViewModel = new FuzzySearchViewModel();
        
        // Create the use case components first so the view is ready immediately
        FuzzySearchUserDataAccessInterface fuzzySearchDataAccessObject = firebasePostDataAccessObject;
        FuzzySearchOutputBoundary fuzzySearchPresenter = new FuzzySearchPresenter(fuzzySearchViewModel);
        fuzzySearchUseCaseInteractor = new FuzzySearchInteractor(fuzzySearchDataAccessObject, fuzzySearchPresenter);
        
        // Create controller with the interactor
        fuzzySearchController = new FuzzySearchController(fuzzySearchUseCaseInteractor);
        
        // Create view with the ready controller
        FuzzySearchView fuzzySearchView = new FuzzySearchView(fuzzySearchViewModel, fuzzySearchController, viewManagerModel);
        
        // Set the component name to match the viewName
        fuzzySearchView.setName("fuzzy search");
        
        cardPanel.add(fuzzySearchView, fuzzySearchView.viewName);
        return this;
    }

    /**
     * Adds the Signup Use Case to the application.
     * @return this builder
     */
    public AppBuilder addSignupUseCase() {
        if (dashboardController == null) {
            addDashboardUseCase();
        }
        final SignupOutputBoundary signupOutputBoundary = new SignupPresenter(viewManagerModel,
                signupViewModel, loginViewModel, dashboardController);
        final SignupInputBoundary userSignupInteractor = new SignupInteractor(
                userDataAccessObject, signupOutputBoundary, userFactory);

        final SignupController controller = new SignupController(userSignupInteractor);
        signupView.setSignupController(controller);
        return this;
    }

    /**
     * Adds the Login Use Case to the application.
     * @return this builder
     */
    public AppBuilder addLoginUseCase() {
        if (dashboardController == null) {
            addDashboardUseCase();
        }
        final LoginOutputBoundary loginOutputBoundary = new LoginPresenter(viewManagerModel,
                loggedInViewModel, loginViewModel, dashboardController, adminloggedInView, adminloggedInViewModel);
        final LoginInputBoundary loginInteractor = new LoginInteractor(
                userDataAccessObject, loginOutputBoundary);

        final LoginController loginController = new LoginController(loginInteractor);
        loginView.setLoginController(loginController);
        return this;
    }

    /**
     * Adds the Change Password Use Case to the application.
     * @return this builder
     */
    public AppBuilder addChangePasswordUseCase() {
        final ChangePasswordOutputBoundary changePasswordOutputBoundary =
                new ChangePasswordPresenter(loggedInViewModel);

        final ChangePasswordInputBoundary changePasswordInteractor =
                new ChangePasswordInteractor(userDataAccessObject, changePasswordOutputBoundary, userFactory);

        final ChangePasswordController changePasswordController =
                new ChangePasswordController(changePasswordInteractor);
        loggedInView.setChangePasswordController(changePasswordController);
        if (accountView != null) accountView.setChangePasswordController(changePasswordController);
        return this;
    }

    /**
     * Adds the Logout Use Case to the application.
     * @return this builder
     */
    public AppBuilder addLogoutUseCase() {
        final LogoutOutputBoundary logoutOutputBoundary = new LogoutPresenter(viewManagerModel,
                loggedInViewModel, loginViewModel);

        final LogoutInputBoundary logoutInteractor =
                new LogoutInteractor(userDataAccessObject, logoutOutputBoundary);

        final LogoutController logoutController = new LogoutController(logoutInteractor);
        loggedInView.setLogoutController(logoutController);
        if (accountView != null) accountView.setLogoutController(logoutController);
        return this;
    }

    /**
     * Adds the Search Use Case to the application.
     * @return this builder
     */
    public AppBuilder addSearchUseCase() {
        final SearchOutputBoundary searchOutputBoundary = new SearchPresenter(searchViewModel);
        final SearchInputBoundary searchInteractor = new SearchInteractor(postDataAccessObject, searchOutputBoundary);
        final SearchController searchController = new SearchController(searchInteractor, viewManagerModel);
        searchView.setSearchController(searchController);
        
        // Also set the controller for AdvancedSearchView if it exists
        if (advancedSearchView != null) {
            advancedSearchView.setSearchController(searchController);
        }
        return this;
    }

    /**
     * Adds the Dashboard Use Case to the application.
     * @return this builder
     */
    public AppBuilder addDashboardUseCase() {
        final DashboardOutputBoundary dashboardOutputBoundary = new DashboardPresenter(dashboardViewModel);
        final DashboardInputBoundary dashboardInteractor = new DashboardInteractor(dashboardDataAccessObject, dashboardOutputBoundary);
        this.dashboardController = new DashboardController(dashboardInteractor, viewManagerModel);
        dashboardView.setDashboardController(dashboardController);
        dashboardController.setDashboardView(dashboardView);
        // Wire into loggedInView if it exists
        if (loggedInView != null) {
            loggedInView.setDashboardController(dashboardController);
        }
        return this;
    }

    public AppBuilder addAdminUseCase() {
        final AdminOutputBoundary adminOutputBoundary = new AdminPresenter(adminViewModel);
        final AdminInputBoundary adminInteractor = new AdminInteractor(adminDataAccessObject, adminOutputBoundary);
        final AdminController adminController = new AdminController(adminInteractor, viewManagerModel);
        adminView.setAdminController(adminController);
        return this;
    }

    /**
     * Adds the Change Username Use Case to the application.
     * @return this builder
     */
    public AppBuilder addChangeUsernameUseCase() {
        changeUsernameViewModel = new ChangeUsernameViewModel();
        ChangeUsernameOutputBoundary outputBoundary = new ChangeUsernamePresenter(changeUsernameViewModel, loggedInViewModel);
        ChangeUsernameUserDataAccessInterface dao = userDataAccessObject;
        ChangeUsernameInputBoundary interactor = new ChangeUsernameInteractor(dao, outputBoundary);
        changeUsernameController = new ChangeUsernameController(interactor);
        if (accountView != null) {
            accountView.setChangeUsernameController(changeUsernameController);
            accountView.setChangeUsernameViewModel(changeUsernameViewModel);
        }
        return this;
    }

    /**
     * Adds the DMs Use Case to the application.
     * @return this builder
     */
    public AppBuilder addDMsUseCase() {
        final DMsOutputBoundary dMsOutputBoundary = new DMsPresenter(dmsViewModel);
        final DMsInputBoundary dMsInteractor = new DMsInteractor(dmsDataAccessObject, dMsOutputBoundary);
        final DMsController dMsController = new DMsController(dMsInteractor);
        dmsView.setDMsController(dMsController);
        
        // Only set DMsView if loggedInView exists
        if (loggedInView != null) {
            loggedInView.setDMsView(dmsView);
        }
        
        // Only set DMsView if adminloggedInView exists
        if (adminloggedInView != null) {
            adminloggedInView.setDMsView(dmsView);
        }
        
        return this;
    }

    public AppBuilder addDeletePostUseCase() {
        final AdminOutputBoundary deletePostOutputBoundary =
                new AdminPresenter(adminViewModel);
        final AdminInputBoundary deletePostInteractor =
                new AdminInteractor(deletePostDataAccessObject, deletePostOutputBoundary);
        final AdminController deletePostController =
                new AdminController(deletePostInteractor,viewManagerModel);

        // Add the controller to admin view
        adminView.setAdminController(deletePostController);
        return this;
    }

    public AppBuilder addDeleteUserUseCase() {
        // Create all necessary components
        FirebaseUserDataAccessObject userDataAccessObject = new FirebaseUserDataAccessObject();
        DeleteUserOutputBoundary deleteUserPresenter = new DeleteUserPresenter(deleteUserViewModel);
        deleteUserUseCaseInteractor = new DeleteUserInteractor(userDataAccessObject, deleteUserPresenter);

        // Create controller with the interactor and set it to the view
        deleteUserController = new DeleteUserController(deleteUserUseCaseInteractor);

        // This line is crucial - it connects the controller to the view
        deleteUserView.setDeleteUserController(deleteUserController);

        // Add debug logging
        System.out.println("DEBUG: DeleteUserUseCase initialized - Controller: " + (deleteUserController != null) +
                         ", Interactor: " + (deleteUserUseCaseInteractor != null));

        return this;
    }




    /**
     * Creates the JFrame for the application and initially sets the SignupView to be displayed.
     * @return the application
     */
    public JFrame build() {
        final JFrame application = new JFrame("Login Example");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        application.add(cardPanel);

        viewManagerModel.setMainFrame(application);
        
        // Choose an initial view to display
        if (signupView != null) {
            viewManagerModel.pushView(signupView.getViewName());
        } else if (loginView != null) {
            viewManagerModel.pushView(loginView.getViewName());
        } else if (dashboardView != null) {
            viewManagerModel.pushView(dashboardView.getViewName());
        } else if (adminView != null) {
            viewManagerModel.pushView(adminView.getViewName());
        }
        // If none of the above views exist, don't push any view

        return application;
    }


}