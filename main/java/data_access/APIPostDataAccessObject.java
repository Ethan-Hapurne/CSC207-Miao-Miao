package data_access;

import entity.Post;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import use_case.search.SearchUserDataAccessInterface;
import use_case.search.util.FuzzyMatchHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API implementation of data access for posts.
 */
public class APIPostDataAccessObject implements SearchUserDataAccessInterface {
    private final OkHttpClient client;  // HTTP client for API calls
    private final String API_BASE_URL = "https://your-api-endpoint.com/posts";

    public APIPostDataAccessObject(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Post post = parseJsonToPost(json);
                        if (post != null) {
                            posts.add(post);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching posts from API: " + e.getMessage());
        }
        return posts;
    }

    @Override
    public List<Post> searchPosts(String query) {
        List<Post> allPosts = getAllPosts();
        List<Post> matchingPosts = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Post post : allPosts) {
            if (post.getTitle().toLowerCase().contains(lowerQuery) ||
                    post.getDescription().toLowerCase().contains(lowerQuery) ||
                    post.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerQuery))) {
                matchingPosts.add(post);
            }
        }
        return matchingPosts;
    }

    @Override
    public List<Post> fuzzySearch(String query) {
        List<Post> allPosts = getAllPosts();
        return FuzzyMatchHelper.fuzzyMatchPosts(allPosts, query);
    }

    @Override
    public List<Post> searchPostsByCriteria(String title, String location,
                                            List<String> tags, Boolean isLost) {
        List<Post> allPosts = getAllPosts();
        List<Post> matchingPosts = new ArrayList<>();

        for (Post post : allPosts) {
            // Title condition
            if (title != null && !title.isEmpty() &&
                    !post.getTitle().toLowerCase().contains(title.toLowerCase())) {
                continue;
            }

            // Location condition
            if (location != null && !location.isEmpty() &&
                    !post.getLocation().toLowerCase().contains(location.toLowerCase())) {
                continue;
            }

            // Tags condition
            if (tags != null && !tags.isEmpty()) {
                boolean allTagsPresent = true;
                for (String tag : tags) {
                    if (!post.getTags().contains(tag)) {
                        allTagsPresent = false;
                        break;
                    }
                }
                if (!allTagsPresent) {
                    continue;
                }
            }

            // Lost/Found status condition
            if (isLost != null && post.isLost() != isLost) {
                continue;
            }

            matchingPosts.add(post);
        }
        return matchingPosts;
    }

    private Post parseJsonToPost(JSONObject json) {
        try {
            // Parse timestamp string to LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime timestamp = LocalDateTime.parse(json.getString("timestamp"), formatter);

            return new Post(
                    json.getInt("postID"),
                    json.getString("title"),
                    json.getString("description"),
                    parseTags(json.getJSONArray("tags")),
                    timestamp,
                    json.getString("author"),
                    json.getString("location"),
                    json.optString("imageURL", null),
                    json.getBoolean("isLost"),
                    json.optInt("numberOfLikes", 0),
                    parseReactions(json.optJSONObject("reactions"))
            );
        } catch (Exception e) {
            System.err.println("Error parsing post JSON: " + e.getMessage());
            return null;
        }
    }

    private List<String> parseTags(JSONArray jsonArray) {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tags.add(jsonArray.getString(i));
        }
        return tags;
    }

    private Map<Integer, String> parseReactions(JSONObject json) {
        Map<Integer, String> reactions = new HashMap<>();
        if (json != null) {
            for (String key : json.keySet()) {
                try {
                    int userId = Integer.parseInt(key);
                    reactions.put(userId, json.getString(key));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid user ID in reactions: " + key);
                }
            }
        }
        return reactions;
    }
}