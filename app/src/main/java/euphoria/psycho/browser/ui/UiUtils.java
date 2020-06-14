package euphoria.psycho.browser.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import android.widget.ListAdapter;

public class UiUtils {
    /**
     * Iterates through all items in the specified ListAdapter (including header and footer views)
     * and returns the width of the widest item (when laid out with height and width set to
     * WRAP_CONTENT).
     *
     * WARNING: do not call this on a ListAdapter with more than a handful of items, the performance
     * will be terrible since it measures every single item.
     *
     * @param adapter The ListAdapter whose widest item's width will be returned.
     * @param parentView The parent view.
     * @return The measured width (in pixels) of the widest item in the passed-in ListAdapter.
     */
    public static int computeMaxWidthOfListAdapterItems(ListAdapter adapter, ViewGroup parentView) {
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);

        int maxWidth = 0;
        View[] itemViews = new View[adapter.getViewTypeCount()];
        for (int i = 0; i < adapter.getCount(); ++i) {
            View itemView;
            int type = adapter.getItemViewType(i);
            if (type < 0) {
                // Type is negative for header/footer views, or views the adapter does not want
                // recycled.
                itemView = adapter.getView(i, null, parentView);
            } else {
                itemViews[type] = adapter.getView(i, itemViews[type], parentView);
                itemView = itemViews[type];
            }

            itemView.setLayoutParams(params);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            maxWidth = Math.max(maxWidth, itemView.getMeasuredWidth());
        }

        return maxWidth;
    }

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
