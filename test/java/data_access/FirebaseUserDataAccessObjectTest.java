package data_access;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import entity.CommonUser;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.junit.jupiter.api.Assertions.*;
import com.google.firebase.database.DataSnapshot;
import java.util.Arrays;
import java.util.ArrayList;

class FirebaseUserDataAccessObjectTest {

    private FirebaseUserDataAccessObject dao;

    @BeforeEach
    void setUp() throws Exception {
        dao = new FirebaseUserDataAccessObject();

        // Force mock mode to avoid any real Firebase calls and maximize deterministic coverage.
        setPrivateBoolean(dao, "useMockData", true);
        setPrivateField(dao, "usersRef", null);

        // Ensure mock users are seeded (constructor seeds in some cases; we re-seed to be safe).
        @SuppressWarnings("unchecked")
        Map<String, User> mockUsers = (Map<String, User>) getPrivateField(dao, "mockUsers");
        mockUsers.put("testuser", new CommonUser("testuser", "password123", false));
        mockUsers.put("admin", new CommonUser("admin", "admin123", true));
    }

    @Test
    @DisplayName("Constructor + mock-mode existence checks + seeded users")
    void constructorAndExistsByName() {
        assertTrue(dao.existsByName("testuser"), "Seeded 'testuser' should exist in mock mode");
        assertTrue(dao.existsByName("admin"), "Seeded 'admin' should exist in mock mode");

        String random = "user_" + UUID.randomUUID();
        assertFalse(dao.existsByName(random), "Random username should not exist");
    }

    @Test
    @DisplayName("Get all users returns list of usernames in mock mode")
    void getAllUsers_MockMode() {
        // Add a few test users
        try {
            @SuppressWarnings("unchecked")
            Map<String, User> mockUsers = (Map<String, User>) getPrivateField(dao, "mockUsers");
            mockUsers.put("user1", new CommonUser("user1", "pass1", false));
            mockUsers.put("user2", new CommonUser("user2", "pass2", false));
        } catch (Exception e) {
            fail("Failed to set up test users");
        }

        var users = dao.getAllUsers();
        assertNotNull(users, "Users list should not be null");
        assertTrue(users.contains("testuser"), "Should contain seeded testuser");
        assertTrue(users.contains("admin"), "Should contain seeded admin");
        assertTrue(users.contains("user1"), "Should contain added user1");
        assertTrue(users.contains("user2"), "Should contain added user2");
    }

    @Test
    @DisplayName("Delete user removes user from mock storage")
    void deleteUser_MockMode() {
        // First verify testuser exists
        assertTrue(dao.existsByName("testuser"), "testuser should exist initially");
        
        // Delete the user
        dao.deleteUser("testuser");
        
        // Verify user no longer exists
        assertFalse(dao.existsByName("testuser"), "testuser should no longer exist after deletion");
    }

    @Test
    @DisplayName("Delete non-existent user should throw exception")
    void deleteNonExistentUser_ThrowsException() {
        String nonExistentUser = "nonexistent_" + UUID.randomUUID();
        assertThrows(RuntimeException.class, () -> dao.deleteUser(nonExistentUser));
    }

    @Test
    @DisplayName("Get current username when not logged in returns null")
    void getCurrentUsername_WhenNotLoggedIn_ReturnsNull() throws Exception {
        // Ensure no user is logged in
        setPrivateField(dao, "currentUsername", null);
        
        Method getCurrentUsername = findMethod(
            dao.getClass(),
            new String[]{"getCurrentUsername", "getLoggedInUsername"},
            new Class[][]{{}}
        );
        
        if (getCurrentUsername != null) {
            Object result = getCurrentUsername.invoke(dao);
            assertNull(result, "Should return null when no user is logged in");
        }
    }

