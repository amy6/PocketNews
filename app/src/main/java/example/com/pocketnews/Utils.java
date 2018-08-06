package example.com.pocketnews;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class Utils {

    private Utils() {
    }

    public static List<NewsItem> fetchNews(String reqUrl) {

        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(createUrl(reqUrl));
            Log.d("Response is : ", jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractNews(jsonResponse);
    }

    private static List<NewsItem> extractNews(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<NewsItem> news = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray jsonArray = response.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject newsObj = jsonArray.getJSONObject(i);
                String title = newsObj.getString("webTitle");
                String section = newsObj.getString("sectionName");
                String publishDate = newsObj.getString("webPublicationDate");
                String webUrl = newsObj.getString("webUrl");
                JSONObject fields = newsObj.getJSONObject("fields");
                String thumbnailUrl = fields.getString("thumbnail");

                NewsItem newsItem = new NewsItem(title, section, webUrl, thumbnailUrl, publishDate);
                news.add(newsItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return news;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        if (url == null) {
            return null;
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static URL createUrl(String reqUrl) {
        URL url = null;
        try {
            url = new URL(reqUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String formatDate(String publishDate) {
        String formattedDate = "";
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
        try {
            Date date = inputSdf.parse(publishDate);
            formattedDate = outputSdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }
}
