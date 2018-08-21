package com.example.lenovo.note;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lenovo.note.db.Folder;
import com.example.lenovo.note.db.FolderDBUtil;
import com.example.lenovo.note.recy.MyDividerItemDecoration;
import com.example.lenovo.note.util.NoteAnalUtil;

public class FolderActivity extends AppCompatActivity {
    private static final String TAG = "FolderActivity";

    private View allNotes;
    private AlertDialog addFolderDialog;
    private FolderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        // FIXME: 2018/8/21
        FolderDBUtil.clearCache();

        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView notesCount= (TextView) findViewById(R.id.all_file_count);
        notesCount.setText(String.valueOf(FolderDBUtil.totalNotes()));
        allNotes=findViewById(R.id.all_note);
        allNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.actionStart(FolderActivity.this,-1,"全部便签");
            }
        });

        initRecyclerView();

        FloatingActionButton fab= (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addFolderDialog==null){
                    initAddFolderDialog();
                }
                addFolderDialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                finish();
                break;
            }
        }
        return true;
    }

    private void initAddFolderDialog(){
        addFolderDialog=new AlertDialog.Builder(this)
                .setTitle("新建便签夹")
                .setView(R.layout.edit_dialog)
                .show();

        final EditText editText= (EditText) (addFolderDialog.getWindow().findViewById(R.id.edit_text));
        editText.setSingleLine();
        TextView ok= (TextView) (addFolderDialog.getWindow().findViewById(R.id.action_ok));
        TextView cancel= (TextView)( addFolderDialog.getWindow().findViewById(R.id.action_cancel));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String folderName= NoteAnalUtil.trimWhiteChar(editText.getText().toString());
                Log.d(TAG, "onClick: "+folderName);
                Folder folder=new Folder();
                folder.setFolderName(folderName);
                FolderDBUtil.add(folder);
                // FIXME: 2018/8/20 改为局部刷新
                adapter.notifyDataSetChanged();
                editText.setText("");
                addFolderDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                addFolderDialog.dismiss();
            }
        });
    }

    private void initRecyclerView(){
        RecyclerView recyclerView= (RecyclerView) findViewById(R.id.folder_list);
        adapter=new FolderAdapter(getMenuInflater(),new FolderAdapter.ItemOnClickListener() {
            @Override
            public void onClick(int position) {
                Folder folder=FolderDBUtil.get(position);
                MainActivity.actionStart(FolderActivity.this,
                        folder.getId(),folder.getFolderName());
            }

            @Override
            public boolean onLongClick(int position) {
                return true;
            }
        });
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        MyDividerItemDecoration divider=new MyDividerItemDecoration(this,
                DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(divider);
    }
}
