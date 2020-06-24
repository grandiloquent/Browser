package euphoria.psycho.browser.downlaod;


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

    public Pair<String, String>[] getHeaders() {
        return null;
    }

    public String getProxy() {
        return mProxy;
    }

    public void setProxy(String proxy) {
        mProxy = proxy;
    }

    public String getUserAgent() {
        return PC_USER_AGENT;
    }

    public boolean isMeteredAllowed(long totalBytes) {
        return true;
    }
}