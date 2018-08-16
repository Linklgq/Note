package com.example.lenovo.note;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.lenovo.note.db.DBUtil;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.recy.MyDividerItemDecoration;
import com.example.lenovo.note.recy.MyViewHolder;
import com.example.lenovo.note.recy.MyViewHolderFactory;
import com.example.lenovo.note.recy.NoteAdapter;
import com.example.lenovo.note.recy.NoteClickListener;
import com.example.lenovo.note.recy.SelectCountsListener;
import com.example.lenovo.note.util.AnimationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.lenovo.note.recy.MyViewHolderFactory.GRID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int EDIT_NOTE=0;
    public static final int NEW_NOTE=1;
    private static final String TAG = "MainActivity";
    private final LinearLayoutManager DEFAULT_LAYOUT=new LinearLayoutManager(this);
    private final StaggeredGridLayoutManager GRID_LAYOUT=new StaggeredGridLayoutManager(2,
            StaggeredGridLayoutManager.VERTICAL);
    private int layoutType;
    private NoteAdapter adapter;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private ActionMode actionMode;
    private FloatingActionButton fab;
    private AlertDialog layoutDialog;
    private RecyclerView recyclerView;
    private String[] layoutItems={"默认布局","网格布局"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
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

        recyclerView=(RecyclerView)findViewById(R.id.content_main);
        recyclerView.setLayoutManager(DEFAULT_LAYOUT);
        recyclerView.setPadding(0,8,0,8);
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
        // FIXME: 2018/8/15 
        recyclerView.getRecycledViewPool().setMaxRecycledViews(MyViewHolderFactory.DEFAULT,15);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(GRID,10);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE){
                    adapter.setScroll(false);
//                    adapter.notifyDataSetChanged();
//                    GRID_LAYOUT.invalidateSpanAssignments();
                    long time1=System.currentTimeMillis();

                    if(layoutType==MyViewHolderFactory.GRID){
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                int[] intoStart=GRID_LAYOUT.findFirstVisibleItemPositions(null);
                                int[] intoEnd=GRID_LAYOUT.findLastVisibleItemPositions(null);
                                int start=intoStart[0]<intoStart[1]?intoStart[0]:intoStart[1];
                                int end=intoEnd[0]>intoEnd[1]?intoEnd[0]:intoEnd[1];
                                for(int i=start;i<=end;i++){
                                    ((MyViewHolder)recyclerView.findViewHolderForLayoutPosition(i))
                                            .updateView();
                                }
                            }
                        });
                    }
                    long time2=System.currentTimeMillis();
                    Log.d(TAG, "onScrollStateChanged: "+(time2-time1)+"ms");
                }else{
                    adapter.setScroll(true);
                }
            }
        });
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.setWidth(recyclerView.getWidth());
//                adapter.notifyDataSetChanged();
            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_layout:{
                if(layoutDialog==null){
                    createLayoutDialog();
                }
                layoutDialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.notifyDataSetChanged();
//        Toast.makeText(this, "result", Toast.LENGTH_SHORT).show();
    }

    private void createLayoutDialog(){
        Toast.makeText(this, "create layoutdialog", Toast.LENGTH_SHORT).show();
         layoutDialog=new AlertDialog.Builder(this)
                .setSingleChoiceItems(layoutItems, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if("默认布局".equals(layoutItems[i])){
                            if(adapter.setLayoutType(MyViewHolderFactory.DEFAULT)) {
                                recyclerView.setLayoutManager(DEFAULT_LAYOUT);
                                recyclerView.setPadding(0,0,0,8);
                                layoutType=MyViewHolderFactory.DEFAULT;
                            }
                        }else if("网格布局".equals(layoutItems[i])){
                            if(adapter.setLayoutType(MyViewHolderFactory.GRID)){
                                recyclerView.setLayoutManager(GRID_LAYOUT);
                                recyclerView.setPadding(12,0,12,12);
                                layoutType=MyViewHolderFactory.GRID;
                            }
                        }
                        layoutDialog.dismiss();
                    }
                })
                .create();
    }

    private void setSelect(boolean select) {
        adapter.setSelect(select);
        if(select){
            AnimationUtil.animateOut(fab,AnimationUtil.INTERPOLATOR,null);
        }else {
            adapter.notifyDataSetChanged();
            AnimationUtil.animateIn(fab,AnimationUtil.INTERPOLATOR,null);
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
