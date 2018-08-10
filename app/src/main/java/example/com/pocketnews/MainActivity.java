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

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<NewsItem>> {

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
    private LoaderManager loaderManager;

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
            queryText = PARAM_VALUE;
            //initialize a new loader by calling onCreateLoader, if there isn't an existing one to reuse
            loaderManager.initLoader(1, null, this);
        }

        //initialize the array list to contain the news
        news = new ArrayList<>();

        //initialize the custom news adapter
        adapter = new NewsAdapter(this, news);

        //set up the recyclerview
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        //attach the adapter
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //get reference to the search menu item
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        //get reference to SearchManager
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        //resolve the reference to the SearchView widget
        searchView = (SearchView) searchItem.getActionView();
        if (searchManager != null) {
            //set up search view
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            //expand the search view to the full width of the app bar when clicked on
            searchView.setMaxWidth(Integer.MAX_VALUE);
            //set the user query hint
            searchView.setQueryHint(getString(R.string.serach_hint));
        }
        //set search query listener
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        //on query submit, make sure the device is still connected to the internet
        isConnected = Utils.isConnectedToNetwork(MainActivity.this);
        //clear any visible news data if the internet connection lost and update empty state views
        if (!isConnected) {
            news.clear();
            adapter.notifyDataSetChanged();
            emptyTextView.setText(R.string.no_internet_connection);
            emptyTextView.setVisibility(View.VISIBLE);
            return false;
        }
        //if the device is still connected to the internet, clear any empty state views
        emptyTextView.setVisibility(View.GONE);
        //display the progress bar
        progressBar.setVisibility(View.VISIBLE);
        //update the query text to be the newly entered user text
        queryText = s;
        //restart loader to discard old data and trigger API call for new query
        loaderManager.restartLoader(1, null, MainActivity.this);
        if (searchView != null) {
            //clear edit text focus on search bar
            searchView.clearFocus();
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return true;
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
        //parse the base URL for Guardian API
        Uri.Builder builder = Uri.parse(GDN_REQ_URL).buildUpon();
        //set up the query parameters - topic for the news, thumbnail, author name and api key
        builder.appendQueryParameter(QUERY_PARAM, queryText)
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

        //verify if the data loaded is empty
        if (data != null && !data.isEmpty()) {
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


    /**
     * called when user presses the back button
     */
    @Override
    public void onBackPressed() {
        //handle back press while the searchview is expanded
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }
}
