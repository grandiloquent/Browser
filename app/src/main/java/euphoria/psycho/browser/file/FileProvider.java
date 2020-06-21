package euphoria.psycho.browser.file;

import java.util.List;

public interface FileProvider {

    void destroy();

    void markItemForRemoval(FileItem i);

    void queryFile(String directory, int sortType, int sortDirection);

    void removeItems();

    void setObserver(BrowsingFileObserver observer);

    public interface BrowsingFileObserver {
        void onFileDeleted();

        void onQueryFileComplete(List<FileItem> items);
    }
}
