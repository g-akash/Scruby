package scrubyeditor232;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import petrov.kristiyan.colorpicker.ColorPicker;
import scrubyeditor232.R;

public class Settings extends AppCompatActivity {

    private final String TAG = "Settings";

    // root view and layouts used in settings
    private LinearLayout mRootView = null;
    private LinearLayout mFonts = null;
    private LinearLayout mFontSize = null;
    private LinearLayout mFontColor = null;
    private LinearLayout mBackgroundColor = null;
    private LinearLayout mVersion = null;
    private LinearLayout mAboutEditor = null;

    // dialogs for options for the attributes
    private AlertDialog mFontTypeDialog = null;
    private AlertDialog mFontSizeDialog = null;
    private AlertDialog mAboutEditorDialog = null;

    // configuration data
    private String mSelectedFontType = "";
    private String mSelectedFontSize = "";
    private String mSelectedFontColor = "";
    private String mSelectedBackgroundColor = "";

    // views for attributes
    private View mFontTypeDialogView = null;
    private View mFontSizeDialogView = null;

    private SharedPreferences sharedprefs = null;
    private SharedPreferences.Editor prefEditor = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflator = this.getLayoutInflater();

        mRootView = (LinearLayout)inflator.inflate(R.layout.activity_settings,null,false);
        mFonts = mRootView.findViewById(R.id.fonts);
        mFontSize = mRootView.findViewById(R.id.font_size);
        mFontColor = mRootView.findViewById(R.id.font_color);
        mBackgroundColor = mRootView.findViewById(R.id.background_color);
        mVersion = mRootView.findViewById(R.id.version);
        mAboutEditor = mRootView.findViewById(R.id.about_editor);

