package euphoria.psycho.browser.downlaod;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;


import static java.net.HttpURLConnection.*;

class DownloadThread extends Thread {
    public static final int MAX_RETRY_AFTER = 24 * 60 * 60;
    public static final int MIN_RETRY_AFTER = 30;
    static final int BUFFER_SIZE = 8192;
    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_RETRIES = 5;
    static final int MIN_PROGRESS_STEP = 65536;
    static final long MIN_PROGRESS_TIME = 2000;
    private static final int SECOND_IN_MILLIS = 1000;
    private static final int DEFAULT_TIMEOUT = 20 * SECOND_IN_MILLIS;
    private static final int STATUS_BAD_REQUEST = 0;
    private static final int STATUS_CANNOT_RESUME = 1;
    private static final int STATUS_FILE_ERROR = 2;
    private static final int STATUS_HTTP_DATA_ERROR = 3;
    private static final int STATUS_QUEUED_FOR_WIFI = 4;
    private static final int STATUS_RUNNING = 5;
    private static final int STATUS_SUCCESS = 6;
    private static final int STATUS_TOO_MANY_REDIRECTS = 7;
    private static final int STATUS_UNHANDLED_HTTP_CODE = 8;
    private static final int STATUS_UNHANDLED_REDIRECT = 9;
    private static final int STATUS_UNKNOWN_ERROR = 10;
    private static final int STATUS_WAITING_FOR_NETWORK = 11;
    private static final int STATUS_WAITING_TO_RETRY = 12;
    private final long mId;
    private final DownloadInfo mInfo;
    private final DownloadInfoDelta mInfoDelta;
    private final DownloadNotifier mNotifier;
    private volatile boolean mShutdownRequested;
    private boolean mMadeProgress;
    private long mSpeedSampleStart;
    private long mSpeedSampleBytes;
    private long mSpeed;
    private long mLastUpdateBytes;
    private long mLastUpdateTime;


    public DownloadThread(DownloadInfo info, DownloadNotifier notifier) {
        mNotifier = notifier;
        mId = info.mId;
        mInfo = info;
        mInfoDelta = new DownloadInfoDelta(info);
    }

    public static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    public static String getFileNameFromUri(String uri) {
        return substringBefore(substringAfterLast(uri, '/'), '?');
    }

    public static boolean isStatusRetryable(int status) {
        switch (status) {
            case STATUS_HTTP_DATA_ERROR:
            case HTTP_UNAVAILABLE:
            case HTTP_INTERNAL_ERROR:
            case STATUS_FILE_ERROR:
                return true;
            default:
                return false;
        }
    }

