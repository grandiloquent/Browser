package euphoria.psycho.browser.file;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import euphoria.psycho.browser.base.Share;

public class FileItemComparator implements Comparator<FileItem> {
    private final int mSortType;
    private final Collator mCollator;
    private final boolean mIsAscending;

    public FileItemComparator(int sortDirection, int sortType) {
        mIsAscending = sortDirection == FileHelper.SORT_BY_ASCENDING;
        mSortType = sortType;
        mCollator = Collator.getInstance(Locale.CHINA);
    }

    @Override
    public int compare(FileItem o1, FileItem o2) {
        if ((o1.getType() == o2.getType()) || (
                o1.getType() != FileHelper.TYPE_FOLDER && o2.getType() != FileHelper.TYPE_FOLDER
        )) {
            switch (mSortType) {
                case FileHelper.SORT_BY_SIZE:
                    long dif = o1.getSize() - o2.getSize();
                    int ret = 0;
                    if (dif > 0) ret = 1;
                    if (dif < 0) ret = -1;
                    return ret * (mIsAscending ? 1 : -1);
                case FileHelper.SORT_BY_DATA_MODIFIED:
                    dif = o1.getTimestamp() - o2.getTimestamp();
                    ret = 0;
                    if (dif > 0) ret = 1;
                    if (dif < 0) ret = -1;
                    return ret * (mIsAscending ? 1 : -1);
                case FileHelper.SORT_BY_TYPE:
                    if (o1.getType() == FileHelper.TYPE_FOLDER) {
                        return mCollator.compare(o1.getTitle(), o2.getTitle()) * (mIsAscending ? 1 : -1);
                    } else {
                        return mCollator.compare(Share.substringAfterLast(o1.getTitle(), '.'), Share.substringAfterLast(o2.getTitle(), '.')) * (mIsAscending ? 1 : -1);
                    }
                default:
                    return mCollator.compare(o1.getTitle(), o2.getTitle()) * (mIsAscending ? 1 : -1);

            }
        } else if (o1.getType() == FileHelper.TYPE_FOLDER) {
            return mIsAscending ? -1 :1;
        }
        return mIsAscending ? 1 : -1;
    }
}
