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

package org.tech.frontier.net;

import android.os.AsyncTask;

import org.tech.frontier.listeners.DataListener;
import org.tech.frontier.net.parser.DefaultParser;
import org.tech.frontier.net.parser.RespParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 网络执行引擎
 * 
 * @author mrsimple
 */
public final class HttpFlinger {

    private static final DefaultParser DEFAULT_PARSER = new DefaultParser();

    private HttpFlinger() {
    }

    public static void get(String reqUrl, DataListener<String> listener) {
        get(reqUrl, DEFAULT_PARSER, listener);
    }

    /**
     * 发送get请求
     * 
     * @param reqUrl 网页地址
     * @param listener 回调,执行在UI线程
     */
    public static <T> void get(final String reqUrl, final RespParser<T> parser,
            final DataListener<T> listener) {
        new AsyncTask<Void, Void, T>() {
            @Override
            protected T doInBackground(Void... params) {
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) new URL(reqUrl)
                            .openConnection();
                    urlConnection.connect();
                    String result = streamToString(urlConnection.getInputStream());
                    return parser.parseResponse(result);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                // TODO : 当请求失败时数据会返回为null,导致很多地方需要判空,如何优化这一步呢?
                return null;
            }

            @Override
            protected void onPostExecute(T result) {
                if (listener != null) {
                    listener.onComplete(result);
                }
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

}
