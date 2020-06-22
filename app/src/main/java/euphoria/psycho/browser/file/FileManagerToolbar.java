package euphoria.psycho.browser.file;
import android.content.Context;
import android.util.AttributeSet;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.widget.SelectableListToolbar;
public class FileManagerToolbar extends SelectableListToolbar<FileItem> {
    /**
     * Constructor for inflating from XML.
     *
     * @param context
     * @param attrs
     */
    public FileManagerToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateMenu(R.menu.file_manager_menu);
        updateMenuItemVisibility();
    }
    public void setManager(FileManager fileManager) {
    }
    private void updateMenuItemVisibility() {
    }
}