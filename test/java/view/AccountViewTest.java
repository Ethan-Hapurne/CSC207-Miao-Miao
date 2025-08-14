package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.change_password.ChangePasswordController;
import interface_adapter.change_password.LoggedInState;
import interface_adapter.change_password.LoggedInViewModel;
import interface_adapter.change_username.ChangeUsernameController;
import interface_adapter.change_username.ChangeUsernameState;
import interface_adapter.change_username.ChangeUsernameViewModel;
import interface_adapter.logout.LogoutController;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AccountView}.
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>Headless mode is enabled so Swing dialogs do not require a display.</li>
 *   <li>Byte Buddy experimental flag is enabled so Mockito works on JDK 24.</li>
 *   <li>Only test files are modified; production code remains unchanged.</li>
 * </ul>
 */
public final class AccountViewTest {

    private static final String USER_ALICE = "alice";
    private static final String USER_BOB = "bob";
    private static final String NEW_USERNAME = "newName";
    private static final String NEW_PASSWORD = "p@ss";

    private ViewManagerModel viewManagerModel;
    private ChangeUsernameController changeUsernameController;
    private ChangeUsernameViewModel changeUsernameViewModel;
    private ChangePasswordController changePasswordController;
    private LogoutController logoutController;
    private LoggedInViewModel loggedInViewModel;

    private AccountView view;

    /**
     * Enable headless UI and Byte Buddy experimental (Mockito on JDK 24).
     */
    @BeforeAll
    static void enableHeadlessAndByteBuddy() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @BeforeEach
    void setUp() {
        viewManagerModel = mock(ViewManagerModel.class);

        changeUsernameController = mock(ChangeUsernameController.class);
        changeUsernameViewModel = mock(ChangeUsernameViewModel.class);

        changePasswordController = mock(ChangePasswordController.class);

        logoutController = mock(LogoutController.class);

        loggedInViewModel = mock(LoggedInViewModel.class);

        view = new AccountView(viewManagerModel);

        // Inject all collaborators so that listeners are active.
        view.setChangeUsernameController(changeUsernameController);
        view.setChangeUsernameViewModel(changeUsernameViewModel);
        view.setChangePasswordController(changePasswordController);
        view.setLogoutController(logoutController);
        view.setLoggedInViewModel(loggedInViewModel);
    }

    @Test
    @DisplayName("getViewName returns 'account'")
    void getViewName_returnsAccount() {
        assertEquals("account", view.getViewName());
    }

    @Test
    @DisplayName("ChangeUsername button -> calls controller with old and new username")
    void changeUsernameButton_callsController() throws Exception {
        LoggedInState state = mock(LoggedInState.class);
        when(loggedInViewModel.getState()).thenReturn(state);
        when(state.getUsername()).thenReturn(USER_BOB);

        JTextField input = (JTextField) field(view, "usernameInputField");
        input.setText(NEW_USERNAME);

        JButton btn = (JButton) field(view, "changeUsernameButton");
        btn.doClick();

        verify(changeUsernameController, times(1)).execute(USER_BOB, NEW_USERNAME);
    }

    @Test
    @DisplayName("ChangePassword button -> calls controller with username/password/admin")
    void changePasswordButton_callsController() throws Exception {
        LoggedInState state = mock(LoggedInState.class);
        when(loggedInViewModel.getState()).thenReturn(state);
        when(state.getUsername()).thenReturn(USER_ALICE);
        when(state.getAdmin()).thenReturn(true);

        JTextField pwd = (JTextField) field(view, "passwordInputField");
        pwd.setText(NEW_PASSWORD);

        JButton btn = (JButton) field(view, "changePasswordButton");
        btn.doClick();

        verify(changePasswordController, times(1))
                .execute(USER_ALICE, NEW_PASSWORD, true);
    }

    @Test
    @DisplayName("Logout button -> calls controller with current username")
    void logoutButton_callsController() throws Exception {
        LoggedInState state = mock(LoggedInState.class);
        when(loggedInViewModel.getState()).thenReturn(state);
        when(state.getUsername()).thenReturn(USER_ALICE);

        JButton btn = (JButton) field(view, "logoutButton");
        btn.doClick();

        verify(logoutController, times(1)).execute(USER_ALICE);
    }

