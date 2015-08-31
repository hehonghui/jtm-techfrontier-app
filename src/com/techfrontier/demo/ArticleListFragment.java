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

package com.techfrontier.demo;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tech.frontier.demo.listeners.OnItemClickListener;
import com.tech.frontier.demo.widgets.AutoLoadRecyclerView;
import com.tech.frontier.demo.widgets.AutoLoadRecyclerView.OnLoadListener;
import com.techfrontier.demo.adapters.ArticleAdapter;
import com.techfrontier.demo.beans.Article;
import com.techfrontier.demo.mvpview.ArticleListView;
import com.techfrontier.demo.presenter.ArticleListPresenter;

import java.util.List;

/**
 * 文章列表主界面,包含自动滚动广告栏、文章列表
 * 
 * @author mrsimple
 */
public class ArticleListFragment extends Fragment implements OnRefreshListener,
        OnLoadListener, ArticleListView {
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected AutoLoadRecyclerView mRecyclerView;
    protected ArticleAdapter mAdapter;
    private ArticleListPresenter mPresenter = new ArticleListPresenter();

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
        mPresenter.attach(this);
        mPresenter.fetchLastestArticles();
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
        mAdapter = new ArticleAdapter();
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
    }

    protected void jumpToDetailActivity(Article article) {
        Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
        intent.putExtra("post_id", article.post_id);
        intent.putExtra("title", article.title);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        mPresenter.fetchLastestArticles();
    }

    @Override
    public void onLoad() {
        mPresenter.loadNextPageArticles();
    }

    @Override
    public void onFetchedArticles(List<Article> result) {
        mAdapter.addItems(result);
    }
    
    @Override
    public void clearCacheArticles() {
        mAdapter.clear();
    }

    @Override
    public void onShowLoding() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onHideLoding() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detach();
    }
}