        // get shared preferences and load values from it
        sharedprefs = this.getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE);
        prefEditor = sharedprefs.edit();
        mSelectedFontType = sharedprefs.getString(getString(R.string.font_type),
                getString(R.string.default_font_type));
        mSelectedFontSize = sharedprefs.getString(getString(R.string.font_size),
                getString(R.string.default_font_size));
        mSelectedFontColor = sharedprefs.getString(getString(R.string.font_color),
                getString(R.string.default_font_color));
        mSelectedBackgroundColor = sharedprefs.getString(getString(R.string.background_color),
                getString(R.string.default_background_color));
        mRootView.findViewById(R.id.font_color_show).setBackgroundColor(Integer.parseInt(mSelectedFontColor));
        mRootView.findViewById(R.id.background_color_show).setBackgroundColor(Integer.parseInt(mSelectedBackgroundColor));


        // set onclick listener for the elements
        mFonts.setOnClickListener(mAttributeListener);
        mFontSize.setOnClickListener(mAttributeListener);
        mFontColor.setOnClickListener(mAttributeListener);
        mBackgroundColor.setOnClickListener(mAttributeListener);
        mVersion.setOnClickListener(mAttributeListener);
        mAboutEditor.setOnClickListener(mAttributeListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok,mFontTypeClickListener);
        builder.setNegativeButton(R.string.cancel,mFontTypeClickListener);
        mFontTypeDialog = builder.create();
        mFontTypeDialog.setTitle(getString(R.string.font_type_title));
        mFontTypeDialogView = inflator.inflate(R.layout.font_type,null,false);
        mFontTypeDialogView.findViewById(R.id.monospace_layout).setOnClickListener(mFontTypeSelectionClickListener);
        mFontTypeDialogView.findViewById(R.id.serif_layout).setOnClickListener(mFontTypeSelectionClickListener);
        mFontTypeDialogView.findViewById(R.id.sans_serif_layout).setOnClickListener(mFontTypeSelectionClickListener);
        mFontTypeDialog.setView(mFontTypeDialogView);


        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok,mFontSizeClickListener);
        builder.setNegativeButton(R.string.cancel,mFontSizeClickListener);
        mFontSizeDialog = builder.create();
        mFontSizeDialog.setTitle(getString(R.string.font_size_title));
        mFontSizeDialogView = inflator.inflate(R.layout.font_size,null,false);
        mFontSizeDialogView.findViewById(R.id.extrasmall_layout).setOnClickListener(mFontSizeSelectionClickListener);
        mFontSizeDialogView.findViewById(R.id.small_layout).setOnClickListener(mFontSizeSelectionClickListener);
        mFontSizeDialogView.findViewById(R.id.medium_layout).setOnClickListener(mFontSizeSelectionClickListener);
        mFontSizeDialogView.findViewById(R.id.large_layout).setOnClickListener(mFontSizeSelectionClickListener);
        mFontSizeDialogView.findViewById(R.id.huge_layout).setOnClickListener(mFontSizeSelectionClickListener);
        mFontSizeDialog.setView(mFontSizeDialogView);

        builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(R.string.cancel, mOnAboutEditorListener);
        mAboutEditorDialog = builder.create();
        mAboutEditorDialog.setTitle(getString(R.string.about_editor_title));
        mAboutEditorDialog.setView(inflator.inflate(R.layout.about_editor,null,false));

        setContentView(mRootView);
    }

    // root View click listener
    private LinearLayout.OnClickListener mAttributeListener = new LinearLayout.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG,"rootview onclick: "+v.getId());
            switch (v.getId()) {
                case R.id.fonts: {
                    Log.v(TAG,"onclick root view: id fonts");
                    setSelectedFont();
                    mFontTypeDialog.show();
                    break;
                }
                case R.id.font_size: {
                    Log.v(TAG,"onClick rootview: id size");
                    mFontSizeDialog.show();
                    break;
                }
                case R.id.font_color: {
                    ColorPicker colorPicker = new ColorPicker(Settings.this);
                    colorPicker.show();
                    colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                        @Override
                        public void onChooseColor(int position, int color) {
                            Log.v(TAG,"colorpicker onchooselistener "+position+" "+color);
                            mRootView.findViewById(R.id.font_color_show).setBackgroundColor(color);
                            prefEditor.putString(getString(R.string.font_color),Integer.toString(color));
                            prefEditor.commit();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    break;
                }
                case R.id.background_color: {
                    ColorPicker colorPicker = new ColorPicker(Settings.this);
                    colorPicker.show();
                    colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                        @Override
                        public void onChooseColor(int position, int color) {
                            Log.v(TAG,"colorpicker onchooselistener "+position+" "+color);
                            mRootView.findViewById(R.id.background_color_show).setBackgroundColor(color);
                            prefEditor.putString(getString(R.string.background_color),Integer.toString(color));
                            prefEditor.commit();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    break;
                }
                case R.id.about_editor: {
                    mAboutEditorDialog.show();
                    break;
                }
            }
        }
    };




    // dialog font type click listener
    private DialogInterface.OnClickListener mFontTypeClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    // store font type in shared preferences here
                    if (!mSelectedFontType.equals("")) {
                        prefEditor.putString(getString(R.string.font_type),mSelectedFontType);
                        prefEditor.commit();
                    }
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE: {
                    break;
                }
            }
            mFontTypeDialog.dismiss();
            return;
        }
    };

    private View.OnClickListener mFontTypeSelectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG,"fonttypeselectionlistener ");
            switch (v.getId()) {
                case R.id.monospace_layout: {
                    mSelectedFontType = "monospace";
                    setSelectedFont();
                    ((RadioButton)mFontTypeDialogView.findViewById(R.id.monospace_button)).setChecked(true);
                    break;
                }
                case R.id.serif_layout: {
                    mSelectedFontType = "serif";
                    setSelectedFont();
                    ((RadioButton)mFontTypeDialogView.findViewById(R.id.serif_button)).setChecked(true);
                    break;
                }
                case R.id.sans_serif_layout: {
                    mSelectedFontType = "sans_serif";
                    setSelectedFont();
                    ((RadioButton)mFontTypeDialogView.findViewById(R.id.sans_serif_button)).setChecked(true);
                    break;
                }
                default: {
                    break;
                }
            }
            return;
        }
    };

    private DialogInterface.OnClickListener mOnAboutEditorListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:{
                    mAboutEditorDialog.dismiss();
                    break;
                }
            }
        }
    };

    private void setSelectedFont() {
        ((RadioButton)mFontTypeDialogView.findViewById(R.id.monospace_button)).setChecked(false);
        ((RadioButton)mFontTypeDialogView.findViewById(R.id.serif_button)).setChecked(false);
        ((RadioButton)mFontTypeDialogView.findViewById(R.id.sans_serif_button)).setChecked(false);
    }


    // listener for font size

    private DialogInterface.OnClickListener mFontSizeClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    // store font size in shared preferences here
                    prefEditor.putString(getString(R.string.font_size),
                            mSelectedFontSize);
                    prefEditor.commit();
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE: {
                    break;
                }
            }
            mFontSizeDialog.dismiss();
            return;
        }
    };

    private View.OnClickListener mFontSizeSelectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG,"fontsizeselectionlistener "+v.getId());
            switch (v.getId()) {
                case R.id.extrasmall_layout: {
                    mSelectedFontSize = "extrasmall";
                    setSelectedSize(v);
                    ((RadioButton)mFontSizeDialogView.findViewById(R.id.extrasmall_button)).setChecked(true);
                    break;
                }
                case R.id.small_layout: {
                    mSelectedFontSize = "small";
                    setSelectedSize(v);
                    ((RadioButton)mFontSizeDialogView.findViewById(R.id.small_button)).setChecked(true);
                    break;
                }
                case R.id.medium_layout: {
                    mSelectedFontSize = "medium";
                    setSelectedSize(v);
                    ((RadioButton)mFontSizeDialogView.findViewById(R.id.medium_button)).setChecked(true);
                    break;
                }
                case R.id.large_layout: {
                    mSelectedFontSize = "large";
                    setSelectedSize(v);
                    ((RadioButton)mFontSizeDialogView.findViewById(R.id.large_button)).setChecked(true);
                    break;
                }
                case R.id.huge_layout: {
                    mSelectedFontSize = "huge";
                    setSelectedSize(v);
                    ((RadioButton)mFontSizeDialogView.findViewById(R.id.huge_button)).setChecked(true);
                    break;
                }
                default: {
                    break;
                }
            }
            return;
        }
    };

    private void setSelectedSize(View v) {
        ((RadioButton)mFontSizeDialogView.findViewById(R.id.extrasmall_button)).setChecked(false);
        ((RadioButton)mFontSizeDialogView.findViewById(R.id.small_button)).setChecked(false);
        ((RadioButton)mFontSizeDialogView.findViewById(R.id.medium_button)).setChecked(false);
        ((RadioButton)mFontSizeDialogView.findViewById(R.id.large_button)).setChecked(false);
        ((RadioButton)mFontSizeDialogView.findViewById(R.id.huge_button)).setChecked(false);
    }
}