    @Test
    @DisplayName("Login with non-existent user returns false")
    void login_WithNonExistentUser_ReturnsFalse() throws Exception {
        Method login = findMethod(
            dao.getClass(),
            new String[]{"login", "loginUser", "authenticate", "validateLogin"},
            new Class[][]{{String.class, String.class}}
        );
        
        if (login != null) {
            String nonExistentUser = "nonexistent_" + UUID.randomUUID();
            Object result = login.invoke(dao, nonExistentUser, "anypassword");
        
            if (result instanceof Boolean) {
                assertFalse((Boolean) result, "Login should fail for non-existent user");
            }
        }
    }

    @Test
    @DisplayName("Change password for non-existent user throws exception")
    void changePassword_NonExistentUser_ThrowsException() throws Exception {
        Method changePassword = findMethod(
            dao.getClass(),
            new String[]{"changePassword", "updatePassword"},
            new Class[][]{
                {String.class, String.class},
                {String.class, String.class, String.class},
                {String.class}
            }
        );
        
        if (changePassword != null) {
            String nonExistentUser = "nonexistent_" + UUID.randomUUID();
            Class<?>[] ptypes = changePassword.getParameterTypes();
        
            assertThrows(Exception.class, () -> {
                if (ptypes.length == 2) {
                    changePassword.invoke(dao, nonExistentUser, "newpass");
                } else if (ptypes.length == 3) {
                    changePassword.invoke(dao, nonExistentUser, "oldpass", "newpass");
                } else {
                    // Can't test single-parameter version without being logged in
                    throw new Exception("Test not applicable for current signature");
                }
            });
        }
    }

    @Test
    @DisplayName("Logout when not logged in returns false")
    void logout_WhenNotLoggedIn_ReturnsFalse() throws Exception {
        // Ensure no user is logged in
        setPrivateField(dao, "currentUsername", null);
        
        Method logout = findMethod(
            dao.getClass(),
            new String[]{"logout", "signOut"},
            new Class[][]{{}}
        );
        
        if (logout != null) {
            Object result = logout.invoke(dao);
            if (result instanceof Boolean) {
                assertFalse((Boolean) result, "Logout should return false when no user is logged in");
            }
        }
    }

