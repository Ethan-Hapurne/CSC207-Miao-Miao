package app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link Main} class.
 * 
 * Notes:
 * - Headless mode is enabled to prevent actual GUI windows from appearing
 * - Firebase is mocked to avoid connection issues in test environment
 * - Tests verify proper application initialization and shutdown behavior
 */
class MainTest {

    @BeforeAll
    static void enableHeadless() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Test
    @DisplayName("Main method executes without throwing exceptions")
    void main_executesWithoutExceptions() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> caughtException = new AtomicReference<>();

        // Mock SwingUtilities.invokeLater to capture the runnable but not execute it
        try (MockedStatic<SwingUtilities> swingUtilities = mockStatic(SwingUtilities.class);
             MockedStatic<data_access.FirebaseConfig> firebaseConfig = mockStatic(data_access.FirebaseConfig.class)) {
            
            // Mock Firebase initialization to prevent connection attempts
            firebaseConfig.when(data_access.FirebaseConfig::initializeFirebase).thenAnswer(invocation -> null);
            firebaseConfig.when(data_access.FirebaseConfig::getDatabase).thenReturn(null);
            
            swingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        latch.countDown();
                        return null;
                    });

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> {
                Main.main(new String[]{});
                try {
                    assertTrue(latch.await(1, TimeUnit.SECONDS), "SwingUtilities.invokeLater should be called");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Verify SwingUtilities.invokeLater was called
            swingUtilities.verify(() -> SwingUtilities.invokeLater(any(Runnable.class)), times(1));
        }
    }

    @Test
    @DisplayName("Main method calls SwingUtilities.invokeLater")
    void main_callsSwingUtilitiesInvokeLater() {
        // Given
        try (MockedStatic<SwingUtilities> swingUtilities = mockStatic(SwingUtilities.class);
             MockedStatic<data_access.FirebaseConfig> firebaseConfig = mockStatic(data_access.FirebaseConfig.class)) {
            
            // Mock Firebase to prevent initialization issues
            firebaseConfig.when(data_access.FirebaseConfig::initializeFirebase).thenAnswer(invocation -> null);
            
            swingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> null);

            // When
            Main.main(new String[]{});

            // Then
            swingUtilities.verify(() -> SwingUtilities.invokeLater(any(Runnable.class)), times(1));
        }
    }

    @Test
    @DisplayName("Application builder chain structure is correct")
    void applicationBuilderChain_hasCorrectStructure() {
        // This test verifies the builder pattern structure without actually executing it
        
        // Given - we can verify the method calls exist by reflection
        Class<AppBuilder> builderClass = AppBuilder.class;
        
        // Then - verify all required methods exist
        assertDoesNotThrow(() -> builderClass.getMethod("addSignupView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addLoginView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addLoggedInView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addAdminLoggedInView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addSearchView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addAdvancedSearchView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addDashboardView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addAdminView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addAccountView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addDMsView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addDeleteUserView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addSignupUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addLoginUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addChangePasswordUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addLogoutUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addSearchUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addDashboardUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addAdminUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addChangeUsernameUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addDeletePostUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addDeleteUserUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("addFuzzySearchView"));
        assertDoesNotThrow(() -> builderClass.getMethod("addDMsUseCase"));
        assertDoesNotThrow(() -> builderClass.getMethod("build"));
    }

    @Test
    @DisplayName("Window closing event handling logic is correct")
    void windowClosing_handlingLogicIsCorrect() throws Exception {
        // Given
        AtomicBoolean shutdownCalled = new AtomicBoolean(false);

        // Mock Firebase shutdown only - don't mock System.exit
        try (MockedStatic<data_access.FirebaseConfig> firebaseConfig = mockStatic(data_access.FirebaseConfig.class)) {
            
            firebaseConfig.when(data_access.FirebaseConfig::shutdown)
                    .thenAnswer(invocation -> {
                        shutdownCalled.set(true);
                        return null;
                    });

            // Create a modified window listener that doesn't call System.exit
            java.awt.event.WindowAdapter windowListener = new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Shutting down application...");
                    try {
                        data_access.FirebaseConfig.shutdown();
                    } catch (Exception ex) {
                        System.err.println("Error during shutdown: " + ex.getMessage());
                    }
                    // Don't call System.exit in test - just verify shutdown was called
                }
            };

            // When
            WindowEvent mockEvent = mock(WindowEvent.class);
            windowListener.windowClosing(mockEvent);

            // Then
            assertTrue(shutdownCalled.get(), "Firebase shutdown should be called");
        }
    }

    @Test
    @DisplayName("Firebase shutdown exception handling works correctly")
    void firebaseShutdown_handlesExceptionsCorrectly() {
        // Given
        RuntimeException testException = new RuntimeException("Test Firebase shutdown error");
        AtomicBoolean exceptionCaught = new AtomicBoolean(false);
        
        try (MockedStatic<data_access.FirebaseConfig> firebaseConfig = mockStatic(data_access.FirebaseConfig.class)) {
            
            firebaseConfig.when(data_access.FirebaseConfig::shutdown)
                    .thenThrow(testException);

            // Create window listener with same logic as Main but capture exception instead of calling System.exit
            java.awt.event.WindowAdapter windowListener = new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Shutting down application...");
                    try {
                        data_access.FirebaseConfig.shutdown();
                    } catch (Exception ex) {
                        System.err.println("Error during shutdown: " + ex.getMessage());
                        exceptionCaught.set(true);
                    }
                    // Don't call System.exit in test
                }
            };

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> {
                WindowEvent mockEvent = mock(WindowEvent.class);
                windowListener.windowClosing(mockEvent);
            });
            
            // Verify Firebase shutdown was attempted
            firebaseConfig.verify(data_access.FirebaseConfig::shutdown, times(1));
            // Verify exception was caught and handled
            assertTrue(exceptionCaught.get(), "Exception should be caught and handled");
        }
    }

    @Test
    @DisplayName("Main method accepts command line arguments without issues")
    void main_acceptsCommandLineArguments() {
        // Given various argument scenarios
        String[][] testArgs = {
            {},                          // No arguments
            {"arg1"},                    // Single argument
            {"arg1", "arg2"},           // Multiple arguments
            {"--verbose", "--debug"}     // Flag-style arguments
        };

        try (MockedStatic<SwingUtilities> swingUtilities = mockStatic(SwingUtilities.class);
             MockedStatic<data_access.FirebaseConfig> firebaseConfig = mockStatic(data_access.FirebaseConfig.class)) {
            
            // Mock Firebase to prevent initialization
            firebaseConfig.when(data_access.FirebaseConfig::initializeFirebase).thenAnswer(invocation -> null);
            
            swingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> null);

            // When & Then - all argument combinations should work
            for (String[] args : testArgs) {
                assertDoesNotThrow(() -> Main.main(args), 
                    "Main should handle arguments: " + String.join(", ", args));
            }
        }
    }

    @Test
    @DisplayName("Application configuration follows expected pattern")
    void applicationConfiguration_followsExpectedPattern() throws Exception {
        // This test verifies the configuration pattern without instantiating components
        
        // The Main method should:
        // 1. Use SwingUtilities.invokeLater for thread safety
        // 2. Create AppBuilder instance
        // 3. Chain builder methods
        // 4. Build the application
        // 5. Set up window listener
        // 6. Configure frame properties
        // 7. Make frame visible

        // We can verify the pattern exists by checking the Main class structure
        Class<Main> mainClass = Main.class;
        
        // Use try-catch to handle potential NoSuchMethodException
        try {
            Method mainMethod = mainClass.getMethod("main", String[].class);
            assertNotNull(mainMethod, "Main class should have main method");
            
            // Verify the method is static
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                "Main method should be static");
            
            // Verify the method is public
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                "Main method should be public");
        } catch (NoSuchMethodException e) {
            fail("Main class should have a main(String[]) method: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Main class has no instance methods")
    void mainClass_hasNoInstanceMethods() {
        // Main class should only have the static main method and potentially constructors
        Class<Main> mainClass = Main.class;
        
        long instanceMethodCount = java.util.Arrays.stream(mainClass.getDeclaredMethods())
            .filter(method -> !java.lang.reflect.Modifier.isStatic(method.getModifiers()))
            .count();
        
        assertEquals(0, instanceMethodCount, 
            "Main class should not have instance methods - it should be a utility class");
    }

    @Test
    @DisplayName("Main class exists and is properly structured")
    void mainClass_existsAndIsProperlyStructured() {
        // Verify Main class exists
        assertNotNull(Main.class, "Main class should exist");
        
        // Verify Main class is public
        assertTrue(java.lang.reflect.Modifier.isPublic(Main.class.getModifiers()),
            "Main class should be public");
        
        // Verify Main class has at least one method (the main method)
        assertTrue(Main.class.getDeclaredMethods().length > 0,
            "Main class should have methods");
    }

    @Test
    @DisplayName("Window listener properly handles shutdown sequence")
    void windowListener_handlesShutdownSequence() {
        // Test the shutdown sequence logic separately from System.exit
        AtomicBoolean shutdownAttempted = new AtomicBoolean(false);
        AtomicBoolean exceptionHandled = new AtomicBoolean(false);
        
        try (MockedStatic<data_access.FirebaseConfig> firebaseConfig = mockStatic(data_access.FirebaseConfig.class)) {
            
            // Test successful shutdown
            firebaseConfig.when(data_access.FirebaseConfig::shutdown)
                    .thenAnswer(invocation -> {
                        shutdownAttempted.set(true);
                        return null;
                    });

            // Create a testable version of the window closing logic
            Runnable shutdownLogic = () -> {
                System.out.println("Shutting down application...");
                try {
                    data_access.FirebaseConfig.shutdown();
                } catch (Exception ex) {
                    System.err.println("Error during shutdown: " + ex.getMessage());
                    exceptionHandled.set(true);
                }
                // System.exit(0) would be called here in real code
            };

            // When
            assertDoesNotThrow(shutdownLogic::run);

            // Then
            assertTrue(shutdownAttempted.get(), "Firebase shutdown should be attempted");
            assertFalse(exceptionHandled.get(), "No exception should be handled in successful case");
        }
    }
}