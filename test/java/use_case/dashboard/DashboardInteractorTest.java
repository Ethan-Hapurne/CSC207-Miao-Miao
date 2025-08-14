package use_case.dashboard;

import entity.Post;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DashboardInteractorTest {

    private DashboardUserDataAccessInterface dashboardDataAccessObject;
    private DashboardOutputBoundary dashboardOutputBoundary;
    private DashboardInteractor dashboardInteractor;

    // Test data
    private Post post1;
    private Post post2;
    private List<Post> postList;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create mocks
        dashboardDataAccessObject = mock(DashboardUserDataAccessInterface.class);
        dashboardOutputBoundary = mock(DashboardOutputBoundary.class);
        
        // Create interactor with mocked dependencies
        dashboardInteractor = new DashboardInteractor(dashboardDataAccessObject, dashboardOutputBoundary);
        
        // Setup test data
        post1 = mock(Post.class);
        when(post1.getTitle()).thenReturn("First Test Post");
        post2 = mock(Post.class);
        when(post2.getTitle()).thenReturn("Second Test Post");
        
        postList = Arrays.asList(post1, post2);
        
        // Setup basic mock behaviors
        when(dashboardDataAccessObject.getAllPosts()).thenReturn(postList);
        
        testUser = mock(User.class);
        when(testUser.getName()).thenReturn("testUser");
        when(testUser.getCredibilityScore()).thenReturn(5);
    }

    @Nested
    @DisplayName("Load Posts Tests")
    class LoadPostsTests {
        
        @Test
        @DisplayName("Successfully load all posts")
        void loadPosts_Success() {
            // Arrange - use constructor with just action
            // Arrange
            DashboardInputData inputData = new DashboardInputData("load_posts");
            
            // Act
            dashboardInteractor.execute(inputData);
            
            // Assert
            verify(dashboardDataAccessObject).getAllPosts();
            
            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());
            
            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals(postList, outputData.getPosts());
        }
        
        @Test
        @DisplayName("Handle exception during load posts")
        void loadPosts_Exception() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData("load_posts");
            when(dashboardDataAccessObject.getAllPosts()).thenThrow(new RuntimeException("Database error"));
            
            // Act
            dashboardInteractor.execute(inputData);
            
            // Assert
            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());
            
            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("An error occurred: Database error", outputData.getError());
        }
    }

    @Nested
    @DisplayName("Search Posts Tests")
    class SearchPostsTests {

        @Test
        @DisplayName("Search with valid query")
        void searchPosts_ValidQuery() {
            // Arrange - use constructor with action and searchQuery
            // Arrange
            String searchQuery = "test query";
            // Create with action and searchQuery
            DashboardInputData inputData = new DashboardInputData("search_posts", searchQuery);

            List<Post> searchResults = Arrays.asList(post1);
            when(dashboardDataAccessObject.searchPosts(searchQuery)).thenReturn(searchResults);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).searchPosts(searchQuery);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals(searchResults, outputData.getPosts());
        }

        @Test
        @DisplayName("Search with empty query returns sorted posts")
        void searchPosts_EmptyQuery() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData("search_posts", "");

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getAllPosts();
            verify(dashboardDataAccessObject, never()).searchPosts(anyString());

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertNotNull(outputData.getPosts());
        }

        @Test
        @DisplayName("Search with null query returns sorted posts")
        void searchPosts_NullQuery() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData("search_posts", (String)null);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getAllPosts();
            verify(dashboardDataAccessObject, never()).searchPosts(anyString());

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertNotNull(outputData.getPosts());
        }
    }

    @Nested
    @DisplayName("Advanced Search Tests")
    class AdvancedSearchTests {

        @Test
        @DisplayName("Advanced search with criteria")
        void advancedSearch_WithCriteria() {
            // Arrange
            String title = "test title";
            String location = "test location";
            List<String> tags = Arrays.asList("tag1", "tag2");
            Boolean isLost = true;

            // Use constructor with title, location, tags, isLost
            DashboardInputData inputData = new DashboardInputData("advanced_search", title, location, tags, isLost);

            List<Post> advancedSearchResults = Arrays.asList(post2);
            when(dashboardDataAccessObject.searchPostsByCriteria(
                    eq(title),
                    eq(location),
                    eq(tags),
                    eq(Boolean.TRUE)
            )).thenReturn(advancedSearchResults);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).searchPostsByCriteria(
                    eq(title),
                    eq(location),
                    eq(tags),
                    eq(Boolean.TRUE)
            );

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals(advancedSearchResults, outputData.getPosts());
        }

        @Test
        @DisplayName("Advanced search with null criteria")
        void advancedSearch_NullCriteria() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData("advanced_search", null, null, null, null);

            List<Post> advancedSearchResults = Arrays.asList(post1);
            when(dashboardDataAccessObject.searchPostsByCriteria(
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()
            )).thenReturn(advancedSearchResults);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).searchPostsByCriteria(
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()
            );

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals(advancedSearchResults, outputData.getPosts());
        }
    }

    @Nested
    @DisplayName("Add Post Tests")
    class AddPostTests {

        @Test
        @DisplayName("Add post with valid data")
        void addPost_ValidData() {
            // Arrange
            String title = "New Post Title";
            String content = "New Post Content";
            List<String> tags = Arrays.asList("new", "post");
            String location = "New Location";
            boolean isLost = true;
            String author = "testAuthor";

            DashboardInputData inputData = new DashboardInputData(
                    "add_post",
                    title,
                    content,
                    tags,
                    location,
                    isLost,
                    author
            );

            when(dashboardDataAccessObject.addPost(
                    eq(title),
                    eq(content),
                    eq(tags),
                    eq(location),
                    eq(isLost),
                    eq(author)
            )).thenReturn(post1);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).addPost(
                    eq(title),
                    eq(content),
                    eq(tags),
                    eq(location),
                    eq(isLost),
                    eq(author)
            );

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertTrue(outputData.getSuccessMessage().contains("Post created successfully"));
        }

        @Test
        @DisplayName("Add post with missing title")
        void addPost_MissingTitle() {
            // Arrange
            String title = ""; // Empty title
            String content = "New Post Content";

            // Use constructor without author
            DashboardInputData inputData = new DashboardInputData(
                    "add_post",
                    title,
                    content,
                    null,
                    null,
                    false
            );

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject, never()).addPost(anyString(), anyString(), anyList(), anyString(), anyBoolean(), anyString());

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Post title and content are required.", outputData.getError());
        }

        @Test
        @DisplayName("Add post with missing content")
        void addPost_MissingContent() {
            // Arrange
            String title = "New Post Title";
            String content = ""; // Empty content

            // Use constructor without author
            DashboardInputData inputData = new DashboardInputData(
                    "add_post",
                    title,
                    content,
                    null,
                    null,
                    false
            );

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject, never()).addPost(anyString(), anyString(), anyList(), anyString(), anyBoolean(), anyString());

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Post title and content are required.", outputData.getError());
        }

        @Test
        @DisplayName("Add post with null title")
        void addPost_NullTitle() {
            // Arrange
            String title = null; // Null title
            String content = "New Post Content";

            // Use constructor without author
            DashboardInputData inputData = new DashboardInputData(
                    "add_post",
                    title,
                    content,
                    null,
                    null,
                    false
            );

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject, never()).addPost(anyString(), anyString(), anyList(), anyString(), anyBoolean(), anyString());

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Post title and content are required.", outputData.getError());
        }
    }

    @Nested
    @DisplayName("Update Post Tests")
    class UpdatePostTests {
        
        @Test
        @DisplayName("Update post successfully")
        void updatePost_Success() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData("update_post", post1);
            when(dashboardDataAccessObject.updatePost(post1)).thenReturn(true);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).updatePost(post1);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertTrue(outputData.getSuccessMessage().contains("Post updated successfully"));
        }

        @Test
        @DisplayName("Update post failure")
        void updatePost_Failure() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData("update_post", post1);

            when(dashboardDataAccessObject.updatePost(post1)).thenReturn(false);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).updatePost(post1);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Failed to update post.", outputData.getError());
        }

        @Test
        @DisplayName("Update post with null post")
        void updatePost_NullPost() {
            // Arrange - use constructor with null post
            Post nullPost = null;
            // Arrange
            DashboardInputData inputData = new DashboardInputData("update_post", nullPost);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject, never()).updatePost(any(Post.class));

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Post data is required for update.", outputData.getError());
        }
    }

    @Nested
    @DisplayName("Resolve Post Tests")
    class ResolvePostTests {
        @Test
        @DisplayName("Resolve post with credit")
        void resolvePost_WithCredit() {
            // Arrange
            String postId = "123";
            String resolvedByUsername = "resolver";
            String creditedUsername = "finder";

            // Use constructor for resolve post
            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    postId,
                    creditedUsername,
                    resolvedByUsername
            );

            Post postToResolve = mock(Post.class);
            when(postToResolve.isResolved()).thenReturn(false);

            User creditedUser = mock(User.class);
            when(creditedUser.getName()).thenReturn(creditedUsername);
            when(creditedUser.getCredibilityScore()).thenReturn(6); // After +1 point

            when(dashboardDataAccessObject.getPostById(postId)).thenReturn(postToResolve);
            when(dashboardDataAccessObject.getUserByUsername(creditedUsername)).thenReturn(creditedUser);
            when(dashboardDataAccessObject.updatePost(postToResolve)).thenReturn(true);
            when(dashboardDataAccessObject.updateUser(creditedUser)).thenReturn(true);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getPostById(postId);
            verify(dashboardDataAccessObject).getUserByUsername(creditedUsername);

            verify(postToResolve).setResolved(true);
            verify(postToResolve).setResolvedBy(resolvedByUsername);
            verify(postToResolve).setCreditedTo(creditedUsername);

            verify(creditedUser).addResolvedPost(postId);
            verify(creditedUser).addCredibilityPoints(1);

            verify(dashboardDataAccessObject).updatePost(postToResolve);
            verify(dashboardDataAccessObject).updateUser(creditedUser);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            String successMsg = outputData.getSuccessMessage();
            assertNotNull(successMsg);
            assertTrue(successMsg.contains("Post resolved successfully"));
            assertTrue(successMsg.contains(creditedUsername));
        }

        @Test
        @DisplayName("Resolve post without credit (skip credit)")
        void resolvePost_SkipCredit() {
            // Arrange
            String postId = "123";
            String resolvedByUsername = "resolver";
            String creditedUsername = "0"; // "0" means skip credit

            // Use constructor for resolve post
            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    postId,
                    creditedUsername,
                    resolvedByUsername
            );

            Post postToResolve = mock(Post.class);
            when(postToResolve.isResolved()).thenReturn(false);

            when(dashboardDataAccessObject.getPostById(postId)).thenReturn(postToResolve);
            when(dashboardDataAccessObject.updatePost(postToResolve)).thenReturn(true);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getPostById(postId);
            verify(dashboardDataAccessObject, never()).getUserByUsername(anyString());

            verify(postToResolve).setResolved(true);
            verify(postToResolve).setResolvedBy(resolvedByUsername);
            verify(postToResolve).setCreditedTo(null); // Should be set to null when skipping credit

            verify(dashboardDataAccessObject).updatePost(postToResolve);
            verify(dashboardDataAccessObject, never()).updateUser(any(User.class));

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Post resolved successfully.", outputData.getSuccessMessage());
        }

        @Test
        @DisplayName("Resolve post with missing resolver username")
        void resolvePost_MissingResolverUsername() {
            // Arrange - use constructor with null resolver
            // Arrange
            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    "123",
                    "finder",
                    null // null resolver
            );

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject, never()).getPostById(anyString());

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Resolving username is required.", outputData.getError());
        }

        @Test
        @DisplayName("Resolve post with non-existent post")
        void resolvePost_NonExistentPost() {
            // Arrange
            String postId = "999"; // Non-existent post ID

            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    postId,
                    "finder",
                    "resolver"
            );

            when(dashboardDataAccessObject.getPostById(postId)).thenReturn(null);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getPostById(postId);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Post not found.", outputData.getError());
        }

        @Test
        @DisplayName("Resolve post that is already resolved")
        void resolvePost_AlreadyResolved() {
            // Arrange
            String postId = "123";

            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    postId,
                    "finder",
                    "resolver"
            );

            Post alreadyResolvedPost = mock(Post.class);
            when(alreadyResolvedPost.isResolved()).thenReturn(true);

            when(dashboardDataAccessObject.getPostById(postId)).thenReturn(alreadyResolvedPost);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getPostById(postId);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Post is already resolved.", outputData.getError());
        }

        @Test
        @DisplayName("Resolve post with missing credited username")
        void resolvePost_MissingCreditedUsername() {
            // Arrange
            String postId = "123";

            // Use constructor with null credited username
            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    postId,
                    null, // null credited
                    "resolver"
            );

            Post postToResolve = mock(Post.class);
            when(postToResolve.isResolved()).thenReturn(false);

            when(dashboardDataAccessObject.getPostById(postId)).thenReturn(postToResolve);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getPostById(postId);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Credited username is required or type 0 to skip.", outputData.getError());
        }

        @Test
        @DisplayName("Resolve post with non-existent credited user")
        void resolvePost_NonExistentCreditedUser() {
            // Arrange
            String postId = "123";
            String creditedUsername = "nonexistent";

            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    postId,
                    creditedUsername,
                    "resolver"
            );

            Post postToResolve = mock(Post.class);
            when(postToResolve.isResolved()).thenReturn(false);

            when(dashboardDataAccessObject.getPostById(postId)).thenReturn(postToResolve);
            when(dashboardDataAccessObject.getUserByUsername(creditedUsername)).thenReturn(null);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).getPostById(postId);
            verify(dashboardDataAccessObject).getUserByUsername(creditedUsername);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Credited user not found.", outputData.getError());
        }

        @Test
        @DisplayName("Resolve post with database failure on post update")
        void resolvePost_DatabaseFailureOnPostUpdate() {
            // Arrange
            String postId = "123";
            String creditedUsername = "finder";

            DashboardInputData inputData = new DashboardInputData(
                    "resolve_post",
                    postId,
                    creditedUsername,
                    "resolver"
            );

            Post postToResolve = mock(Post.class);
            when(postToResolve.isResolved()).thenReturn(false);

            User creditedUser = mock(User.class);

            when(dashboardDataAccessObject.getPostById(postId)).thenReturn(postToResolve);
            when(dashboardDataAccessObject.getUserByUsername(creditedUsername)).thenReturn(creditedUser);
            when(dashboardDataAccessObject.updatePost(postToResolve)).thenReturn(false);

            // Act
            dashboardInteractor.execute(inputData);

            // Assert
            verify(dashboardDataAccessObject).updatePost(postToResolve);

            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());

            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Failed to update post or user in database.", outputData.getError());
        }
    }
    
    @Nested
    @DisplayName("Delete Post Tests")
    class DeletePostTests {
        
    @Test
    @DisplayName("Delete post successfully")
    void deletePost_Success() {
        // Arrange
        int postId = 123;
        DashboardInputData inputData = new DashboardInputData("delete_post", postId);

        when(dashboardDataAccessObject.deletePost(postId)).thenReturn(true);

        // Act
        dashboardInteractor.execute(inputData);

        // Assert
        verify(dashboardDataAccessObject).deletePost(postId);

        ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
        verify(dashboardOutputBoundary).prepareSuccessView(outputDataCaptor.capture());

        DashboardOutputData outputData = outputDataCaptor.getValue();
        assertTrue(outputData.getSuccessMessage().contains("Post deleted successfully"));
    }
        
        @Test
        @DisplayName("Delete post failure")
        void deletePost_Failure() {
            // Arrange
            int postId = 123;

            DashboardInputData inputData = new DashboardInputData("delete_post", postId);
            
            when(dashboardDataAccessObject.deletePost(postId)).thenReturn(false);
            
            // Act
            dashboardInteractor.execute(inputData);
            
            // Assert
            verify(dashboardDataAccessObject).deletePost(postId);
            
            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());
            
            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Failed to delete post.", outputData.getError());
        }
    }
    
    @Nested
    @DisplayName("Invalid Action Tests")
    class InvalidActionTests {
        
        @Test
        @DisplayName("Invalid action")
        void invalidAction() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData("invalid_action");
            
            // Act
            dashboardInteractor.execute(inputData);
            
            // Assert
            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());
            
            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Invalid action.", outputData.getError());
        }
        
        @Test
        @DisplayName("Null action")
        void nullAction() {
            // Arrange
            DashboardInputData inputData = new DashboardInputData(null);
            
            // Act
            dashboardInteractor.execute(inputData);
            
            // Assert
            ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
            verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());
            
            DashboardOutputData outputData = outputDataCaptor.getValue();
            assertEquals("Invalid action.", outputData.getError());
        }
    }
    
    @Test
    @DisplayName("Handle unexpected exceptions")
    void handleUnexpectedException() {
        // Arrange
        DashboardInputData inputData = new DashboardInputData("load_posts");
        RuntimeException unexpectedException = new RuntimeException("Unexpected error");
        when(dashboardDataAccessObject.getAllPosts()).thenThrow(unexpectedException);
        
        // Act
        dashboardInteractor.execute(inputData);
        
        // Assert
        ArgumentCaptor<DashboardOutputData> outputDataCaptor = ArgumentCaptor.forClass(DashboardOutputData.class);
        verify(dashboardOutputBoundary).prepareFailView(outputDataCaptor.capture());
        
        DashboardOutputData outputData = outputDataCaptor.getValue();
        assertEquals("An error occurred: Unexpected error", outputData.getError());
    }
}