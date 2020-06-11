package euphoria.psycho.browser.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

public class UiUtils {
    /**
     * Gets a drawable from the resources and applies the specified tint to it. Uses Support Library
     * for vector drawables and tinting on older Android versions.
     *
     * @param drawableId  The resource id for the drawable.
     * @param tintColorId The resource id for the color or ColorStateList.
     */
    public static Drawable getTintedDrawable(
            Context context, @DrawableRes int drawableId, @ColorRes int tintColorId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
        assert drawable != null;
        drawable = DrawableCompat.wrap(drawable).mutate();
        DrawableCompat.setTintList(
                drawable, AppCompatResources.getColorStateList(context, tintColorId));
        return drawable;
    }

}