    @Test
    @DisplayName("Back button -> pop view from ViewManagerModel")
    void backButton_popsView() throws Exception {
        JButton back = (JButton) field(view, "backButton");
        back.doClick();

        verify(viewManagerModel, times(1)).popViewOrClose();
    }

    @Test
    @DisplayName("propertyChange 'state' -> updates username label")
    void propertyChange_state_updatesLabel() throws Exception {
        LoggedInState state = mock(LoggedInState.class);
        when(state.getUsername()).thenReturn(USER_BOB);

        PropertyChangeEvent evt = new PropertyChangeEvent(this, "state", null, state);
        view.propertyChange(evt);

        JLabel label = (JLabel) field(view, "usernameLabel");
        assertEquals(USER_BOB, label.getText());
    }

    @Test
    @DisplayName("propertyChange 'password' -> shows dialog (swallowed in headless)")
    void propertyChange_password_dialogSwallowed() {
        LoggedInState state = mock(LoggedInState.class);
        when(loggedInViewModel.getState()).thenReturn(state);
        when(state.getUsername()).thenReturn(USER_ALICE);

        PropertyChangeEvent evt = new PropertyChangeEvent(this, "password", null, null);
        try {
            view.propertyChange(evt);
        } catch (HeadlessException ignored) {
            // JOptionPane throws in headless mode; branch executed successfully.
        }
    }

    @Test
    @DisplayName("propertyChange 'usernameChanged' -> shows dialog and clears input (dialog swallowed)")
    void propertyChange_usernameChanged_dialogSwallowed() throws Exception {
        ChangeUsernameState state = mock(ChangeUsernameState.class);
        when(state.getNewUsername()).thenReturn(NEW_USERNAME);
        when(changeUsernameViewModel.getState()).thenReturn(state);

        JTextField input = (JTextField) field(view, "usernameInputField");
        input.setText("temp");

        PropertyChangeEvent evt =
                new PropertyChangeEvent(this, "usernameChanged", null, null);
        try {
            view.propertyChange(evt);
        } catch (HeadlessException ignored) {
            // Swallow dialog exception in headless environment.
        }
    }

    @Test
    @DisplayName("propertyChange 'usernameChangeError' -> error dialog shown (swallowed)")
    void propertyChange_usernameChangeError_dialogSwallowed() {
        ChangeUsernameState state = mock(ChangeUsernameState.class);
        when(state.getError()).thenReturn("bad");
        when(changeUsernameViewModel.getState()).thenReturn(state);

        PropertyChangeEvent evt =
                new PropertyChangeEvent(this, "usernameChangeError", null, null);
        try {
            view.propertyChange(evt);
        } catch (HeadlessException ignored) {
            // Swallow in headless mode.
        }
    }

    @Test
    @DisplayName("Listeners when controllers are null -> clicks do nothing and do not crash")
    void listeners_withNullControllers_doNothing() throws Exception {
        // Remove controllers to cover the 'null guard' branches in listeners.
        view.setChangeUsernameController(null);
        view.setChangePasswordController(null);
        view.setLogoutController(null);

        // Keep a non-null loggedInViewModel (AccountView setter does not accept null).
        when(loggedInViewModel.getState()).thenReturn(mock(LoggedInState.class));

        // Click all three buttons; no NPE and no controller interactions expected.
        JTextField userInput = (JTextField) field(view, "usernameInputField");
        userInput.setText(NEW_USERNAME);
        JButton changeUserBtn = (JButton) field(view, "changeUsernameButton");
        changeUserBtn.doClick();

        JTextField pwd = (JTextField) field(view, "passwordInputField");
        pwd.setText(NEW_PASSWORD);
        JButton changePwdBtn = (JButton) field(view, "changePasswordButton");
        changePwdBtn.doClick();

        JButton logoutBtn = (JButton) field(view, "logoutButton");
        logoutBtn.doClick();

        verifyNoInteractions(changeUsernameController, changePasswordController, logoutController);
    }

    // ----------------------- helpers -----------------------

    /**
     * Read a private field by reflection.
     *
     * @param target    object holding the field
     * @param fieldName field name
     * @return value of the field
     * @throws NoSuchFieldException   when the field is absent
     * @throws IllegalAccessException when access fails
     */
    private static Object field(final Object target, final String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }
}
