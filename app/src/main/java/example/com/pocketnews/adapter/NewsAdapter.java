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

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsItem> news;

    public NewsAdapter(Context context, List<NewsItem> news) {
        this.context = context;
        this.news = news;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_news_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        final NewsItem newsItem = news.get(position);
        RequestOptions requestOptions = Utils.setUpGlide();
        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(newsItem.getThumbnailUrl())
                .into(holder.imageView);
        holder.title.setText(newsItem.getTitle());
        holder.title.setLines(2);
        holder.section.setText(newsItem.getSection());
        holder.authorName.setText(newsItem.getAuthorName());
        String formattedDate = Utils.formatDate(newsItem.getPublishDate());
        holder.publishDate.setText(formattedDate);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getWebUrl()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

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
