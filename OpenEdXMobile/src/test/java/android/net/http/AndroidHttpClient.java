package android.net.http;

/**
 * Stub class to workaround a bug in Robolectric, which provides a shadow for this class in the base
 * module, even though it has been removed by default from API 23. See the report at
 * https://github.com/robolectric/robolectric/issues/1862 for more details.
 *
 * @deprecated This will be removed when Robolectric is upgraded to a version which fixes the issue.
 */
@Deprecated
public class AndroidHttpClient {
    // Make this class non-instantiable
    private AndroidHttpClient() {
        throw new UnsupportedOperationException();
    }
}
