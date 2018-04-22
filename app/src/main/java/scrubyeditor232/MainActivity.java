package scrubyeditor232;



import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import scrubyeditor232.FileExplorer.FileExplorer;
import scrubyeditor232.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final int OPEN_FILE_REQUEST_CODE = 101;
    private final int SAVE_FILE_REQUEST_CODE = 102;
    private final int SAVE_AS_FILE_REQUEST_CODE = 103;
    private final int PERMISSION_READ_WRITE_STORAGE_REQUEST_CODE = 501;
    private final String BUNDLE_FILE_SAVE_KEY = "bundleFileSaveKey";

    private PopupMenu mPopupMenu = null;
    private View mRootView = null;
    private TextView mTitleText = null;
    private ImageView mTitleImage = null;
    private EditText mSearchText = null;
    private ImageButton mMenuButton = null;
    private ImageButton mSearchButton = null;
    private EditText mFileContent = null;
    private AlertDialog mFileSaveDialog = null;

    // configuration data
    private String mFontType = "";
    private String mFontSize = "";
    private String mFontColor = "";
    private String mBackgroundColor = "";
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    // current path stores full path including filename. it only stores something when file already exists
    private String currentPath = null;
    private String filename = null;
    private boolean mSearchModeActive = false;
    private ArrayList<Integer> mQueryMatchingPositions = new ArrayList<>();
    private int mCurrentSelectedIndex = -1;
    private String mQueryText = "";
    private boolean fileSaved = true;
    

    // shared preferences
    private SharedPreferences prefs = null;
  
  
  
  
  
  private TextWatcher mFileContentWatcher = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int before, int count) {
    
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
    
    @Override
    public void afterTextChanged(Editable s) {
      Log.v(TAG,"after text changed listener called");
      fileSaved = false;
      mFileContent.removeTextChangedListener(mFileContentWatcher);
      mFileContent.setTag(null);
      
      
    }
  };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get data from shared prefs

      if (savedInstanceState != null) {
        fileSaved = savedInstanceState.getBoolean(BUNDLE_FILE_SAVE_KEY,true);
      }


        LayoutInflater inflator = this.getLayoutInflater();
        mRootView = inflator.inflate(R.layout.activity_main,null,false);
        mTitleText = (TextView)mRootView.findViewById(R.id.toolbar_filename);
        mTitleImage = (ImageView)mRootView.findViewById(R.id.toolbar_image);
        mSearchText = (EditText)mRootView.findViewById(R.id.search_text);
        mSearchButton = (ImageButton)mRootView.findViewById(R.id.search_button);
        mMenuButton = (ImageButton)mRootView.findViewById(R.id.menu_button);
        mFileContent = (EditText)mRootView.findViewById(R.id.fileContent);
        mPopupMenu = new PopupMenu(this,mMenuButton);
        try {
          Field[] fields = mPopupMenu.getClass().getDeclaredFields();
          for (Field field : fields) {
            if ("mPopup".equals(field.getName())) {
              field.setAccessible(true);
              Object mPopupHelper = field.get(mPopupMenu);
              Class<?> classPopupHelper = Class.forName(mPopupHelper.getClass().getName());
              Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon",boolean.class);
              setForceIcons.invoke(mPopupHelper,true);
              break;
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        MenuInflater menuInflater = mPopupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.editor_menu,mPopupMenu.getMenu());
        init();
        setContentView(mRootView);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]
                                {Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_READ_WRITE_STORAGE_REQUEST_CODE);
            }
        }



        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                menuItemClick(item);
                return true;
            }
        });
        
        mSearchButton.setOnClickListener(new ImageButton.OnClickListener(){
          @Override
          public void onClick(View v) {
            searchText();
          }
        });
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            finish();
          }
        });
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (mFileSaveDialog != null) mFileSaveDialog.dismiss();
            return;
          }
        });
        mFileSaveDialog = builder.create();
        View fileSaveView = inflator.inflate(R.layout.about_editor,null,false);
        ((TextView)fileSaveView.findViewById(R.id.text_view_item)).setText(R.string.save_file_warning);
        mFileSaveDialog.setView(fileSaveView);

    }

    @Override
    public void onStart() {
        super.onStart();
        prefs = this.getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE);
        mFontType = prefs.getString(getString(R.string.font_type),
                getString(R.string.default_font_type));
        mFontSize = prefs.getString(getString(R.string.font_size),
                getString(R.string.default_font_size));
        mFontColor = prefs.getString(getString(R.string.font_color),
                getString(R.string.default_font_color));
        mBackgroundColor = prefs.getString(getString(R.string.background_color),
                getString(R.string.default_background_color));
        currentPath = prefs.getString(getString(R.string.current_file_path),null);
        if (currentPath != null) {
            File file = new File(currentPath);
            if (!file.exists()) {
                currentPath = null;
            }
        }
        if (currentPath == null) {
            filename = getString(R.string.new_file_name);
            mTitleText.setText(filename);
        } else {
            File file = new File(currentPath);
            filename = file.getName();
            mTitleText.setText(filename);
          if (mFileContent == null) {
            Log.v(TAG, "onStart: filecontent is null" + mFileContent);
          }
            mFileContent.setText(readTextFromFile(file));
        }
        saveFilepath(currentPath);
        setConfiguration();
        mFileContent.addTextChangedListener(mFileContentWatcher);
        mFileContent.setTag(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG,"onActivityResult called with resultCode "+resultCode+" and requestCode "+requestCode);
        switch (requestCode) {
            case OPEN_FILE_REQUEST_CODE:{
                if (resultCode == RESULT_OK) {
                    currentPath = data.getStringExtra(getString(R.string.result_file_path));
                    saveFilepath(currentPath);
                    File file = new File(currentPath);
                    filename = file.getName();
                    Log.v(TAG,"onActivityResult: open file code path "+currentPath+" name "+filename);
                    mFileContent.setText(readTextFromFile(file));
                    fileSaved = true;

                }
                break;
            }
            case SAVE_FILE_REQUEST_CODE:
            case SAVE_AS_FILE_REQUEST_CODE: {
                Log.v(TAG,"onActivityResult: got right place");
                if (resultCode == RESULT_OK) {
                    currentPath = data.getStringExtra(getString(R.string.result_file_path));
                    saveFilepath(currentPath);
                    Log.v(TAG,"onActivityResult: "+currentPath);
                    File file = new File(currentPath);
                    filename = file.getName();
                    Log.v(TAG,"onActivityResult: filename "+filename+" path "+currentPath);

                    fileSaved = true;
                    
                }
                break;
            }
            default:{
                Log.v(TAG,"onActivityResult: unrecognized requestCode");
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        if (mSearchModeActive) exitSearchMode();
        else {
          if (fileSaved) super.onBackPressed();
          else mFileSaveDialog.show();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(BUNDLE_FILE_SAVE_KEY,fileSaved);
      if (mFileContent.getTag()!=null) {
        mFileContent.removeTextChangedListener(mFileContentWatcher);
      }
      mFileContent.setTag(null);
      
    
    }


    private void init() {
        setTitleText(getString(R.string.new_file_name));
        if (mMenuButton != null) {
            mMenuButton.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPopupMenu!=null) {
                        mPopupMenu.show();
                    }
                }
            });
        }
        // TODO: load confoguration from sharedPreferences and apply it.
        return;
    }

    private void setConfiguration() {
        Log.v(TAG,"setConfiguration fonttype:"+mFontType+" fontsize:"+mFontSize);
        if (mFontType.equals("monospace")) mFileContent.setTypeface(Typeface.MONOSPACE);
        else if (mFontType.equals("serif")) mFileContent.setTypeface(Typeface.SERIF);
        else if (mFontType.equals("sans_serif")) mFileContent.setTypeface(Typeface.SANS_SERIF);

        if (mFontSize.equals("extrasmall")) mFileContent.setTextSize(10);
        else if (mFontSize.equals("small")) mFileContent.setTextSize(12);
        else if (mFontSize.equals("medium")) mFileContent.setTextSize(16);
        else if (mFontSize.equals("large")) mFileContent.setTextSize(20);
        else if (mFontSize.equals("huge")) mFileContent.setTextSize(24);

        mFileContent.setTextColor(Integer.parseInt(mFontColor));
        mFileContent.setBackgroundColor(Integer.parseInt(mBackgroundColor));

    }


    private void makeNewFile() {
        currentPath = null;
        filename = getString(R.string.new_file_name);
        mFileContent.setText("");
        setTitleText(filename);
        saveFilepath(null);
    }

    private void menuItemClick(MenuItem item) {
        Log.v(TAG,"menuItemClick: itemId "+item.getItemId());
        switch(item.getItemId()){
            case R.id.menu_new:{
                makeNewFile();
                break;
            }
            case R.id.menu_open: {
                Intent intent = new Intent(this,FileExplorer.class);
                intent.putExtra(getString(R.string.explorer_type),
                        getString(R.string.explorer_open));
                startActivityForResult(intent,OPEN_FILE_REQUEST_CODE);
                break;
            }
            case R.id.menu_save: {
                Log.v(TAG,"menuItemClick: menusave option: "+currentPath);
                if (currentPath == null) {
                    Intent intent = new Intent(this, FileExplorer.class);
                    intent.putExtra(getString(R.string.explorer_type),
                            getString(R.string.explorer_save));
                    String text = mFileContent.getText().toString();
                    intent.putExtra(getString(R.string.file_content),text);
                    startActivityForResult(intent, SAVE_FILE_REQUEST_CODE);
                } else {
                    //TODO implement saving method here
                    try {
                        File file = new File(currentPath);
                        file.createNewFile();
                        String text = mFileContent.getText().toString();
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        writer.write(text);
                        writer.close();
                        fileSaved = true;
                        Toast.makeText(this,"file saved",Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this,"couldn't save file",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
            case R.id.menu_save_as: {
                Intent intent = new Intent(this, FileExplorer.class);
                intent.putExtra(getString(R.string.explorer_type),
                        getString(R.string.explorer_save_as));
                String text = mFileContent.getText().toString();
                intent.putExtra(getString(R.string.file_content),text);
                if (currentPath == null) {
                    intent.putExtra(getString(R.string.current_directory_path),rootPath);

                } else {
                    File file = new File(currentPath);
                    intent.putExtra(getString(R.string.current_directory_path),
                            file.getParent());
                }
                startActivityForResult(intent,SAVE_AS_FILE_REQUEST_CODE);
                break;
            }
            case R.id.menu_search: {
                if (!mSearchModeActive) {
                    activateSearchMode();
                }
                break;
            }
            case R.id.menu_reset: {
                mBackgroundColor = getString(R.string.default_background_color);
                mFontColor = getString(R.string.default_font_color);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.background_color),mBackgroundColor);
                editor.putString(getString(R.string.font_color),mFontColor);
                editor.commit();
                mFileContent.setBackgroundColor(Integer.parseInt(mBackgroundColor));
                mFileContent.setTextColor(Integer.parseInt(mFontColor));
                break;
            }
            case R.id.menu_settings: {
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            }
          case R.id.menu_exit: {
            onBackPressed();
            break;
          }
        }
    }
    
    
    private void searchText() {
      mFileContent.requestFocus();
      String query = mSearchText.getText().toString();
      if (query == null || query.equals("")) {
        return;
      }
      int queryLength = query.length();
      if (mCurrentSelectedIndex == -1 || ! query.equals(mQueryText)) {
        Log.v(TAG, "searchText: called");
        mQueryText = query;
        
        Log.v(TAG, "searchText: " + query + " " + queryLength);
        String fileText = mFileContent.getText().toString();
        mQueryMatchingPositions = new ArrayList<>();
        int start = 0;
        int found = fileText.indexOf(query, start);
        while (found != -1) {
          Log.v(TAG, "searchText: " + found);
          mQueryMatchingPositions.add(found);
          found = fileText.indexOf(query, found + 1);
        }
        if (mQueryMatchingPositions.size() == 0) {
          return;
        }
        mCurrentSelectedIndex = 0;
        start = mQueryMatchingPositions.get(mCurrentSelectedIndex);
        mFileContent.setSelection(start, start+queryLength);
      } else {
        
        if (mQueryMatchingPositions.size()==0) return;
        mCurrentSelectedIndex = (mCurrentSelectedIndex+1)%(mQueryMatchingPositions.size());
        int start = mQueryMatchingPositions.get(mCurrentSelectedIndex);
        mFileContent.setSelection(start,start+queryLength);
      }
    }
    
    


    private void setTitleText(String filename) {
        if (mTitleText != null) {
            mTitleText.setText(filename);
        }
    }

    private void saveFilepath(String path) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.current_file_path),path);
        editor.commit();
    }

    private String readTextFromFile(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String text = "", line = "";
            while((line = reader.readLine()) != null) {
                text+=line;
            }
            Log.v(TAG,"readTextFromFile: "+text);
            return text;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,getString(R.string.file_not_open),
                    Toast.LENGTH_LONG);
            makeNewFile();
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        boolean permissionGranted = false;
        if (requestCode == PERMISSION_READ_WRITE_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true;
            }
        }
        if (!permissionGranted) {
            Toast.makeText(this,getString(R.string.permission_explanation),
                    Toast.LENGTH_LONG);
            finish();
        }

    }
    
    
    private void exitSearchMode(){
        mSearchModeActive = false;
        mSearchButton.setVisibility(View.GONE);
        mSearchText.setVisibility(View.GONE);
        mTitleText.setVisibility(View.VISIBLE);
        mMenuButton.setVisibility(View.VISIBLE);
    }
    
    private void activateSearchMode() {
        mSearchModeActive = true;
        mTitleText.setVisibility(View.GONE);
        mMenuButton.setVisibility(View.GONE);
        mSearchText.setText("");
        mSearchText.setVisibility(View.VISIBLE);
        mSearchButton.setVisibility(View.VISIBLE);
        mSearchText.requestFocus();
        mCurrentSelectedIndex = -1;
    }
}
