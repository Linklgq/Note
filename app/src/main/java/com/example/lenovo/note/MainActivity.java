package com.example.lenovo.note;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.lenovo.note.db.Folder;
import com.example.lenovo.note.db.FolderDBUtil;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.db.NoteDBUtil;
import com.example.lenovo.note.recy.MyDividerItemDecoration;
import com.example.lenovo.note.recy.MyViewHolder;
import com.example.lenovo.note.recy.MyViewHolderFactory;
import com.example.lenovo.note.recy.NoteAdapter;
import com.example.lenovo.note.recy.NoteClickListener;
import com.example.lenovo.note.recy.SelectCountsListener;
import com.example.lenovo.note.util.AnimationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.example.lenovo.note.recy.MyViewHolderFactory.GRID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
//    public static void actionStart(Context context,int folderId,String folderName){
//        Intent intent=new Intent(context,MainActivity.class);
//        intent.putExtra("folderId",folderId);
//        intent.putExtra("folderName",folderName);
//        context.startActivity(intent);
//    }

    public static final int EDIT_NOTE = 0;
    public static final int NEW_NOTE = 1;
    public static final int NOTE_FOLDER = 2;
    private static final String TAG = "MainActivity";
    private final LinearLayoutManager DEFAULT_LAYOUT = new LinearLayoutManager(this);
    private final StaggeredGridLayoutManager GRID_LAYOUT = new StaggeredGridLayoutManager(2,
            StaggeredGridLayoutManager.VERTICAL);
    private int layoutType;
    private NoteAdapter adapter;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private ActionMode actionMode;
    private FloatingActionButton fab;
    private AlertDialog layoutDialog;
    private RecyclerView recyclerView;
    private String[] layoutItems = {"默认布局", "网格布局"};
    private int currentFolderId;
    AppCompatSpinner spinner;
    FolderSpinnerAdapter spinnerAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        spinner = (AppCompatSpinner) findViewById(R.id.spinner_folder);
        spinnerAdapter = new FolderSpinnerAdapter();
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: " + i);
                int t=-1;
                if(i>0) {
                    t = FolderDBUtil.get(i - 1).getId();
                    Log.d(TAG, "onItemSelected: folderdb "+t);
                }
                if(t!=currentFolderId) {
                    currentFolderId=t;
                    NoteDBUtil.setsFolderId(currentFolderId);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: nothing");
            }
        });

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        currentFolderId = pref.getInt("folderId", -1);
        if (currentFolderId < 0) {
            spinner.setSelection(0);
        } else {
            Folder folder = FolderDBUtil.findByFolderId(currentFolderId);
            if (folder == null) {
                currentFolderId = -1;
                spinner.setSelection(0);
            } else {
                spinner.setSelection(FolderDBUtil.getRank(currentFolderId) + 1);
            }
            Log.d(TAG, "onCreate: 便签夹");
        }
        NoteDBUtil.setsFolderId(currentFolderId);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteEditActivity.startForResult(MainActivity.this, -1, NEW_NOTE);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final ActionMode.Callback callback = new ActionMode.Callback() {
            boolean done = false;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                setSelect(true);
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.select_main, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_done_all: {
                        adapter.selectAll();
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                    case R.id.menu_remove: {
                        remove(adapter.getSelectedSet());
                        done = true;
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (!done) {
//                    adapter.notifyDataSetChanged();
                    Set<Integer> set = adapter.getSelectedSet();
                    for (int i : set) {
                        adapter.notifyItemChanged(i);
                    }
                }
                setSelect(false);
                actionMode = null;
                done = false;
            }
        };

        recyclerView = (RecyclerView) findViewById(R.id.content_main);
        recyclerView.setLayoutManager(DEFAULT_LAYOUT);
        recyclerView.setPadding(0, 8, 0, 8);
        adapter = new NoteAdapter();
        adapter.setNoteClickListener(new NoteClickListener() {
            @Override
            public void onClick(MyViewHolder holder) {
                if (actionMode != null) {
                    adapter.select(holder);
                } else {
                    NoteEditActivity.startForResult(MainActivity.this
                            , holder.getAdapterPosition(), EDIT_NOTE);
                }
            }

            @Override
            public boolean onLongClick(MyViewHolder holder) {
                if (actionMode == null) {
                    actionMode = startSupportActionMode(callback);
                }
                adapter.select(holder);
                return true;
            }
        });
        adapter.setSelectCountsListener(new SelectCountsListener() {
            @Override
            public void setCounts(int counts) {
                if (actionMode != null) {
                    if (counts == 0) {
                        actionMode.finish();
                    } else {
                        actionMode.setTitle("选中 " + counts + " 项");
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);
        // FIXME: 2018/8/15 
        recyclerView.getRecycledViewPool().setMaxRecycledViews(MyViewHolderFactory.DEFAULT, 15);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(GRID, 10);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.setScroll(false);
                    long time1 = System.currentTimeMillis();

                    if (layoutType == GRID) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if(GRID_LAYOUT.getItemCount()==0){
                                    return;
                                }
                                int[] intoStart = GRID_LAYOUT.findFirstVisibleItemPositions(null);
                                int[] intoEnd = GRID_LAYOUT.findLastVisibleItemPositions(null);
                                int start = intoStart[0] < intoStart[1] ? intoStart[0] : intoStart[1];
                                int end = intoEnd[0] > intoEnd[1] ? intoEnd[0] : intoEnd[1];
                                start = start < 0 ? 0 : start;
                                end = end < 0 ? 0 : end;
                                for (int i = start; i <= end; i++) {
                                    ((MyViewHolder) recyclerView.findViewHolderForLayoutPosition(i))
                                            .updateView();
                                }
                            }
                        });
                    }
                    long time2 = System.currentTimeMillis();
                    Log.d(TAG, "onScrollStateChanged: " + (time2 - time1) + "ms");
                } else {
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
        MyDividerItemDecoration divider = new MyDividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(divider);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putInt("folderId", currentFolderId);
            editor.apply();
            Log.d(TAG, "onSaveInstanceState: " + currentFolderId);
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt("folderId", currentFolderId);
        editor.apply();
        Log.d(TAG, "onSaveInstanceState: " + currentFolderId);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.all_note: {
                Intent intent = new Intent(this, FolderActivity.class);
                startActivityForResult(intent, NOTE_FOLDER);
                break;
            }
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem=menu.findItem(R.id.action_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_layout: {
                if (layoutDialog == null) {
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
//        adapter.notifyDataSetChanged();
        // FIXME: 2018/8/16 完善
        if (requestCode == NEW_NOTE) {
            if (resultCode == RESULT_OK) {
                int id = data.getIntExtra("id", 0);
                int position = NoteDBUtil.getRank(id);
                adapter.notifyItemInserted(position);
                recyclerView.smoothScrollToPosition(position);
                Log.d(TAG, "onActivityResult: " + position);
            }
        } else if (requestCode == EDIT_NOTE) {
            if (resultCode == RESULT_OK) {
                int index = data.getIntExtra("index", 0);
                int id = data.getIntExtra("id", 0);
                int position = NoteDBUtil.getRank(id);
                adapter.notifyItemMoved(index, position);
                adapter.notifyItemChanged(position);
                // TODO: 2018/8/16 用scrollToPosition没效果，找原因
                recyclerView.smoothScrollToPosition(position);

                Log.d(TAG, "onActivityResult: change from " + index + " to " + position);
            } else if (resultCode == RESULT_FIRST_USER) {
                int index = data.getIntExtra("index", 0);
                adapter.notifyItemRemoved(index);
                Log.d(TAG, "onActivityResult: remove " + index);
            }
        } else if (requestCode == NOTE_FOLDER) {
            if(actionMode!=null){
                adapter.setSelect(false);
                actionMode.finish();
            }

            spinnerAdapter.notifyDataSetChanged();
//            spinner.setAdapter(spinnerAdapter);
            if (resultCode == RESULT_CANCELED) {
                // 判断便签夹是否已经被删除了
                Folder folder = FolderDBUtil.findByFolderId(currentFolderId);
                if (folder == null) {   // 被删除，切换到全部便签
                    currentFolderId = -1;
                    spinner.setSelection(0);
//                    NoteDBUtil.setsFolderId(currentFolderId);
                }
//                else {     // 没被删除也有可能被清空了，更新数据
//                    adapter.notifyDataSetChanged();
//                }
            } else if (resultCode == RESULT_OK) {
                currentFolderId = data.getIntExtra("folderId", -1);
                Log.d(TAG, "onActivityResult: folderdb "+currentFolderId);
//                NoteDBUtil.setsFolderId(currentFolderId);
                if (currentFolderId < 0) {
                    spinner.setSelection(0);
                } else {
                    spinner.setSelection(FolderDBUtil.getRank(currentFolderId) + 1);
                }
            }

            NoteDBUtil.setsFolderId(currentFolderId);
            adapter.notifyDataSetChanged();
        }
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        int folderId=intent.getIntExtra("folderId",-1);
//        NoteDBUtil.setsFolderId(folderId);
//        toolbar.setTitle(intent.getStringExtra("folderName"));
//        adapter.notifyDataSetChanged();
//    }

    private void createLayoutDialog() {
//        Toast.makeText(this, "create layoutdialog", Toast.LENGTH_SHORT).show();
        layoutDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(layoutItems, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if ("默认布局".equals(layoutItems[i])) {
                            if (adapter.setLayoutType(MyViewHolderFactory.DEFAULT)) {
                                recyclerView.setLayoutManager(DEFAULT_LAYOUT);
                                recyclerView.setPadding(0, 0, 0, 8);
                                layoutType = MyViewHolderFactory.DEFAULT;
                            }
                        } else if ("网格布局".equals(layoutItems[i])) {
                            if (adapter.setLayoutType(GRID)) {
                                recyclerView.setLayoutManager(GRID_LAYOUT);
                                recyclerView.setPadding(12, 0, 12, 12);
                                layoutType = GRID;
                            }
                        }
                        layoutDialog.dismiss();
                    }
                })
                .create();
    }

    private void setSelect(boolean select) {
        adapter.setSelect(select);
        if (select) {
            AnimationUtil.animateOut(fab, AnimationUtil.INTERPOLATOR, null);
        } else {
//            adapter.notifyDataSetChanged();
            AnimationUtil.animateIn(fab, AnimationUtil.INTERPOLATOR, null);
        }
    }

    public void remove(Set<Integer> set) {
        List<Integer> mList = new ArrayList<>(set.size());
        for (Integer integer : set) {
            mList.add(integer);
        }
        // 升序排序
        Collections.sort(mList);

        Note note;
        int position;
        for (int i = 0; i < mList.size(); i++) {
            position = mList.get(i) - i;
            note = NoteDBUtil.get(position);
            NoteDBUtil.remove(note);
            adapter.notifyItemRemoved(position);
        }
    }
}
