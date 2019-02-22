package androidx.lifecycle;

/**
 * Accessor for {@link ViewModelStore#get(String)} as it is package-private.
 */
public class ViewModelStoreAccessor {
    public static ViewModel get(ViewModelStore viewModelStore, String key) {
        return viewModelStore.get(key);
    }
}
