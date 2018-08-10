package example.com.pocketnews.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import example.com.pocketnews.model.NewsItem;
import example.com.pocketnews.utils.Utils;

public class NewsLoader extends AsyncTaskLoader<List<NewsItem>> {

    private String reqUrl;

    public NewsLoader(@NonNull Context context, String reqUrl) {
        super(context);
        this.reqUrl = reqUrl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public List<NewsItem> loadInBackground() {
        if (reqUrl == null) {
            return null;
        }
        return Utils.fetchNews(reqUrl);
    }
}
