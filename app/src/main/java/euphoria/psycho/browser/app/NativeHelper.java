package euphoria.psycho.browser.app;

public class NativeHelper {
    static {
        System.loadLibrary("native-lib");
    }

    public static native void startServer(String host, String port, String rootDirectory, String videoDirectory, String sdcardDirectory);

    public static native String youdao(String query, boolean isEnglishToChinese, boolean isTranslate);

    public static native String google(String query, boolean isEnglishToChinese);

    public static native boolean deleteFileSystem(String path);


    public static native long dirSize(String path);

    public static native boolean copyFile(String source, String target);

    public static native void createZipFromDirectory(String dir, String filename);

    public static native void extractToDirectory(String filename, String directory);

}