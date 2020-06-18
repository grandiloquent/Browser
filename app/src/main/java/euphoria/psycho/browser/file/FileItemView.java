package euphoria.psycho.browser.file;

import android.content.Context;
import android.graphics.Bitmap;


import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.widget.BasicListMenu;
import euphoria.psycho.browser.widget.LargeIconCallback;
import euphoria.psycho.browser.widget.ListMenu;
import euphoria.psycho.browser.widget.ListMenuButton;
import euphoria.psycho.browser.widget.ListMenuButtonDelegate;
import euphoria.psycho.browser.widget.MVCListAdapter.ModelList;
import euphoria.psycho.browser.widget.SelectableItemView;

public class FileItemView extends SelectableItemView<FileItem> implements LargeIconCallback {
    protected ListMenuButton mMoreIcon;

    public FileItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFileManager(FileManager fileManager) {
    }

    private ModelList getItems() {
        // Rebuild listItems, cause mLocation may be changed anytime.
        boolean canMove = false;
//        if (mDelegate != null && mDelegate.getModel() != null) {
//            BookmarkItem bookmarkItem = mDelegate.getModel().getBookmarkById(mBookmarkId);
//            if (bookmarkItem != null) canMove = bookmarkItem.isMovable();
//        }
        ModelList listItems = new ModelList();
//        listItems.add(buildMenuListItem(R.string.bookmark_item_select, 0, 0));
//        listItems.add(buildMenuListItem(R.string.bookmark_item_edit, 0, 0));
//        listItems.add(buildMenuListItem(R.string.bookmark_item_move, 0, 0, canMove));
//        listItems.add(buildMenuListItem(R.string.bookmark_item_delete, 0, 0));
//
//        if (mDelegate.getCurrentState() == BookmarkUIState.STATE_SEARCHING) {
//            listItems.add(buildMenuListItem(R.string.bookmark_show_in_folder, 0, 0));
//        } else if (mDelegate.getCurrentState() == BookmarkUIState.STATE_FOLDER
//                && mLocation != Location.SOLO && canMove) {
//            // Only add move up / move down buttons if there is more than 1 item
//            if (mLocation != Location.TOP) {
//                listItems.add(buildMenuListItem(R.string.menu_item_move_up, 0, 0));
//            }
//            if (mLocation != Location.BOTTOM) {
//                listItems.add(buildMenuListItem(R.string.menu_item_move_down, 0, 0));
//            }
//        }

        return listItems;
    }

    private ListMenu getListMenu() {
        ModelList listItems = getItems();
        ListMenu.Delegate delegate = item -> {

        };
        return new BasicListMenu(getContext(), listItems, delegate);
    }

    private ListMenuButtonDelegate getListMenuButtonDelegate() {
        return this::getListMenu;
    }

    @Override
    protected void onClick() {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.list_menu_button, mContentView);
        mMoreIcon = findViewById(R.id.more);
        mMoreIcon.setDelegate(getListMenuButtonDelegate());


    }

    @Override
    public void onLargeIconAvailable(@Nullable Bitmap icon, int fallbackColor, boolean isFallbackColorDefault, int iconType) {
//        Drawable iconDrawable = FaviconUtils.getIconDrawableWithoutFilter(
//                icon, mUrl, fallbackColor, mIconGenerator, getResources(), mDisplayedIconSize);
//        setStartIconDrawable(iconDrawable);
    }

    @Override
    public void setItem(FileItem item) {
        if (getItem() == item) return;
        super.setItem(item);
        mTitleView.setText(item.getTitle());

        switch (item.getType()) {
            case FileHelper.TYPE_DIRECTORY:
                setStartIconDrawable(FileHelper.sDirectoryDrawable);
                break;
            case FileHelper.TYPE_FILE_AUDIO:
                setStartIconDrawable(FileHelper.sAudioDrawable);
                break;
            case FileHelper.TYPE_FILE_TEXT:
                setStartIconDrawable(FileHelper.sTextDrawable);
                break;
            case FileHelper.TYPE_FILE_UNKNOWN:
                setStartIconDrawable(FileHelper.sOthersDrawable);
                break;
        }
    }
}