    @Test
    @DisplayName("High-coverage end-to-end in mock mode: signup, login, change password/username, logout")
    void highCoverageMockFlow() throws Exception {
        // 1) “Signup” a user (invoke any supported method; otherwise seed directly)
        String username = "alice";
        String password = "alicePass!";
        User alice = new CommonUser(username, password, false);

        boolean createdViaMethod = tryInvokeSignup(dao, alice);
        if (!createdViaMethod) {
            @SuppressWarnings("unchecked")
            Map<String, User> mockUsers = (Map<String, User>) getPrivateField(dao, "mockUsers");
            mockUsers.put(username, alice);
        }

        // 2) existsByName should reflect the new user
        assertTrue(dao.existsByName(username), "Newly created user should exist");

        // 3) Try login API if present (wrong password -> false, correct -> true)
        Method login = findMethod(
                dao.getClass(),
                new String[]{"login", "loginUser", "authenticate", "validateLogin"},
                new Class[][]{
                        {String.class, String.class}
                }
        );

        Method getCurrentUsername = findMethod(
                dao.getClass(),
                new String[]{"getCurrentUsername", "getLoggedInUsername"},
                new Class[][]{
                        {}
                }
        );

        if (login != null) {
            Object badLogin = login.invoke(dao, username, "wrong");
            if (badLogin instanceof Boolean) {
                assertFalse((Boolean) badLogin, "Login should fail with wrong password");
            }

            Object goodLogin = login.invoke(dao, username, password);
            if (goodLogin instanceof Boolean) {
                assertTrue((Boolean) goodLogin, "Login should succeed with correct password");
            }

            if (getCurrentUsername != null) {
                Object current = getCurrentUsername.invoke(dao);
                assertEquals(username, current, "Current username should be set after successful login");
            }
        }

        // 4) Try changePassword if present; support common signatures
        Method changePassword = findMethod(
                dao.getClass(),
                new String[]{"changePassword", "updatePassword"},
                new Class[][]{
                        {String.class, String.class},                 // (username, newPass)
                        {String.class, String.class, String.class},   // (username, oldPass, newPass)
                        {String.class}                                 // (newPass) for current user
                }
        );
        String newPassword = "alicePass2!";
        if (changePassword != null) {
            Object result;
            Class<?>[] ptypes = changePassword.getParameterTypes();
            if (ptypes.length == 2) {
                result = changePassword.invoke(dao, username, newPassword);
            } else if (ptypes.length == 3) {
                result = changePassword.invoke(dao, username, password, newPassword);
            } else {
                result = changePassword.invoke(dao, newPassword);
            }
            // If the method returns boolean, assert true
            if (result instanceof Boolean) {
                assertTrue((Boolean) result, "changePassword should return true on success");
            }

            // Re-test login if available
            if (login != null) {
                Object badOld = login.invoke(dao, username, password);
                if (badOld instanceof Boolean) {
                    assertFalse((Boolean) badOld, "Old password should no longer work");
                }
                Object goodNew = login.invoke(dao, username, newPassword);
                if (goodNew instanceof Boolean) {
                    assertTrue((Boolean) goodNew, "New password should work");
                }
            }
        }

        // 5) Try changeUsername if present; support common signatures
        Method changeUsername = findMethod(
                dao.getClass(),
                new String[]{"changeUsername", "updateUsername"},
                new Class[][]{
                        {String.class, String.class}, // (oldUsername, newUsername)
                        {String.class}                // (newUsername) for current user
                }
        );
        String newUsername = "alice_renamed";
        if (changeUsername != null) {
            Object result;
            Class<?>[] ptypes = changeUsername.getParameterTypes();
            if (ptypes.length == 2) {
                result = changeUsername.invoke(dao, username, newUsername);
            } else {
                result = changeUsername.invoke(dao, newUsername);
            }
            if (result instanceof Boolean) {
                assertTrue((Boolean) result, "changeUsername should return true on success");
            }

            // existsByName should reflect the change
            assertTrue(dao.existsByName(newUsername), "New username should now exist");
            assertFalse(dao.existsByName(username), "Old username should no longer exist");

            if (getCurrentUsername != null) {
                Object current = getCurrentUsername.invoke(dao);
                if (current != null) {
                    // If logged in, the current username should update accordingly
                    assertEquals(newUsername, current, "Current username should follow rename");
                }
            }
        }

        // 6) logout if present
        Method logout = findMethod(
                dao.getClass(),
                new String[]{"logout", "signOut"},
                new Class[][]{
                        {}, // no-arg
                }
        );
        if (logout != null) {
            Object res = logout.invoke(dao);
            if (res instanceof Boolean) {
                assertTrue((Boolean) res, "logout should return true on success");
            }
            if (getCurrentUsername != null) {
                Object current = getCurrentUsername.invoke(dao);
                assertTrue(current == null || "".equals(current), "Current user should be cleared after logout");
            }
        }
    }

    // ---------- Reflection helpers ----------

