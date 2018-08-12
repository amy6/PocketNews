package example.com.pocketnews.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.bumptech.glide.request.RequestOptions;

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

import example.com.pocketnews.R;
import example.com.pocketnews.model.NewsItem;

import static android.content.Context.CONNECTIVITY_SERVICE;

public final class Utils {

    private Utils() {
    }

    /**
     * called by loader to fetch the data
     *
     * @param reqUrl request URL from which to fetch the data
     * @return list of custom objects parsed from JSON response
     */
    public static List<NewsItem> fetchNews(String reqUrl) {

        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(createUrl(reqUrl));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractNews(jsonResponse);
    }

    /**
     * parse json response obtained by the API call
     *
     * @param jsonResponse json string to be parsed
     * @return list of parsed objects
     */
    private static List<NewsItem> extractNews(String jsonResponse) {
        //validate if the json string is empty
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<NewsItem> news = new ArrayList<>();

        try {
            //create new json object for the json string
            JSONObject jsonObject = new JSONObject(jsonResponse);
            //extract the root object
            JSONObject response = jsonObject.getJSONObject("response");
            //fetch the results array
            JSONArray jsonArray = response.getJSONArray("results");
            //loop through the array elements and parse the individual fields
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject newsObj = jsonArray.getJSONObject(i);
                String title = newsObj.getString("webTitle");
                String section = newsObj.getString("sectionName");
                String publishDate = newsObj.getString("webPublicationDate");
                String webUrl = newsObj.getString("webUrl");

                JSONObject fields = newsObj.getJSONObject("fields");
                String thumbnailUrl = fields.getString("thumbnail");

                String authorName = "";
                JSONArray tags = newsObj.getJSONArray("tags");
                if (tags.length() > 0) {
                    JSONObject authorProfile = (JSONObject) tags.get(0);
                    authorName = authorProfile.getString("webTitle");
                }

                //initialize and add news items to the array list
                NewsItem newsItem = new NewsItem(title, section, webUrl, thumbnailUrl, publishDate, authorName);
                news.add(newsItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return news;
    }

    /**
     * opens a http request for the required url
     * reads the data from the input stream
     *
     * @param url url from which to fetch the results
     * @return return json response from the url
     * @throws IOException exception while reading from stream objects
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        //validate if the request url is null
        if (url == null) {
            return null;
        }

        try {
            //set up the http url connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            //get the input stream to read data from
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //disconnect the url connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            //close input stream
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    /**
     * reads the data from the input stream
     *
     * @param inputStream stream of data to be read
     * @return read data in the form of string
     * @throws IOException exceptions while reading from the input stream
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            //initialize an input stream reader
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            //connect the input stream reader to a buffered reader
            BufferedReader reader = new BufferedReader(inputStreamReader);
            //read the data
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * creates a url from a string
     *
     * @param reqUrl request url in string format
     * @return URL for the API call
     */
    private static URL createUrl(String reqUrl) {
        URL url = null;
        try {
            url = new URL(reqUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * formats the date in the required format
     *
     * @param publishDate date to be formatted
     * @return formatted date in the form of a string
     */
    public static String formatDate(String publishDate) {
        String formattedDate = "";
        //define input date format
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        //define output date format
        SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        try {
            //parse and format the input date
            Date date = inputSdf.parse(publishDate);
            formattedDate = outputSdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    /**
     * set up glide error and placeholder images
     *
     * @return reference to RequestOptions
     */
    @SuppressLint("CheckResult")
    public static RequestOptions setUpGlide() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.news_placeholder);
        requestOptions.error(R.drawable.ic_error_outline);

        return requestOptions;
    }

    /**
     * tests network connectivity
     *
     * @param context reference to application resources
     * @return status of network connectivity
     */
    public static boolean isConnectedToNetwork(Context context) {
        //get reference to connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (connectivityManager != null) {
            //get the status of the network
            activeNetwork = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
