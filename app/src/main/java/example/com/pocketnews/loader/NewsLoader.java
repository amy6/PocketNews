package example.com.pocketnews.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import example.com.pocketnews.model.NewsItem;
import example.com.pocketnews.utils.Utils;

//custom loader class for fetching the news data
public class NewsLoader extends AsyncTaskLoader<List<NewsItem>> {

    private String reqUrl;

    //constructor initializing the request URL for the API call
    public NewsLoader(@NonNull Context context, String reqUrl) {
        super(context);
        this.reqUrl = reqUrl;
    }

    /**
     * starts an asynchronous load of the loader's data
     */
    @Override
    protected void onStartLoading() {
        //forces an asynchronous load, ignoring any previously loaded data
        forceLoad();
    }

    /**
     * called on a worker thread to perform the loading of the data
     *
     * @return fetched data
     */
    @Nullable
    @Override
    public List<NewsItem> loadInBackground() {
        if (reqUrl == null) {
            return null;
        }
        return Utils.fetchNews(reqUrl);
    }
}
