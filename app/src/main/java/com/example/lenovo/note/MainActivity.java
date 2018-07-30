package com.example.lenovo.note;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.lenovo.note.db.Note;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private NoteAdapter adapter;
    private Toolbar toolbar;
    private Toolbar selectToolbar;
    private boolean isSelect=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        selectToolbar=(Toolbar)findViewById(R.id.select_toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initTestData();

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.content_main);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
//        StaggeredGridLayoutManager layoutManager=new StaggeredGridLayoutManager(
//                2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new NoteAdapter(this);
        recyclerView.setAdapter(adapter);
//        DividerItemDecoration divider=new DividerItemDecoration(this,
//                DividerItemDecoration.VERTICAL);
//        divider.setDrawable(ContextCompat.getDrawable(this,R.drawable.divider_rect));
//        recyclerView.addItemDecoration(divider);
        MyDividerItemDecoration divider=new MyDividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(divider);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(isSelect){
            setSelect(false);
            adapter.setSelect(false);
            adapter.notifyDataSetChanged();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        menu.clear();
//        Log.d(TAG, "onCreateOptionsMenu: "+menu.hasVisibleItems());
        if(isSelect){
            getMenuInflater().inflate(R.menu.select_main, menu);

            ActionBar actionBar=getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }else {
            getMenuInflater().inflate(R.menu.main, menu);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }
//        Log.d(TAG, "onCreateOptionsMenu: create menu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                if(isSelect){
                    setSelect(false);
                    adapter.setSelect(false);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.menu_done_all:
                adapter.selectAll();
                adapter.notifyDataSetChanged();
                break;
            case R.id.menu_remove:
                adapter.removeSelect();
                adapter.setSelect(false);
                adapter.notifyDataSetChanged();
                setSelect(false);
                break;
        }

//        return super.onOptionsItemSelected(item);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initTestData(){
        for(int i=0;i<20;i++){
            Note note=new Note();
            note.setContent(i+"android:ellipsize=\"end\"\nandroid:textStyle=\"bold\" ");
            note.setModifiedTime(System.currentTimeMillis());
            note.save();
        }
    }

    public void setSelect(boolean select) {
        isSelect = select;
        if(isSelect){
            toolbar.setVisibility(View.GONE);
            selectToolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(selectToolbar);
        }else{
            selectToolbar.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);
        }

    }

    public void setSelectedCounts(int counts){
        if(counts==0){
            setSelect(false);
            adapter.setSelect(false);
            adapter.notifyDataSetChanged();
        }else{
            selectToolbar.setTitle("选择 "+counts+" 项");
        }
    }


}
