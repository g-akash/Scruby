package scrubyeditor232.FileExplorer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import scrubyeditor232.R;

import java.util.ArrayList;

public class FileListAdapter extends BaseAdapter {

    private final String TAG = "fileListAdapter";
    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<FileData> mFileList = new ArrayList<>();

    public FileListAdapter(@NonNull Context context, ArrayList<FileData> fileList) {
        this.mContext = context;
        this.mFileList = fileList;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent ) {
        Log.v(TAG,"getView called with position "+position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_item_view, parent,false);
        }
        FileData data = mFileList.get(position);
        if(data != null) {
            ((TextView)convertView.findViewById(R.id.file_name)).setText(data.getname());
            if (!data.getIsFile()) {
                ((ImageView) convertView.findViewById(R.id.file_icon)).setImageResource(R.mipmap.folder_small);
            } else {
                String filename = data.getname();
                if(filename.endsWith(".pdf")) {
                    ((ImageView)convertView.findViewById(R.id.file_icon)).setImageResource(R.mipmap.pdf_icon);
                } else if (filename.endsWith(".jpg") || filename.endsWith("jpeg")|| filename.endsWith("png")) {
                    ((ImageView)convertView.findViewById(R.id.file_icon)).setImageResource(R.mipmap.photo_icon);
                } else
                    ((ImageView) convertView.findViewById(R.id.file_icon)).setImageResource(R.mipmap.file_icon);
            }
        }
        return convertView;
    }


    @Override
    public FileData getItem(int position) {
        if (mFileList!=null) return mFileList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public int getCount() {
        if(mFileList!=null) return mFileList.size();
        return 0;
    }

    public void setItemList(ArrayList<FileData> fileList) {
        this.mFileList = fileList;
        return;
    }

}
