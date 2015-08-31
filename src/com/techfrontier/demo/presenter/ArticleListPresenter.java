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

package com.techfrontier.demo.presenter;

import com.tech.frontier.demo.db.DatabaseHelper;
import com.tech.frontier.demo.listeners.DataListener;
import com.tech.frontier.demo.net.HttpFlinger;
import com.tech.frontier.demo.net.parser.ArticleParser;
import com.techfrontier.demo.beans.Article;
import com.techfrontier.demo.mvpview.ArticleListView;

import java.util.List;

/**
 * 文章列表的Presenter,负责从网络上加载最新的文章列表。第一次加载最新文章列表时先从数据库中加载缓存，然后再从网络上加载最新的数据。
 * 
 * @author mrsimple
 */
public class ArticleListPresenter extends BasePresenter<ArticleListView> {

    public static final int FIRST_PAGE = 1;
    private int mPageIndex = FIRST_PAGE;
    ArticleParser mArticleParser = new ArticleParser();
    private boolean isCacheLoaded = false;

    /**
     * 第一次先从数据库中加载缓存,然后再从网络上获取数据
     */
    public void fetchLastestArticles() {
        if (!isCacheLoaded) {
            mView.onFetchedArticles(DatabaseHelper.getInstance().loadArticles());
        }
        // 从网络上获取最新的数据
        fetchArticlesAsync(FIRST_PAGE);
    }

    private void fetchArticlesAsync(final int page) {
        mView.onShowLoding();
        HttpFlinger.get(prepareRequestUrl(page), mArticleParser, new DataListener<List<Article>>() {
            @Override
            public void onComplete(List<Article> result) {
                mView.onHideLoding();
                if (!isCacheLoaded && result != null) {
                    mView.clearCacheArticles();
                    isCacheLoaded = true;
                }

                if (result == null) {
                    return;
                }
                mView.onFetchedArticles(result);
                // 存储文章列表
                DatabaseHelper.getInstance().saveArticles(result);
                updatePageIndex(page, result);
            }
        });
    }

    /**
     * 更新下一页的索引,当请求成功且不是第一次请求最新数据时更新索引值。
     * 
     * @param loadPage
     * @param result
     */
    public void updatePageIndex(int curPage, List<Article> result) {
        if (result.size() > 0
                && shouldUpdatePageIndex(curPage)) {
            mPageIndex++;
        }
    }

    /**
     * 是否应该更新Page索引。更新索引值的时机有两个，一个是首次成功加载最新数据时mPageIndex需要更新;另一个是每次加载更多数据时需要更新.
     * 
     * @param curPage
     * @return
     */
    private boolean shouldUpdatePageIndex(int curPage) {
        return (mPageIndex > 1 && curPage > 1)
                || (curPage == 1 && mPageIndex == 1);
    }

    public int getPageIndex() {
        return mPageIndex;
    }

    public void loadNextPageArticles() {
        fetchArticlesAsync(mPageIndex);
    }

    private String prepareRequestUrl(int page) {
        return "http://www.devtf.cn/api/v1/?type=articles&page=" + page
                + "&count=20&category=1";
    }
}
