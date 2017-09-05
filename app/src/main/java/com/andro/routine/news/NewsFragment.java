package com.andro.routine.news;


import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.awareness.fence.LocationFence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.google.android.gms.internal.zzagz.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "NewsFragment";
    private static final String[] newsDataValues = {"title", "author", "description", "url", "imageUrl"};
    private RecyclerView newsRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeLayout;
    private ArrayList<News> newsList;
    private ArrayList<News> cachedNews;
    private GetNews mNews;
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
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        mSharedPreferences = getActivity().getSharedPreferences("NewsObject", MODE_PRIVATE);

        swipeLayout = view.findViewById(R.id.swipe_refresh_news);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_green_dark)
                , getResources().getColor(android.R.color.holo_red_dark)
                , getResources().getColor(android.R.color.holo_blue_dark)
                , getResources().getColor(android.R.color.holo_orange_dark));

        newsList = new ArrayList<>();
        newsRecycler = view.findViewById(R.id.news_recycler);

        layoutManager = new LinearLayoutManager(getActivity());
        newsRecycler.setHasFixedSize(true);
        newsRecycler.setLayoutManager(layoutManager);

        mNews = new GetNews();
        cachedNews = new ArrayList<>();

        if (mSharedPreferences.getBoolean("hasData", false)) {
            Log.d(TAG, "Data Present");
            cachedNews.clear();
            int length = mSharedPreferences.getInt("DataLength", 0);

            for (int i = 0; i < length - 1; i++) {
                News news = new News();
                news.setTitle(mSharedPreferences.getString(Integer.toString(i + 1) + newsDataValues[0], ""));
                news.setAuthor(mSharedPreferences.getString(Integer.toString(i + 1) + newsDataValues[1], ""));
                news.setDescription(mSharedPreferences.getString(Integer.toString(i + 1) + newsDataValues[2], ""));
                news.setUrl(mSharedPreferences.getString(Integer.toString(i + 1) + newsDataValues[3], ""));
                news.setImageUrl(mSharedPreferences.getString(Integer.toString(i + 1) + newsDataValues[4], ""));
                cachedNews.add(news);
            }
            newsAdapter = new NewsAdapter(cachedNews, new NewsClickListener() {
                @Override
                public void onClick(View view, int position) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(view.getResources().getColor(R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(view.getContext(),
                            Uri.parse(cachedNews.get(position).getUrl()));
                }
            });
            newsRecycler.setAdapter(newsAdapter);

        } else {
            onRefresh();
            Log.d(TAG, "Refresh Started");
            swipeLayout.setRefreshing(true);
        }
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mNews.cancel(true);
        swipeLayout.setRefreshing(false);
        Log.d(TAG, "Stopped");
    }

    @Override
    public void onRefresh() {
        if (checkConnection()) {
            Log.d(TAG, "Connection Established");
            mNews.execute();
        } else {
            Log.d(TAG, "Connection Failed");
            Toast.makeText(getContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetNews extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();
            Log.d(TAG, "Request Made");
            String url = "https://newsapi.org/v1/articles?source=the-verge&sortBy=latest&apiKey=6ee4bea512c54bf386c6d4431b6df408";
            String jsonStr = handler.makeServiceCall(url);
            Log.d(TAG, "Fetching Made");
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray newsArray = jsonObj.getJSONArray("articles");
                    mEditor = mSharedPreferences.edit();
                    mEditor.putBoolean("hasData", true);
                    newsList.clear();

                    for (int i = 0; i < newsArray.length(); i++) {
                        JSONObject article = newsArray.getJSONObject(i);
                        News news = new News();
                        news.setAuthor(article.getString("author"));
                        news.setTitle(article.getString("title"));
                        news.setDescription(article.getString("description"));
                        news.setUrl(article.getString("url"));
                        news.setImageUrl(article.getString("urlToImage"));
                        mEditor.putString(Integer.toString(i + 1) + newsDataValues[0], news.getTitle());
                        mEditor.putString(Integer.toString(i + 1) + newsDataValues[1], news.getAuthor());
                        mEditor.putString(Integer.toString(i + 1) + newsDataValues[2], news.getDescription());
                        mEditor.putString(Integer.toString(i + 1) + newsDataValues[3], news.getUrl());
                        mEditor.putString(Integer.toString(i + 1) + newsDataValues[4], news.getImageUrl());
                        newsList.add(news);
                    }
                    mEditor.putInt("DataLength", newsList.size());
                    mEditor.apply();

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            swipeLayout.setRefreshing(false);
            newsList.remove(newsList.size() - 1);
            newsAdapter = new NewsAdapter(newsList, new NewsClickListener() {
                @Override
                public void onClick(View view, int position) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(view.getResources().getColor(R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(view.getContext(),
                            Uri.parse(newsList.get(position).getUrl()));
                }
            });
            newsAdapter.notifyDataSetChanged();
            newsRecycler.setAdapter(newsAdapter);
        }
    }

    public boolean checkConnection() {
        ConnectivityManager connect = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        if (connect.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connect.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connect.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connect.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;
        } else if (connect.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                connect.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }
}

