package example.com.pocketnews;

import android.app.SearchManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.pocketnews.adapter.NewsAdapter;
import example.com.pocketnews.loader.NewsLoader;
import example.com.pocketnews.model.NewsItem;
import example.com.pocketnews.utils.Utils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsItem>> {

    private static final String GDN_REQ_URL = "http://content.guardianapis.com/search";
    private static final String QUERY_API_KEY = "api-key";
    private static final String QUERY_PARAM = "q";
    private static final String QUERY_THUMBNAIL = "show-fields";
    private static final String QUERY_AUTHOR = "show-tags";
    private static final String API_KEY = "02a343d2-4227-4238-ad61-b556100841c4";
    private static final String PARAM_VALUE = "android";
    private static final String THUMBNAIL_VALUE = "thumbnail";
    private static final String AUTHOR_VALUE = "contributor";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.emptyTextView)
    TextView emptyTextView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private NewsAdapter adapter;
    private List<NewsItem> news;
    private String queryText;
    private SearchView searchView;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        isConnected = Utils.isConnectedToNetwork(this);

        if (!isConnected) {
            emptyTextView.setText(R.string.no_internet_connection);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_cloud_off, 0, 0);
            emptyTextView.setCompoundDrawablePadding(8);
            progressBar.setVisibility(View.GONE);
        } else {
            queryText = PARAM_VALUE;
            getSupportLoaderManager().initLoader(1, null, this);
        }

        news = new ArrayList<>();

        adapter = new NewsAdapter(this, news);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MenuItem searchItem =   menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView = (android.widget.SearchView) searchItem.getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint("Search for News");
        }

        android.widget.SearchView.OnQueryTextListener queryTextListener = new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                isConnected = Utils.isConnectedToNetwork(MainActivity.this);
                if (!isConnected) {
                    news.clear();
                    adapter.notifyDataSetChanged();
                    return false;
                }
                emptyTextView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                queryText = s;
                getSupportLoaderManager().restartLoader(1, null, MainActivity.this);
                if (searchView != null) {
                    searchView.clearFocus();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return true;
    }

    @NonNull
    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, @Nullable Bundle args) {
        Uri.Builder builder = Uri.parse(GDN_REQ_URL).buildUpon();
        builder.appendQueryParameter(QUERY_PARAM, queryText)
                .appendQueryParameter(QUERY_THUMBNAIL, THUMBNAIL_VALUE)
                .appendQueryParameter(QUERY_AUTHOR, AUTHOR_VALUE)
                .appendQueryParameter(QUERY_API_KEY, API_KEY);

        Log.d("URI is : ", builder.toString());

        return new NewsLoader(this, builder.toString());
    }


    @Override
    public void onLoadFinished(@NonNull Loader<List<NewsItem>> loader, List<NewsItem> data) {

        progressBar.setVisibility(View.GONE);

        if (data != null && !data.isEmpty()) {
            news.addAll(data);
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            emptyTextView.setText(R.string.no_data);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_search, 0, 0);
            emptyTextView.setCompoundDrawablePadding(8);
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsItem>> loader) {
        news.clear();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        news.clear();
        adapter.notifyDataSetChanged();
    }
}
