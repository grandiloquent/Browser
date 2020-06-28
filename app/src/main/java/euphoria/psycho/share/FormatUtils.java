package euphoria.psycho.share;

import android.content.Context;

public class FormatUtils {
    // Returns a (localized) string for the given duration (in seconds).
    public static String formatDuration(final Context context, int duration) {
        int h = duration / 3600;
        int m = (duration - h * 3600) / 60;
        int s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format("%02d:%02d", m, s);
        } else {
            durationValue = String.format("%02d:%02d:%02d", h, m, s);
        }
        return durationValue;
    }

    public static String formatFileSize(long number) {
        float result = number;
        String suffix = "";
        if (result > 900) {
            suffix = " KB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " MB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " GB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " TB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " PB";
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.1f", result);
        } else if (result < 100) {
            value = String.format("%.0f", result);
        } else {
            value = String.format("%.0f", result);
        }
        return value + suffix;
    }
}
