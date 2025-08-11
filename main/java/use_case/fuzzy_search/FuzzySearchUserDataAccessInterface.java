package use_case.fuzzy_search;

import use_case.dashboard.DashboardUserDataAccessInterface;

/**
 * Data access interface for the fuzzy search use case.
 * Extends the existing search interface to reuse the same data access infrastructure.
 */
public interface FuzzySearchUserDataAccessInterface extends DashboardUserDataAccessInterface {
    // No additional methods needed - fuzzy search just uses the existing getAllPosts() method
}
