package entity;

/**
 * Factory for creating users.
 */
public interface UserFactory {
    /**
     * Backward compatible factory method without admin flag.
     */
    User create(String name, String password);

    /**
     * Variant with admin flag. Default delegates to non-admin creation so
     * existing implementations that only define the two-arg method still work.
     */
    default User create(String name, String password, boolean admin) {
        return create(name, password);
    }

}
