package euphoria.psycho.browser.file;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class FileItemComparator implements Comparator<FileItem> {
    private final boolean mIsAscending;
    private final int mSortType;
    private final Collator mCollator;

    public FileItemComparator(boolean isAscending, int sortType) {
        mIsAscending = isAscending;
        mSortType = sortType;
        mCollator = Collator.getInstance(Locale.CHINA);
    }

    @Override
    public int compare(FileItem o1, FileItem o2) {
        if ((o1.getType() == o2.getType()) || (
                o1.getType() != FileHelper.TYPE_DIRECTORY && o2.getType() != FileHelper.TYPE_DIRECTORY
        )) {
            return mCollator.compare(o1.getTitle(), o2.getTitle());
        } else if (o1.getType() == FileHelper.TYPE_DIRECTORY) {
            return -1;
        }
        return 1;
    }
}
