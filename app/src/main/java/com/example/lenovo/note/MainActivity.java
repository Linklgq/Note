package com.example.lenovo.note;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.example.lenovo.note.db.Folder;
import com.example.lenovo.note.db.FolderDBHelper;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.db.NoteDBHelper;
import com.example.lenovo.note.db.Order;
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
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static final int EDIT_NOTE = 0;
    public static final int NEW_NOTE = 1;
    public static final int NOTE_FOLDER = 2;

    private static final String TAG = "MainActivity";

    private final LinearLayoutManager DEFAULT_LAYOUT = new LinearLayoutManager(this);
    private final StaggeredGridLayoutManager GRID_LAYOUT = new StaggeredGridLayoutManager(2,
            StaggeredGridLayoutManager.VERTICAL);

    private int layoutType;
    private Order orderType;
    private int dbType = NoteDBHelper.GENERAL;

    private NoteAdapter adapter;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private ActionMode actionMode;
    private FloatingActionButton fab;
    private AlertDialog layoutDialog;
    private AlertDialog orderDialog;
    private RecyclerView recyclerView;
    private String[] layoutItems;
    private String layoutLinear;
    private String layoutGrid;
    private String[] orderItems;
    private int currentFolderId;
    AppCompatSpinner spinner;
    FolderSpinnerAdapter spinnerAdapter;
    private SearchView mSearchView;
    private boolean isSearching = false;

    private final List<Note> mTempNotes = new ArrayList<>();
    private final List<Integer> mTempInts = new ArrayList<>();
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        layoutItems = res.getStringArray(R.array.layout_items);
        layoutLinear = res.getString(R.string.layout_linear);
        layoutGrid = res.getString(R.string.layout_grid);
        orderItems = res.getStringArray(R.array.order_items);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("回收站");
        actionBar.setDisplayShowTitleEnabled(false);

        spinner = (AppCompatSpinner) findViewById(R.id.spinner_folder);
        spinnerAdapter = new FolderSpinnerAdapter();
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: spinner "+i);
                int t = -1;
                if (i > 0) {
                    t = FolderDBHelper.get(i - 1).getId();
                }
                if (t != currentFolderId) {
                    currentFolderId = t;
                    NoteDBHelper.setsFolderId(currentFolderId);
                    adapter.notifyDataSetChanged();
                    updateRecyBg();

                    AnimationUtil.animateIn(fab, AnimationUtil.INTERPOLATOR, null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        loadPrefs();
        NoteDBHelper.setsOrder(orderType);

        if (currentFolderId < 0) {
            spinner.setSelection(0);
        } else {
            Folder folder = FolderDBHelper.findByFolderId(currentFolderId);
            if (folder == null) {
                currentFolderId = -1;
                spinner.setSelection(0);
            } else {
                spinner.setSelection(FolderDBHelper.getRank(currentFolderId) + 1);
            }
        }
        NoteDBHelper.setsFolderId(currentFolderId);
        NoteDBHelper.setType(dbType);

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
                if (dbType == NoteDBHelper.GENERAL) {
                    inflater.inflate(R.menu.select_main, menu);
                } else if (dbType == NoteDBHelper.REMOVED) {
                    inflater.inflate(R.menu.select_recycle, menu);
                }
                setStatusBarColor(getResources().getColor(R.color.gray3));
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
                        if (dbType == NoteDBHelper.GENERAL) {
                            mTempInts.clear();
                            mTempInts.addAll(adapter.getSelectedSet());
                            Collections.sort(mTempInts);
                            mTempNotes.clear();
                            for (int i = 0; i < mTempInts.size(); i++) {
                                mTempNotes.add(NoteDBHelper.get(mTempInts.get(i)));
                            }

                            remove(adapter.getSelectedSet(), false);
                            done = true;
                            if (actionMode != null) {
                                actionMode.finish();
                            }

                            mSnackbar = Snackbar.make(recyclerView, "已将所选" + mTempInts.size() + "项移至回收站",
                                    Snackbar.LENGTH_LONG)
                                    .setAction("撤销", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d(TAG, "onClick: 撤销");
                                            for (int i = 0; i < mTempInts.size(); i++) {
                                                NoteDBHelper.restore(mTempNotes.get(i));
                                                adapter.notifyItemInserted(mTempInts.get(i));
                                            }
                                            updateRecyBg();
                                            if (layoutType == MyViewHolderFactory.GRID) {
                                                recyclerView.smoothScrollToPosition(mTempInts.get(0));
                                            }
                                            mTempNotes.clear();
                                            mTempInts.clear();
                                        }
                                    });
                            mSnackbar.show();
                        } else if (dbType == NoteDBHelper.REMOVED) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("所选便签将被永久删除。确定继续？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            remove(adapter.getSelectedSet(), true);
                                            done = true;
                                            if (actionMode != null) {
                                                actionMode.finish();
                                            }
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                        return true;
                    }
                    case R.id.menu_restore: {
                        restore(adapter.getSelectedSet());
                        done = true;
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        return true;
                    }
                    default: {
                        return true;
                    }
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (done) {
                    updateRecyBg();
                } else {
                    Set<Integer> set = adapter.getSelectedSet();
                    for (int i : set) {
                        adapter.notifyItemChanged(i);
                    }
                }
                setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                setSelect(false);
                actionMode = null;
                done = false;
            }
        };

        recyclerView = (RecyclerView) findViewById(R.id.content_main);

        updateRecyLayout();

        adapter = new NoteAdapter();
        adapter.setLayoutType(layoutType);
        adapter.setNoteClickListener(new NoteClickListener() {
            @Override
            public void onClick(MyViewHolder holder) {
                if (dbType == NoteDBHelper.GENERAL) {
                    if (actionMode != null) {
                        adapter.select(holder);
                    } else {
                        NoteEditActivity.startForResult(MainActivity.this
                                , holder.getAdapterPosition(), EDIT_NOTE);
                    }
                } else if (dbType == NoteDBHelper.REMOVED) {
                    if (actionMode == null) {
                        actionMode = startSupportActionMode(callback);
                    }
                    adapter.select(holder);
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
        recyclerView.getRecycledViewPool().setMaxRecycledViews(MyViewHolderFactory.DEFAULT, 15);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(GRID, 10);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.setScroll(false);

                    if (layoutType == MyViewHolderFactory.GRID) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (GRID_LAYOUT.getItemCount() == 0) {
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
                } else {
                    adapter.setScroll(true);
                }
            }
        });
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.setWidth(recyclerView.getWidth());
                if (layoutType == MyViewHolderFactory.GRID) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
        MyDividerItemDecoration divider = new MyDividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(divider);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        updateRecyBg();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            savePrefs();
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savePrefs();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.my_notes: {
                if (dbType != NoteDBHelper.GENERAL) {
                    dbType = NoteDBHelper.GENERAL;
                    NoteDBHelper.setType(dbType);
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    spinner.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onNavigationItemSelected: "+currentFolderId);
                    spinnerAdapter.notifyDataSetChanged();
                    if(currentFolderId<0){
                        adapter.notifyDataSetChanged();
                        updateRecyBg();
                    }else{
                        spinner.setSelection(0);
                    }
                    AnimationUtil.animateIn(fab, AnimationUtil.INTERPOLATOR, null);
                }
                break;
            }
            case R.id.note_folder: {
                if (actionMode != null) {
                    actionMode.finish();
                }
                Intent intent = new Intent(this, FolderActivity.class);
                startActivityForResult(intent, NOTE_FOLDER);
                break;
            }
            case R.id.recycle_bin: {
                if (dbType != NoteDBHelper.REMOVED) {
                    dbType = NoteDBHelper.REMOVED;
                    NoteDBHelper.setType(dbType);
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    spinner.setVisibility(View.GONE);
                    updateRecyBg();
                    fab.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                fab.setVisibility(View.GONE);
                isSearching = true;
                recyclerView.setBackgroundColor(getResources().getColor(R.color.grayE));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (dbType == NoteDBHelper.GENERAL) {
                    fab.setVisibility(View.VISIBLE);
                }
                isSearching = false;
                updateRecyBg();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_layout: {
                if (layoutDialog == null) {
                    initLayoutDialog();
                }
                layoutDialog.show();
                break;
            }
            case R.id.action_order: {
                if (orderDialog == null) {
                    initOrderDialog();
                }
                orderDialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSnackbar != null && mSnackbar.isShown()) {
                View view=mSnackbar.getView();
                int[] out=new int[2];
                view.getLocationOnScreen(out);
                Rect mRect=new Rect(out[0],out[1], out[0]+view.getWidth(),
                        out[1]+view.getHeight());
                // 触摸snackbar以外的区域时不再显示snackbar
                if (!mRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    mSnackbar.dismiss();
                    mSnackbar = null;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AnimationUtil.animateIn(fab, AnimationUtil.INTERPOLATOR, null);

        if (requestCode == NEW_NOTE) {
            if (resultCode == RESULT_OK) {
                int id = data.getIntExtra("id", 0);
                int position = NoteDBHelper.getRank(id);
                adapter.notifyItemInserted(position);
                updateRecyBg();
                recyclerView.smoothScrollToPosition(position);
            } else if (resultCode == RESULT_CANCELED) {
                Snackbar.make(recyclerView, "空便签将不会被添加", Snackbar.LENGTH_LONG)
                        .setAction("知道了", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "onClick: 知道了");
                            }
                        })
                        .show();
            }
        } else if (requestCode == EDIT_NOTE) {
            if (resultCode == RESULT_OK) {
                int index = data.getIntExtra("index", 0);
                int id = data.getIntExtra("id", 0);
                int position = NoteDBHelper.getRank(id);
                adapter.notifyItemMoved(index, position);
                adapter.notifyItemChanged(position);
                updateRecyBg();
                // TODO: 2018/8/16 用scrollToPosition没效果，找原因
                recyclerView.smoothScrollToPosition(position);

            } else if (resultCode == RESULT_FIRST_USER) {
                int index = data.getIntExtra("index", 0);
                adapter.notifyItemRemoved(index);
                updateRecyBg();

                Snackbar.make(recyclerView, "空便签已删除", Snackbar.LENGTH_LONG)
                        .setAction("知道了", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "onClick: 知道了");
                            }
                        })
                        .show();
            }
        } else if (requestCode == NOTE_FOLDER) {
            spinnerAdapter.notifyDataSetChanged();
//            spinner.setAdapter(spinnerAdapter);
            if (resultCode == RESULT_CANCELED) {
                // 判断便签夹是否已经被删除了
                Folder folder = FolderDBHelper.findByFolderId(currentFolderId);
                if (folder == null) {   // 被删除，切换到全部便签
                    currentFolderId = -1;
                    spinner.setSelection(0);
                } else {  // 可能已经被改名，重新设置选中项
                    spinner.setSelection(FolderDBHelper.getRank(currentFolderId) + 1);
                }
                if (dbType == NoteDBHelper.REMOVED) {
                    fab.setVisibility(View.GONE);
                }
            } else if (resultCode == RESULT_OK) {
                if (dbType != NoteDBHelper.GENERAL) {
                    dbType = NoteDBHelper.GENERAL;
                    NoteDBHelper.setType(dbType);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    spinner.setVisibility(View.VISIBLE);
                    updateRecyBg();
                    AnimationUtil.animateIn(fab, AnimationUtil.INTERPOLATOR, null);
                }
                currentFolderId = data.getIntExtra("folderId", -1);
                if (currentFolderId < 0) {
                    spinner.setSelection(0);
                } else {
                    spinner.setSelection(FolderDBHelper.getRank(currentFolderId) + 1);
                }
            }

            NoteDBHelper.setsFolderId(currentFolderId);
            adapter.notifyDataSetChanged();
            updateRecyBg();
        }
    }

    private void initLayoutDialog() {
        int checkedItem = 0;
        // FIXME: 2018/8/25 改用枚举？
        String itemText = "";
        if (layoutType == MyViewHolderFactory.DEFAULT) {
            itemText = layoutLinear;
        } else if (layoutType == MyViewHolderFactory.GRID) {
            itemText = layoutGrid;
        }
        for (int i = 0; i < layoutItems.length; i++) {
            if (itemText.equals(layoutItems[i])) {
                checkedItem = i;
                break;
            }
        }
        layoutDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(layoutItems, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (layoutLinear.equals(layoutItems[i])) {
                            if (adapter.setLayoutType(MyViewHolderFactory.DEFAULT)) {
                                layoutType = MyViewHolderFactory.DEFAULT;
                                updateRecyLayout();
                            }
                        } else if (layoutGrid.equals(layoutItems[i])) {
                            if (adapter.setLayoutType(MyViewHolderFactory.GRID)) {
                                layoutType = MyViewHolderFactory.GRID;
                                updateRecyLayout();
                            }
                        }
                        layoutDialog.dismiss();
                    }
                })
                .create();
    }

    private void initOrderDialog() {
        int checkedItem = 0;
        for (int i = 0; i < orderItems.length; i++) {
            if (orderType.mTag.equals(orderItems[i])) {
                checkedItem = i;
                break;
            }
        }
        orderDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(orderItems, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String tag = orderItems[i];
                        Order e = Order.findByTag(tag);
                        if (orderType != e) {
                            orderType = e;
                            NoteDBHelper.setsOrder(orderType);
                            adapter.notifyDataSetChanged();
                        }
                        orderDialog.dismiss();
                    }
                })
                .create();
    }

    private void setSelect(boolean select) {
        adapter.setSelect(select);
        if (dbType == NoteDBHelper.GENERAL && !isSearching) {
            if (select) {
                AnimationUtil.animateOut(fab, AnimationUtil.INTERPOLATOR,
                        new ViewPropertyAnimatorListener() {
                            @Override
                            public void onAnimationStart(View view) {

                            }

                            @Override
                            public void onAnimationEnd(View view) {
                                view.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(View view) {

                            }
                        });
            } else {
                AnimationUtil.animateIn(fab, AnimationUtil.INTERPOLATOR, null);
            }
        }
    }

    public void remove(Set<Integer> set, boolean forever) {
        List<Integer> mList = new ArrayList<>(set.size());
        mList.addAll(set);
        // 升序排序
        Collections.sort(mList);
        List<Note> mNotes = new ArrayList<>(mList.size());
        for (int i = 0; i < mList.size(); i++) {
            mNotes.add(NoteDBHelper.get(mList.get(i)));
        }

        Note note;
        for (int i = mList.size() - 1; i >= 0; i--) {
            note = mNotes.get(i);
            NoteDBHelper.remove(note, forever);
            adapter.notifyItemRemoved(mList.get(i));
        }
    }

    public void restore(Set<Integer> set) {
        List<Integer> mList = new ArrayList<>(set.size());
        mList.addAll(set);
        // 升序排序
        Collections.sort(mList);
        List<Note> mNotes = new ArrayList<>(mList.size());
        for (int i = 0; i < mList.size(); i++) {
            mNotes.add(NoteDBHelper.get(mList.get(i)));
        }

        Note note;
        for (int i = mList.size() - 1; i >= 0; i--) {
            note = mNotes.get(i);
            NoteDBHelper.restore(note);
            adapter.notifyItemRemoved(mList.get(i));
        }
    }

    private void updateRecyBg() {
        if (isSearching) {
            return;
        }
        if (adapter.getItemCount() == 0) {
            if (dbType == NoteDBHelper.GENERAL) {
                recyclerView.setBackgroundResource(R.drawable.blank_bg);
            } else if (dbType == NoteDBHelper.REMOVED) {
                recyclerView.setBackgroundResource(R.drawable.recycle_bin_blank_bg);
            }
        } else {
            recyclerView.setBackgroundColor(getResources().getColor(R.color.grayE));
        }
    }

    private void updateRecyLayout() {
        if (layoutType == MyViewHolderFactory.DEFAULT) {
            recyclerView.setLayoutManager(DEFAULT_LAYOUT);
            recyclerView.setPadding(0, 0, 0, 8);
        } else if (layoutType == MyViewHolderFactory.GRID) {
            recyclerView.setLayoutManager(GRID_LAYOUT);
            recyclerView.setPadding(12, 0, 12, 12);
        }
    }

    private void setStatusBarColor(int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //取消状态栏透明
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(statusColor);
            //设置系统状态栏处于可见状态
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//            //让view不根据系统窗口来调整自己的布局
//            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
//            View mChildView = mContentView.getChildAt(0);
//            if (mChildView != null) {
//                ViewCompat.setFitsSystemWindows(mChildView, false);
//                ViewCompat.requestApplyInsets(mChildView);
//            }
        }
    }

    private void savePrefs() {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt("folderId", currentFolderId);
        editor.putInt("layoutType", layoutType);
        editor.putInt("orderType", orderType.mId);
        editor.apply();
    }

    private void loadPrefs() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        currentFolderId = pref.getInt("folderId", -1);
        layoutType = pref.getInt("layoutType", MyViewHolderFactory.DEFAULT);
        int orderId = pref.getInt("orderType", -1);
        orderType = Order.findById(orderId);
    }
}
