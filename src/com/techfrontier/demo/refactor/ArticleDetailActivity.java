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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.techfrontier.demo.R;
import com.techfrontier.demo.beans.ArticleDetail;

import org.tech.frontier.db.DatabaseHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

        ArticleDetail cacheDetail = DatabaseHelper.getInstance().loadArticleDetail(mPostId);
        if (!TextUtils.isEmpty(cacheDetail.content)) {
            loadArticle2Webview(cacheDetail.content);
        } else if (!TextUtils.isEmpty(mPostId)) {
            fetchArticleContentAsync();
        } else {
            mWebView.loadUrl(mJobUrl);
        }
    }

    private void fetchArticleContentAsync() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = performGetRequest(mPostId);
                    result = streamToString(urlConnection.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                return result;
            }

            private HttpURLConnection performGetRequest(String postId)
                    throws MalformedURLException, IOException {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(
                        "http://www.devtf.cn/api/v1/?type=article&post_id=" + postId)
                        .openConnection();
                urlConnection.connect();
                return urlConnection;
            }

            @Override
            protected void onPostExecute(String result) {
                loadArticle2Webview(result);
                DatabaseHelper.getInstance().saveArticleDetails(new ArticleDetail(mPostId, result));
            }
        }.execute();
    }

    private static String streamToString(InputStream inputStream) throws IOException {
        StringBuilder sBuilder = new StringBuilder();
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                inputStream));
        while ((line = bufferedReader.readLine()) != null) {
            sBuilder.append(line).append("\n");
        }
        return sBuilder.toString();
    }

    private void loadArticle2Webview(String htmlContent) {
        mWebView.loadDataWithBaseURL("", wrapHtml(mTitle, htmlContent),
                "text/html", "utf8", "404");
    }

    private static String wrapHtml(String title, String content) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html dir=\"ltr\" lang=\"zh\">");
        sb.append("<head>");
        sb.append("<meta name=\"viewport\" content=\"width=100%; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\" />");
        sb.append("<link rel=\"stylesheet\" href='file:///android_asset/style.css' type=\"text/css\" media=\"screen\" />");
        sb.append("<link rel=\"stylesheet\" href='file:///android_asset/default.min.css' type=\"text/css\" media=\"screen\" />");
        sb.append("</head>");
        sb.append("<body style=\"padding:0px 8px 8px 8px;\">");
        sb.append("<div id=\"pagewrapper\">");
        sb.append("<div id=\"mainwrapper\" class=\"clearfix\">");
        sb.append("<div id=\"maincontent\">");
        sb.append("<div class=\"post\">");
        sb.append("<div class=\"posthit\">");
        sb.append("<div class=\"postinfo\">");
        sb.append("<h2 class=\"thetitle\">");
        sb.append("<a>");
        sb.append(title);
        sb.append("</a>");
        sb.append("</h2>");
        sb.append("<hr/>");
        sb.append("</div>");
        sb.append("<div class=\"entry\">");
        sb.append(content);
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<script src=\'file:///android_asset/highlight.pack.js\'></script>");
        sb.append("<script>hljs.initHighlightingOnLoad();</script>");
        sb.append("</body>");
        sb.append("</html>");
        Log.e("", "html : " + sb.toString());
        return sb.toString();
    }

    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }
}
