package com.example.lenovo.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.util.TimeUtil;

public class NoteEditActivity extends AppCompatActivity {
    private ActionMode actionMode;

    public static void startForResult(Activity context, Note note){
        Intent intent=new Intent(context,NoteEditActivity.class);
        intent.putExtra("content",note.getContent());
        intent.putExtra("time",note.getModifiedTime());
        context.startActivityForResult(intent,note.getId());
    }

    private Toolbar toolbar;
    private EditText noteEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        toolbar=(Toolbar)findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);

        Intent intent=getIntent();
        String content=intent.getStringExtra("content");
        long time=intent.getLongExtra("time",0);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(TimeUtil.timeString(time));
        noteEdit=(EditText)findViewById(R.id.note_edit);
        noteEdit.setText(content);

        final ActionMode.Callback callback=new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater=mode.getMenuInflater();
                inflater.inflate(R.menu.edit_operate,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()){
                    case R.id.add_photo: {
                        Toast.makeText(NoteEditActivity.this, "click add photo"
                                , Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    case R.id.menu_done: {
                        mode.finish();
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if(isSoftInputShowing()){
                    InputMethodManager imm = (InputMethodManager) getSystemService
                            (Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow()
                            .getDecorView().getWindowToken(), 0); //强制隐藏键盘
                }
                actionMode=null;
            }
        };

        getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if(actionMode==null&&isSoftInputShowing()){
                    actionMode=startSupportActionMode(callback);
                }else if(actionMode!=null&&!isSoftInputShowing()){
                    actionMode.finish();
                }
            }
        });
    }

    private boolean isSoftInputShowing(){
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();

        //获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight-rect.bottom>0.25*screenHeight;
    }
}
