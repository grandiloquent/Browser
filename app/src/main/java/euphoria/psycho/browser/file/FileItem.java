package euphoria.psycho.browser.file;

public class FileItem {
    private final long mMostRecentJavaTimestamp;
    private final String mTitle;
    private final String mUrl;
    private Long mStableId;
    private FileManager mManager;

    public FileItem(String title, String url, long mostRecentJavaTimestamp) {
        mTitle = title;
        mUrl = url;
        mMostRecentJavaTimestamp = mostRecentJavaTimestamp;
    }

    public long getStableId() {
        if (mStableId == null) {
            // Generate a stable ID that combines the timestamp and the URL.
            mStableId = (long) mUrl.hashCode();
            mStableId = (mStableId << 32) + (getTimestamp());
        }
        return mStableId;
    }

    public long getTimestamp() {
        return mMostRecentJavaTimestamp;
    }

    public void setHistoryManager(FileManager manager) {
        mManager = manager;
    }

}
