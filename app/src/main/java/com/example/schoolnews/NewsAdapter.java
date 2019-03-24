package com.example.schoolnews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private Context mContext;
    private List<News> mNewsList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView newsImage;
        TextView newsTitle;

        TextView newsAuthor;
        TextView newsTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (CardView) itemView;
            newsImage = itemView.findViewById(R.id.imageView_news_image);
            newsTitle = itemView.findViewById(R.id.textView_news_title);
            newsAuthor=itemView.findViewById(R.id.textView_news_author);
            newsTime=itemView.findViewById(R.id.textView_news_time);
        }
    }

    public NewsAdapter(List<News> newsList) {
        mNewsList = newsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(mContext==null){
            mContext=viewGroup.getContext();
        }

        View view= LayoutInflater.from(mContext).inflate(R.layout.news_item,viewGroup,false);

        final ViewHolder holder=new ViewHolder(view);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                News news=mNewsList.get(position);
                Intent intent=new Intent(mContext,NewsActivity.class);
                intent.putExtra("news",news);
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        News news=mNewsList.get(i);
        viewHolder.newsTitle.setText(news.getTitle());
        Glide.with(mContext).load(Uri.parse(news.getImageUrl())).into(viewHolder.newsImage);
        viewHolder.newsTime.setText(news.getTime());
        viewHolder.newsAuthor.setText(news.getAuthor());
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }
}
