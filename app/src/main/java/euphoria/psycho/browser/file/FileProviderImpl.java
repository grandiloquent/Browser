package euphoria.psycho.browser.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileProviderImpl implements FileProvider {
    private BrowsingFileObserver mObserver;
    private List<FileItem> mItems = new ArrayList<>();

    @Override
    public void destroy() {

    }

    @Override
    public void queryFile(String directory) {

        mItems.clear();

        File dir = new File(directory);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {


                FileItem fileItem = new FileItem(file.getName(),
                        file.getAbsolutePath(),
                        file.lastModified(), FileHelper.getFileType(file));
                mItems.add(fileItem);
            }
        }

        if (mObserver != null)
            mObserver.onQueryFileComplete(mItems);
    }

    @Override
    public void removeItems() {

    }

    @Override
    public void setObserver(BrowsingFileObserver observer) {
        mObserver = observer;

    }
}
