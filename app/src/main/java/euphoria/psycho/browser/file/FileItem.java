package euphoria.psycho.browser.file;

public class FileItem {
    private final long mMostRecentJavaTimestamp;
    private final String mTitle;
    private final String mUrl;
    private Long mStableId;
    private FileManager mManager;
    private int mType;
    private long mSize;
    private String mDescription;


    public FileItem(String title, String url, long mostRecentJavaTimestamp, int type,long size) {
        mTitle = title;
        mUrl = url;
        mMostRecentJavaTimestamp = mostRecentJavaTimestamp;
        mType = type;
        mSize=size;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
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

    public String getTitle() {
        return mTitle;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getUrl() {
        return mUrl;
    }

    public void remove() {
        if (mManager != null) {
            mManager.removeItem(this);
        }
    }

    public void open() {
        mManager.openUrl(mUrl);
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setFileManager(FileManager manager) {
        mManager = manager;
    }


}
