package com.example.lenovo.note;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lenovo.note.db.DBUtil;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.util.BitmapUtil;
import com.example.lenovo.note.util.NoteAnalUtil;
import com.example.lenovo.note.util.TimeUtil;

import java.io.File;
import java.io.IOException;

public class NoteEditActivity extends AppCompatActivity {
    public static void startForResult(Activity activity, int index,int requestCode){
        Intent intent=new Intent(activity,NoteEditActivity.class);
        intent.putExtra("index",index);
        activity.startActivityForResult(intent,requestCode);
    }

    private static final String TAG = "NoteEditActivity";
    public static final int TAKE_PHOTO=0;
    public static final int CHOOSE_FROM_ALBUM=1;
    private ActionMode actionMode;
    private Toolbar toolbar;
    private EditText noteEdit;
    private final String[] photoItems={"拍照","从相册选择"};
    private AlertDialog photoDialog;
//    private ProgressDialog progressDialog;
    File photo;
    private Uri photoUri;
    private Note note;
    private int index;
//    private int editorWidth;
    private ProgressBar progressBar;
    private ProgressBar loadNote;
    private InsertPictureTask insertPictureTask;
//    private int picCounts=0;
    private LoadNotePicTask loadNotePicTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        toolbar=(Toolbar)findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);

        Intent intent=getIntent();
        index=intent.getIntExtra("index",MainActivity.NEW_NOTE);
        if(index<0){
            note=new Note();
        }else{
            note= DBUtil.get(index);
        }
        long time=note.getModifiedTime();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(TimeUtil.timeString(time));

        noteEdit=(EditText)findViewById(R.id.note_edit);
        noteEdit.setFocusable(false);
        long time1=System.currentTimeMillis();
        Spannable content=NoteAnalUtil.contentAnalyze(note.getContent(),
                NoteEditActivity.this,
                new NoteAnalUtil.MatchPictureListener() {
                    @Override
                    public void match(Bitmap bitmap, int start, int end) {
//                        picCounts++;
//                        ImageSpan imageSpan = new ImageSpan(NoteEditActivity.this, bitmap);
//                        noteEdit.getText().setSpan(imageSpan, start, end,
//                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    @Override
                    public boolean cancel() {
                        return false;
                    }

                    @Override
                    public Bitmap customBitmap() {
                        // FIXME: 2018/8/11 改为其他图片
                        return BitmapUtil.getFailed(getResources());
                    }
                });
        noteEdit.setText(content);
        long time2=System.currentTimeMillis();
        Log.d(TAG, "onCreate: "+(time2-time1)+"ms");

        loadNote= (ProgressBar) findViewById(R.id.load_progress_bar);
//        loadNote.setMax(picCounts*30);

        noteEdit.setCursorVisible(false);
        // TODO: 2018/8/8 复制粘贴
        noteEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence,
                                          int start, int count, int after) {
                // 删除
                if(after==0){
                    CharSequence cs=charSequence.subSequence(start,start+count);
                    NoteAnalUtil.rmText(NoteEditActivity.this,cs);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        picCounts=0;
        loadNotePicTask=new LoadNotePicTask(note.getContent(), this,
                new LoadNotePicTask.LoadNotePicListener() {
                    @Override
                    public void onMatch(Bitmap bitmap, int start, int end) {
                        ImageSpan imageSpan = new ImageSpan(NoteEditActivity.this, bitmap);
                        noteEdit.getText().setSpan(imageSpan,start,end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        picCounts++;
//                        for(int i=1;i<=30;i++){
//                            loadNote.setProgress(picCounts*i);
//                        }
                    }

                    @Override
                    public void onCompleted(Spannable spannable) {
//                        Log.d(TAG, "onCompleted: "+spannable.length());
//                        noteEdit.setText(spannable);
                        noteEdit.setFocusable(true);
                        noteEdit.setFocusableInTouchMode(true);
                        loadNote.setVisibility(View.GONE);
                    }
                });
        loadNotePicTask.execute();

        final ActionMode.Callback callback=new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater=mode.getMenuInflater();
                inflater.inflate(R.menu.edit_operate,menu);
                noteEdit.setCursorVisible(true);
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
                        if(photoDialog==null){
                            createPhotoDialog();
                        }
                        photoDialog.show();
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
                noteEdit.setCursorVisible(false);
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
        progressBar= (ProgressBar) findViewById(R.id.progress_bar);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case TAKE_PHOTO:{
                if(resultCode==RESULT_OK){
                    insertPhoto(noteEdit.getText(),photo,TAKE_PHOTO);
                }
                break;
            }
            case CHOOSE_FROM_ALBUM:{
                if(resultCode==RESULT_OK){
                    Uri selectedPhoto=data.getData();
                    String[] filePathColumns = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedPhoto, filePathColumns,
                            null, null, null);
                    cursor.moveToFirst();
                    String path=cursor.getString(cursor.getColumnIndex(filePathColumns[0]));
                    File file=new File(path);
                    insertPhoto(noteEdit.getText(),file,CHOOSE_FROM_ALBUM);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                saveNote();
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(loadNote.isShown()){
            if(loadNotePicTask!=null&&loadNotePicTask.getStatus()== AsyncTask.Status.RUNNING){
                loadNotePicTask.cancel(true);
            }
            loadNote.setVisibility(View.GONE);
            Toast.makeText(this, "取消加载", Toast.LENGTH_SHORT).show();
            return;
        }
        if(progressBar.isShown()){
            if(insertPictureTask!=null&&insertPictureTask.getStatus()== AsyncTask.Status.RUNNING) {
                insertPictureTask.cancel(true);
            }
            hideProgressBar();
            Toast.makeText(this, "取消图片插入", Toast.LENGTH_SHORT).show();
            return;
        }
        saveNote();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "没有权限打开相册", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private boolean isSoftInputShowing(){
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();

        //获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight-rect.bottom>0.25*screenHeight;
    }

    private void takePhoto(){
        photo=new File(getCacheDir(),System.currentTimeMillis()+".png");
        try {
            photo.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法创建图片", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Build.VERSION.SDK_INT>=24){
            photoUri= FileProvider.getUriForFile(NoteEditActivity.this,
                    "com.example.lenovo.note.fileprovider",photo);
        }else{
            photoUri=Uri.fromFile(photo);
        }

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

//    // FIXME: 2018/8/9 在其他线程下保存图片
//    private void insertPhoto(Editable editable,File photo) {
//        Log.d(TAG, "insertPhoto: "+photo.getAbsolutePath());
//        Bitmap bitmap= BitmapUtil.decodeFromFile(photo.getAbsolutePath(),
//                editorWidth,BitmapUtil.NO_REQUEST);
//        String fileName=System.currentTimeMillis()+".png";
//        BitmapUtil.save(this,bitmap,fileName);
//        ImageSpan imageSpan = new ImageSpan(NoteEditActivity.this, bitmap);
//        SpannableString spannableString = new SpannableString(
//                "\n<img src=\""+fileName+"\">\n");
//        spannableString.setSpan(imageSpan, 1, spannableString.length() - 1,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        editable.insert(noteEdit.getSelectionStart(), spannableString);
//        Log.d(TAG, "insertPhoto: "+editable);
//    }

    private void insertPhoto(final Editable editable, File file,int srcType) {
        insertPictureTask= new InsertPictureTask(this,
                noteEdit.getWidth(), file, srcType,
                new InsertPictureTask.InsertPictureListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap,String fileName) {
                        if(bitmap==null){
                            bitmap= BitmapUtil.getFailed(getResources());
                        }
                        ImageSpan imageSpan = new ImageSpan(NoteEditActivity.this, bitmap);
                        SpannableString spannableString = new SpannableString(
                                "\n<img src=\""+fileName+"\">\n");
                        spannableString.setSpan(imageSpan, 1, spannableString.length() - 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editable.insert(noteEdit.getSelectionStart(), spannableString);
//                        progressDialog.dismiss();
                        hideProgressBar();
                    }

                    @Override
                    public void onCanceled(String fileName) {
                    }
                });
//        if(progressDialog==null){
//            createProgressDialog();
//        }
//        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                if(task!=null&&task.getStatus()== AsyncTask.Status.RUNNING) {
//                    task.cancel(true);
//                }
//            }
//        });
//        progressDialog.show();
//        task.execute();
        displayProgressBar();
        insertPictureTask.execute();
    }

    private void saveNote(){
        // 没有修改
        if(note.getContent().equals(noteEdit.getText().toString())){
            setResult(RESULT_CANCELED);
            return;
        }
        note.setContent(noteEdit.getText().toString());
        note.setModifiedTime(System.currentTimeMillis());
        if(index<0){    // 添加便签
            DBUtil.add(note);
            setResult(RESULT_FIRST_USER);
        }else{          // 修改便签
            DBUtil.modify(note);
            setResult(RESULT_OK);
        }
    }

    private void createPhotoDialog(){
        photoDialog=new AlertDialog.Builder(NoteEditActivity.this)
                .setItems(photoItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if("拍照".equals(photoItems[i])){
                            Log.d(TAG, "onClick: "+getFilesDir().getPath());
                            actionMode.finish();
                            takePhoto();
                        }else if("从相册选择".equals(photoItems[i])){
                            if(ContextCompat.checkSelfPermission(NoteEditActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager
                                    .PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(NoteEditActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                            }else{
                                openAlbum();
                            }
                        }
                    }
                })
                .create();
    }

//    private void createProgressDialog(){
//        progressDialog=new ProgressDialog(this);
//        progressDialog.setMessage("加载图片...");
//        // FIXME: 2018/8/9 以后再来看
//        progressDialog.setCancelable(true);
//        progressDialog.setCanceledOnTouchOutside(false);
//    }

    private void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void displayProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void openAlbum(){
        Intent intent=new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,CHOOSE_FROM_ALBUM);
    }
}
