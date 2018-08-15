package example.com.pocketnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.pocketnews.adapter.NewsAdapter;
import example.com.pocketnews.loader.NewsLoader;
import example.com.pocketnews.model.NewsItem;
import example.com.pocketnews.utils.Utils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsItem>>, SwipeRefreshLayout.OnRefreshListener, OnLoadMoreListener {

    private static final String GDN_REQ_URL = "http://content.guardianapis.com/search";
    private static final String QUERY_API_KEY = "api-key";
    private static final String QUERY_PARAM = "q";
    private static final String QUERY_THUMBNAIL = "show-fields";
    private static final String QUERY_AUTHOR = "show-tags";
    private static final String API_KEY = BuildConfig.GUARDIAN_API_KEY;
    private static final String THUMBNAIL_VALUE = "thumbnail";
    private static final String AUTHOR_VALUE = "contributor";
    public static final String QUERY_ORDER_BY = "order-by";
    public static final String QUERY_PAGE = "page";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.emptyTextView)
    TextView emptyTextView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;

    private NewsAdapter adapter;
    private List<NewsItem> news;
    private String queryText;
    private LoaderManager loaderManager;
    private int startPage = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //get reference to the loader manager to handle callbacks to loader
        loaderManager = getSupportLoaderManager();

        //verify internet connectivity
        if (Utils.isConnectedToNetwork(this)) {
            //set the default query parameter value to be used for API call
            queryText = getString(R.string.settings_search_for_default);
            Log.d("TAG", "InitLoader");
            //initialize a new loader
            loaderManager.initLoader(1, null, this);
        } else {
            //display helpful text to user if the internet connection is not available
            emptyTextView.setText(R.string.no_internet_connection);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_cloud_off, 0, 0);
            emptyTextView.setCompoundDrawablePadding(8);
            progressBar.setVisibility(View.GONE);
        }

        //set custom color of refresh layout progress indicator
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        refreshLayout.setOnRefreshListener(this);

        //initialize the news array list
        news = new ArrayList<>();

        //set up the recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        //initialize the custom news adapter
        adapter = new NewsAdapter(this, news, recyclerView);

        //attach the adapter to the recyclerview
        recyclerView.setAdapter(adapter);

        //set on load more listener for the adapter to enable pagination
        adapter.setOnLoadMoreListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu options
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle settings menu option

        if (item.getItemId() == R.id.settings) {
            //start settings activity when settings menu option is chosen
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return true;
    }

    /**
     * called on pull to refresh gesture
     */
    @Override
    public void onRefresh() {

        //hide progress bar as refresh layout has it's own progress indicator
        progressBar.setVisibility(View.GONE);

        //clear existing data on refresh and notify the adapter
        news.clear();
        adapter.notifyDataSetChanged();

        //remove pagination progress loading footer item
        adapter.removeLoadingFooter(null);
        //disable pagination while refreshing
        adapter.setLoaded();
        adapter.setOnLoadMoreListener(null);

        //reset the start page
        startPage = 1;

        //verify connectivity before fetching the results
        if (Utils.isConnectedToNetwork(this)) {
            Log.d("TAG", "RestartLoader on Refresh");
            //restart loader to reload the data
            loaderManager.restartLoader(1, null, this);
        } else {
            refreshLayout.setRefreshing(false);
            //update the empty state views for no internet connection
            updateEmptyState(R.string.no_internet_connection, R.drawable.ic_cloud_off);
        }

    }

    /**
     * called when the system needs a new loader to be created
     * in order to set up the means to fetch the data required to be displayed
     *
     * @param id   integer identifier for the loader
     * @param args optional arguments required to initialize the loader
     * @return loader that fetches the data
     */
    @NonNull
    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, @Nullable Bundle args) {

        Log.d("TAG", "OnCreateLoader");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //fetch order_by value from SharedPreferences
        String orderBy = sharedPrefs.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));

        //fetch search_for value from SharedPreferences
        queryText = sharedPrefs.getString(getString(R.string.settings_search_for_key), getString(R.string.settings_search_for_default));

        //parse the base URL for Guardian API
        Uri.Builder builder = Uri.parse(GDN_REQ_URL).buildUpon();
        //set up the query parameters - topic for the news, thumbnail, author name and api key
        builder.appendQueryParameter(QUERY_PARAM, queryText)
                .appendQueryParameter(QUERY_ORDER_BY, orderBy)
                .appendQueryParameter(QUERY_THUMBNAIL, THUMBNAIL_VALUE)
                .appendQueryParameter(QUERY_PAGE, String.valueOf(startPage))
                .appendQueryParameter(QUERY_AUTHOR, AUTHOR_VALUE)
                .appendQueryParameter(QUERY_API_KEY, API_KEY);

        //return a new loader object that loads the data based on the URI
        return new NewsLoader(this, builder.toString());
    }


    /**
     * called when a loader has finished loading the data
     * the fetched data is displayed in the app
     *
     * @param loader reference to loader that performs the loading
     * @param data   the data set where the loader fetched data is present
     */
    @Override
    public void onLoadFinished(@NonNull Loader<List<NewsItem>> loader, List<NewsItem> data) {

        Log.d("TAG", "OnLoadFinished");

        //hide the progress indicator once the loading is complete
        progressBar.setVisibility(View.GONE);

        //remove pagination progress loading footer item
        adapter.removeLoadingFooter(null);
        //disable pagination
        adapter.setLoaded();
        //enable the listener if it was previously disabled on refresh
        adapter.setOnLoadMoreListener(this);

        //update the empty stat views for server error
        if (data == null) {
            updateEmptyState(R.string.server_error, R.drawable.ic_error_outline);
            return;
        }

        //verify if the data loaded is empty
        if (!data.isEmpty()) {
            news.addAll(data);
            adapter.notifyItemRangeChanged(news.size() + 1, data.size());
            startPage++;

            //display the recyclerview and hide the empty state views
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            //update the empty stat views for no data
            updateEmptyState(R.string.no_data, R.drawable.ic_search);
        }

        //hide refresh progress indicator
        refreshLayout.setRefreshing(false);
        //enable refresh layout once the data is fetched
        refreshLayout.setEnabled(true);

        //destroy loader to prevent unnecessary calls to load finished
        loaderManager.destroyLoader(1);

    }

    /**
     * called when a previously created loader is being reset - when activity/fragment is destroyed
     * handle releasing any data associated with the loader
     *
     * @param loader reference to the loader instance associated with the data
     */
    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsItem>> loader) {
        Log.d("TAG", "OnLoaderReset - not clearing data anymore!");
//        news.clear();
//        adapter.notifyDataSetChanged();
    }

    /*
    handles pagination - calls loader to fetch data from the next page
     */
    @Override
    public void onLoadMore() {

        if (Utils.isConnectedToNetwork(this)) {
            //disable pull to refresh
            refreshLayout.setEnabled(false);

            //add null, so the adapter will show progress bar at the bottom after resolving the view type
            adapter.addLoadingFooter(null);
            /*
            restart loader to be called with the updated start page number
            handle the call as a separate callback to prevent updating recyclerview inside scroll callback
             */
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG", "RestartLoader on LoadMore");
                    loaderManager.restartLoader(1, null, MainActivity.this);
                }
            });

        } else {
            //display internet connectivity error
            updateEmptyState(R.string.no_internet_connection, R.drawable.ic_cloud_off);
        }

    }

    /**
     * updates empty state views accordingly
     *
     * @param emptyStringId display string for the empty state
     * @param emptyImageId  vector drawable for the empty state
     */
    private void updateEmptyState(int emptyStringId, int emptyImageId) {
        emptyTextView.setText(emptyStringId);
        emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, emptyImageId, 0, 0);
        emptyTextView.setCompoundDrawablePadding(8);
        emptyTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

}

