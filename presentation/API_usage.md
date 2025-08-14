# API Usage Presentation

## Rubric Requirements & Outline

### 1. Team made significant use of one or more APIs in their project
- **Firebase Realtime Database API**: Single, comprehensive database solution for all data operations
- **Used for**: Posts, comments, users, chat messages, and real-time synchronization

### 2. Two specific API endpoints are mentioned in the slides
- **Firebase Realtime Database: `getReference().child().setValue()`** - Stores data (posts, comments, users)
- **Firebase Realtime Database: `addListenerForSingleValueEvent()`** - Retrieves real-time data updates

### 3. It is clear how the API was used and the API was clearly appropriate for the program
- **Single API Solution**: Firebase Realtime Database handles all our data needs in one place
- **Real-time Updates**: Provides instant synchronization for social media features
- **Perfect Data Structure**: (Not unique), but hierarchical model naturally fits posts, comments, and user relationships. in comparison to basic databasea pis such as SQLite, we can get posts by user, or comments by post, etc.

    i.e user -> usernames ...

## 30-Second Presentation Script

"Our team made significant use of Firebase Realtime Database API. We used this single API for all our data operations - storing posts, comments, user data, and chat messages

Unlike traditional REST APIs with specific endpoints, the Realtime Database provides a unified data access layer where we specify data paths like:
• /users/{username} for user data
• /posts/{postId} for post content
• /users/{username}/posts for user's posts"

in a traditional REST API, the endpoints would look like:
• PUT /database/{project}/users/{userId}.json - Store user data
• GET /database/{project}/posts/{postId}.json - Retrieve posts
• POST /database/{project}/users/{userId}/posts.json - Create user posts"

Why did we choose this API? 
**1. Single Solution:**
"We chose Firebase Realtime Database because it's a single, comprehensive API that handles all our database needs in one place."

**2. Real-time Data Synchronization:**
"Second, it provides instant synchronization across all users, which is essential for a social media platform where users need to see updates in real-time."

**3. Natural Data Structure:**
"Finally, the hierarchical data structure naturally fits our social platform's data model - users have posts, posts have comments, and everything is organized logically. in contrast to Flat APIs like SQLite, we wouldn't be able to do things like get all posts by user, or that would require more complex querying/relationship management.
