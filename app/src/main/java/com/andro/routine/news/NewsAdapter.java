package com.andro.routine.news;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andro.routine.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by andro on 03/09/17.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private static final String TAG = "NewsAdapter";
    private TextView title, description, author;
    private ImageView photoBackground;
    private String imageUrl;
    private ArrayList<News> newsList;
    private final NewsClickListener listener;
    private CardView cardView;

    public NewsAdapter(ArrayList<News> newsList, NewsClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.news_card);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            author = itemView.findViewById(R.id.author);
            photoBackground = itemView.findViewById(R.id.background_card_image);
            photoBackground.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SCREEN);
        }

        void setData(final int pos, final NewsClickListener listener) {
            title.setText(newsList.get(pos).getTitle());
            description.setText(newsList.get(pos).getDescription());
            author.setText(newsList.get(pos).getAuthor());
            imageUrl = newsList.get(pos).getImageUrl();
            Glide.with(photoBackground.getContext()).load(imageUrl).into(photoBackground);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view, pos);
                }
            });
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public NewsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_news, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsAdapter.MyViewHolder holder, int position) {
        holder.setData(position, listener);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }
}
