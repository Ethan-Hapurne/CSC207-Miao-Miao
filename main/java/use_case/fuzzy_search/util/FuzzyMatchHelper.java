package use_case.fuzzy_search.util;

import entity.Post;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FuzzyMatchHelper with an internal Chain of Responsibility:
 * Normalize -> Score -> Rank
 *
 * Public API stays the same so callers/tests don't need changes.
 */
public class FuzzyMatchHelper {

    /* -------------------- constants (unchanged) -------------------- */
    private static final int FUZZY_THRESHOLD = 2;
    private static final List<String> SEARCH_FIELDS = Arrays.asList(
            "title", "tags", "description", "location"
    );
    private static final int EXACT_MATCH_WEIGHT = 3;
    private static final int FUZZY_MATCH_WEIGHT = 1;
    private static final int PHRASE_MATCH_WEIGHT = 2;

    /* -------------------- public API (unchanged) -------------------- */
    public static List<Post> fuzzyMatchPosts(List<Post> posts, String query) {
        if (posts == null || posts.isEmpty()) return Collections.emptyList();
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();

        // Build the chain: Normalize -> Score -> Rank
        SearchContext ctx = new SearchContext(query);
        SearchHandler h1 = new NormalizeHandler();  // 规范化 & 同义词扩展
        SearchHandler h2 = new ScoreHandler();      // 逐帖打分（>0 保留）
        SearchHandler h3 = new RankHandler();       // 结果排序（分数降序）

        h1.setNext(h2);
        h2.setNext(h3);

        List<PostWithScore> finalScored = h1.handle(posts, ctx);

        // Extract posts only
        List<Post> results = new ArrayList<>(finalScored.size());
        for (PostWithScore pws : finalScored) results.add(pws.getPost());
        return results;
    }

    /* ==================== Chain of Responsibility ==================== */

    /** Context passed along the chain. */
    private static class SearchContext {
        final String originalQuery;
        Set<String> keywords; // filled by NormalizeHandler

        SearchContext(String originalQuery) {
            this.originalQuery = originalQuery;
        }
    }

    /** Handler interface. */
    private interface SearchHandler {
        void setNext(SearchHandler next);
        List<PostWithScore> handle(List<Post> inputPosts, SearchContext ctx);
    }

    /** Base handler with next linkage. */
    private abstract static class BaseHandler implements SearchHandler {
        protected SearchHandler next;
        @Override public void setNext(SearchHandler next) { this.next = next; }
        protected List<PostWithScore> nextOr(List<PostWithScore> out, SearchContext ctx) {
            return (next == null) ? out : next.handle(extractPosts(out), ctx);
        }
        // helper: convert PostWithScore list -> pass as posts to next.handle
        private List<Post> extractPosts(List<PostWithScore> list) {
            List<Post> posts = new ArrayList<>(list.size());
            for (PostWithScore pws : list) posts.add(pws.getPost());
            return posts;
        }
    }

    /** Step 1: normalize query & expand synonyms, pass all posts through. */
    private static class NormalizeHandler extends BaseHandler {
        @Override
        public List<PostWithScore> handle(List<Post> inputPosts, SearchContext ctx) {
            ctx.keywords = preprocessQuery(ctx.originalQuery);
            if (ctx.keywords == null || ctx.keywords.isEmpty()) return Collections.emptyList();

            // Wrap input as score=0 and pass along
            List<PostWithScore> passthrough = new ArrayList<>(inputPosts.size());
            for (Post p : inputPosts) passthrough.add(new PostWithScore(p, 0));
            return nextOr(passthrough, ctx);
        }
    }

    /** Step 2: score each post (>0 are kept). */
    private static class ScoreHandler extends BaseHandler {
        @Override
        public List<PostWithScore> handle(List<Post> inputPosts, SearchContext ctx) {
            List<PostWithScore> scored = new ArrayList<>();
            for (Post p : inputPosts) {
                int score = calculatePostScore(p, ctx.keywords);
                if (score > 0) scored.add(new PostWithScore(p, score));
            }
            return nextOr(scored, ctx);
        }
    }

    /** Step 3: sort by score desc and finish. */
    private static class RankHandler extends BaseHandler {
        @Override
        public List<PostWithScore> handle(List<Post> inputPosts, SearchContext ctx) {
            // Input here are plain posts; rescore to ensure order by score
            List<PostWithScore> scored = new ArrayList<>();
            for (Post p : inputPosts) {
                int score = calculatePostScore(p, ctx.keywords);
                if (score > 0) scored.add(new PostWithScore(p, score));
            }
            scored.sort(Comparator.comparingInt(PostWithScore::getScore).reversed());
            return scored; // end of chain
        }
    }

    /* ==================== original helper logic ==================== */

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
            // include synonyms if available
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
        if (fieldValue == null || fieldValue.isEmpty() || keyword == null || keyword.isEmpty()) return 0;

        // exact match on full field
        if (fieldValue.equals(keyword)) return EXACT_MATCH_WEIGHT;

        // phrase inclusion
        if (keyword.contains(" ") && fieldValue.contains(keyword)) return PHRASE_MATCH_WEIGHT;

        int maxScore = 0;
        String[] tokens = fieldValue.split("\\W+");

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (token.equals(keyword)) return EXACT_MATCH_WEIGHT;

            if (FuzzyMatcher.isFuzzyMatch(token, keyword, FUZZY_THRESHOLD)) {
                maxScore = Math.max(maxScore, FUZZY_MATCH_WEIGHT);
            }
        }
        return maxScore;
    }

    /* -------------------- local type -------------------- */
    private static class PostWithScore {
        private final Post post;
        private final int score;

        PostWithScore(Post post, int score) {
            this.post = post;
            this.score = score;
        }
        public Post getPost() { return post; }
        public int getScore() { return score; }
    }
}
