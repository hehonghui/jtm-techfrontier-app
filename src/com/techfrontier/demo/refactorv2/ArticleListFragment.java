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

package com.techfrontier.demo.refactorv2;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techfrontier.demo.R;
import com.techfrontier.demo.adapters.ArticleAdapter;
import com.techfrontier.demo.beans.Article;

import org.tech.frontier.db.DatabaseHelper;
import org.tech.frontier.listeners.DataListener;
import org.tech.frontier.listeners.OnItemClickListener;
import org.tech.frontier.net.HttpFlinger;
import org.tech.frontier.net.parser.ArticleParser;
import org.tech.frontier.widgets.AutoLoadRecyclerView;
import org.tech.frontier.widgets.AutoLoadRecyclerView.OnLoadListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章列表主界面,包含自动滚动广告栏、文章列表
 * 
 * @author mrsimple
 */
public class ArticleListFragment extends Fragment implements OnRefreshListener,
        OnLoadListener {

    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected AutoLoadRecyclerView mRecyclerView;
    protected ArticleAdapter mAdapter;
    final protected List<Article> mDataSet = new ArrayList<Article>();
    private int mPageIndex = 1;
    ArticleParser mArticleParser = new ArticleParser();

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
        loadArticlesFromDB();
    }

    private void loadArticlesFromDB() {
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
                    jumpToDetailActivity(article);
                }
            }
        });
        // 设置Adapter
        mRecyclerView.setAdapter(mAdapter);
        fetchArticles(1);
    }

    private void fetchArticles(final int page) {
        mSwipeRefreshLayout.setRefreshing(true);
        HttpFlinger.get(prepareRequestUrl(), mArticleParser, new DataListener<List<Article>>() {
            @Override
            public void onComplete(List<Article> result) {
                // 移除已经更新的数据
                result.removeAll(mDataSet);
                // 添加心数据
                mDataSet.addAll(result);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                // 存储文章列表
                DatabaseHelper.getInstance().saveArticles(result);
                if (result.size() > 0) {
                    mPageIndex++;
                }
            }
        });
    }

    private String prepareRequestUrl() {
        return "http://www.devtf.cn/api/v1/?type=articles&page=" + mPageIndex
                + "&count=20&category=1";
    }

    protected void jumpToDetailActivity(Article article) {
        Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
        intent.putExtra("post_id", article.post_id);
        intent.putExtra("title", article.title);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        fetchArticles(1);
    }

    @Override
    public void onLoad() {
        mSwipeRefreshLayout.setRefreshing(true);
        fetchArticles(mPageIndex);
    }
}
