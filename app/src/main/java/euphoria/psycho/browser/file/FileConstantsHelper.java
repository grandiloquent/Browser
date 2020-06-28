package euphoria.psycho.browser.file;

import java.util.regex.Pattern;

public class FileConstantsHelper {
    public static final int SORT_BY_ASCENDING = 1 << 5;
    public static final int SORT_BY_DATA_MODIFIED = 1 << 1;
    public static final int SORT_BY_NAME = 1 << 2;
    public static final int SORT_BY_SIZE = 1 << 3;
    public static final int SORT_BY_TYPE = 1 << 4;
    // ["name","size","type","data_modified"]
    public static final int SORT_TYPE_DEFAULT = 33;
    public static final int TYPE_APK = 0;
    public static final int TYPE_EXCEL = 1;
    public static final int TYPE_FOLDER = 2;
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_MUSIC = 4;
    public static final int TYPE_OTHERS = 5;
    public static final int TYPE_PDF = 6;
    public static final int TYPE_PPS = 7;
    public static final int TYPE_TEXT = 8;
    public static final int TYPE_VCF = 9;
    public static final int TYPE_VIDEO = 10;
    public static final int TYPE_WORD = 11;
    public static final int TYPE_ZIP = 12;
    public static boolean sIsHasSD;
    public static Pattern sMusicPattern = Pattern.compile("\\.(?:mp3|m4a|aac|flac|gsm|mid|xmf|mxmf|rtttl|rtx|ota|imy|wav|ogg)$", Pattern.CASE_INSENSITIVE);
    public static String sSDPath;
    public static Pattern sVideoPattern = Pattern.compile("\\.(?:mp4|3gp|webm|ts|mkv)$", Pattern.CASE_INSENSITIVE);

}
