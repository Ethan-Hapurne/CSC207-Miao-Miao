package data_access;

import com.google.firebase.database.*;
import entity.Post;
import entity.Comment;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FirebasePostDataAccessObjectTest {

    private FirebasePostDataAccessObject dao;
    
    @Mock
    private DatabaseReference mockPostsRef;
    @Mock
    private FirebaseDatabase mockDatabase;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDatabase.getReference(anyString())).thenReturn(mockPostsRef);
        dao = new FirebasePostDataAccessObject();
        setPrivateField(dao, "postsRef", mockPostsRef);
        setPrivateField(dao, "database", mockDatabase);
    }

    @Test
    @DisplayName("getAllPosts - Database Error")
    void getAllPosts_DatabaseError() {
        // Arrange
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onCancelled(DatabaseError.fromException(new Exception("Database error")));
            return null;
        }).when(mockPostsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dao.getAllPosts());
    }

    @Test
    @DisplayName("getAllPosts - Success scenario")
    void getAllPosts_Success() {
        // Arrange
        DatabaseReference mockOrderByRef = mock(DatabaseReference.class);
        Query mockQuery = mock(Query.class);
        when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockQuery);  // Using string literal instead of constant
        
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        List<Post> expectedPosts = new ArrayList<>();
        Post post1 = new Post(
            1,                    // postId
            "Title1",            // title
            "Description1",      // description/content
            new ArrayList<>(),   // tags
            LocalDateTime.now(), // timestamp
            "Author1",          // author
            "Location1",        // location
            null,               // imageURL
            true,               // isLost
            0,                  // numberOfLikes
            new HashMap<>()     // reactions
        );

        expectedPosts.add(post1);
        
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildren()).thenReturn(Arrays.asList(mockSnapshot));
        when(mockSnapshot.getValue(Post.class)).thenReturn(expectedPosts.get(0));
        
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));
        
        // Act
        List<Post> actualPosts = dao.getAllPosts();
        
        // Assert
        assertFalse(actualPosts.isEmpty());
        assertEquals(expectedPosts.get(0).getTitle(), actualPosts.get(0).getTitle());
    }

    @Test
    @DisplayName("deletePost - Success")
    void deletePost_Success() {
        // Arrange
        String postId = "123";
        DatabaseReference mockOrderByRef = mock(DatabaseReference.class);
        Query mockQuery = mock(Query.class);
        Query mockEqualToQuery = mock(Query.class);

        // Mock the query chain
        when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
        when(mockQuery.equalTo(anyDouble())).thenReturn(mockEqualToQuery);

        // Mock data snapshot
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildrenCount()).thenReturn(1L);
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
        when(mockChildSnapshot.getKey()).thenReturn("mockKey");

        // Mock database references
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockPostsRef.child(anyString())).thenReturn(mockChildRef);

        // Setup listener behavior
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Mock removal behavior
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(0);
            listener.onComplete(null, mockChildRef);
            return null;
        }).when(mockChildRef).removeValue(any(DatabaseReference.CompletionListener.class));

        // Act & Assert
        assertDoesNotThrow(() -> dao.deletePost(postId));
    }

    @Test
    @DisplayName("addPost - Success")
    void addPost_Success() {
        // Arrange
        String title = "Test Title";
        String content = "Test Content";
        List<String> tags = Arrays.asList("tag1", "tag2");
        String location = "Test Location";
        boolean isLost = true;
        String author = "Test Author";

        // Mock for getAllPosts
        DatabaseReference mockOrderByRef = mock(DatabaseReference.class);
        when(mockPostsRef.orderByChild(anyString())).thenReturn(mockOrderByRef);

        // Mock the push operation
        DatabaseReference mockNewPostRef = mock(DatabaseReference.class);
        when(mockPostsRef.push()).thenReturn(mockNewPostRef);
        when(mockNewPostRef.getKey()).thenReturn("newPostId");

        // Mock the data snapshot for getAllPosts
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildren()).thenReturn(Collections.emptyList());

        // Setup listener behavior
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockOrderByRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Mock child and setValue behavior
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockPostsRef.child(anyString())).thenReturn(mockChildRef);
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);
            listener.onComplete(null, mockChildRef);
            return mockChildRef;
        }).when(mockChildRef).setValue(any(), any(DatabaseReference.CompletionListener.class));

        // Act
        Post result = dao.addPost(title, content, tags, location, isLost, author);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(author, result.getAuthor());
        assertEquals(content, result.getDescription());
        assertEquals(location, result.getLocation());
        assertEquals(isLost, result.isLost());
        assertEquals(tags, result.getTags());
    }

    @Test
    @DisplayName("searchPostsByCriteria - Success")
    void searchPostsByCriteria_Success() {
        // Arrange
        String title = "Test";
        String location = "Location";
        List<String> tags = Arrays.asList("tag1");
        Boolean isLost = true;

        // Mock for getAllPosts
        DatabaseReference mockOrderByRef = mock(DatabaseReference.class);
        Query mockQuery = mock(Query.class);
        when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockQuery);

        // Setup mock behavior for the query
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        Post mockPost = new Post();
        mockPost.setTitle(title);
        mockPost.setLocation(location);
        mockPost.setTags(tags);
        mockPost.setLost(isLost);

        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildren()).thenReturn(Arrays.asList(mockSnapshot));
        when(mockSnapshot.getValue(Post.class)).thenReturn(mockPost);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act
        List<Post> results = dao.searchPostsByCriteria(title, location, tags, isLost);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(mockPost.getTitle(), results.get(0).getTitle());
    }

    @Test
    @DisplayName("deletePost - Post Not Found")
    void deletePost_PostNotFound() {
        // Arrange
        String postId = "123";
        Query mockQuery = mock(Query.class);
        DatabaseReference mockOrderByRef = mock(DatabaseReference.class);
        when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
        when(mockQuery.equalTo(anyDouble())).thenReturn(mockQuery);

        // Mock empty snapshot
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(false);
        when(mockSnapshot.getChildrenCount()).thenReturn(0L);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dao.deletePost(postId));
    }

    @Test
    @DisplayName("deletePost - Invalid ID Format")
    void deletePost_InvalidIdFormat() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dao.deletePost(invalidId));
    }



    @Test
    @DisplayName("deletePost - Database Error")
    void deletePost_DatabaseError() {
        // Arrange
        String postId = "123";
        Query mockQuery = mock(Query.class);
        when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
        when(mockQuery.equalTo(123.0)).thenReturn(mockQuery);

        // Create mock error
        DatabaseError mockError = mock(DatabaseError.class);
        when(mockError.getMessage()).thenReturn("Database error");
        when(mockError.getCode()).thenReturn(-1);
        when(mockError.getDetails()).thenReturn("Error details");
        when(mockError.toException()).thenReturn(new DatabaseException("Database error"));

        // Set up a CountDownLatch to handle async operation
        CountDownLatch latch = new CountDownLatch(1);

        // Setup listener behavior to trigger error immediately
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onCancelled(mockError);
            latch.countDown();
            return null;
        }).when(mockQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            try {
                dao.deletePost(postId);
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new TimeoutException("Operation timed out");
                }
            } catch (InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("deletePost - Delete Operation Error")
    void deletePost_DeleteError() {
        // Arrange
        String postId = "123";
        Query mockQuery = mock(Query.class);
        when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
        when(mockQuery.equalTo(anyDouble())).thenReturn(mockQuery);

        // Mock snapshot with data
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildrenCount()).thenReturn(1L);
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
        when(mockChildSnapshot.getKey()).thenReturn("mockKey");

        // Mock database references
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockPostsRef.child(anyString())).thenReturn(mockChildRef);

        // Setup listener behavior
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Mock removal error
        doAnswer(invocation -> {
            DatabaseReference.CompletionListener listener = invocation.getArgument(0);
            listener.onComplete(DatabaseError.fromException(new Exception("Delete error")), mockChildRef);
            return null;
        }).when(mockChildRef).removeValue(any(DatabaseReference.CompletionListener.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dao.deletePost(postId));
    }

    @Test
    @DisplayName("deletePost - Timeout")
    void deletePost_Timeout() {
        // Arrange
        String postId = "123";
        Query mockQuery = mock(Query.class);
        when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
        when(mockQuery.equalTo(anyDouble())).thenReturn(mockQuery);

        // Setup listener that never calls back
        doAnswer(invocation -> {
            Thread.sleep(6000); // Longer than timeout
            return null;
        }).when(mockQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dao.deletePost(postId));
    }

    // Helper method to set private fields
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void searchPosts_MatchesInTitle() {
        // Arrange
        Query mockTimeQuery = mock(Query.class);
        when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockTimeQuery);

        // Setup mock posts data
        Post post1 = new Post();
        post1.setTitle("Lost wallet");
        post1.setDescription("Found at library");

        Post post2 = new Post();
        post2.setTitle("Found keys");
        post2.setDescription("Has a wallet keychain");

        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        List<DataSnapshot> snapshots = Arrays.asList(
                createMockPostSnapshot(post1),
                createMockPostSnapshot(post2)
        );

        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildren()).thenReturn(snapshots);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockTimeQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act
        List<Post> results = dao.searchPosts("wallet");

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getTitle().equals("Lost wallet")));
    }

    @Test
    void getPostById_ValidNumericId_Success() {
        // Arrange
        String postId = "123";
        Post expectedPost = new Post();
        expectedPost.setPostID(123);

        // Create mock snapshot for findPostByHashCode method
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
        List<DataSnapshot> childrenSnapshots = Collections.singletonList(mockChildSnapshot);

        // Set up mock behavior
        when(mockSnapshot.getChildren()).thenReturn(childrenSnapshots);
        when(mockChildSnapshot.getValue(Post.class)).thenReturn(expectedPost);

        // Setup the listener behavior directly on postsRef
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockPostsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act
        Post result = dao.getPostById(postId);

        // Assert
        assertNotNull(result);
        assertEquals(123, result.getPostID());
    }
    @Test
    void getPostById_NonNumericId_DatabaseError() {
        // Arrange
        String postId = "abc123";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockPostsRef.child(postId)).thenReturn(mockChildRef);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onCancelled(DatabaseError.fromException(new DatabaseException("Test error")));
            return null;
        }).when(mockChildRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act
        Post result = dao.getPostById(postId);

        // Assert
        assertNull(result);
    }

    @Test
    void getPostById_NumericId_Success() {
        // Arrange
        String postId = "456";
        Post expectedPost = new Post();
        expectedPost.setPostID(456);

        // Mock for postsRef direct query (used in findPostByHashCode)
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);

        // Mock exists() and getChildrenCount() for the initial snapshot
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getChildrenCount()).thenReturn(1L);

        // Set up the children for iteration
        List<DataSnapshot> children = Collections.singletonList(mockChildSnapshot);
        when(mockSnapshot.getChildren()).thenReturn(children);

        // Set up the mock child snapshot
        when(mockChildSnapshot.getValue(Post.class)).thenReturn(expectedPost);

        // Set up the listener behavior on postsRef
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockPostsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Act
        Post result = dao.getPostById(postId);

        // Assert
        assertNotNull(result);
        assertEquals(456, result.getPostID());

        // Verify the correct method was called
        verify(mockPostsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
    }

    // Helper method to create mock DataSnapshot for a Post
    private DataSnapshot createMockPostSnapshot(Post post) {
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.getValue(Post.class)).thenReturn(post);
        return mockSnapshot;
    }


    @Nested
    @DisplayName("deletePost(int postId) tests")
    class DeletePostIntTests {

        @Test
        void deletePost_Int_Success() {
            // Arrange
            int postId = 123;

            // Mock for getAllPosts query chain
            Query mockTimeQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockTimeQuery);

            // Setup getAllPosts behavior
            DataSnapshot mockTimeSnapshot = mock(DataSnapshot.class);
            Post targetPost = new Post();
            targetPost.setPostID(postId);
            when(mockTimeSnapshot.exists()).thenReturn(true);
            when(mockTimeSnapshot.getChildren()).thenReturn(Collections.singletonList(mockTimeSnapshot));
            when(mockTimeSnapshot.getValue(Post.class)).thenReturn(targetPost);

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockTimeSnapshot);
                return null;
            }).when(mockTimeQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock for delete operation query chain
            Query mockPostIdQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockPostIdQuery);
            when(mockPostIdQuery.equalTo(postId)).thenReturn(mockEqualToQuery);

            // Mock delete operation snapshot
            DataSnapshot mockDeleteSnapshot = mock(DataSnapshot.class);
            DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
            when(mockDeleteSnapshot.exists()).thenReturn(true);
            when(mockDeleteSnapshot.getChildrenCount()).thenReturn(1L);
            when(mockDeleteSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
            when(mockChildSnapshot.getKey()).thenReturn("mockKey");

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockDeleteSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock the actual delete operation
            DatabaseReference mockChildRef = mock(DatabaseReference.class);
            when(mockPostsRef.child(anyString())).thenReturn(mockChildRef);
            doAnswer(invocation -> {
                DatabaseReference.CompletionListener listener = invocation.getArgument(0);
                listener.onComplete(null, mockChildRef);
                return null;
            }).when(mockChildRef).removeValue(any(DatabaseReference.CompletionListener.class));

            // Act & Assert
            assertTrue(dao.deletePost(postId));
        }

        @Test
        void deletePost_Int_PostNotFound() {
            // Arrange
            int postId = 999;

            // Mock for getAllPosts query chain
            Query mockTimeQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockTimeQuery);

            DataSnapshot mockTimeSnapshot = mock(DataSnapshot.class);
            when(mockTimeSnapshot.exists()).thenReturn(false);
            when(mockTimeSnapshot.getChildren()).thenReturn(Collections.emptyList());

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockTimeSnapshot);
                return null;
            }).when(mockTimeQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock for delete operation query chain
            Query mockPostIdQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockPostIdQuery);
            when(mockPostIdQuery.equalTo(postId)).thenReturn(mockEqualToQuery);

            // Mock the snapshot for delete operation showing no results
            DataSnapshot mockDeleteSnapshot = mock(DataSnapshot.class);
            when(mockDeleteSnapshot.exists()).thenReturn(false);
            when(mockDeleteSnapshot.getChildrenCount()).thenReturn(0L);
            when(mockDeleteSnapshot.getChildren()).thenReturn(Collections.emptyList());

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockDeleteSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Act & Assert
            assertFalse(dao.deletePost(postId));
        }

        @Test
        @DisplayName("Failure - database error during initial check")
        void deletePost_Int_DatabaseErrorDuringCheck() {
            // Arrange
            int postId = 123;

            // Mock for getAllPosts query chain
            Query mockTimeQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockTimeQuery);

            // Mock database error during getAllPosts
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onCancelled(DatabaseError.fromException(new DatabaseException("Database error")));
                return null;
            }).when(mockTimeQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock for delete operation query chain (even though it shouldn't be reached)
            Query mockPostIdQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockPostIdQuery);
            when(mockPostIdQuery.equalTo(postId)).thenReturn(mockEqualToQuery);

            // Act & Assert
            assertFalse(dao.deletePost(postId));
        }

        @Test
        @DisplayName("Failure - error during delete operation")
        void deletePost_Int_DeleteOperationError() {
            // Arrange
            int postId = 123;

            // Mock for getAllPosts
            Query mockTimeQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockTimeQuery);
            // Setup mock data for getAllPosts
            DataSnapshot mockTimeSnapshot = mock(DataSnapshot.class);
            Post targetPost = new Post();
            targetPost.setPostID(postId);
            when(mockTimeSnapshot.exists()).thenReturn(true);
            when(mockTimeSnapshot.getChildren()).thenReturn(Collections.singletonList(mockTimeSnapshot));
            when(mockTimeSnapshot.getValue(Post.class)).thenReturn(targetPost);

            // Setup getAllPosts behavior
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockTimeSnapshot);
                return null;
            }).when(mockTimeQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock for delete operation
            Query mockDeleteQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockDeleteQuery);
            when(mockDeleteQuery.equalTo(postId)).thenReturn(mockDeleteQuery);

            // Mock the snapshot for delete operation
            DataSnapshot mockDeleteSnapshot = mock(DataSnapshot.class);
            DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
            when(mockDeleteSnapshot.exists()).thenReturn(true);
            when(mockDeleteSnapshot.getChildrenCount()).thenReturn(1L);
            when(mockDeleteSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
            when(mockChildSnapshot.getKey()).thenReturn("mockKey");

            // Setup delete operation behavior
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockDeleteSnapshot);
                return null;
            }).when(mockDeleteQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock error during delete
            DatabaseReference mockChildRef = mock(DatabaseReference.class);
            when(mockPostsRef.child(anyString())).thenReturn(mockChildRef);
            doAnswer(invocation -> {
                DatabaseReference.CompletionListener listener = invocation.getArgument(0);
                listener.onComplete(DatabaseError.fromException(new Exception("Delete failed")), mockChildRef);
                return null;
            }).when(mockChildRef).removeValue(any(DatabaseReference.CompletionListener.class));

            // Act & Assert
            assertFalse(dao.deletePost(postId));
        }

        @Test
        @DisplayName("Failure - timeout during operation")
        void deletePost_Int_Timeout() {
            // Arrange
            int postId = 123;

            // Mock for getAllPosts query chain
            Query mockTimeQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("timestamp")).thenReturn(mockTimeQuery);

            // Mock operation that never completes
            doAnswer(invocation -> {
                Thread.sleep(6000); // Longer than the timeout
                return null;
            }).when(mockTimeQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock for delete operation query chain (even though it shouldn't be reached)
            Query mockPostIdQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockPostIdQuery);
            when(mockPostIdQuery.equalTo(postId)).thenReturn(mockEqualToQuery);

            // Act & Assert
            assertFalse(dao.deletePost(postId));
        }
    }

    @Nested
    @DisplayName("updatePost tests")
    class UpdatePostTests {


        @Test
        @DisplayName("updatePost - Success")
        void updatePost_Success() {
            // Arrange
            Post post = new Post();
            post.setPostID(789);
            post.setTitle("Updated Title");
            post.setDescription("Updated Description");

            // Mock the snapshot with multiple children to simulate the database
            DataSnapshot mockRootSnapshot = mock(DataSnapshot.class);
            DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);

            // Create a list with one child snapshot
            List<DataSnapshot> childrenList = new ArrayList<>();
            childrenList.add(mockChildSnapshot);

            // Setup the root snapshot to return our child
            when(mockRootSnapshot.getChildren()).thenReturn(childrenList);

            // Setup the child snapshot to return our test post and a key
            when(mockChildSnapshot.getValue(Post.class)).thenReturn(post);
            when(mockChildSnapshot.getKey()).thenReturn("firebase-key-123");

            // Setup the mock reference for the child update
            DatabaseReference mockChildRef = mock(DatabaseReference.class);
            when(mockPostsRef.child("firebase-key-123")).thenReturn(mockChildRef);

            // Mock the initial listener for the query to find the post
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockRootSnapshot);
                return null;
            }).when(mockPostsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock the setValue behavior with successful completion
            doAnswer(invocation -> {
                DatabaseReference.CompletionListener completionListener = invocation.getArgument(1);
                completionListener.onComplete(null, mockChildRef);
                return null;
            }).when(mockChildRef).setValue(any(Post.class), any(DatabaseReference.CompletionListener.class));

            // Act
            boolean result = dao.updatePost(post);

            // Assert
            assertTrue(result);
            verify(mockPostsRef).addListenerForSingleValueEvent(any(ValueEventListener.class));
            verify(mockChildRef).setValue(any(Post.class), any(DatabaseReference.CompletionListener.class));
        }

        @Test
        @DisplayName("updatePost - Post Not Found")
        void updatePost_PostNotFound() {
            // Arrange
            Post post = new Post();
            post.setPostID(999);
            post.setTitle("Non-existent Post");

            // Mock the query chain
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(999.0)).thenReturn(mockEqualToQuery);

            // Mock empty snapshot (no posts found)
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            when(mockSnapshot.getChildrenCount()).thenReturn(0L);
            when(mockSnapshot.getChildren()).thenReturn(Collections.emptyList());

            // Set up listener behavior
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Act
            boolean result = dao.updatePost(post);

            // Assert
            assertFalse(result);
            verify(mockPostsRef, never()).child(anyString());
        }

        @Test
        @DisplayName("updatePost - Database Error")
        void updatePost_DatabaseError() {
            // Arrange
            Post post = new Post();
            post.setPostID(555);
            post.setTitle("Error Post");

            // Mock the query chain
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(555.0)).thenReturn(mockEqualToQuery);

            // Mock database error
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onCancelled(DatabaseError.fromException(new Exception("Database error")));
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Act
            boolean result = dao.updatePost(post);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("updatePost - Null Post")
        void updatePost_NullPost() {
            // Act
            boolean result = dao.updatePost(null);

            // Assert
            assertFalse(result);
            verify(mockPostsRef, never()).orderByChild(anyString());
        }

        @Test
        @DisplayName("updatePost - setValue Error")
        void updatePost_SetValueError() {
            // Arrange
            Post post = new Post();
            post.setPostID(777);
            post.setTitle("Error on Save Post");

            // Mock the query chain
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(777.0)).thenReturn(mockEqualToQuery);

            // Mock the snapshot for post lookup
            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            when(mockSnapshot.getChildrenCount()).thenReturn(1L);
            when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
            when(mockChildSnapshot.getKey()).thenReturn("mockKey");

            // Mock database references
            DatabaseReference mockChildRef = mock(DatabaseReference.class);
            when(mockPostsRef.child("mockKey")).thenReturn(mockChildRef);

            // Set up listener behavior
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock setValue error
            doAnswer(invocation -> {
                DatabaseReference.CompletionListener listener = invocation.getArgument(1);
                listener.onComplete(DatabaseError.fromException(new Exception("Save error")), mockChildRef);
                return null;
            }).when(mockChildRef).setValue(eq(post), any(DatabaseReference.CompletionListener.class));

            // Act
            boolean result = dao.updatePost(post);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("updatePost - Timeout")
        void updatePost_Timeout() {
            // Arrange
            Post post = new Post();
            post.setPostID(888);
            post.setTitle("Timeout Post");

            // Mock the query chain
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(888.0)).thenReturn(mockEqualToQuery);

            // Set up listener that never calls back
            doAnswer(invocation -> null)
                    .when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Act & Assert
            assertTimeout(Duration.ofSeconds(7), () -> {
                boolean result = dao.updatePost(post);
                assertFalse(result);
            });
        }
    }

    @Nested
    @DisplayName("editPost tests")
    class EditPostTests {

        @Test
        @DisplayName("editPost - Success")
        void editPost_Success() {
            // Arrange
            String postId = "123";
            String newTitle = "Edited Title";
            String description = "Edited Description";
            String location = "New Location";
            List<String> tags = Arrays.asList("tag1", "tag2", "newTag");
            boolean isLost = false;

            // Create the original post
            Post originalPost = new Post();
            originalPost.setPostID(123);
            originalPost.setTitle("Original Title");
            originalPost.setDescription("Original Description");
            originalPost.setLocation("Original Location");
            originalPost.setTags(Arrays.asList("tag1"));
            originalPost.setLost(true);
            originalPost.setTimestamp("2023-01-01T10:00:00");
            originalPost.setAuthor("testUser");

            // Mock getPostById behavior to return the original post
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(123.0)).thenReturn(mockEqualToQuery);

            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            when(mockSnapshot.getChildrenCount()).thenReturn(1L);
            when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
            when(mockChildSnapshot.getValue(Post.class)).thenReturn(originalPost);

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock updatePost to return success
            // Use an argument captor to capture the modified post
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

            // Mock child reference for updatePost
            when(mockChildSnapshot.getKey()).thenReturn("mockKey");
            DatabaseReference mockChildRef = mock(DatabaseReference.class);
            when(mockPostsRef.child("mockKey")).thenReturn(mockChildRef);

            doAnswer(invocation -> {
                DatabaseReference.CompletionListener listener = invocation.getArgument(1);
                listener.onComplete(null, mockChildRef);
                return null;
            }).when(mockChildRef).setValue(postCaptor.capture(), any(DatabaseReference.CompletionListener.class));

            // Act
            boolean result = dao.editPost(postId, newTitle, description, location, tags, isLost);

            // Assert
            assertTrue(result);

            // Verify captured post has correct modifications
            Post capturedPost = postCaptor.getValue();
            assertEquals(123, capturedPost.getPostID());
            assertEquals(newTitle, capturedPost.getTitle());
            assertEquals(description, capturedPost.getDescription());
            assertEquals(location, capturedPost.getLocation());
            assertEquals(tags, capturedPost.getTags());
            assertEquals(isLost, capturedPost.isLost());
            // Verify original attributes were preserved
            assertEquals("testUser", capturedPost.getAuthor());
            assertEquals("2023-01-01T10:00:00", capturedPost.getTimestamp());
        }

        @Test
        @DisplayName("editPost - Post Not Found")
        void editPost_PostNotFound() {
            // Arrange
            String postId = "999";
            String newTitle = "Edited Title";
            String description = "Edited Description";
            String location = "New Location";
            List<String> tags = Arrays.asList("tag1", "tag2");
            boolean isLost = false;

            // Mock getPostById behavior to return null
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(999.0)).thenReturn(mockEqualToQuery);

            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            when(mockSnapshot.getChildrenCount()).thenReturn(0L);
            when(mockSnapshot.getChildren()).thenReturn(Collections.emptyList());

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Act
            boolean result = dao.editPost(postId, newTitle, description, location, tags, isLost);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("editPost - Invalid ID Format")
        void editPost_InvalidIdFormat() {
            // Arrange
            String postId = "abc"; // Non-numeric ID
            String newTitle = "Edited Title";
            String description = "Edited Description";
            String location = "New Location";
            List<String> tags = Arrays.asList("tag1", "tag2");
            boolean isLost = false;

            // Act
            boolean result = dao.editPost(postId, newTitle, description, location, tags, isLost);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("editPost - Database Error")
        void editPost_DatabaseError() {
            // Arrange
            String postId = "555";
            String newTitle = "Edited Title";
            String description = "Edited Description";
            String location = "New Location";
            List<String> tags = Arrays.asList("tag1", "tag2");
            boolean isLost = false;

            // Mock getPostById behavior to throw database error
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(555.0)).thenReturn(mockEqualToQuery);

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onCancelled(DatabaseError.fromException(new Exception("Database error")));
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Act
            boolean result = dao.editPost(postId, newTitle, description, location, tags, isLost);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("editPost - updatePost Failure")
        void editPost_UpdatePostFailure() {
            // Arrange
            String postId = "123";
            String newTitle = "Edited Title";
            String description = "Edited Description";
            String location = "New Location";
            List<String> tags = Arrays.asList("tag1", "tag2");
            boolean isLost = false;

            // Create the original post with a valid timestamp
            Post originalPost = new Post();
            originalPost.setPostID(123);
            originalPost.setTitle("Original Title");
            originalPost.setDescription("Original Description");
            originalPost.setLocation("Original Location");
            originalPost.setTags(Arrays.asList("tag1"));
            originalPost.setLost(true);

            // THIS IS THE KEY FIX: Set a valid timestamp that can be parsed
            // Use ISO_LOCAL_DATE_TIME format which is the standard for LocalDateTime
            originalPost.setTimestamp(String.valueOf(LocalDateTime.now()));

            // Mock getPostById behavior to return the original post
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(123.0)).thenReturn(mockEqualToQuery);

            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            when(mockSnapshot.getChildrenCount()).thenReturn(1L);
            when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
            when(mockChildSnapshot.getValue(Post.class)).thenReturn(originalPost);

            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Mock child reference for updatePost to fail
            when(mockChildSnapshot.getKey()).thenReturn("mockKey");
            DatabaseReference mockChildRef = mock(DatabaseReference.class);
            when(mockPostsRef.child("mockKey")).thenReturn(mockChildRef);

            doAnswer(invocation -> {
                DatabaseReference.CompletionListener listener = invocation.getArgument(1);
                listener.onComplete(DatabaseError.fromException(new Exception("Update error")), mockChildRef);
                return null;
            }).when(mockChildRef).setValue(any(Post.class), any(DatabaseReference.CompletionListener.class));

            // Act
            boolean result = dao.editPost(postId, newTitle, description, location, tags, isLost);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("editPost - Null Parameters")
        void editPost_NullParameters() {
            // Arrange
            String postId = "123";

            // Create the original post with valid timestamp
            Post originalPost = new Post();
            originalPost.setPostID(123);
            originalPost.setTitle("Original Title");
            originalPost.setDescription("Original Description");
            originalPost.setLocation("Original Location");
            originalPost.setTags(Arrays.asList("tag1"));
            originalPost.setLost(true);
            originalPost.setTimestamp(String.valueOf(LocalDateTime.now()));  // Set valid timestamp

            // Prepare to return the original post for Post lookup
            Query mockQuery = mock(Query.class);
            Query mockEqualToQuery = mock(Query.class);
            when(mockPostsRef.orderByChild("postID")).thenReturn(mockQuery);
            when(mockQuery.equalTo(123.0)).thenReturn(mockEqualToQuery);

            DataSnapshot mockSnapshot = mock(DataSnapshot.class);
            DataSnapshot mockChildSnapshot = mock(DataSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            when(mockSnapshot.getChildrenCount()).thenReturn(1L);
            when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(mockChildSnapshot));
            when(mockChildSnapshot.getValue(Post.class)).thenReturn(originalPost);
            when(mockChildSnapshot.getKey()).thenReturn("mockKey");

            // Setup listener behavior to return our mock snapshot
            doAnswer(invocation -> {
                ValueEventListener listener = invocation.getArgument(0);
                listener.onDataChange(mockSnapshot);
                return null;
            }).when(mockEqualToQuery).addListenerForSingleValueEvent(any(ValueEventListener.class));

            // Setup update behavior to simulate success
            DatabaseReference mockChildRef = mock(DatabaseReference.class);
            when(mockPostsRef.child("mockKey")).thenReturn(mockChildRef);

            // Create a placeholder to capture the Post being saved
            final ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

            // Setup setValue to simulate success and capture the post being saved
            doAnswer(invocation -> {
                Post capturedPost = invocation.getArgument(0);
                DatabaseReference.CompletionListener listener = invocation.getArgument(1);
                // Return success
                listener.onComplete(null, mockChildRef);
                return null;
            }).when(mockChildRef).setValue(postCaptor.capture(), any(DatabaseReference.CompletionListener.class));

            // Act - Call editPost with null parameters
            boolean result = dao.editPost(postId, null, null, null, null, true);

            // Assert
            assertTrue(result, "Edit with null parameters should succeed");

            // Verify the post was updated
            verify(mockChildRef).setValue(any(Post.class), any(DatabaseReference.CompletionListener.class));

            // Get the captured post that was saved
            Post savedPost = postCaptor.getValue();

            // Verify original values were preserved for null parameters
            assertEquals("Original Title", savedPost.getTitle(), "Title should remain unchanged");
            assertEquals("Original Description", savedPost.getDescription(), "Description should remain unchanged");
            assertEquals("Original Location", savedPost.getLocation(), "Location should remain unchanged");
            assertEquals(Arrays.asList("tag1"), savedPost.getTags(), "Tags should remain unchanged");
            // isLost was provided as true, so it should be updated
            assertTrue(savedPost.isLost(), "isLost should be updated to true");
        }
    }

}
