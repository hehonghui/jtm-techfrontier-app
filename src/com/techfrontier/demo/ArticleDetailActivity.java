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

import com.techfrontier.demo.mvpview.ArticleDetailView;
import com.techfrontier.demo.presenter.ArticleDetailPresenter;

/**
 * 文章阅读页面,使用WebView加载文章。
 * 
 * @author mrsimple
 */
public class ArticleDetailActivity extends BaseActionBarActivity implements ArticleDetailView {

    ProgressBar mProgressBar;
    WebView mWebView;
    private String mPostId;
    private String mTitle ;
    String mJobUrl;
    ArticleDetailPresenter mPresenter = new ArticleDetailPresenter();

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
            mTitle = extraBundle.getString("title") ;
        } else {
            mJobUrl = extraBundle.getString("job_url");
        }

        mPresenter.attach(this);

        // 从数据库上获取文章内容缓存，如果没有缓存则从网络获取
        if (!TextUtils.isEmpty(mPostId)) {
            mPresenter.fetchArticleContent(mPostId, mTitle);
        } else {
            mWebView.loadUrl(mJobUrl);
        }
    }

    @Override
    public void onShowLoding() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHideLoding() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onFetchedArticleContent(String html) {
        mWebView.loadDataWithBaseURL("", html,
                "text/html", "utf8", "404");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detach();
    }
}
