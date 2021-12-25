package euphoria.psycho.browser.file;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class FileItemComparator implements Comparator<FileItem> {
    private final int mSortType;
    private final Collator mCollator;
    private final boolean mIsAscending;

    public FileItemComparator(int sortType) {
        mIsAscending = (sortType & FileConstantsHelper.SORT_BY_ASCENDING) == FileConstantsHelper.SORT_BY_ASCENDING;
        mSortType = sortType;
        mCollator = Collator.getInstance(Locale.CHINA);
    }

    @Override
    public int compare(FileItem o1, FileItem o2) {
        if ((o1.getType() == o2.getType()) || (
                o1.getType() != FileConstantsHelper.TYPE_FOLDER && o2.getType() != FileConstantsHelper.TYPE_FOLDER
        )) {
            if ((mSortType & FileConstantsHelper.SORT_BY_SIZE) == FileConstantsHelper.SORT_BY_SIZE) {
                long dif = o1.getSize() - o2.getSize();
                int ret = 0;
                if (dif > 0) ret = 1;
                if (dif < 0) ret = -1;
                return ret * (mIsAscending ? 1 : -1);
            } else if ((mSortType & FileConstantsHelper.SORT_BY_DATA_MODIFIED) == FileConstantsHelper.SORT_BY_DATA_MODIFIED) {

                long dif = o1.getTimestamp() - o2.getTimestamp();
                int ret = 0;
                if (dif > 0) ret = 1;
                if (dif < 0) ret = -1;
                return ret * (mIsAscending ? 1 : -1);
            } else if ((mSortType & FileConstantsHelper.SORT_BY_TYPE) == FileConstantsHelper.SORT_BY_TYPE) {

                if (o1.getType() == FileConstantsHelper.TYPE_FOLDER) {
                    return mCollator.compare(o1.getTitle(), o2.getTitle()) * (mIsAscending ? 1 : -1);
                } else {
                    return mCollator.compare(Shared.substringAfterLast(o1.getTitle(), '.'), Shared.substringAfterLast(o2.getTitle(), '.')) * (mIsAscending ? 1 : -1);
                }
            } else {
                return mCollator.compare(o1.getTitle(), o2.getTitle()) * (mIsAscending ? 1 : -1);
            }
        } else if (o1.getType() == FileConstantsHelper.TYPE_FOLDER) {
            return mIsAscending ? -1 : 1;
        }
        return mIsAscending ? 1 : -1;
    }
}