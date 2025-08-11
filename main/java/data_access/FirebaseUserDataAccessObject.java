package data_access;

import com.google.firebase.database.*;
import entity.User;
import entity.CommonUser;
import use_case.login.LoginUserDataAccessInterface;
import use_case.signup.SignupUserDataAccessInterface;
import use_case.change_password.ChangePasswordUserDataAccessInterface;
import use_case.logout.LogoutUserDataAccessInterface;
import use_case.change_username.ChangeUsernameUserDataAccessInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.HashMap;
import java.util.Map;


/**
 * Firebase implementation of data access for user authentication.
 */
public class FirebaseUserDataAccessObject implements
        LoginUserDataAccessInterface,
        SignupUserDataAccessInterface,
        ChangePasswordUserDataAccessInterface,
        LogoutUserDataAccessInterface,
        ChangeUsernameUserDataAccessInterface {

    private DatabaseReference usersRef;
    private boolean useMockData;
    private final Map<String, User> mockUsers = new HashMap<>();
    private final Map<String, User> accounts = new HashMap<>();
    private String currentUsername = null;
    public FirebaseUserDataAccessObject() {
        System.out.println("DEBUG: FirebaseUserDataAccessObject constructor called");
        // Try to initialize Firebase
        try {
            FirebaseConfig.initializeFirebase();
            this.usersRef = FirebaseConfig.getDatabase().getReference("users");
            this.useMockData = false;
            System.out.println("DEBUG: ✅ Using Firebase for user authentication");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
        } catch (Exception e) {
            System.err.println("DEBUG: ❌ Firebase not available for users, using mock data: " + e.getMessage());
            this.usersRef = null;
            this.useMockData = true;
            // Add some mock users for testing
            mockUsers.put("testuser", new CommonUser("testuser", "password123", false));
            mockUsers.put("admin", new CommonUser("admin", "admin123", true));
            System.out.println("DEBUG: Mock users created: " + mockUsers.keySet());
        }
    }
//    public FirebaseUserDataAccessObject(FirebaseDatabase firebaseDatabase) {
//        this.firebaseDatabase = firebaseDatabase;
//    }

    @Override
    public boolean existsByName(String identifier) {
        System.out.println("\n=== DEBUG: FirebaseUserDataAccessObject.existsByName() called ===");
        System.out.println("DEBUG: Checking if user exists: '" + identifier + "'");
        System.out.println("DEBUG: Using mock data: " + useMockData);

        if (useMockData) {
            // Mock data for testing
            boolean exists = mockUsers.containsKey(identifier);
            System.out.println("DEBUG: Mock user exists: " + exists);
            return exists;
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        usersRef.child(identifier).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();
                System.out.println("DEBUG: Firebase user exists: " + exists);
                future.complete(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: Firebase error checking user existence: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to check user existence: " + databaseError.getMessage()));
            }
        });

        try {
            // Add timeout to prevent blocking indefinitely
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("DEBUG: Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void save(User user) {
        System.out.println("\n=== DEBUG: FirebaseUserDataAccessObject.save() called ===");
        System.out.println("DEBUG: Saving user: '" + user.getName() + "'");
        System.out.println("DEBUG: Using mock data: " + useMockData);

        if (useMockData) {
            mockUsers.put(user.getName(), user);
            System.out.println("DEBUG: Mock user saved: " + user.getName());
            return;
        }

        usersRef.child(user.getName()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.err.println("DEBUG: Firebase error saving user: " + databaseError.getMessage());
                } else {
                    System.out.println("DEBUG: Firebase user saved successfully: " + user.getName());
                }
            }
        });
    }

    @Override
    public User get(String username) {
        System.out.println("\n=== DEBUG: FirebaseUserDataAccessObject.get() called ===");
        System.out.println("DEBUG: Getting user: '" + username + "'");
        System.out.println("DEBUG: Using mock data: " + useMockData);

        if (useMockData) {
            User user = mockUsers.get(username);
            System.out.println("DEBUG: Mock user retrieved: " + (user != null ? user.getName() : "null"));
            return user;
        }

        CompletableFuture<User> future = new CompletableFuture<>();

        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CommonUser user = dataSnapshot.getValue(CommonUser.class);
                System.out.println("DEBUG: Firebase user retrieved: " + (user != null ? user.getName() : "null"));
                future.complete(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: Firebase error getting user: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to get user: " + databaseError.getMessage()));
            }
        });

        try {
            // Add timeout to prevent blocking indefinitely
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("DEBUG: Error getting user: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getCurrentUsername() {
        System.out.println("DEBUG: getCurrentUsername() called - returning: " + currentUsername);
        // For Firebase implementation, we'll store current user in memory
        // In a production app, you might use Firebase Auth or session management
        return currentUsername;
    }

    @Override
    public void setCurrentUsername(String username) {
        System.out.println("DEBUG: setCurrentUsername() called with: '" + username + "'");
        this.currentUsername = username;
    }

//    private String currentUsername = null;

    @Override
    public void changePassword(User user) {
        System.out.println("\n=== DEBUG: FirebaseUserDataAccessObject.changePassword() called ===");
        System.out.println("DEBUG: Changing password for user: '" + user.getName() + "'");
        System.out.println("DEBUG: Using mock data: " + useMockData);

        if (useMockData) {
            mockUsers.put(user.getName(), user);
            System.out.println("DEBUG: Mock password changed for user: " + user.getName());
            return;
        }

        // Update the user's password in Firebase
        usersRef.child(user.getName()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.err.println("DEBUG: Firebase error changing password: " + databaseError.getMessage());
                } else {
                    System.out.println("DEBUG: Firebase password changed successfully for user: " + user.getName());
                }
            }
        });
    }

    @Override
    public boolean changeUsername(String oldUsername, String newUsername) {
        if (useMockData) {
            if (!mockUsers.containsKey(oldUsername) || mockUsers.containsKey(newUsername)) {
                return false;
            }
            User user = mockUsers.remove(oldUsername);
            ((entity.CommonUser)user).setName(newUsername);
            mockUsers.put(newUsername, user);
            if (currentUsername != null && currentUsername.equals(oldUsername)) {
                currentUsername = newUsername;
            }
            return true;
        }
        try {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            usersRef.child(oldUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        future.complete(false);
                        return;
                    }
                    usersRef.child(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot newUserSnapshot) {
                            if (newUserSnapshot.exists()) {
                                future.complete(false);
                                return;
                            }
                            CommonUser user = dataSnapshot.getValue(CommonUser.class);
                            user.setName(newUsername);
                            usersRef.child(newUsername).setValue(user, (err, ref) -> {
                                if (err != null) {
                                    future.complete(false);
                                } else {
                                    usersRef.child(oldUsername).removeValue((err2, ref2) -> {
                                        if (err2 != null) {
                                            future.complete(false);
                                        } else {
                                            if (currentUsername != null && currentUsername.equals(oldUsername)) {
                                                currentUsername = newUsername;
                                            }
                                            future.complete(true);
                                        }
                                    });
                                }
                            });
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            future.complete(false);
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    future.complete(false);
                }
            });
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }
    
    public List<String> getAllUsers() {
        System.out.println("DEBUG: Starting to fetch users");
        
        if (useMockData) {
            List<String> users = mockUsers.values().stream()
                .map(User::getName)
                .collect(java.util.stream.Collectors.toList());
            System.out.println("DEBUG: Retrieved users from mock storage: " + users);
            return users;
        }

        // Original Firebase implementation
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> users = new ArrayList<>();
                System.out.println("DEBUG: Firebase snapshot exists: " + dataSnapshot.exists());
                System.out.println("DEBUG: Firebase snapshot children count: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String username = userSnapshot.child("name").getValue(String.class);
                    System.out.println("DEBUG: Processing user node: " + userSnapshot.getKey() +
                            ", username field: " + username);
                    if (username != null) {
                        users.add(username);
                    }
                }
                future.complete(users);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("DEBUG: Firebase error: " + error.getMessage());
                future.completeExceptionally(new RuntimeException("Failed to load users: " + error.getMessage()));
            }
        });

        try {
            List<String> result = future.get(5, TimeUnit.SECONDS);
            System.out.println("DEBUG: Successfully retrieved users from Firebase: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("DEBUG: Error fetching users: " + e.getMessage());
            throw new RuntimeException("Error fetching users: " + e.getMessage());
        }
    }


    public void deleteUser(String username) {
        System.out.println("DEBUG: Attempting to delete user: " + username);

        if (useMockData) {
            if (!mockUsers.containsKey(username)) {
                System.out.println("DEBUG: User not found in mock storage: " + username);
                throw new RuntimeException("User not found");
            }
            mockUsers.remove(username);
            System.out.println("DEBUG: User deleted from mock storage: " + username);
            return;
        }

        // Original Firebase implementation
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference userRef = usersRef.child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    System.out.println("DEBUG: User node not found for: " + username);
                    future.completeExceptionally(new RuntimeException("User not found"));
                    return;
                }

                // Delete the user node
                userRef.removeValue((error, ref) -> {
                    if (error != null) {
                        System.err.println("DEBUG: Error deleting user: " + error.getMessage());
                        future.completeExceptionally(new RuntimeException("Failed to delete user: " + error.getMessage()));
                    } else {
                        System.out.println("DEBUG: Successfully deleted user: " + username);
                        future.complete(null);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("DEBUG: Delete operation cancelled: " + error.getMessage());
                future.completeExceptionally(new RuntimeException("Operation cancelled: " + error.getMessage()));
            }
        });

        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("DEBUG: Exception while deleting user: " + e.getMessage());
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }
}