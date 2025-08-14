package use_case.fuzzy_search.util;

import java.util.*;

public class SynonymExpander {
    private static final Map<String, Set<String>> synonymGraph = new HashMap<>();

    static {
        addSynonyms("laptop", "macbook", "computer", "pc");
        addSynonyms("phone", "mobile", "smartphone", "cellphone");
        addSynonyms("tablet", "ipad", "android tablet");
        addSynonyms("earbuds", "earphones", "headphones");
        addSynonyms("charger", "charging cable", "power adapter");
        addSynonyms("watch", "smartwatch", "wristwatch");
        addSynonyms("wallet", "billfold", "cardholder");
        addSynonyms("keys", "keychain", "car keys");
        addSynonyms("bag", "backpack", "tote", "handbag");
        addSynonyms("id card", "identification", "student id");
        addSynonyms("passport", "travel document");
        addSynonyms("jacket", "coat", "windbreaker");
        addSynonyms("glasses", "eyeglasses", "sunglasses");
        addSynonyms("hat", "cap", "beanie");
        addSynonyms("scarf", "neck warmer");
        addSynonyms("umbrella", "rain umbrella");
        addSynonyms("notebook", "notepad", "writing pad");
        addSynonyms("pen", "ballpoint", "marker");
        addSynonyms("textbook", "course book");
        addSynonyms("credit card", "debit card", "payment card");
        addSynonyms("student card", "campus id");
        addSynonyms("bottle", "water bottle", "tumbler");
        addSynonyms("calculator", "scientific calculator");
    }

    public static List<String> expand(String term) {
        if (term == null) {
            return Collections.emptyList();
        }

        String normalized = term.trim().toLowerCase();
        Set<String> results = new HashSet<>();
        results.add(normalized);

        if (synonymGraph.containsKey(normalized)) {
            results.addAll(synonymGraph.get(normalized));
        }

        return new ArrayList<>(results);
    }

    private static void addSynonyms(String baseTerm, String... synonyms) {
        Set<String> termGroup = new HashSet<>();
        termGroup.add(baseTerm.toLowerCase());

        for (String synonym : synonyms) {
            termGroup.add(synonym.toLowerCase());
        }

        for (String term : termGroup) {
            synonymGraph.computeIfAbsent(term, k -> new HashSet<>());

            Set<String> connections = new HashSet<>(termGroup);
            connections.remove(term);

            synonymGraph.get(term).addAll(connections);
        }
    }
}