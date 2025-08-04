package data_access;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Firebase configuration and initialization.
 */
public class FirebaseConfig {
    private static FirebaseDatabase database;
    private static boolean initialized = false;
    
    public static void initializeFirebase() {
        if (initialized) {
            return; // Already initialized
        }
        
        try {
            // Load properties
            Properties properties = new Properties();
            try (InputStream input = FirebaseConfig.class.getClassLoader()
                    .getResourceAsStream("firebase.properties")) {
                if (input != null) {
                    properties.load(input);
                }
            }
            
            String projectId = properties.getProperty("firebase.project.id", "csc207-cfda3");
            String databaseUrl = properties.getProperty("firebase.database.url", 
                "https://csc207-cfda3-default-rtdb.firebaseio.com");
            String serviceAccountPath = properties.getProperty("firebase.service.account.path", 
                "main/resources/csc207-cfda3-firebase-adminsdk-fbsvc-5f9167c0b2.json");
            
            // Load service account key file
            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .build();
            
            // Only initialize if not already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            database = FirebaseDatabase.getInstance();
            initialized = true;
            
            System.out.println("Firebase initialized successfully for project: " + projectId);
        } catch (IOException e) {
            System.err.println("Error loading Firebase service account: " + e.getMessage());
            System.err.println("Using mock data instead...");
        } catch (Exception e) {
            System.err.println("Error initializing Firebase: " + e.getMessage());
            System.err.println("Using mock data instead...");
        }
    }
    
    public static FirebaseDatabase getDatabase() {
        if (!initialized) {
            initializeFirebase();
        }
        return database;
    }
    
    public static boolean isInitialized() {
        return initialized && database != null;
    }
} 