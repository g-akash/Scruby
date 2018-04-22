package scrubyeditor232.FileExplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import scrubyeditor232.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class FileExplorer extends AppCompatActivity {

    private final String TAG = "FileExplorer";

    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private FileListAdapter mFileAdapter = null;
    private View mRootView = null;
    private ArrayList<FileData> mFileList = new ArrayList<>();
    private ListView mFileListView = null;
    private LinearLayout mSaveBar = null;
    private EditText mSaveFileName = null;
    private String mCurrentPath = rootPath;
    private String mPurpose = "open";
    private Button mSaveButton = null;
    private Button mCancelButton = null;
    private String mFileContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate "+rootPath);
        mRootView = getLayoutInflater().inflate(R.layout.activity_file_explorer,null,false);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(getString(R.string.explorer_type)) != null) {
                mPurpose = intent.getStringExtra(getString(R.string.explorer_type));
            }
            if (intent.getStringExtra(getString(R.string.current_directory_path)) != null) {
                mCurrentPath = intent.getStringExtra(getString(R.string.current_directory_path));
            }
            mFileContent = intent.getStringExtra(getString(R.string.file_content));
            if(mFileContent==null) {
                mFileContent="";
            }
        }

        mFileList = getDirFromRoot(mCurrentPath);
        mFileAdapter = new FileListAdapter(this, mFileList);
        mFileListView = (ListView)mRootView.findViewById(R.id.file_list);
        mFileListView.setAdapter(mFileAdapter);
        mFileListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                FileData data = mFileAdapter.getItem(position);
                handleClick(data);
            }
        });

        mSaveBar = mRootView.findViewById(R.id.save_bar_layout);
        if (mPurpose.equals(getString(R.string.explorer_save)) ||
                mPurpose.equals(getString(R.string.explorer_save_as))) {
            mSaveBar.setVisibility(View.VISIBLE);
        }
        mSaveFileName = mRootView.findViewById(R.id.save_file_name);


        mSaveButton = mRootView.findViewById(R.id.save_button);
        mCancelButton = mRootView.findViewById(R.id.cancel_button);

        mSaveButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        mCancelButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                setResult(Activity.RESULT_CANCELED,result);
                finish();
            }
        });


        setContentView(mRootView);
    }

    private void finishActivityAndReturn(String filePath) {
        Intent result = new Intent();
        result.putExtra(getString(R.string.result_file_path),filePath);
        setResult(Activity.RESULT_OK,result);
        finish();
    }


    public void handleClick(FileData data) {
        if (mPurpose.equals(getString(R.string.explorer_open))) {
            if (!data.getIsFile()) {
                String path = data.getpath();
                mCurrentPath = path;
                mFileList = getDirFromRoot(mCurrentPath);
                mFileAdapter.setItemList(mFileList);
                mFileAdapter.notifyDataSetChanged();
            } else {
                finishActivityAndReturn(data.getpath());
            }
        } else if(mPurpose.equals(getString(R.string.explorer_save)) ||
                mPurpose.equals(getString(R.string.explorer_save_as))) {
            if (!data.getIsFile()) {
                String path = data.getpath();
                mCurrentPath = path;
                mFileList = getDirFromRoot(mCurrentPath);
                mFileAdapter.setItemList(mFileList);
                mFileAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!mCurrentPath.equals(rootPath)) {
            File file = new File(mCurrentPath);
            String path = file.getParent();
            mCurrentPath = path;
            mFileList = getDirFromRoot(path);
            mFileAdapter.setItemList(mFileList);
            mFileAdapter.notifyDataSetChanged();

        } else {
            super.onBackPressed();
        }
    }


    public ArrayList<FileData> getDirFromRoot(String path) {
        ArrayList<FileData> fileList = new ArrayList<>();
        File file = new File(path);
        File[] filesArray = file.listFiles();
        if (!path.equals(rootPath)) {
            FileData fileData = new FileData("..",file.getParent(),false);
            fileList.add(fileData);
        }
        if (filesArray != null) {
            for (int i = 0; i < filesArray.length; i++) {
                File currFile = filesArray[i];
                if (currFile.isDirectory()) {
                    FileData currFileData = new FileData(currFile.getName(),
                            currFile.getAbsolutePath(), false);
                    fileList.add(currFileData);
                } else {
                    FileData currFileData = new FileData(currFile.getName(),
                            currFile.getAbsolutePath(), true);
                    fileList.add(currFileData);
                }
            }
        }

        return fileList;
    }

    private void onSave() {
        Log.v(TAG,"onSave: called");
        String filename = mSaveFileName.getText().toString();
        if (filename.equals("")) {
            filename = getString(R.string.new_file_name);
        }
        File file = new File(mCurrentPath,filename);
        try {
            boolean isFileCreated = file.createNewFile();
            Log.v(TAG,"onSave: filecreated "+isFileCreated);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(mFileContent);
            writer.close();
            finishActivityAndReturn(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"couldn't save file",Toast.LENGTH_LONG);
        }

    }
}
