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

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.tech.frontier.demo.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 适用于RecyclerView的抽象Adapter，封装了数据集、ViewHolder的创建与绑定过程,简化子类的操作
 *
 * @param <D> 数据集中的类型，例如Article等
 * @param <V> ViewHolder类型
 */
public abstract class RecyclerBaseAdapter<D, V extends ViewHolder> extends Adapter<V> {
    /**
     * RecyclerView中的数据集
     */
    protected final List<D> mDataSet = new ArrayList<D>();
    /**
     * 点击事件处理回调
     */
    private OnItemClickListener<D> mItemClickListener;

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    protected D getItem(int position) {
        return mDataSet.get(position);
    }

    public void addItems(List<D> items) {
        // 移除已经存在的数据,避免数据重复
        items.removeAll(mDataSet) ;
        // 添加新数据
        mDataSet.addAll(items);
        notifyDataSetChanged();
    }
    
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    /*
     * 绑定数据,主要分为两步,绑定数据与设置每项的点击事件处理
     * @see
     * android.support.v7.widget.RecyclerView.Adapter#onBindViewHolder(android
     * .support.v7.widget.RecyclerView.ViewHolder, int)
     */
    @Override
    public final void onBindViewHolder(V viewHolder, int position) {
        final D item = getItem(position);
        bindDataToItemView(viewHolder, item);
        setupItemViewClickListener(viewHolder, item);
    }

    protected View inflateItemView(ViewGroup viewGroup, int layoutId) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
    }

    public void setOnItemClickListener(OnItemClickListener<D> mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    /**
     * ItemView的点击事件
     * 
     * @param viewHolder
     * @param position
     */
    protected void setupItemViewClickListener(V viewHolder, final D item) {
        viewHolder.itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(item);
                }
            }
        });
    }

    /**
     * 将数据绑定到ItemView上
     */
    protected abstract void bindDataToItemView(V viewHolder, D item);

}
