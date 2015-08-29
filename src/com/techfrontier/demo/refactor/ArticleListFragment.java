/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.techfrontier.demo.refactor;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techfrontier.demo.R;
import com.techfrontier.demo.adapters.ArticleAdapter;
import com.techfrontier.demo.beans.Article;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tech.frontier.db.DatabaseHelper;
import org.tech.frontier.listeners.OnItemClickListener;
import org.tech.frontier.widgets.AutoLoadRecyclerView;
import org.tech.frontier.widgets.AutoLoadRecyclerView.OnLoadListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 文章列表主界面,包含自动滚动广告栏、文章列表
 * 
 * @author mrsimple
 */
public class ArticleListFragment extends Fragment implements OnRefreshListener,
        OnLoadListener {
    protected int mCategory = Article.ALL;
    protected ArticleAdapter mAdapter;

    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected AutoLoadRecyclerView mRecyclerView;
    final protected List<Article> mDataSet = new ArrayList<Article>();
    private int mPageIndex = 1;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        initRefreshView(rootView);
        initAdapter();
        mSwipeRefreshLayout.setRefreshing(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDataSet.addAll(DatabaseHelper.getInstance().loadArticles());
        mAdapter.notifyDataSetChanged();
    }

    protected void initRefreshView(View rootView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (AutoLoadRecyclerView) rootView.findViewById(R.id.articles_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()
                .getApplicationContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setOnLoadListener(this);
    }

    protected void initAdapter() {
        mAdapter = new ArticleAdapter(mDataSet);
        mAdapter.setOnItemClickListener(new OnItemClickListener<Article>() {

            @Override
            public void onClick(Article article) {
                if (article != null) {
                    loadArticle(article);
                }
            }
        });
        // 设置Adapter
        mRecyclerView.setAdapter(mAdapter);
        getArticles(1);
    }

    public void setArticleCategory(int category) {
        mCategory = category;
    }

    private void getArticles(final int page) {
        new AsyncTask<Void, Void, List<Article>>() {

            protected void onPreExecute() {
                mSwipeRefreshLayout.setRefreshing(true);
            };

            @Override
            protected List<Article> doInBackground(Void... params) {
                return performRequest(page);
            }

            protected void onPostExecute(List<Article> result) {
                // 移除已经更新的数据
                result.removeAll(mDataSet);
                // 添加心数据
                mDataSet.addAll(result);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                // 存储文章列表
                DatabaseHelper.getInstance().saveArticles(result);
                if (mPageIndex > 1 && page != 1) {
                    mPageIndex++;
                }
            };
        }.execute();
    }

    private List<Article> performRequest(int page) {
        HttpURLConnection urlConnection = null;
        try {
            String getUrl =
                    "http://www.devtf.cn/api/v1/?type=articles&page=" + mPageIndex
                            + "&count=20&category=1";
            urlConnection = (HttpURLConnection) new URL(getUrl)
                    .openConnection();
            urlConnection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sBuilder.append(line).append("\n");
            }
            String result = sBuilder.toString();
            return parse(new JSONArray(result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return new ArrayList<Article>();
    }

    @SuppressLint("SimpleDateFormat")
    private List<Article> parse(JSONArray jsonArray) {
        List<Article> articleLists = new LinkedList<Article>();
        int count = jsonArray.length();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (int i = 0; i < count; i++) {
            JSONObject itemObject = jsonArray.optJSONObject(i);
            Article articleItem = new Article();
            articleItem.title = itemObject.optString("title");
            articleItem.author = itemObject.optString("author");
            articleItem.post_id = itemObject.optString("post_id");
            String category = itemObject.optString("category");
            articleItem.category = TextUtils.isEmpty(category) ? 0 : Integer.valueOf(category);
            articleItem.publishTime = formatDate(dateformat, itemObject.optString("date"));
            Log.d("", "title : " + articleItem.title + ", id = " + articleItem.post_id);
            articleLists.add(articleItem);
        }
        return articleLists;
    }

    private String formatDate(SimpleDateFormat dateFormat, String dateString) {
        try {
            Date date = dateFormat.parse(dateString);
            return dateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void loadArticle(Article article) {
        Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
        intent.putExtra("post_id", article.post_id);
        intent.putExtra("title", article.title);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        getArticles(1);
    }

    @Override
    public void onLoad() {
        mSwipeRefreshLayout.setRefreshing(true);
        getArticles(mPageIndex);
    }
}
