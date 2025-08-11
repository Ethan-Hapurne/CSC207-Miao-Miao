package use_case.fuzzy_search.util;

import entity.Post;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuzzyMatchHelper {
    private static final int FUZZY_THRESHOLD = 2;
    private static final List<String> SEARCH_FIELDS = Arrays.asList(
            "title", "tags", "description", "location"
    );
    private static final int EXACT_MATCH_WEIGHT = 3;
    private static final int FUZZY_MATCH_WEIGHT = 1;
    private static final int PHRASE_MATCH_WEIGHT = 2;

    public static List<Post> fuzzyMatchPosts(List<Post> posts, String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> keywords = preprocessQuery(query);
        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        List<PostWithScore> scoredPosts = new ArrayList<>();
        for (Post post : posts) {
            int score = calculatePostScore(post, keywords);
            if (score > 0) {
                scoredPosts.add(new PostWithScore(post, score));
            }
        }

        scoredPosts.sort(Comparator.comparingInt(PostWithScore::getScore).reversed());

        List<Post> results = new ArrayList<>();
        for (PostWithScore pws : scoredPosts) {
            results.add(pws.getPost());
        }
        return results;
    }

    private static Set<String> preprocessQuery(String query) {
        Set<String> allKeywords = new HashSet<>();
        String normalized = query.trim().toLowerCase();

        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(normalized);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1));
            } else {
                tokens.add(matcher.group(2));
            }
        }

        for (String token : tokens) {
            allKeywords.add(token);
            allKeywords.addAll(SynonymExpander.expand(token));
        }

        return allKeywords;
    }

    private static int calculatePostScore(Post post, Set<String> keywords) {
        int totalScore = 0;

        for (String field : SEARCH_FIELDS) {
            String fieldValue = getFieldValue(post, field);
            if (fieldValue == null || fieldValue.isEmpty()) continue;

            String cleanField = fieldValue.toLowerCase();

            for (String keyword : keywords) {
                int matchScore = matchKeyword(cleanField, keyword);
                if (matchScore > 0) {
                    int fieldWeight = SEARCH_FIELDS.size() - SEARCH_FIELDS.indexOf(field);
                    totalScore += matchScore * fieldWeight;
                }
            }
        }

        return totalScore;
    }

    private static String getFieldValue(Post post, String fieldName) {
        switch (fieldName) {
            case "title":
                return post.getTitle() != null ? post.getTitle() : "";
            case "description":
                return post.getDescription() != null ? post.getDescription() : "";
            case "location":
                return post.getLocation() != null ? post.getLocation() : "";
            case "tags":
                if (post.getTags() == null) return "";
                return String.join(" ", post.getTags());
            default:
                return "";
        }
    }

    private static int matchKeyword(String fieldValue, String keyword) {
        if (fieldValue.isEmpty() || keyword.isEmpty()) return 0;

        if (fieldValue.equals(keyword)) {
            return EXACT_MATCH_WEIGHT;
        }

        if (keyword.contains(" ") && fieldValue.contains(keyword)) {
            return PHRASE_MATCH_WEIGHT;
        }

        int maxScore = 0;
        String[] tokens = fieldValue.split("\\W+");

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (token.equals(keyword)) {
                return EXACT_MATCH_WEIGHT;
            }

            if (FuzzyMatcher.isFuzzyMatch(token, keyword, FUZZY_THRESHOLD)) {
                maxScore = Math.max(maxScore, FUZZY_MATCH_WEIGHT);
            }
        }

        return maxScore;
    }

    private static class PostWithScore {
        private final Post post;
        private final int score;

        public PostWithScore(Post post, int score) {
            this.post = post;
            this.score = score;
        }

        public Post getPost() { return post; }
        public int getScore() { return score; }
    }
}