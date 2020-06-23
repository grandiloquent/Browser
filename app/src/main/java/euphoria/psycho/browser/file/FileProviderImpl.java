package euphoria.psycho.browser.file;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import euphoria.psycho.browser.app.NativeHelper;
public class FileProviderImpl implements FileProvider {
    private BrowsingFileObserver mObserver;
    private List<FileItem> mItems = new ArrayList<>();
    private List<FileItem> mRemovalItems;
    @Override
    public void destroy() {
    }
    @Override
    public void markItemForRemoval(FileItem i) {
        if (mRemovalItems == null) {
            mRemovalItems = new ArrayList<>();
        }
        mRemovalItems.add(i);
    }
    @Override
    public void queryFile(String directory, FileManager fileManager) {
        mItems.clear();
        File dir = new File(directory);
        if (dir.isDirectory()) {
            File[] files = fileManager.getShowHidden() ? dir.listFiles() : dir.listFiles((file, s) -> !s.startsWith("."));
            if (files != null) {
                for (File file : files) {
                    FileItem fileItem = new FileItem(file.getName(),
                            file.getAbsolutePath(),
                            file.lastModified(), FileHelper.getFileType(file), FileHelper.getFileSize(file));
                    mItems.add(fileItem);
                }
            }
        }
        Collections.sort(mItems, new FileItemComparator(fileManager.getSortDirection(), fileManager.getSortType()));
        if (mObserver != null)
            mObserver.onQueryFileComplete(mItems);
    }
    @Override
    public void removeItems() {
        for (FileItem i : mRemovalItems) {
            NativeHelper.deleteFileSystem(i.getUrl());
        }
        mRemovalItems.clear();
        mRemovalItems = null;
        if (mObserver != null) mObserver.onFileDeleted();
    }
    @Override
    public void setObserver(BrowsingFileObserver observer) {
        mObserver = observer;
    }
}