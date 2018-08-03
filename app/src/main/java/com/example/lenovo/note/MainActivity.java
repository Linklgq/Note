package com.example.lenovo.note;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.lenovo.note.db.DBUtil;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.recy.MyDividerItemDecoration;
import com.example.lenovo.note.recy.MyViewHolder;
import com.example.lenovo.note.recy.NoteAdapter;
import com.example.lenovo.note.recy.NoteClickListener;
import com.example.lenovo.note.recy.SelectCountsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private NoteAdapter adapter;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private boolean select=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(select){
                    setSelect(false);
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initTestData();

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.content_main);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new NoteAdapter();
        adapter.setNoteClickListener(new NoteClickListener() {
            @Override
            public void onClick(MyViewHolder holder) {
                if(select){
                    adapter.select(holder);
                }
            }

            @Override
            public boolean onLongClick(MyViewHolder holder) {
                if(!select){
                    setSelect(true);
                    adapter.select(holder);
                    return true;
                }else{
                    return false;
                }
            }
        });
        adapter.setSelectCountsListener(new SelectCountsListener() {
            @Override
            public void setCounts(int counts) {
                if(select) {
                    if (counts == 0) {
                        setSelect(false);
                    } else {
                        toolbar.setTitle("选中 "+counts+" 项");
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);
        MyDividerItemDecoration divider=new MyDividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(divider);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(select){
            setSelect(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(select){
            toggle.setDrawerIndicatorEnabled(false);
            toolbar.setBackgroundColor(getResources().getColor(R.color.gray7));
            getMenuInflater().inflate(R.menu.select_main, menu);
        }else{
            toggle.setDrawerIndicatorEnabled(true);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(toggle.onOptionsItemSelected(item)){
            Toast.makeText(this, "fuck1", Toast.LENGTH_SHORT).show();
            return true;
        }
        int id = item.getItemId();
        switch(id){
            case android.R.id.home: {
                Toast.makeText(this, "fuck2", Toast.LENGTH_SHORT).show();
                if (select) {
                    setSelect(false);
                }
                break;
            }
            case R.id.menu_done_all: {
                adapter.selectAll();
                adapter.notifyDataSetChanged();
                break;
            }
            case R.id.menu_remove: {
                remove(adapter.getSelectedSet());
                setSelect(false);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
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
        if(this.select!=select){
            this.select=select;
            adapter.setSelect(this.select);
            if(!this.select){
                adapter.notifyDataSetChanged();
                toolbar.setTitle(R.string.app_name);
            }
            invalidateOptionsMenu();
        }
    }

    public void remove(Set<Integer> set){
        List<Note> tList=new ArrayList<>();
        for(int i:set){
            tList.add(DBUtil.get(i));
        }
        for(Note note:tList){
            DBUtil.remove(note);
        }
    }
}
