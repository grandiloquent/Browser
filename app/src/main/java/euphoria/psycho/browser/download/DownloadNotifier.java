package euphoria.psycho.browser.download;

interface DownloadNotifier {
    void notifyDownloadSpeed(long id, long speed);
}