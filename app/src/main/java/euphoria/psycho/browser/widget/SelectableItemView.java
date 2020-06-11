package euphoria.psycho.browser.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.ApiCompatibilityUtils;

/**
 * Default implementation of SelectableItemViewBase. Contains a start icon, title, description, and
 * optional end icon (GONE by default). Views may be accessed through protected member variables.
 *
 * @param <E> The type of the item associated with this SelectableItemViewBase.
 */
public abstract class SelectableItemView<E> extends SelectableItemViewBase<E> {
    protected final AnimatedVectorDrawableCompat mCheckDrawable;
    protected final int mDefaultLevel;
    protected final int mSelectedLevel;
    /**
     * The LinearLayout containing the rest of the views for the selectable item.
     */
    protected LinearLayout mContentView;

    /**
     * An icon displayed at the start of the item row.
     */
    protected ImageView mStartIconView;

    /**
     * An optional button displayed at the end of the item row, GONE by default.
     */
    protected AppCompatImageButton mEndButtonView;

    /**
     * A title line displayed between the start and (optional) end icon.
     */
    protected TextView mTitleView;

    /**
     * A description line displayed below the title line.
     */
    protected TextView mDescriptionView;

    /**
     * The color state list for the start icon view when the item is selected.
     */
    protected ColorStateList mStartIconSelectedColorList;

    private Drawable mStartIconDrawable;

    /**
     * Constructor for inflating from XML.
     */
    public SelectableItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mStartIconSelectedColorList = AppCompatResources.getColorStateList(
                getContext(), R.color.default_icon_color_inverse);
        mDefaultLevel = getResources().getInteger(R.integer.list_item_level_default);
        mSelectedLevel = getResources().getInteger(R.integer.list_item_level_selected);
        mCheckDrawable = AnimatedVectorDrawableCompat.create(
                getContext(), R.drawable.ic_check_googblue_24dp_animated);
    }

    /**
     * Sets the icon for the image view: the default icon if unselected, the check mark if selected.
     *
     * @param imageView     The image view in which the icon will be presented.
     * @param defaultIcon   The default icon that will be displayed if not selected.
     * @param isSelected    Whether the item is selected or not.
     */
    public static void applyModernIconStyle(
            ImageView imageView, Drawable defaultIcon, boolean isSelected) {
        imageView.setBackgroundResource(R.drawable.list_item_icon_modern_bg);
        imageView.setImageDrawable(
                isSelected ? TintedDrawable.constructTintedDrawable(imageView.getContext(),
                        R.drawable.ic_check_googblue_24dp, R.color.default_icon_color_inverse)
                        : defaultIcon);
        imageView.getBackground().setLevel(isSelected
                ? imageView.getResources().getInteger(R.integer.list_item_level_selected)
                : imageView.getResources().getInteger(R.integer.list_item_level_default));
    }

    @VisibleForTesting
    public void endAnimationsForTests() {
        mCheckDrawable.stop();
    }

    /**
     * @return The {@link ColorStateList} used to tint the start icon drawable set via
     *         {@link #setStartIconDrawable(Drawable)} when the item is not selected.
     */
    protected @Nullable
    ColorStateList getDefaultStartIconTint() {
        return null;
    }

    /**
     * Returns the drawable set for the start icon view, if any.
     */
    protected Drawable getStartIconDrawable() {
        return mStartIconDrawable;
    }

    /**
     * Set drawable for the start icon view. Note that you may need to use this method instead of
     * mIconView#setImageDrawable to ensure icon view is correctly set in selection mode.
     */
    protected void setStartIconDrawable(Drawable iconDrawable) {
        mStartIconDrawable = iconDrawable;
        updateView(false);
    }

    // FrameLayout implementations.
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.modern_list_item_view, this);

        mContentView = findViewById(R.id.content);
        mStartIconView = findViewById(R.id.start_icon);
        mEndButtonView = findViewById(R.id.end_button);
        mTitleView = findViewById(R.id.title);
        mDescriptionView = findViewById(R.id.description);

        if (mStartIconView != null) {
            mStartIconView.setBackgroundResource(R.drawable.list_item_icon_modern_bg);
            ApiCompatibilityUtils.setImageTintList(mStartIconView, getDefaultStartIconTint());
        }
    }

    /**
     * Update start icon image and background based on whether this item is selected.
     */
    @Override
    protected void updateView(boolean animate) {
        // TODO(huayinz): Refactor this method so that mIconView is not exposed to subclass.
        if (mStartIconView == null) return;

        if (isChecked()) {
            mStartIconView.getBackground().setLevel(mSelectedLevel);
            mStartIconView.setImageDrawable(mCheckDrawable);
            ApiCompatibilityUtils.setImageTintList(mStartIconView, mStartIconSelectedColorList);
            if (animate) mCheckDrawable.start();
        } else {
            mStartIconView.getBackground().setLevel(mDefaultLevel);
            mStartIconView.setImageDrawable(mStartIconDrawable);
            ApiCompatibilityUtils.setImageTintList(mStartIconView, getDefaultStartIconTint());
        }
    }
}