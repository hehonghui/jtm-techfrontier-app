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

import android.text.TextUtils;

import com.techfrontier.demo.beans.ArticleDetail;
import com.techfrontier.demo.mvpview.ArticleDetailView;

import org.tech.frontier.db.DatabaseHelper;
import org.tech.frontier.listeners.DataListener;
import org.tech.frontier.net.HtmlUtls;
import org.tech.frontier.net.HttpFlinger;

/**
 * 文章详情页面的Presenter,负责加载文章内容。如果数据库中有缓存，那么使用缓存，否则从网络上下载内容到本地，并存储。
 * 
 * @author mrsimple
 */
public class ArticleDetailPresenter extends BasePresenter<ArticleDetailView> {

    /**
     * 加载文章的具体内容,先从数据库中加载,如果数据库中有，那么则不会从网络上获取
     * 
     * @param postId
     */
    public void fetchArticleContent(final String postId,String title) {
        // 从数据库上获取文章内容缓存
        // ArticleDetail cacheDetail =
        // DatabaseHelper.getInstance().loadArticleDetail(postId);
        // String articleContent = cacheDetail.content;

        String articleContent = loadArticleContentFromDB(postId);
        if (!TextUtils.isEmpty(articleContent)) {
            String htmlContent = HtmlUtls.wrapArticleContent(title, articleContent);
            mView.onFetchedArticleContent(htmlContent);
        } else {
            fetchContentFromServer(postId, title);
        }
    }

    public String loadArticleContentFromDB(String postId) {
        return DatabaseHelper.getInstance().loadArticleDetail(postId).content;
    }

    protected void fetchContentFromServer(final String postId,final String title) {
        mView.onShowLoding();
        String reqURL = "http://www.devtf.cn/api/v1/?type=article&post_id=" + postId;
        HttpFlinger.get(reqURL,
                new DataListener<String>() {
                    @Override
                    public void onComplete(String result) {
                        mView.onHideLoding();
                        if (TextUtils.isEmpty(result)) {
                            result = "未获取到文章内容~";
                        }
                        mView.onFetchedArticleContent(HtmlUtls.wrapArticleContent(title, result));
                        DatabaseHelper.getInstance().saveArticleDetail(
                                new ArticleDetail(postId, result));
                    }
                });
    }
}
