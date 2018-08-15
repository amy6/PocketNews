package example.com.pocketnews.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.pocketnews.OnLoadMoreListener;
import example.com.pocketnews.R;
import example.com.pocketnews.model.NewsItem;
import example.com.pocketnews.utils.Utils;

//custom adapter for the recyclerview
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_ITEM = 1;
    private static final int VIEW_PROG = 0;
    private Context context;
    private List<NewsItem> news;
    private int totalItemCount;
    private int lastVisibleItem;
    private int visibleThreshold = 1;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    //constructor to get reference to application context, the data source and the recyclerview
    public NewsAdapter(final Context context, List<NewsItem> news, RecyclerView recyclerView) {
        this.context = context;
        this.news = news;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            //get reference to recyclerview's layout manager
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            //add scroll listener to recyclerview to implement pagination on user scroll
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    /*
                    verify there is no current pagination loading,
                    check if the recyclerview has been scrolled past the last item
                     */
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreListener != null) {
                            //load more items after verifying the listener has been initialized
                            onLoadMoreListener.onLoadMore();
                        }
                        //set pagination loading to true
                        loading = true;
                    }
                }
            });
        }
    }

    /**
     * called when a new view is required to be created for the recyclerview
     *
     * @param parent   viewgroup to which the new view is to be attached to
     * @param viewType view type of the new view
     * @return newly created view
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the individual recyclerview item layout
        RecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_news_item, parent, false);

            viewHolder = new NewsViewHolder(view);
        } else {
            //inflate the footer progress bar item layout
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_progress, parent, false);

            viewHolder = new ProgressViewHolder(view);
        }
        return viewHolder;
    }

    /**
     * called to resolve references to individual views in an item view
     *
     * @param holder   reference to holder which resolves the references to views in the item view
     * @param position position of the item in the adapter to be returned to recyclerview
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof NewsViewHolder) {
            //get reference to current NewsItem
            final NewsItem newsItem = news.get(position);

            NewsViewHolder newsViewHolder = (NewsViewHolder) holder;

            //set up requestoptions for Glide to add error and placeholder images
            RequestOptions requestOptions = Utils.setUpGlide();

            //load news thumbnail image
            Glide.with(context)
                    .setDefaultRequestOptions(requestOptions)
                    .load(newsItem.getThumbnailUrl())
                    .into(newsViewHolder.imageView);

            //display the title, author, section name and publish date
            newsViewHolder.title.setText(newsItem.getTitle());
            newsViewHolder.title.setMaxLines(2);
            newsViewHolder.section.setText(newsItem.getSection());
            newsViewHolder.authorName.setText(newsItem.getAuthorName());

            //format the data from JSON response
            String formattedDate = Utils.formatDate(newsItem.getPublishDate());
            newsViewHolder.publishDate.setText(formattedDate);
            //set up click listener for individual items to open the news in a browser intent
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getWebUrl()));
                    context.startActivity(intent);
                }
            });
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    /**
     * called by recyclerview to know the number of items to be displayed
     *
     * @return number of items to display
     */
    @Override
    public int getItemCount() {
        return news == null ? 0 : news.size();
    }

    //ViewHolder to resolve references to individual views in news item
    class NewsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.section)
        TextView section;
        @BindView(R.id.publishDate)
        TextView publishDate;
        @BindView(R.id.author)
        TextView authorName;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //ViewHolder to resolve references to individual items in footer item layout
    public static class ProgressViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.progressBar)
        ProgressBar progressBar;

        ProgressViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * called by adapter to identify the type of view to create view holder for
     *
     * @param position position of the new view to be inflated
     * @return view type of the new view
     */
    @Override
    public int getItemViewType(int position) {
        return news.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    /**
     * disable loading flag after current page loading is complete via pagination
     */
    public void setLoaded() {
        loading = false;
    }

    /**
     * initializes onLoadMore listener
     *
     * @param onLoadMoreListener listener reference
     */
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    /**
     * removes the additional progress bar loading item added as a recyclerview footer
     *
     * @param object object to be removed
     *               object will be null, casted to NewsItem type to handle suspicious remove warning
     */
    public void removeLoadingFooter(Object object) {
        NewsItem newsItem = (NewsItem) object;
        news.remove(newsItem);

        //notify adapter on item removal
        notifyItemRemoved(news.size());
    }


    /**
     * adds a progress bar item as the recyclerview's footer
     *
     * @param object object to be added
     */
    public void addLoadingFooter(final Object object) {

        /*
        handle adding new item to adapter as a separate callback as updating adapter data source in the same callback
        as the recyclerview's scroll callback may result in unexpected response,
        as the recyclerview's layout might be undergoing modifications
         */
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                NewsItem newsItem = (NewsItem) object;
                news.add(newsItem);
                //notify adapter on item added
                notifyItemInserted(news.size());
            }
        };
        handler.post(r);
    }

}
