package use_case.search;

import java.util.List;

public class SearchInputData {
    private final String query;
    private final boolean isFuzzy;
    private final String title;
    private final String location;
    private final List<String> tags;
    private final Boolean isLost;

    public SearchInputData(String query, boolean isFuzzy) {
        this.query = query;
        this.isFuzzy = isFuzzy;
        this.title = null;
        this.location = null;
        this.tags = null;
        this.isLost = null;
    }

    public SearchInputData(String title, String location,
                           List<String> tags, Boolean isLost) {
        this.query = null;
        this.title = title;
        this.location = location;
        this.tags = tags;
        this.isLost = isLost;
        this.isFuzzy = false;
    }

    public String getQuery() { return query; }
    public boolean isFuzzy() { return isFuzzy; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public List<String> getTags() { return tags; }
    public Boolean getIsLost() { return isLost; }
}