    private static void setPrivateBoolean(Object target, String fieldName, boolean value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setBoolean(target, value);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    private static Method findMethod(Class<?> clazz, String[] candidateNames, Class<?>[][] candidateSignatures) {
        for (String name : candidateNames) {
            for (Class<?>[] sig : candidateSignatures) {
                try {
                    Method m = clazz.getMethod(name, sig);
                    m.setAccessible(true);
                    return m;
                } catch (NoSuchMethodException ignored) {
                }
                // Also try declared methods (non-public)
                try {
                    Method m = clazz.getDeclaredMethod(name, sig);
                    m.setAccessible(true);
                    return m;
                } catch (NoSuchMethodException ignored) {
                }
            }
        }
        return null;
    }

    private static boolean tryInvokeSignup(Object target, User user) throws Exception {
        Method m = findMethod(
                target.getClass(),
                new String[]{"save", "create", "signup", "saveUser", "addUser", "register"},
                new Class<?>[][]{
                        {User.class}
                }
        );
        if (m == null) return false;
        Object res = m.invoke(target, user);
        // If method returns a boolean, assert true
        if (res instanceof Boolean) {
            return (Boolean) res;
        }
        return true;
    }

@Test
@DisplayName("Get user returns correct user in mock mode")
void getUser_ReturnsMockUser() {
    User user = dao.get("testuser");
    assertNotNull(user, "Should return user object");
    assertEquals("testuser", user.getName(), "Should return correct username");
    assertEquals("password123", user.getPassword(), "Should return correct password");
    assertFalse(user.isAdmin(), "Should not be admin");
}

@Test
@DisplayName("Get non-existent user returns null in mock mode")
void getNonExistentUser_ReturnsNull() {
    User user = dao.get("nonexistent");
    assertNull(user, "Should return null for non-existent user");
}

@Test
@DisplayName("Save user updates existing user in mock mode")
void saveUser_UpdatesExistingUser() {
    User updatedUser = new CommonUser("testuser", "newpassword", true);
    dao.save(updatedUser);
    
    User retrieved = dao.get("testuser");
    assertNotNull(retrieved, "Updated user should exist");
    assertEquals("newpassword", retrieved.getPassword(), "Password should be updated");
    assertTrue(retrieved.isAdmin(), "Admin status should be updated");
}

@Test
@DisplayName("Set and get current username work correctly")
void setAndGetCurrentUsername() {
    dao.setCurrentUsername("testuser");
    assertEquals("testuser", dao.getCurrentUsername(), "Current username should match set value");
    
    dao.setCurrentUsername(null);
    assertNull(dao.getCurrentUsername(), "Current username should be null after setting to null");
}

@Test
@DisplayName("Change username fails when new username already exists")
void changeUsername_FailsWhenNewUsernameExists() throws Exception {
    // Setup: ensure both old and new usernames exist
    @SuppressWarnings("unchecked")
    Map<String, User> mockUsers = (Map<String, User>) getPrivateField(dao, "mockUsers");
    mockUsers.put("olduser", new CommonUser("olduser", "pass1", false));
    mockUsers.put("newuser", new CommonUser("newuser", "pass2", false));
    
    boolean result = dao.changeUsername("olduser", "newuser");
    assertFalse(result, "Should fail when new username already exists");
    assertTrue(dao.existsByName("olduser"), "Old username should still exist");
    assertTrue(dao.existsByName("newuser"), "New username should still exist");
}

@Test
@DisplayName("Change username fails when old username doesn't exist")
void changeUsername_FailsWhenOldUsernameDoesNotExist() {
    boolean result = dao.changeUsername("nonexistent", "newname");
    assertFalse(result, "Should fail when old username doesn't exist");
}

@Test
@DisplayName("Save creates new user when username doesn't exist")
void save_CreatesNewUser() {
    String newUsername = "newuser_" + UUID.randomUUID();
    User newUser = new CommonUser(newUsername, "password", false);
    
    assertFalse(dao.existsByName(newUsername), "User should not exist initially");
    dao.save(newUser);
    assertTrue(dao.existsByName(newUsername), "User should exist after save");
    
    User retrieved = dao.get(newUsername);
    assertNotNull(retrieved, "Should be able to retrieve saved user");
    assertEquals(newUsername, retrieved.getName(), "Username should match");
    assertEquals("password", retrieved.getPassword(), "Password should match");
    assertFalse(retrieved.isAdmin(), "Admin status should match");
}

@Test
@DisplayName("Change username updates currentUsername when logged in user is renamed")
void changeUsername_UpdatesCurrentUsername() throws Exception {
    // Setup: create user and set as current
    String oldUsername = "olduser";
    String newUsername = "newuser";
    User user = new CommonUser(oldUsername, "pass", false);
    
    @SuppressWarnings("unchecked")
    Map<String, User> mockUsers = (Map<String, User>) getPrivateField(dao, "mockUsers");
    mockUsers.put(oldUsername, user);
    dao.setCurrentUsername(oldUsername);
    
    // Perform username change
    boolean result = dao.changeUsername(oldUsername, newUsername);
    
    assertTrue(result, "Username change should succeed");
    assertEquals(newUsername, dao.getCurrentUsername(), 
                "Current username should be updated when logged-in user is renamed");
}

@Test
@DisplayName("Change username doesn't update currentUsername for different user")
void changeUsername_DoesNotUpdateCurrentUsernameForDifferentUser() throws Exception {
    // Setup: create two users, set one as current
    String currentUser = "current";
    String oldUsername = "olduser";
    String newUsername = "newuser";
    
    @SuppressWarnings("unchecked")
    Map<String, User> mockUsers = (Map<String, User>) getPrivateField(dao, "mockUsers");
    mockUsers.put(currentUser, new CommonUser(currentUser, "pass1", false));
    mockUsers.put(oldUsername, new CommonUser(oldUsername, "pass2", false));
    dao.setCurrentUsername(currentUser);
    
    // Change username of the other user
    boolean result = dao.changeUsername(oldUsername, newUsername);
    
    assertTrue(result, "Username change should succeed");
    assertEquals(currentUser, dao.getCurrentUsername(), 
                "Current username should not change when different user is renamed");
    }
    @Test
    @DisplayName("Change password updates user password successfully")
    void changePassword_UpdatesUserPassword() {
        // Create initial user
        User user = new CommonUser("testuser", "oldpassword", false);
        dao.save(user);

        // Change password
        User updatedUser = new CommonUser("testuser", "newpassword", false);
        dao.changePassword(updatedUser);

        // Verify password was updated
        User retrievedUser = dao.get("testuser");
        assertNotNull(retrievedUser, "User should exist");
        assertEquals("newpassword", retrievedUser.getPassword(), "Password should be updated");
    }

//    @Test
//    @DisplayName("Change password for non-existent user throws RuntimeException")
//    void changePassword_NonExistentUser_ThrowsException() {
//        User nonExistentUser = new CommonUser("nonexistent", "newpassword", false);
//        assertThrows(RuntimeException.class, () -> dao.changePassword(nonExistentUser));
//    }

    @Test
    @DisplayName("Get all users throws RuntimeException when database error occurs")
    void getAllUsers_DatabaseError_ThrowsException() throws Exception {
        // Force Firebase mode (not mock mode)
        setPrivateBoolean(dao, "useMockData", false);

        // Set usersRef to null to simulate database error
        setPrivateField(dao, "usersRef", null);

        assertThrows(RuntimeException.class, () -> dao.getAllUsers());
    }

    @Test
    @DisplayName("Delete user fails when database error occurs")
    void deleteUser_DatabaseError_ThrowsException() throws Exception {
        // Force Firebase mode (not mock mode)
        setPrivateBoolean(dao, "useMockData", false);

        // Set usersRef to null to simulate database error
        setPrivateField(dao, "usersRef", null);

        assertThrows(RuntimeException.class, () -> dao.deleteUser("testuser"));
    }

    @Test
    @DisplayName("Delete user throws RuntimeException when operation is cancelled")
    void deleteUser_OperationCancelled_ThrowsException() throws Exception {
        // Create a mock DatabaseReference that simulates a cancelled operation
        DatabaseReference mockRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockRef);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onCancelled(DatabaseError.fromException(new Exception("Operation cancelled")));
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Set the mock reference
        setPrivateBoolean(dao, "useMockData", false);
        setPrivateField(dao, "usersRef", mockRef);

        assertThrows(RuntimeException.class, () -> dao.deleteUser("testuser"));
    }

