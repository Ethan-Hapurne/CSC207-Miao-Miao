package use_case.search;

import entity.Post;
import java.util.List;

public interface SearchUserDataAccessInterface {
    List<Post> getAllPosts();
    List<Post> searchPosts(String query);
    List<Post> fuzzySearch(String query);
    List<Post> searchPostsByCriteria(String title, String location, List<String> tags, Boolean isLost);
}