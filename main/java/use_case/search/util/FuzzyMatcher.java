package use_case.search.util;

public class FuzzyMatcher {
    private static final int DEFAULT_THRESHOLD = 2;

    public static boolean isFuzzyMatch(String a, String b) {
        return isFuzzyMatch(a, b, DEFAULT_THRESHOLD);
    }

    public static boolean isFuzzyMatch(String a, String b, int threshold) {
        if (a == null || b == null) return false;
        if (a.isEmpty() || b.isEmpty()) return false;
        if (Math.abs(a.length() - b.length()) > threshold) {
            return false;
        }
        return levenshteinDistance(a.toLowerCase(), b.toLowerCase(), threshold) <= threshold;
    }

    public static int levenshteinDistance(String s1, String s2, int threshold) {
        if (s1.length() > s2.length()) {
            String temp = s1;
            s1 = s2;
            s2 = temp;
        }

        int m = s1.length();
        int n = s2.length();

        if (n - m > threshold) {
            return threshold + 1;
        }

        int[] dp = new int[m + 1];
        for (int i = 0; i <= m; i++) {
            dp[i] = i;
        }

        for (int j = 1; j <= n; j++) {
            int prev = dp[0];
            dp[0] = j;
            int minInRow = dp[0];

            for (int i = 1; i <= m; i++) {
                int temp = dp[i];
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i] = Math.min(
                        Math.min(dp[i] + 1, dp[i - 1] + 1),
                        prev + cost
                );

                prev = temp;
                minInRow = Math.min(minInRow, dp[i]);
            }

            if (minInRow > threshold) {
                return threshold + 1;
            }
        }

        return dp[m];
    }
}