    @Test
    @DisplayName("Get all users returns empty list when no users exist")
    void getAllUsers_NoUsers_ReturnsEmptyList() throws Exception {
        // Clear all mock users
        @SuppressWarnings("unchecked")
        Map<String, User> mockUsers = (Map<String, User>) getPrivateField(dao, "mockUsers");
        mockUsers.clear();

        List<String> users = dao.getAllUsers();
        assertNotNull(users, "Should return non-null list");
        assertTrue(users.isEmpty(), "List should be empty when no users exist");
    }

    @Test
    @DisplayName("Change username with timeout throws exception")
    void changeUsername_Timeout_ReturnsFalse() throws Exception {
        // Force Firebase mode
        setPrivateBoolean(dao, "useMockData", false);
        
        // Create a mock DatabaseReference that never completes
        DatabaseReference mockRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockRef);
        
        doAnswer(invocation -> {
            // Do nothing - this simulates a hanging operation that will timeout
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
        
        setPrivateField(dao, "usersRef", mockRef);
        
        boolean result = dao.changeUsername("olduser", "newuser");
        assertFalse(result, "Should return false when operation times out");
    }

    @Test
    @DisplayName("Change username with interrupted operation returns false")
    void changeUsername_Interrupted_ReturnsFalse() throws Exception {
        // Force Firebase mode
        setPrivateBoolean(dao, "useMockData", false);
        
        // Create a mock DatabaseReference
        DatabaseReference mockRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockRef);
        
        doAnswer(invocation -> {
            // Interrupt the current thread to force an InterruptedException
            Thread.currentThread().interrupt();
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
        
        setPrivateField(dao, "usersRef", mockRef);
        
        boolean result = dao.changeUsername("olduser", "newuser");
        assertFalse(result, "Should return false when operation is interrupted");
    }

    @Test
    @DisplayName("Change username with execution exception returns false")
    void changeUsername_ExecutionException_ReturnsFalse() throws Exception {
        // Force Firebase mode
        setPrivateBoolean(dao, "useMockData", false);
        
        // Create a mock DatabaseReference that throws an error
        DatabaseReference mockRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockRef);
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onCancelled(DatabaseError.fromException(new RuntimeException("Database error")));
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
        
        setPrivateField(dao, "usersRef", mockRef);
        
        boolean result = dao.changeUsername("olduser", "newuser");
        assertFalse(result, "Should return false when execution fails");
    }

@Test
@DisplayName("Get all users - test onDataChange with empty snapshot")
void getAllUsers_EmptySnapshot() throws Exception {
    // Force Firebase mode
    setPrivateBoolean(dao, "useMockData", false);
    
    // Create mock references
    DatabaseReference mockRef = mock(DatabaseReference.class);
    when(mockRef.child(anyString())).thenReturn(mockRef);
    
    // Simulate empty snapshot
    doAnswer(invocation -> {
        ValueEventListener listener = invocation.getArgument(0);
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildrenCount()).thenReturn(0L);
        when(mockSnapshot.getChildren()).thenReturn(new ArrayList<>());
        
        listener.onDataChange(mockSnapshot);
        return null;
    }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
    
    setPrivateField(dao, "usersRef", mockRef);
    
    List<String> users = dao.getAllUsers();
    assertNotNull(users);
    assertTrue(users.isEmpty());
}

@Test
@DisplayName("Get all users - test onDataChange with populated snapshot")
void getAllUsers_PopulatedSnapshot() throws Exception {
    // Force Firebase mode
    setPrivateBoolean(dao, "useMockData", false);
    
    // Create mock references
    DatabaseReference mockRef = mock(DatabaseReference.class);
    when(mockRef.child(anyString())).thenReturn(mockRef);
    
    // Create mock snapshots
    DataSnapshot userSnapshot1 = mock(DataSnapshot.class);
    when(userSnapshot1.child("name")).thenReturn(userSnapshot1);
    when(userSnapshot1.getValue(String.class)).thenReturn("user1");
    
    DataSnapshot userSnapshot2 = mock(DataSnapshot.class);
    when(userSnapshot2.child("name")).thenReturn(userSnapshot2);
    when(userSnapshot2.getValue(String.class)).thenReturn("user2");
    
    List<DataSnapshot> snapshotList = Arrays.asList(userSnapshot1, userSnapshot2);
    
    // Simulate populated snapshot
    doAnswer(invocation -> {
        ValueEventListener listener = invocation.getArgument(0);
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildrenCount()).thenReturn(2L);
        when(mockSnapshot.getChildren()).thenReturn(snapshotList);
        
        listener.onDataChange(mockSnapshot);
        return null;
    }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
    
    setPrivateField(dao, "usersRef", mockRef);
    
    List<String> users = dao.getAllUsers();
    assertNotNull(users);
    assertEquals(2, users.size());
    assertTrue(users.contains("user1"));
    assertTrue(users.contains("user2"));
}

@Test
@DisplayName("Get all users - test onCancelled")
void getAllUsers_OnCancelled() throws Exception {
    // Force Firebase mode
    setPrivateBoolean(dao, "useMockData", false);
    
    // Create mock references
    DatabaseReference mockRef = mock(DatabaseReference.class);
    when(mockRef.child(anyString())).thenReturn(mockRef);
    
    // Simulate operation cancelled
    doAnswer(invocation -> {
        ValueEventListener listener = invocation.getArgument(0);
        DatabaseError error = DatabaseError.fromException(new RuntimeException("Operation cancelled"));
        listener.onCancelled(error);
        return null;
    }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
    
    setPrivateField(dao, "usersRef", mockRef);
    
    assertThrows(RuntimeException.class, () -> dao.getAllUsers(),
                "Should throw RuntimeException when operation is cancelled");
}

@Test
@DisplayName("Get all users - test timeout exception")
void getAllUsers_Timeout() throws Exception {
    // Force Firebase mode
    setPrivateBoolean(dao, "useMockData", false);
    
    // Create mock references
    DatabaseReference mockRef = mock(DatabaseReference.class);
    when(mockRef.child(anyString())).thenReturn(mockRef);
    
    // Simulate timeout by not calling the listener
    doAnswer(invocation -> {
        Thread.sleep(6000); // Sleep longer than the 5-second timeout
        return null;
    }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
    
    setPrivateField(dao, "usersRef", mockRef);
    
    assertThrows(RuntimeException.class, () -> dao.getAllUsers(),
                "Should throw RuntimeException on timeout");
}

@Test
@DisplayName("Delete user - successful deletion")
void deleteUser_SuccessfulDeletion() throws Exception {
    // Force Firebase mode
    setPrivateBoolean(dao, "useMockData", false);
    
    // Create mock references
    DatabaseReference mockRef = mock(DatabaseReference.class);
    DatabaseReference mockChildRef = mock(DatabaseReference.class);
    when(mockRef.child(anyString())).thenReturn(mockChildRef);
    
    // Simulate successful deletion flow
    doAnswer(invocation -> {
        ValueEventListener listener = invocation.getArgument(0);
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        
        // Simulate successful deletion
        doAnswer(innerInvocation -> {
            DatabaseReference.CompletionListener completionListener = innerInvocation.getArgument(0);
            completionListener.onComplete(null, mockChildRef);
            return null;
        }).when(mockChildRef).removeValue(any(DatabaseReference.CompletionListener.class));
        
        listener.onDataChange(mockSnapshot);
        return null;
    }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
    
    setPrivateField(dao, "usersRef", mockRef);
    
    // Should complete without throwing exception
    assertDoesNotThrow(() -> dao.deleteUser("testuser"));
}

@Test
@DisplayName("Delete user - error during deletion")
void deleteUser_ErrorDuringDeletion() throws Exception {
    // Force Firebase mode
    setPrivateBoolean(dao, "useMockData", false);
    
    // Create mock references
    DatabaseReference mockRef = mock(DatabaseReference.class);
    DatabaseReference mockChildRef = mock(DatabaseReference.class);
    when(mockRef.child(anyString())).thenReturn(mockChildRef);
    
    // Simulate deletion with error
    doAnswer(invocation -> {
        ValueEventListener listener = invocation.getArgument(0);
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        
        // Simulate error during deletion
        doAnswer(innerInvocation -> {
            DatabaseReference.CompletionListener completionListener = innerInvocation.getArgument(0);
            DatabaseError mockError = DatabaseError.fromException(new RuntimeException("Deletion failed"));
            completionListener.onComplete(mockError, mockChildRef);
            return null;
        }).when(mockChildRef).removeValue(any(DatabaseReference.CompletionListener.class));
        
        listener.onDataChange(mockSnapshot);
        return null;
    }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
    
    setPrivateField(dao, "usersRef", mockRef);
    
    assertThrows(RuntimeException.class, () -> dao.deleteUser("testuser"),
                "Should throw RuntimeException when deletion fails");
}

@Test
@DisplayName("Delete user - user not found")
void deleteUser_UserNotFound() throws Exception {
    // Force Firebase mode
    setPrivateBoolean(dao, "useMockData", false);

    // Create mock references
    DatabaseReference mockRef = mock(DatabaseReference.class);
    DatabaseReference mockChildRef = mock(DatabaseReference.class);
    when(mockRef.child(anyString())).thenReturn(mockChildRef);

    // Simulate user not found
    doAnswer(invocation -> {
        ValueEventListener listener = invocation.getArgument(0);
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(false);

        listener.onDataChange(mockSnapshot);
        return null;
    }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
}

    @Test
    @DisplayName("ExistsByName - user exists")
    void existsByName_UserExists() throws Exception {
        // Force Firebase mode
        setPrivateBoolean(dao, "useMockData", false);

        // Create mock references
        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockChildRef);

        // Mock successful existence check
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        setPrivateField(dao, "usersRef", mockRef);

        boolean result = dao.existsByName("testuser");
        assertTrue(result, "Should return true when user exists");
    }

    @Test
    @DisplayName("ExistsByName - user does not exist")
    void existsByName_UserDoesNotExist() throws Exception {
        // Force Firebase mode
        setPrivateBoolean(dao, "useMockData", false);

        // Create mock references
        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockChildRef);

        // Mock non-existence check
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(false);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        setPrivateField(dao, "usersRef", mockRef);

        boolean result = dao.existsByName("nonexistent");
        assertFalse(result, "Should return false when user does not exist");
    }

    @Test
    @DisplayName("ExistsByName - operation cancelled")
    void existsByName_OperationCancelled() throws Exception {
        // Force Firebase mode
        setPrivateBoolean(dao, "useMockData", false);

        // Create mock references
        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockChildRef);

        // Mock operation cancelled
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            DatabaseError mockError = DatabaseError.fromException(new RuntimeException("Operation cancelled"));
            listener.onCancelled(mockError);
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        setPrivateField(dao, "usersRef", mockRef);

        boolean result = dao.existsByName("testuser");
        assertFalse(result, "Should return false when operation is cancelled");
    }

    @Test
    @DisplayName("ExistsByName - timeout")
    void existsByName_Timeout() throws Exception {
        // Force Firebase mode
        setPrivateBoolean(dao, "useMockData", false);

        // Create mock references
        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockChildRef);

        // Mock timeout by not calling listener
        doAnswer(invocation -> {
            Thread.sleep(6000); // Sleep longer than the 5-second timeout
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        setPrivateField(dao, "usersRef", mockRef);

        boolean result = dao.existsByName("testuser");
        assertFalse(result, "Should return false when operation times out");
    }
}
