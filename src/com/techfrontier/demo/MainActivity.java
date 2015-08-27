
package com.techfrontier.demo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;

import com.techfrontier.demo.adapters.BaseAdapter.OnItemClickListener;
import com.techfrontier.demo.adapters.MenuAdapter;
import com.techfrontier.demo.beans.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    protected FragmentManager mFragmentManager;
    protected int mFrgmContainer;
    Fragment mArticleFragment = new ArticlesFragment();
    Fragment mJobFragment = new JobsFragment();
    private DrawerLayout mDrawerLayout;
    private RecyclerView mMenuRecyclerView;
    protected Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getFragmentManager();
        // 设置Fragment Container
        setFragmentContainer(R.id.articles_container);
        initViews();
        mFragmentManager.beginTransaction().add(R.id.articles_container, mArticleFragment)
                .commitAllowingStateLoss();
    }

    private void initViews() {
        setupToolbar();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        initMenuLayout();
    }

    private void initMenuLayout() {
        mMenuRecyclerView = (RecyclerView) findViewById(R.id.menu_recyclerview);
        mMenuRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        setupMenuRecyclerView();
    }

    private void setupMenuRecyclerView() {
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem(getString(R.string.all), R.drawable.home));
        menuItems.add(new MenuItem(getString(R.string.jobs), R.drawable.hire_icon));
        menuItems.add(new MenuItem(getString(R.string.about_menu), R.drawable.about));
        menuItems.add(new MenuItem(getString(R.string.exit), R.drawable.exit));
        MenuAdapter menuAdapter = new MenuAdapter(menuItems);
        menuAdapter.setOnItemClickListener(new OnItemClickListener<MenuItem>() {
            @Override
            public void onClick(MenuItem item) {
                clickMenuItem(item);
            }
        });
        mMenuRecyclerView.setAdapter(menuAdapter);
    }

    protected void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void clickMenuItem(MenuItem item) {
        mDrawerLayout.closeDrawers();
        switch (item.iconResId) {
            case R.drawable.home: // 全部
                // mArticlesFragment.setArticleCategory(Article.ALL);
                // mArticlesFragment.fetchDatas();
                // replaceFragment(mArticlesFragment);
                break;
            case R.drawable.hire_icon: // 招聘信息
                if (mJobFragment == null) {
                    mJobFragment = new JobsFragment();
                }
                replaceFragment(mJobFragment);
                break;

            case R.drawable.exit: // 退出
                finish();
                break;

            default:
                break;
        }
    }

    protected void setFragmentContainer(int container) {
        mFrgmContainer = container;
    }

    protected void addFragment(Fragment fragment) {
        mFragmentManager.beginTransaction().add(mFrgmContainer, fragment).commit();
    }

    protected void replaceFragment(Fragment fragment) {
        mFragmentManager.beginTransaction().replace(mFrgmContainer, fragment).commit();
    }
}
