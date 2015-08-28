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

package com.techfrontier.demo.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.techfrontier.demo.R;
import com.techfrontier.demo.adapters.ArticleAdapter.ArticleViewHolder;
import com.techfrontier.demo.beans.Article;

import org.tech.frontier.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页文章列表的Adapter
 * 
 * @author mrsimple
 */
public class ArticleAdapter extends Adapter<ArticleViewHolder> {
    List<Article> mDataSet = new ArrayList<Article>();
    OnItemClickListener<Article> mItemClickListener;

    public ArticleAdapter(List<Article> dataSet) {
        mDataSet = dataSet;
    }

    protected Article getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public final void onBindViewHolder(ArticleViewHolder viewHolder, int position) {
        final Article item = getItem(position);
        bindArticleToItemView(viewHolder, item);
        setupItemViewClickListener(viewHolder, item);
    }

    public void setOnItemClickListener(OnItemClickListener<Article> mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    /**
     * ItemView的点击事件
     * 
     * @param viewHolder
     * @param position
     */
    protected void setupItemViewClickListener(ArticleViewHolder viewHolder, final Article item) {
        viewHolder.itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(item);
                }
            }
        });
    }

    protected View inflateItemView(ViewGroup viewGroup, int layoutId, boolean attach) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, attach);
    }

    protected void bindArticleToItemView(ArticleViewHolder viewHolder, Article item) {
        viewHolder.titleTv.setText(item.title);
        viewHolder.publishTimeTv.setText(item.publishTime);
        viewHolder.authorTv.setText(item.author);
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return createArticleViewHolder(viewGroup);
    }

    protected ArticleViewHolder createArticleViewHolder(ViewGroup viewGroup) {
        return new ArticleViewHolder(inflateItemView(viewGroup, R.layout.recyclerview_article_item,
                false));
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTv;
        public TextView publishTimeTv;
        public TextView authorTv;

        public ArticleViewHolder(View itemView) {
            super(itemView);

            titleTv = (TextView) itemView.findViewById(R.id.article_title_tv);
            publishTimeTv = (TextView) itemView.findViewById(R.id.article_time_tv);
            authorTv = (TextView) itemView.findViewById(R.id.article_author_tv);
        }
    }
}
