package euphoria.psycho.browser.download;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static euphoria.psycho.browser.file.Shared.substringAfterLast;
import static euphoria.psycho.browser.file.Shared.substringBefore;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

class DownloadHelper {

    public static final int MAX_RETRY_AFTER = 24 * 60 * 60;
    public static final int MIN_RETRY_AFTER = 30;
    static final int BUFFER_SIZE = 8192;
    static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    static final int HTTP_TEMP_REDIRECT = 307;
    static final int MAX_REDIRECTS = 5;
    static final int MAX_RETRIES = 5;
    static final int MIN_PROGRESS_STEP = 65536;
    static final long MIN_PROGRESS_TIME = 2000;
    static final int SECOND_IN_MILLIS = 1000;
    static final int DEFAULT_TIMEOUT = 20 * SECOND_IN_MILLIS;
    static final int STATUS_BAD_REQUEST = 0;
    static final int STATUS_CANNOT_RESUME = 1;
    static final int STATUS_FILE_ERROR = 2;
    static final int STATUS_HTTP_DATA_ERROR = 3;
    static final int STATUS_QUEUED_FOR_WIFI = 4;
    static final int STATUS_RUNNING = 5;
    static final int STATUS_SUCCESS = 6;
    static final int STATUS_TOO_MANY_REDIRECTS = 7;
    static final int STATUS_UNHANDLED_HTTP_CODE = 8;
    static final int STATUS_UNHANDLED_REDIRECT = 9;
    static final int STATUS_UNKNOWN_ERROR = 10;
    static final int STATUS_WAITING_FOR_NETWORK = 11;
    static final int STATUS_WAITING_TO_RETRY = 12;

    static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    static String getFileNameFromUri(String uri) {
        return substringBefore(substringAfterLast(uri, '/'), '?');
    }

    static long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    static boolean isStatusRetryable(int status) {
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

    static void printResponseHeaders(HttpURLConnection connection) {
        Map<String, List<String>> map = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
        }
    }

}
