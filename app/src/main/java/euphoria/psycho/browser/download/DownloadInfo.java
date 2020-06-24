package euphoria.psycho.browser.download;


import android.util.Pair;

public class DownloadInfo {

    private static final String PC_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36";

    public int mStatus;
    public long mTotalBytes;
    public long mCurrentBytes;
    public String mUri;
    public String mETag;
    public String mFileName;
    public String mMimeType;
    public int mRetryAfter;
    public long mId;
    public String mProxy;

    public DownloadInfo(
            int status,
            long totalBytes,
            long currentBytes,
            String uri,
            String eTag,
            String fileName,
            String mimeType,
            int retryAfter) {
        mStatus = status;
        mTotalBytes = totalBytes;
        mCurrentBytes = currentBytes;
        mUri = uri;
        mETag = eTag;
        mFileName = fileName;
        mMimeType = mimeType;
        mRetryAfter = retryAfter;
    }

    public long getCurrentBytes() {
        return mCurrentBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        mCurrentBytes = currentBytes;
    }

    public String getETag() {
        return mETag;
    }

    public void setETag(String ETag) {
        mETag = ETag;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public Pair<String, String>[] getHeaders() {
        return null;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public static String getPcUserAgent() {
        return PC_USER_AGENT;
    }

    public String getProxy() {
        return mProxy;
    }

    public void setProxy(String proxy) {
        mProxy = proxy;
    }

    public int getRetryAfter() {
        return mRetryAfter;
    }

    public void setRetryAfter(int retryAfter) {
        mRetryAfter = retryAfter;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        mTotalBytes = totalBytes;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public String getUserAgent() {
        return PC_USER_AGENT;
    }

    public boolean isMeteredAllowed(long totalBytes) {
        return true;
    }
}