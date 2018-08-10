package example.com.pocketnews.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.pocketnews.R;
import example.com.pocketnews.utils.Utils;
import example.com.pocketnews.model.NewsItem;

//custom adapter for the recyclerview
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsItem> news;

    //constructor to get reference to application context and initialize the data source
    public NewsAdapter(Context context, List<NewsItem> news) {
        this.context = context;
        this.news = news;
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
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the individual recyclerview item layout
        return new NewsViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_news_item, parent, false));
    }

    /**
     * called to resolve references to individual views in a recyclerview item view
     *
     * @param holder   reference to holder which resolves the references to views in the item view
     * @param position position of the item in the adapter to be returned to recyclerview
     */
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        //get reference to current NewsItem
        final NewsItem newsItem = news.get(position);

        //set up requestoptions for Glide to add error and placeholder images
        RequestOptions requestOptions = Utils.setUpGlide();
        //load news thumbnail image
        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(newsItem.getThumbnailUrl())
                .into(holder.imageView);
        //display the title, author, section name and publish date
        holder.title.setText(newsItem.getTitle());
        holder.title.setMaxLines(2);
        holder.section.setText(newsItem.getSection());
        holder.authorName.setText(newsItem.getAuthorName());
        //format the data from JSON response
        String formattedDate = Utils.formatDate(newsItem.getPublishDate());
        holder.publishDate.setText(formattedDate);
        //set up click listener for individual items to open the news in a browser intent
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getWebUrl()));
                context.startActivity(intent);
            }
        });
    }

    /**
     * called by recyclerview to know the number of items to be displayed
     *
     * @return number of items to display
     */
    @Override
    public int getItemCount() {
        return news.size();
    }

    //ViewHolder to resolve references to individual views
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
}
