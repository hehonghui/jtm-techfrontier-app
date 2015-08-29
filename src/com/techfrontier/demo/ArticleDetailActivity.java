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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.techfrontier.demo.beans.ArticleDetail;

import org.tech.frontier.db.DatabaseHelper;
import org.tech.frontier.listeners.DataListener;
import org.tech.frontier.net.HtmlUtls;
import org.tech.frontier.net.HttpFlinger;

/**
 * 文章阅读页面,使用WebView加载文章。
 * 
 * @author mrsimple
 */
public class ArticleDetailActivity extends BaseActionBarActivity {

    ProgressBar mProgressBar;
    WebView mWebView;
    private String mPostId;
    private String mTitle;
    String mJobUrl;

    @Override
    protected int getContentViewResId() {
        return R.layout.activity_detail;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initWidgets() {
        mProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        mWebView = (WebView) findViewById(R.id.articles_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WebSettings settings = mWebView.getSettings();
                settings.setBuiltInZoomControls(true);
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void afterOnCreate() {
        Bundle extraBundle = getIntent().getExtras();
        if (extraBundle != null && !extraBundle.containsKey("job_url")) {
            mPostId = extraBundle.getString("post_id");
            mTitle = extraBundle.getString("title");
        } else {
            mJobUrl = extraBundle.getString("job_url");
        }

        // 从数据库上获取文章内容缓存
        ArticleDetail cacheDetail = DatabaseHelper.getInstance().loadArticleDetail(mPostId);
        if (!TextUtils.isEmpty(cacheDetail.content)) {
            loadArticle2Webview(cacheDetail.content);
        } else if (!TextUtils.isEmpty(mPostId)) {
            fetchArticleContent();
        } else {
            mWebView.loadUrl(mJobUrl);
        }
    }

    private void fetchArticleContent() {
        String reqURL = "http://www.devtf.cn/api/v1/?type=article&post_id=" + mPostId;
        HttpFlinger.get(reqURL,
                new DataListener<String>() {
                    @Override
                    public void onComplete(String result) {
                        loadArticle2Webview(result);
                        DatabaseHelper.getInstance().saveArticleDetail(
                                new ArticleDetail(mPostId, result));
                    }
                });
    }

    private void loadArticle2Webview(String htmlContent) {
        mWebView.loadDataWithBaseURL("", HtmlUtls.wrapArticleContent(mTitle, htmlContent),
                "text/html", "utf8", "404");
    }
}
