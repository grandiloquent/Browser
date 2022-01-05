package euphoria.psycho.browser.file;


import android.util.LruCache;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.share.Log;

public class FileProviderImpl implements FileProvider {
    private BrowsingFileObserver mObserver;
    private List<FileItem> mItems = new ArrayList<>();
    private List<FileItem> mRemovalItems;
    private LruCache<String, FileItem> mLruCache = new LruCache<>(1024 * 10);

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

    public static List<File> getFilesRecursively(File dir) {
        List<File> ls = new ArrayList<File>();
        if (dir.isDirectory())
            for (File fObj : dir.listFiles()) {
                if (fObj.isDirectory()) {
                    ls.add(fObj);
                    ls.addAll(getFilesRecursively(fObj));
                } else {
                    ls.add(fObj);
                }
            }
        else
            ls.add(dir);
        return ls;
    }

    @Override
    public void queryFile(String directory, FileManager fileManager) {
        mItems.clear();
        Log.e("B5aOx2", String.format("queryFile, %s", ""));
        File dir = new File(directory);
        if (dir.isDirectory()) {
            File[] files = null;
            if (fileManager.isSearchIn()) {
                files = getFilesRecursively(dir).toArray(new File[0]);
                fileManager.setSearchIn(false);
            } else {
                files = fileManager.getShowHidden() ? dir.listFiles() : dir.listFiles((file, s) -> !s.startsWith("."));
            }
            if (files != null) {
                String searchText = fileManager.getSearchText();
                for (File file : files) {
                    if (searchText != null) {
                        if (!file.getName().contains(searchText)) continue;
                    }
                    FileItem fileItem = mLruCache.get(file.getPath());
                    if (fileItem == null) {
                        fileItem = new FileItem(file.getName(),
                                file.getAbsolutePath(),
                                file.lastModified(), FileHelper.getFileType(file), FileHelper.getFileSize(file, fileManager.getShowHidden(), fileManager.getSortType() == 8));
                        mLruCache.put(file.getPath(), fileItem);
                    }
                    mItems.add(fileItem);
                }
            } else {
                Log.e("TAG", directory);
            }
        } else {
            Log.e("TAG", "directory is null");
        }
        Collections.sort(mItems, new FileItemComparator(fileManager.getSortType()));
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