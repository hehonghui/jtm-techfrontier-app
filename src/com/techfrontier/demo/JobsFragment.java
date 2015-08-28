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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techfrontier.demo.adapters.JobAdapter;
import com.techfrontier.demo.beans.Job;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tech.frontier.listeners.OnItemClickListener;
import org.tech.frontier.widgets.AutoLoadRecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JobsFragment extends Fragment {
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected AutoLoadRecyclerView mRecyclerView;
    JobAdapter mAdapter;
    final protected List<Job> mDataSet = new ArrayList<Job>();

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        mRecyclerView = (AutoLoadRecyclerView) rootView.findViewById(R.id.articles_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()
                .getApplicationContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setVisibility(View.VISIBLE);

        mAdapter = new JobAdapter(mDataSet);
        mAdapter.setOnItemClickListener(new OnItemClickListener<Job>() {

            @Override
            public void onClick(Job item) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("job_url", item.url);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(true);
        getJobs();
        return rootView;
    }

    private List<Job> performRequest() {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(
                    "http://www.devtf.cn/api/v1/?type=jobs")
                    .openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sBuilder.append(line).append("\n");
            }

            JSONArray jsonArray = new JSONArray(sBuilder.toString());
            List<Job> jobs = new ArrayList<Job>();
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                Job jobItem = new Job();
                jobItem.company = jsonObject.optString("company");
                jobItem.type = Integer.valueOf(jsonObject.optInt("type"));
                jobItem.job = jsonObject.optString("job");
                jobItem.desc = jsonObject.optString("job_desc");
                jobItem.email = jsonObject.optString("email");
                jobItem.url = jsonObject.optString("url");
                // 添加到集合中
                jobs.add(jobItem);
            }
            return jobs;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return null;
    }

    // private String readStream(InputStream stream) throws IOException {
    // BufferedReader bufferedReader = new BufferedReader(new
    // InputStreamReader(stream));
    // StringBuilder sBuilder = new StringBuilder();
    // String line = null;
    // while ((line = bufferedReader.readLine()) != null) {
    // sBuilder.append(line).append("\n");
    // }
    // return sBuilder.toString();
    // }
    //
    // public List<Job> parse(JSONArray data) {
    // List<Job> jobs = new ArrayList<Job>();
    // int length = data.length();
    // for (int i = 0; i < length; i++) {
    // JSONObject jsonObject = data.optJSONObject(i);
    // Job jobItem = new Job();
    // jobItem.company = jsonObject.optString("company");
    // jobItem.type = Integer.valueOf(jsonObject.optInt("type"));
    // jobItem.job = jsonObject.optString("job");
    // jobItem.desc = jsonObject.optString("job_desc");
    // jobItem.email = jsonObject.optString("email");
    // jobItem.url = jsonObject.optString("url");
    // // 添加到集合中
    // jobs.add(jobItem);
    // }
    // return jobs;
    // }

    private void getJobs() {
        new AsyncTask<Void, Void, List<Job>>() {
            @Override
            protected List<Job> doInBackground(Void... params) {
                return performRequest();
            }

            protected void onPostExecute(List<Job> result) {
                mDataSet.addAll(result);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            };
        }.execute();
    }
}
