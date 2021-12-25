package euphoria.psycho.browser.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import euphoria.psycho.browser.file.Shared;
import euphoria.psycho.share.Log;
import fi.iki.elonen.NanoHTTPD;

public class LocalFileService extends Service {
    private LocalServer mLocalServer;
    private String mPath;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalServer = new LocalServer();
        try {
            mLocalServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        mLocalServer.close();
        super.onDestroy();
    }

    private class LocalServer extends NanoHTTPD {
        public LocalServer() {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String params = session.getParms().get("v");

            if (params != null) {
                try {
                    mPath = params;
                    return NanoHTTPD.newChunkedResponse(
                            Response.Status.OK,
                            "text/html",
                            new FileInputStream(params)
                    );
                } catch (FileNotFoundException e) {
                    Log.e("TAG/", "[serve]: " + e.getMessage());
                }
            } else {
                String uri = Shared.substringBeforeLast(mPath, '/') + session.getUri();
                try {
                    return NanoHTTPD.newChunkedResponse(
                            Response.Status.OK,
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(Shared.substringAfterLast(session.getUri(),'.')),
                            new FileInputStream(uri)
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e("TAG/", "[serve]: " + e.getMessage());
                }
            }
            Log.e("TAG/", "[LocalServer]: serve, " + session.getUri());
            return super.serve(session);
        }


        public void close() {
            super.closeAllConnections();
        }
    }
}
