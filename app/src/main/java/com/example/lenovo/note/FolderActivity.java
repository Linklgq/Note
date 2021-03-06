package com.example.lenovo.note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lenovo.note.db.Folder;
import com.example.lenovo.note.db.FolderDBHelper;
import com.example.lenovo.note.recy.MyDividerItemDecoration;
import com.example.lenovo.note.util.NoteAnalUtil;

public class FolderActivity extends AppCompatActivity {
    private static final String TAG = "FolderActivity";

    private View allNotes;
    private TextView notesCount;
    private RecyclerView recyclerView;
    private AlertDialog addFolderDialog;
    private FolderAdapter adapter;
    private SearchView mSearchView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        FolderDBHelper.clearCache();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        notesCount = (TextView) findViewById(R.id.all_file_count);
        notesCount.setText(String.valueOf(FolderDBHelper.totalNotes()));
        allNotes = findViewById(R.id.note_folder);
        allNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("folderId", -1);
                intent.putExtra("folderName", "全部便签");
                setResult(RESULT_OK, intent);
                FolderDBHelper.setFilter(false,null);
                finish();
            }
        });

        initRecyclerView();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addFolderDialog == null) {
                    initAddFolderDialog();
                } else {
                    addFolderDialog.show();
                    EditText editText = (addFolderDialog.getWindow().findViewById(R.id.edit_text));
                    editText.selectAll();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_folder_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);

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
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                fab.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                fab.setVisibility(View.VISIBLE);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void initAddFolderDialog() {
        addFolderDialog = new AlertDialog.Builder(this)
                .setTitle("新建便签夹")
                .setView(R.layout.edit_dialog)
                .create();

        addFolderDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            EditText et;
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if(et==null){
                    et=addFolderDialog.getWindow().findViewById(R.id.edit_text);
                }
                et.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                });
            }
        });

        // 没有show无法获取view
        addFolderDialog.show();

        final EditText editText = (EditText) (addFolderDialog.getWindow().findViewById(R.id.edit_text));
        editText.setSingleLine();
        TextView ok = (TextView) (addFolderDialog.getWindow().findViewById(R.id.action_ok));
        TextView cancel = (TextView) (addFolderDialog.getWindow().findViewById(R.id.action_cancel));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();
                if (text.length() > FolderDBHelper.NAME_MAX_LENGTH) {
                    editText.setError("超出限定长度");
                    return;
                }
                String folderName = NoteAnalUtil.trimWhiteChar(text);
                if (folderName.isEmpty()) {
                    editText.setError("名字不能为空");
                    return;
                }
                Folder folder = new Folder();
                folder.setFolderName(folderName);
                if (FolderDBHelper.add(folder)) {
                    int position = FolderDBHelper.getRank(folder.getId());
                    adapter.notifyItemInserted(position);
                    recyclerView.smoothScrollToPosition(position);
                    Log.d(TAG, "onClick: insert " + position);
                    editText.setText("");
                    addFolderDialog.dismiss();
                } else {
                    editText.setError("已存在相同的便签夹");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFolderDialog.dismiss();
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.folder_list);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        adapter = new FolderAdapter(getMenuInflater(),
                new FolderAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(int position) {
                        Folder folder = FolderDBHelper.get(position);
                        Intent intent = new Intent();
                        intent.putExtra("folderId", folder.getId());
                        intent.putExtra("folderName", folder.getFolderName());
                        setResult(RESULT_OK, intent);
                        FolderDBHelper.setFilter(false,null);
                        finish();
                    }
                },
                new FolderAdapter.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item, int position) {
                        switch (item.getItemId()) {
                            case R.id.clear: {
                                showClearNoteDialog(position);
                                break;
                            }
                            case R.id.remove: {
                                showRemoveFolderDialog(position);
                                break;
                            }
                            case R.id.rename: {
                                showRenameDialog(position);
                                break;
                            }
                        }
                        return true;
                    }
                });
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        MyDividerItemDecoration divider = new MyDividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(divider);
    }

    private void showClearNoteDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("清空便签")
                .setMessage("删除该便签夹下的所有便签。确定继续？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FolderDBHelper.clearNotes(position);
                        adapter.notifyItemChanged(position);
                        notesCount.setText(String.valueOf(FolderDBHelper.totalNotes()));
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRemoveFolderDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("删除便签夹")
                .setMessage("删除便签夹以及便签夹下的所有便签。确定继续？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FolderDBHelper.remove(position);
                        adapter.notifyItemRemoved(position);
                        notesCount.setText(String.valueOf(FolderDBHelper.totalNotes()));
                        Log.d(TAG, "onClick: remove " + position);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRenameDialog(final int position) {
        final AlertDialog renameDialog = new AlertDialog.Builder(this)
                .setTitle("重命名")
                .setView(R.layout.edit_dialog)
                .create();

        renameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            EditText et;
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if(et==null){
                    et=renameDialog.getWindow().findViewById(R.id.edit_text);
                }
                et.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                });
            }
        });

        renameDialog.show();

        final EditText editText = (EditText) (renameDialog.getWindow().findViewById(R.id.edit_text));
        editText.setSingleLine();
        editText.setText(FolderDBHelper.get(position).getFolderName());
        editText.selectAll();
        TextView ok = (TextView) (renameDialog.getWindow().findViewById(R.id.action_ok));
        TextView cancel = (TextView) (renameDialog.getWindow().findViewById(R.id.action_cancel));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();
                if (text.length() > FolderDBHelper.NAME_MAX_LENGTH) {
                    editText.setError("超出限定长度");
                    return;
                }
                String folderName = NoteAnalUtil.trimWhiteChar(text);
                if (folderName.isEmpty()) {
                    editText.setError("名字不能为空");
                    return;
                }
                int folderId = FolderDBHelper.get(position).getId();
                if (FolderDBHelper.update(position, folderName)) {
                    int nP = FolderDBHelper.getRank(folderId);
                    Log.d(TAG, "onClick: 修改名字成功 " + nP);
                    adapter.notifyItemMoved(position, nP);
                    adapter.notifyItemChanged(nP);
                    recyclerView.smoothScrollToPosition(nP);
                    renameDialog.dismiss();
                } else {
                    Log.d(TAG, "onClick: 同名");
                    editText.setError("已存在相同的便签夹");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameDialog.dismiss();
            }
        });
    }
}
