package com.example.lenovo.note;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
    public static final int EDIT_NOTE=0;
    public static final int NEW_NOTE=1;
    private static final String TAG = "MainActivity";
    private NoteAdapter adapter;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private ActionMode actionMode;

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
                NoteEditActivity.startForResult(MainActivity.this,-1,NEW_NOTE);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final ActionMode.Callback callback=new ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                setSelect(true);
                MenuInflater inflater=mode.getMenuInflater();
                inflater.inflate(R.menu.select_main,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_done_all: {
                        adapter.selectAll();
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                    case R.id.menu_remove: {
                        remove(adapter.getSelectedSet());
                        if(actionMode!=null) {
                            actionMode.finish();
                        }
                        return true;
                    }
                    default:{
                        return false;
                    }
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                setSelect(false);
                actionMode=null;
            }
        };

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.content_main);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new NoteAdapter();
        adapter.setNoteClickListener(new NoteClickListener() {
            @Override
            public void onClick(MyViewHolder holder) {
                if(actionMode!=null){
                    adapter.select(holder);
                }else{
                    NoteEditActivity.startForResult(MainActivity.this
                            ,holder.getAdapterPosition(),EDIT_NOTE);
                }
            }

            @Override
            public boolean onLongClick(MyViewHolder holder) {
                if(actionMode==null) {
                    actionMode = startSupportActionMode(callback);
                }
                adapter.select(holder);
                return true;
            }
        });
        adapter.setSelectCountsListener(new SelectCountsListener() {
            @Override
            public void setCounts(int counts) {
                if(actionMode!=null) {
                    if (counts == 0) {
                        actionMode.finish();
                    } else {
                        actionMode.setTitle("选中 "+counts+" 项");
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
        } else {
            super.onBackPressed();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.notifyDataSetChanged();
//        Toast.makeText(this, "result", Toast.LENGTH_SHORT).show();
    }

    private void initTestData(){
        for(int i=0;i<20;i++){
            Note note=new Note();
            note.setContent(i+"android:ellipsize=\"end\"\nandroid:textStyle=\"bold\" ");
            note.setModifiedTime(System.currentTimeMillis());
            note.save();
        }
    }

    private void setSelect(boolean select) {
        adapter.setSelect(select);
        if (!select) {
            adapter.notifyDataSetChanged();
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
