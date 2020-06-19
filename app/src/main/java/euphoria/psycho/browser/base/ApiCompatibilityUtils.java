package euphoria.psycho.browser.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.UnsupportedEncodingException;

import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;

public class ApiCompatibilityUtils {
    /**
     * {@link String#getBytes()} but specifying UTF-8 as the encoding and capturing the resulting
     * UnsupportedEncodingException.
     */
    public static byte[] getBytesUtf8(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public static int getColor(Resources res, int id) throws NotFoundException {
        return res.getColor(id);
    }

    public static void setImageTintList(ImageView view, @Nullable ColorStateList tintList) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            // Work around broken workaround in ImageViewCompat, see
            // https://crbug.com/891609#c3.
            if (tintList != null && view.getImageTintMode() == null) {
                view.setImageTintMode(PorterDuff.Mode.SRC_IN);
            }
        }
        ImageViewCompat.setImageTintList(view, tintList);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            // Work around that the tint list is not cleared when setting tint list to null on L
            // in some cases. See https://crbug.com/983686.
            if (tintList == null) view.refreshDrawableState();
        }
    }

    /**
     * @see android.view.Window#setStatusBarColor(int color).
     */
    public static void setStatusBarColor(Window window, int statusBarColor) {
        // If both system bars are black, we can remove these from our layout,
        // removing or shrinking the SurfaceFlinger overlay required for our views.
        // This benefits battery usage on L and M.  However, this no longer provides a battery
        // benefit as of N and starts to cause flicker bugs on O, so don't bother on O and up.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && statusBarColor == Color.BLACK
                && window.getNavigationBarColor() == Color.BLACK) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        window.setStatusBarColor(statusBarColor);
    }

    public static Drawable getDrawable(Resources res, int id) throws NotFoundException {
        return getDrawableForDensity(res, id, 0);
    }

    public static Drawable getDrawableForDensity(Resources res, int id, int density) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (density == 0) {
                    return res.getDrawable(id, null);
                }
                return res.getDrawableForDensity(id, density, null);
            } else if (density == 0) {
                // On newer OS versions, this check is done within getDrawableForDensity().
                return res.getDrawable(id);
            }
            return res.getDrawableForDensity(id, density);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public static void setStatusBarIconColor(View rootView, boolean useDarkIcons) {
        int systemUiVisibility = rootView.getSystemUiVisibility();
        if (useDarkIcons) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        rootView.setSystemUiVisibility(systemUiVisibility);
    }

    public static void setTaskDescription(Activity activity, String title, Bitmap icon, int color) {
        ActivityManager.TaskDescription description =
                new ActivityManager.TaskDescription(title, icon, color);
        activity.setTaskDescription(description);
    }

}
