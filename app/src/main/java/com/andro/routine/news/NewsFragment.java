package com.andro.routine.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andro.routine.HttpHandler;
import com.andro.routine.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "NewsFragment";
    private String URL = "https://newsapi.org/v1/articles?source=%s&sortBy=latest&apiKey=6ee4bea512c54bf386c6d4431b6df408";
    private static final String[] newsDataValues = {"title", "author", "description", "URL", "imageUrl", "source"};
    private boolean hasData = false;
    private RecyclerView newsRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeLayout;
    private ArrayList<String> sourceList;
    private ArrayList<News> newsList;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(R.string.title_news);
        newsList = new ArrayList<>();
        sourceList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mSharedPreferences = getActivity().getSharedPreferences("NewsObject", MODE_PRIVATE);
        newsRecycler = view.findViewById(R.id.news_recycler);
        swipeLayout = view.findViewById(R.id.swipe_refresh_news);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), android.R.color.holo_green_dark),
                ContextCompat.getColor(getContext(), android.R.color.holo_red_dark),
                ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark),
                ContextCompat.getColor(getContext(), android.R.color.holo_orange_dark));
        layoutManager = new LinearLayoutManager(getActivity());
        newsRecycler.setHasFixedSize(true);
        newsRecycler.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter(newsList, new NewsClickListener() {
            @Override
            public void onClick(View view, int position) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setShowTitle(true);
                builder.setToolbarColor(ActivityCompat.getColor(getContext(), R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(view.getContext(),
                        Uri.parse(newsList.get(position).getUrl()));
            }
        });
        newsRecycler.setAdapter(newsAdapter);

        sourceList.add("the-verge");
        sourceList.add("the-next-web");

        if (mSharedPreferences.getBoolean("hasData", false)) {
            newsList.clear();
            int length = mSharedPreferences.getInt("DataLength", 0);
            Log.d(TAG, "hasData");
            for (int i = 1; i <= length; i++) {
                News news = new News();
                news.setTitle(mSharedPreferences.getString(Integer.toString(i) + newsDataValues[0], ""));
                news.setAuthor(mSharedPreferences.getString(Integer.toString(i) + newsDataValues[1], ""));
                news.setDescription(mSharedPreferences.getString(Integer.toString(i) + newsDataValues[2], ""));
                news.setUrl(mSharedPreferences.getString(Integer.toString(i) + newsDataValues[3], ""));
                news.setImageUrl(mSharedPreferences.getString(Integer.toString(i) + newsDataValues[4], ""));
                news.setSource(mSharedPreferences.getString(Integer.toString(i) + newsDataValues[5], ""));
                newsList.add(news);
            }
            newsAdapter.notifyDataSetChanged();
        } else {
            swipeLayout.setRefreshing(true);
            onRefresh();
        }
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        int i = 1;
        swipeLayout.setRefreshing(false);
        if (hasData) {
            mEditor = mSharedPreferences.edit();
            for (News news : newsList) {
                mEditor.putBoolean("hasData", true);
                mEditor.putString(Integer.toString(i) + newsDataValues[0], news.getTitle());
                mEditor.putString(Integer.toString(i) + newsDataValues[1], news.getAuthor());
                mEditor.putString(Integer.toString(i) + newsDataValues[2], news.getDescription());
                mEditor.putString(Integer.toString(i) + newsDataValues[3], news.getUrl());
                mEditor.putString(Integer.toString(i) + newsDataValues[4], news.getImageUrl());
                mEditor.putString(Integer.toString(i) + newsDataValues[5], news.getSource());
                i += 1;
            }
            mEditor.putInt("DataLength", newsList.size());
            mEditor.apply();
        }
    }

    @Override
    public void onRefresh() {
        if (checkConnection()) {
            newsList.clear();
            new GetNews().execute();
        } else {
            Toast.makeText(getContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
            swipeLayout.setRefreshing(false);
        }
    }

    private class GetNews extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler handler = new HttpHandler();
            for (String e : sourceList) {
                String jsonStr = handler.makeServiceCall(String.format(URL, e));
                Log.e(TAG, "Response from URL: " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        JSONArray newsArray = jsonObj.getJSONArray("articles");
                        for (int i = 0; i < newsArray.length(); i++) {
                            JSONObject article = newsArray.getJSONObject(i);
                            News news = new News();
                            news.setAuthor(article.getString("author"));
                            news.setTitle(article.getString("title"));
                            news.setDescription(article.getString("description"));
                            news.setUrl(article.getString("url"));
                            news.setImageUrl(article.getString("urlToImage"));
                            news.setSource(jsonObj.getString("source"));
                            newsList.add(news);
                        }

                    } catch (final JSONException err) {
                        Log.e(TAG, "Json parsing error: " + err.getMessage());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            hasData = true;
            swipeLayout.setRefreshing(false);
            newsAdapter.notifyDataSetChanged();
        }
    }

    public boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && (
                activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
                activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE ||
                activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET);
    }
}