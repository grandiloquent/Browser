package euphoria.psycho.browser.app;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import androidx.annotation.NonNull;
import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.file.FileHelper;

public class TwitterHelper {


    public static List<TwitterVideo> extractTwitterVideo(String id) throws IOException, JSONException {
       /* URL url = new URL(String.format("https://api.twitter.com/1.1/videos/tweet/config/%s.json", id));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAIK1zgAAAAAA2tUWuhGZ2JceoId5GwYWU5GspY4%3DUq7gzFoCZs1QfwGoVdvSac3IniczZEYXIcDyumCauIXpcAPorE");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.56 Mobile Safari/537.36");
*/
        URL url = new URL("https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.56 Mobile Safari/537.36");

        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("id", id);

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getPostDataString(postDataParams));

        writer.flush();
        writer.close();
        os.close();

        int statusCode = connection.getResponseCode();


        Log.e("TAG/", "Debug: extractTwitterVideo, \n" + statusCode);

        if (statusCode == 200) {
            StringBuilder sb = new StringBuilder();
            InputStream in;
            String contentEncoding = connection.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equals("gzip")) {
                in = new GZIPInputStream(connection.getInputStream());
            } else {
                in = connection.getInputStream();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
            reader.close();

            JSONObject object = new JSONObject(sb.toString());
            if (object.has("state") && object.getString("state").equals("success")) {
                if (object.has("videos")) {
                    JSONArray videos = object.getJSONArray("videos");
                    List<TwitterVideo> twitterVideos = new ArrayList<>();

                    for (int i = 0; i < videos.length(); i++) {
                        JSONObject video = videos.getJSONObject(i);
                        TwitterVideo twitterVideo = new TwitterVideo();

                        if (video.has("duration")) {
                            twitterVideo.duration = video.getLong("duration");

                        }
                        if (video.has("size")) {
                            twitterVideo.size = video.getLong("size");
                        }
                        if (video.has("url")) {
                            twitterVideo.url = video.getString("url");
                        }

                        twitterVideos.add(twitterVideo);
                    }

                    return twitterVideos;
                }


            }
        }
        return null;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    public static void showDialog(List<TwitterVideo> twitterVideos, Context context) {
        if (twitterVideos == null) {
            return;
        }
        String[] items = new String[twitterVideos.size()];

        for (int i = 0; i < twitterVideos.size(); i++) {
            items[i] = String.format("%s \n %s", Share.formatFileSize(twitterVideos.get(i).size), twitterVideos.get(i).url);
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setItems(items, (dialog1, which) -> {
                    String url = twitterVideos.get(which).url;
                    Share.setClipboardString(url);
                    FileHelper.downloadFromUrl(context,
                            url,
                            Share.substringAfterLast(url, "/"),
                            Share.substringAfterLast(url, "/")
                    );
                })
                .create();
        dialog.show();
    }

    public static class TwitterVideo {
        public long duration;
        public long size;
        public String url;

        @NonNull
        @Override
        public String toString() {
            return super.toString();
        }
    }

}
