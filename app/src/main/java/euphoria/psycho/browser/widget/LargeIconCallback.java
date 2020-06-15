package euphoria.psycho.browser.widget;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.IconCompat.IconType;

/**
 * Callback for use with GetLargeIconForUrl().
 */
public interface LargeIconCallback {
    /**
     * Called when the icon or fallback color is available.
     *
     * @param icon The icon, or null if none is available.
     * @param fallbackColor The fallback color to use if icon is null.
     * @param isFallbackColorDefault Whether the fallback color is the default color.
     * @param iconType The type of the icon contributing to this event as defined in {@link
     * IconType}.
     */
    void onLargeIconAvailable(@Nullable Bitmap icon, int fallbackColor,
                              boolean isFallbackColorDefault, @IconType int iconType);
}
