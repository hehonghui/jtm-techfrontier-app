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

package org.tech.frontier.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.techfrontier.demo.beans.Article;
import com.techfrontier.demo.beans.ArticleDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章基本信息、文章内容的数据库操作类,包含Article、ArticleDetail的存储、删除操作。 TODO :
 * DatabaseHelper类中直接操作了两个表的查询、删除等功能，职责过多,需要重构 !
 * 
 * @author mrsimple
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_ARTICLES = "articles";
    public static final String TABLE_ARTICLE_CONTENT = "article_content";

    private static final String CREATE_ARTICLES_TABLE_SQL = "CREATE TABLE articles (  "
            + " post_id INTEGER PRIMARY KEY UNIQUE, "
            + " author VARCHAR(30) NOT NULL ,"
            + " title VARCHAR(50) NOT NULL,"
            + " category INTEGER ,"
            + " publish_time VARCHAR(50) "
            + " )";

    private static final String CREATE_ARTICLE_CONTENT_TABLE_SQL = "CREATE TABLE article_content (  "
            + " post_id INTEGER PRIMARY KEY UNIQUE, "
            + " content TEXT NOT NULL "
            + " )";

    static final String DB_NAME = "tech_frontier.db";
    static final int DB_VERSION = 1;
    private SQLiteDatabase mDatabase;
    static DatabaseHelper sDatabaseHelper;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mDatabase = getWritableDatabase();
    }

    public static void init(Context context) {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = new DatabaseHelper(context);
        }
    }

    public static DatabaseHelper getInstance() {
        if (sDatabaseHelper == null) {
            throw new NullPointerException("sDatabaseHelper is null,please call init method first.");
        }
        return sDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ARTICLES_TABLE_SQL);
        db.execSQL(CREATE_ARTICLE_CONTENT_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_ARTICLES);
        db.execSQL("DROP TABLE " + TABLE_ARTICLE_CONTENT);
        onCreate(db);
    }

    public void saveArticles(List<Article> dataList) {
        for (Article article : dataList) {
            saveSingleArticle(article);
        }
    }

    public void saveSingleArticle(Article article) {
        mDatabase.insertWithOnConflict(TABLE_ARTICLES, null, article2ContentValues(article),
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    private ContentValues article2ContentValues(Article item) {
        ContentValues newValues = new ContentValues();
        newValues.put("post_id", item.post_id);
        newValues.put("author", item.author);
        newValues.put("title", item.title);
        newValues.put("category", item.category);
        newValues.put("publish_time", item.publishTime);
        return newValues;
    }

    public List<Article> loadArticles() {
        Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_ARTICLES, null);
        List<Article> result = parseArticles(cursor);
        cursor.close();
        return result;
    }

    private List<Article> parseArticles(Cursor cursor) {
        List<Article> articles = new ArrayList<Article>();
        while (cursor.moveToNext()) {
            Article item = new Article();
            item.post_id = cursor.getString(0);
            item.author = cursor.getString(1);
            item.title = cursor.getString(2);
            item.category = cursor.getInt(3);
            item.publishTime = cursor.getString(4);
            // 解析数据
            articles.add(item);
        }
        return articles;
    }

    public void deleteAllArticles() {
        mDatabase.delete(TABLE_ARTICLES, null, null);
        Log.e("", "### delete all articles");
    }

    public void saveArticleDetail(ArticleDetail detail) {
        mDatabase.insertWithOnConflict(TABLE_ARTICLE_CONTENT, null,
                articleDetailtoContentValues(detail),
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ArticleDetail loadArticleDetail(String postId) {
        Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_ARTICLE_CONTENT
                + " where post_id = "
                + postId, null);
        ArticleDetail detail = parseArticleDetail(cursor);
        cursor.close();
        return detail;
    }

    private ArticleDetail parseArticleDetail(Cursor cursor) {
        ArticleDetail articleDetail = new ArticleDetail();
        if (cursor.getCount() <= 0) {
            return articleDetail;
        }
        articleDetail.postId = cursor.getString(0);
        articleDetail.content = cursor.getString(1);
        return articleDetail;
    }

    protected ContentValues articleDetailtoContentValues(ArticleDetail detail) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("post_id", detail.postId);
        contentValues.put("content", detail.content);
        return contentValues;
    }

    public List<ArticleDetail> loadAllArticleDetails() {
        List<ArticleDetail> articleDetails = new ArrayList<ArticleDetail>();
        Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_ARTICLE_CONTENT, null);
        while (cursor.moveToNext()) {
            articleDetails.add(parseArticleDetail(cursor));
        }
        cursor.close();
        return articleDetails;
    }

    public void deleteAllArticleContent() {
        mDatabase.delete(TABLE_ARTICLE_CONTENT, null, null);
    }

}
