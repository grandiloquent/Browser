package euphoria.psycho.browser.downlaod;

import static euphoria.psycho.browser.downlaod.DownloadHelper.STATUS_UNHANDLED_HTTP_CODE;
import static euphoria.psycho.browser.downlaod.DownloadHelper.STATUS_UNHANDLED_REDIRECT;

public class StopRequestException extends Exception {
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
