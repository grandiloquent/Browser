package euphoria.psycho.browser.app;

import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import euphoria.psycho.browser.video.VideoActivity;
import euphoria.psycho.share.Log;

public class InputServiceHelper {

    public static  void switchVideoPlayer(final Context context,String value) throws IOException {
        final Runtime runtime = Runtime.getRuntime();
        final String intentCommand = "su -c am start -n euphoria.psycho.browser/euphoria.psycho.browser.video.VideoActivity -a android.intent.action.VIEW --es android.intent.extra.TEXT \""+value+"\" -t text/plain";
        runtime.exec(intentCommand);
    }

    public static void launchVideoPlayer(Context context, String uri) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri);
        context.startActivity(intent);
    }

    public static String buildJavaScript(Context context, String html) {
        String asset = readAssetAsString(context, "encode.js");
        StringBuilder sb = new StringBuilder();
        sb.append(asset)
                .append(";")
                .append(" strencode2(\"")
                .append(extractSecret(html))
                .append("\");");
        return sb.toString();
    }

    private static String readAssetAsString(Context context, String assetName) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetName);

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }

        }
        return null;
    }

    private static String extractSecret(String value) {
        String pattern = "document.write(strencode2(";
        int start = value.indexOf(pattern) + pattern.length() + 1;
        int end = value.indexOf("\"));", start);
        return value.substring(start, end);
    }

    public static String getHtml(String url) throws IOException {
        HttpURLConnection connection = null;
        URL u = new URL(url);

        connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Referer", "http://91porn.com");
        Random r = new Random();
        connection.setRequestProperty("X-Forwarded-For", r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256));
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.56 Mobile Safari/537.36");

        int code = connection.getResponseCode();
        Log.e("TAG", code + "");
        if (code < 400 && code >= 200) {
            StringBuilder sb = new StringBuilder();
            InputStream in;
            String contentEncoding = connection.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equals("gzip")) {
                in = new GZIPInputStream(connection.getInputStream());
            } else {
                in = connection.getInputStream();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");
            }


            reader.close();
            return sb.toString();
        }
        return null;

    }
}