    public static void printResponseHeaders(HttpURLConnection connection) {
        Map<String, List<String>> map = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
        }
    }

    public void requestShutdown() {
        mShutdownRequested = true;
    }

    public static String substringAfterLast(String string, char delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(index + 1);
        return string;
    }

    public static String substringBefore(String string, char delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(0, index);
        return string;
    }

    private static long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void addRequestHeaders(HttpURLConnection conn, boolean resuming) {
        if (mInfo.getHeaders() != null) {
            for (Pair<String, String> header : mInfo.getHeaders()) {
                conn.addRequestProperty(header.first, header.second);
            }
        }
        // Only splice in user agent when not already defined
        if (conn.getRequestProperty("User-Agent") == null) {
            conn.addRequestProperty("User-Agent", mInfo.getUserAgent());
        }
        // Defeat transparent gzip compression, since it doesn't allow us to
        // easily resume partial downloads.
        conn.setRequestProperty("Accept-Encoding", "identity");
        // Defeat connection reuse, since otherwise servers may continue
        // streaming large downloads after cancelled.
        conn.setRequestProperty("Connection", "close");
        if (resuming) {
            if (mInfoDelta.mETag != null) {
                conn.addRequestProperty("If-Match", mInfoDelta.mETag);
            }
            conn.addRequestProperty("Range", "bytes=" + mInfoDelta.mCurrentBytes + "-");
        }
    }

    private void checkConnectivity() {
    }

    private void executeDownload() throws StopRequestException {
        final boolean resuming = mInfoDelta.mCurrentBytes != 0;
        URL url;
        try {
            url = new URL(mInfoDelta.mUri);
        } catch (MalformedURLException e) {
            throw new StopRequestException(STATUS_BAD_REQUEST, e);
        }
        int redirectionCount = 0;
        while (redirectionCount++ < MAX_REDIRECTS) {
            HttpURLConnection conn = null;
            try {

                checkConnectivity();
                if (mInfo.getProxy() != null) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(substringBefore(mInfo.getProxy(), ':'), Integer.parseInt(substringAfterLast(mInfo.getProxy(), ':'))));
                    conn = (HttpURLConnection) url.openConnection(proxy);
                } else {
                    conn = (HttpURLConnection) url.openConnection();
                }
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(DEFAULT_TIMEOUT);
                conn.setReadTimeout(DEFAULT_TIMEOUT);
                addRequestHeaders(conn, resuming);
                final int responseCode = conn.getResponseCode();
                printResponseHeaders(conn);
                switch (responseCode) {
                    case HTTP_OK:
                        if (resuming) {
                            throw new StopRequestException(
                                    STATUS_CANNOT_RESUME, "Expected partial, but received OK");
                        }
                        parseOkHeaders(conn);
                        transferData(conn);
                        return;
                    case HTTP_PARTIAL:
                        if (!resuming) {
                            throw new StopRequestException(
                                    STATUS_CANNOT_RESUME, "Expected OK, but received partial");
                        }
                        transferData(conn);
                        return;
                    case HTTP_MOVED_PERM:
                    case HTTP_MOVED_TEMP:
                    case HTTP_SEE_OTHER:
                    case HTTP_TEMP_REDIRECT:
                        final String location = conn.getHeaderField("Location");
                        url = new URL(url, location);
                        if (responseCode == HTTP_MOVED_PERM) {

                            mInfoDelta.mUri = url.toString();
                        }
                        continue;
                    case HTTP_PRECON_FAILED:
                        throw new StopRequestException(
                                STATUS_CANNOT_RESUME, "Precondition failed");
                    case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
                        throw new StopRequestException(
                                STATUS_CANNOT_RESUME, "Requested range not satisfiable");
                    case HTTP_UNAVAILABLE:
                        parseUnavailableHeaders(conn);
                        throw new StopRequestException(
                                HTTP_UNAVAILABLE, conn.getResponseMessage());
                    case HTTP_INTERNAL_ERROR:
                        throw new StopRequestException(
                                HTTP_INTERNAL_ERROR, conn.getResponseMessage());
                    default:
                        StopRequestException.throwUnhandledHttpError(
                                responseCode, conn.getResponseMessage());
                }
            } catch (IOException e) {
                if (e instanceof ProtocolException
                        && e.getMessage().startsWith("Unexpected status line")) {
                    throw new StopRequestException(STATUS_UNHANDLED_HTTP_CODE, e);
                } else {
                    throw new StopRequestException(STATUS_HTTP_DATA_ERROR, e);
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        throw new StopRequestException(STATUS_TOO_MANY_REDIRECTS, "Too many redirects");
    }

    private void parseOkHeaders(HttpURLConnection conn) throws StopRequestException {
        if (mInfoDelta.mFileName == null) {
//            final String contentDisposition = conn.getHeaderField("Content-Disposition");
//            final String contentLocation = conn.getHeaderField("Content-Location");
            try {
                mInfoDelta.mFileName = getFileNameFromUri(mInfoDelta.mUri);
                FileOutputStream outputStream = new FileOutputStream(mInfoDelta.mFileName);
                outputStream.close();
            } catch (IOException e) {
                throw new StopRequestException(
                        STATUS_FILE_ERROR, "Failed to generate filename: " + e);
            }
        }
        if (mInfoDelta.mMimeType == null) {

        }
        final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
        if (transferEncoding == null) {
            mInfoDelta.mTotalBytes = getHeaderFieldLong(conn, "Content-Length", -1);
        } else {
            mInfoDelta.mTotalBytes = -1;
        }
        mInfoDelta.mETag = conn.getHeaderField("ETag");
        mInfoDelta.writeToDatabaseOrThrow();
        // Check connectivity again now that we know the total size
        checkConnectivity();
    }

    private void parseUnavailableHeaders(HttpURLConnection conn) {
        int retryAfter = conn.getHeaderFieldInt("Retry-After", -1);
        retryAfter = constrain(retryAfter, MIN_RETRY_AFTER,
                MAX_RETRY_AFTER);
        mInfoDelta.mRetryAfter = (int) (retryAfter * SECOND_IN_MILLIS);
    }

    private void transferData(HttpURLConnection conn) throws StopRequestException {
        // To detect when we're really finished, we either need a length, closed
        // connection, or chunked encoding.
        final boolean hasLength = mInfoDelta.mTotalBytes != -1;
        final boolean isConnectionClose = "close".equalsIgnoreCase(
                conn.getHeaderField("Connection"));
        final boolean isEncodingChunked = "chunked".equalsIgnoreCase(
                conn.getHeaderField("Transfer-Encoding"));
        final boolean finishKnown = hasLength || isConnectionClose || isEncodingChunked;
        if (!finishKnown) {
            throw new StopRequestException(
                    STATUS_CANNOT_RESUME, "can't know size of download, giving up");
        }
        FileDescriptor outFd = null;
        InputStream in = null;
        RandomAccessFile out = null;
        try {
            try {
                in = conn.getInputStream();
            } catch (IOException e) {
                throw new StopRequestException(STATUS_HTTP_DATA_ERROR, e);
            }
            try {
                out = new RandomAccessFile(mInfoDelta.mFileName, "rw");

                outFd = out.getFD();
                out.seek(mInfoDelta.mCurrentBytes);
                // Move into place to begin writing
                //Os.lseek(outFd, mInfoDelta.mCurrentBytes, OsConstants.SEEK_SET);
            } catch (IOException e) {
                throw new StopRequestException(STATUS_FILE_ERROR, e);
            }
//            try {
//                // Pre-flight disk space requirements, when known
//                if (mInfoDelta.mTotalBytes > 0 && mStorage.isAllocationSupported(outFd)) {
//                    mStorage.allocateBytes(outFd, mInfoDelta.mTotalBytes);
//                }
//            } catch (IOException e) {
//                throw new StopRequestException(STATUS_INSUFFICIENT_SPACE_ERROR, e);
//            }
            // Start streaming data, periodically watch for pause/cancel
            // commands and checking disk space as needed.
            transferData(in, out, outFd);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ignored) {
            }
            try {
                if (outFd != null) outFd.sync();
            } catch (IOException ignored) {
            } finally {
                try {
                    out.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    private void transferData(InputStream in, RandomAccessFile out, FileDescriptor outFd) throws StopRequestException {
        final byte buffer[] = new byte[BUFFER_SIZE];
        while (true) {
            if (mShutdownRequested) {
                throw new StopRequestException(STATUS_HTTP_DATA_ERROR,
                        "Local halt requested; job probably timed out");
            }
            int len = -1;
            try {
                len = in.read(buffer);
            } catch (IOException e) {
                throw new StopRequestException(
                        STATUS_HTTP_DATA_ERROR, "Failed reading response: " + e, e);
            }
            if (len == -1) {
                break;
            }
            try {
                out.write(buffer, 0, len);
                mMadeProgress = true;
                mInfoDelta.mCurrentBytes += len;
                updateProgress(outFd);
            } catch (IOException e) {
                throw new StopRequestException(STATUS_FILE_ERROR, e);
            }
        }
        // Finished without error; verify length if known
        if (mInfoDelta.mTotalBytes != -1 && mInfoDelta.mCurrentBytes != mInfoDelta.mTotalBytes) {
            throw new StopRequestException(STATUS_HTTP_DATA_ERROR, "Content length mismatch; found "
                    + mInfoDelta.mCurrentBytes + " instead of " + mInfoDelta.mTotalBytes);
        }
    }

    private void updateProgress(FileDescriptor outFd) throws IOException, StopRequestException {
        final long now = System.currentTimeMillis();// SystemClock.elapsedRealtime();
        final long currentBytes = mInfoDelta.mCurrentBytes;
        final long sampleDelta = now - mSpeedSampleStart;
        if (sampleDelta > 500) {
            final long sampleSpeed = ((currentBytes - mSpeedSampleBytes) * 1000)
                    / sampleDelta;
            if (mSpeed == 0) {
                mSpeed = sampleSpeed;
            } else {
                mSpeed = ((mSpeed * 3) + sampleSpeed) / 4;
            }
            // Only notify once we have a full sample window
            if (mSpeedSampleStart != 0) {
                mNotifier.notifyDownloadSpeed(mId, mSpeed);
            }
            mSpeedSampleStart = now;
            mSpeedSampleBytes = currentBytes;
        }
        final long bytesDelta = currentBytes - mLastUpdateBytes;
        final long timeDelta = now - mLastUpdateTime;
        if (bytesDelta > MIN_PROGRESS_STEP && timeDelta > MIN_PROGRESS_TIME) {
            // fsync() to ensure that current progress has been flushed to disk,
            // so we can always resume based on latest database information.
            outFd.sync();
            mInfoDelta.writeToDatabaseOrThrow();
            mLastUpdateBytes = currentBytes;
            mLastUpdateTime = now;
        }
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        try {
            mInfoDelta.mStatus = STATUS_RUNNING;
            mInfoDelta.writeToDatabase();
            executeDownload();
            mInfoDelta.mStatus = STATUS_SUCCESS;
            if (mInfoDelta.mTotalBytes == -1) {
                mInfoDelta.mTotalBytes = mInfoDelta.mCurrentBytes;
            }
        } catch (StopRequestException e) {
            mInfoDelta.mStatus = e.getFinalStatus();
            mInfoDelta.mErrorMsg = e.getMessage();
            if (mInfoDelta.mStatus == STATUS_WAITING_TO_RETRY) {
                throw new IllegalStateException("Execution should always throw final error codes");
            }

            if (isStatusRetryable(mInfoDelta.mStatus)) {
                if (mMadeProgress) {
                    mInfoDelta.mNumFailed = 1;
                } else {
                    mInfoDelta.mNumFailed += 1;
                }
                if (mInfoDelta.mNumFailed < MAX_RETRIES) {
//                    final NetworkInfo info = mSystemFacade.getNetworkInfo(mNetwork, mInfo.mUid,
//                            mIgnoreBlocked);
//                    if (info != null && info.getType() == mNetworkType && info.isConnected()) {
//
//                        mInfoDelta.mStatus = STATUS_WAITING_TO_RETRY;
//                    } else {
//
//                        mInfoDelta.mStatus = STATUS_WAITING_FOR_NETWORK;
//                    }
                    if ((mInfoDelta.mETag == null && mMadeProgress)) {
                        mInfoDelta.mStatus = STATUS_CANNOT_RESUME;
                    }
                }
            }


            if (mInfoDelta.mStatus == STATUS_WAITING_FOR_NETWORK
                    && !mInfo.isMeteredAllowed(mInfoDelta.mTotalBytes)) {
                mInfoDelta.mStatus = STATUS_QUEUED_FOR_WIFI;
            }
        } catch (Throwable t) {
            mInfoDelta.mStatus = STATUS_UNKNOWN_ERROR;
            mInfoDelta.mErrorMsg = t.toString();
        } finally {
            mNotifier.notifyDownloadSpeed(mId, 0);

            mInfoDelta.writeToDatabase();
        }

        boolean needsReschedule = false;
        if (mInfoDelta.mStatus == STATUS_WAITING_TO_RETRY
                || mInfoDelta.mStatus == STATUS_WAITING_FOR_NETWORK
                || mInfoDelta.mStatus == STATUS_QUEUED_FOR_WIFI) {
            needsReschedule = true;
        }
    }

    interface DownloadNotifier {

        void notifyDownloadSpeed(long id, long speed);
    }

    public static class DownloadInfo {

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

    public static class StopRequestException extends Exception {
        private final int mFinalStatus;

        public StopRequestException(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        public StopRequestException(int finalStatus, Throwable t) {
            this(finalStatus, t.getMessage());
            initCause(t);
        }

        public StopRequestException(int finalStatus, String message, Throwable t) {
            this(finalStatus, message);
            initCause(t);
        }

        public int getFinalStatus() {
            return mFinalStatus;
        }

        public static StopRequestException throwUnhandledHttpError(int code, String message)
                throws StopRequestException {
            final String error = "Unhandled HTTP response: " + code + " " + message;
            if (code >= 400 && code < 600) {
                throw new StopRequestException(code, error);
            } else if (code >= 300 && code < 400) {
                throw new StopRequestException(STATUS_UNHANDLED_REDIRECT, error);
            } else {
                throw new StopRequestException(STATUS_UNHANDLED_HTTP_CODE, error);
            }
        }
    }

    public static class Pair<F, S> {
        public final F first = null;
        public final S second = null;

        public Pair(F first, S second) {
            throw new RuntimeException("Stub!");
        }

        public static <A, B> Pair<A, B> create(A a, B b) {
            throw new RuntimeException("Stub!");
        }

        public boolean equals(Object o) {
            throw new RuntimeException("Stub!");
        }

        public int hashCode() {
            throw new RuntimeException("Stub!");
        }

        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    private class DownloadInfoDelta {

        public int mStatus;
        public long mTotalBytes;
        public long mCurrentBytes;
        public String mUri;
        public String mETag;
        public String mFileName;
        public String mMimeType;
        public int mRetryAfter;
        public String mErrorMsg;
        public int mNumFailed;

        public DownloadInfoDelta(DownloadInfo info) {

            mStatus = info.mStatus;
            mTotalBytes = info.mTotalBytes;
            mCurrentBytes = info.mCurrentBytes;
            mUri = info.mUri;
            mETag = info.mETag;
            mFileName = info.mFileName;
            mMimeType = info.mMimeType;
            mRetryAfter = info.mRetryAfter;

        }


        public void writeToDatabase() {
        }

        public void writeToDatabaseOrThrow() {

        }
    }


}
