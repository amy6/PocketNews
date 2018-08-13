package example.com.pocketnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsItem>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String GDN_REQ_URL = "http://content.guardianapis.com/search";
    private static final String QUERY_API_KEY = "api-key";
    private static final String QUERY_PARAM = "q";
    private static final String QUERY_THUMBNAIL = "show-fields";
    private static final String QUERY_AUTHOR = "show-tags";
    private static final String API_KEY = BuildConfig.GUARDIAN_API_KEY;
    private static final String THUMBNAIL_VALUE = "thumbnail";
    private static final String AUTHOR_VALUE = "contributor";
    public static final String QUERY_ORDER_BY = "order-by";

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
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //get the reference to the loader manager to handle callbacks to loader
        loaderManager = getSupportLoaderManager();

        //verify internet connectivity
        isConnected = Utils.isConnectedToNetwork(this);

        //display helpful text to user if the internet connection is not working
        if (!isConnected) {
            emptyTextView.setText(R.string.no_internet_connection);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_cloud_off, 0, 0);
            emptyTextView.setCompoundDrawablePadding(8);
            progressBar.setVisibility(View.GONE);
        } else {
            //set the query parameter value to be used for API call
            queryText = getString(R.string.settings_search_for_default);
            //initialize a new loader by calling onCreateLoader, if there isn't an existing one to reuse
            loaderManager.initLoader(1, null, this);
        }

        refreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        refreshLayout.setOnRefreshListener(this);

        //initialize the array list to contain the news
        news = new ArrayList<>();

        //initialize the custom news adapter
        adapter = new NewsAdapter(this, news);

        //set up the recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        //attach the adapter
        recyclerView.setAdapter(adapter);

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

        if (item.getItemId() == R.id.action_refresh) {
            progressBar.setVisibility(View.VISIBLE);
            onRefresh();
        }
        return true;
    }

    /**
     * called on swipe gesture
     */
    @Override
    public void onRefresh() {

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        //verify connectivity before fetching the results
        isConnected = Utils.isConnectedToNetwork(this);
        if (!isConnected) {
            refreshLayout.setRefreshing(false);
            emptyTextView.setText(R.string.no_internet_connection);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_cloud_off, 0, 0);
            emptyTextView.setCompoundDrawablePadding(8);
            emptyTextView.setVisibility(View.VISIBLE);
            return;
        }
            //restart loader to reload the data
            loaderManager.restartLoader(1, null, this);
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

        //hide the progress indicator once the loading is complete
        progressBar.setVisibility(View.GONE);

        //hide refresh progress indicator
        refreshLayout.setRefreshing(false);

        //verify if data is null - possibly due to error from the server
        if (data == null) {
            emptyTextView.setText(R.string.server_error);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_error_outline, 0, 0);
            emptyTextView.setCompoundDrawablePadding(8);
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        //verify if the data loaded is empty
        if (!data.isEmpty()) {
            //clear any existing data displayed
            news.clear();
            news.addAll(data);
            adapter.notifyDataSetChanged();

            //display the recyclerview and hide the empty state views
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            //if no data has been returned, update the empty state views as required
            emptyTextView.setText(R.string.no_data);
            emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_search, 0, 0);
            emptyTextView.setCompoundDrawablePadding(8);
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }


    }

    /**
     * called when a previously created loader is being reset - when activity/fragment is destroyed
     * handle releasing any data associated with the loader
     *
     * @param loader reference to the loader instance associated with the data
     */
    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsItem>> loader) {
        //clear data from the list
        news.clear();
        adapter.notifyDataSetChanged();
    }

}
