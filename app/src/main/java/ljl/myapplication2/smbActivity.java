package ljl.myapplication2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class smbActivity extends AppCompatActivity {
    private ListView mListView;
    private MyFileAdapter mAdapter;
    private Context mContext;
    private ArrayList<FileEntity> mList;
    private Handler mHandler;
    private PDFView pdfView;



    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.file_activity);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if(mAdapter == null) {
                            mAdapter = new MyFileAdapter(mContext,mList);
                            mListView.setAdapter(mAdapter);
                        }else {
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }
            }
        };
        mContext = this;
        mList = new ArrayList<>();
        pdfView = findViewById(R.id.pdfView);
        initView();
        getData();
    }
    private void getData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                connectSmbFile();
            }
        }.start();
    }
    public void connectSmbFile() {
        mList.clear();
        new Thread() {
            public void run() {
                try {
                    Config.registerSmbURLHandler();
                    String user = "ljl";
                    String pass = "Aa!123456";
                    String path="smb://192.168.16.40/ftp/";
                    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("",user, pass);
                    SmbFile smbFile = new SmbFile(path,auth);
                    SmbFile[] files = smbFile.listFiles();
                    for (SmbFile f : files) {
                        FileEntity entity2 = new FileEntity();
                        boolean isDir =  f.isDirectory();
                        if (isDir == true) {
                            entity2.setFileType(FileEntity.Type.Floder);
                        }else {
                            entity2.setFileType(FileEntity.Type.FILE);
                        }
                        entity2.setFileName(f.getName());
                        entity2.setFilePath(f.getCanonicalPath());
                        entity2.setFileSize(f.length()+"");
                        mList.add(entity2);
                    }
                    mHandler.sendEmptyMessage(1);
//                    mainHeader.post(runnableUi);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initView() {
        mListView = findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FileEntity entity = mList.get(position);
                if (entity.getFileType() == FileEntity.Type.FILE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            File currentFile = new File(convertSmbFileToFile(entity.getFilePath()));
//                            InputStream is =
//                            pdfView.fromFile(currentFile)
//                                    .defaultPage(0)
//                                    .enableAnnotationRendering(true)
//                                    .load();
//                            pdfView.fromStream(currentFile).load();
                        }
                    });
                }
            }
        });

    }
    public static String convertSmbFileToFile(String smbFileCanonicalPath) {
        String[] tempVar = smbFileCanonicalPath.substring(6).replace("$", ":").split("/");
        String bar = "\\";
        String finalDirectory = "";
        for (int i = 1; i < tempVar.length; i++) {
            finalDirectory += tempVar[i] + bar;
            if (i == tempVar.length - 1) {
                finalDirectory = finalDirectory.substring(0,finalDirectory.length()-1);
            }
        }
        return finalDirectory;
    }
    class MyFileAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<FileEntity> mAList;
        private LayoutInflater mInflater;

        public  MyFileAdapter(Context mContext, ArrayList<FileEntity>mList) {
            super();
            this.mContext = mContext;
            this.mAList = mList;
            mInflater = LayoutInflater.from(mContext);
        }
        @Override
        public  int getCount() {
            return mAList.size();
        }
        @Override
        public  Object getItem(int position) {
            return mAList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return  position;
        }
        @Override
        public int getItemViewType(int position) {
            if(mAList.get(position).getFileType() == FileEntity.Type.Floder) {
                return 0;
            }else {
                return 1;
            }
        }
        @Override
        public  int getViewTypeCount() {
            return 2;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder = null;
            int type = getItemViewType(position);
            FileEntity  entity = mAList.get(position);
            if(convertView == null){
                holder = new ViewHolder();
                switch (type) {
                    case 0:
                        convertView = mInflater.inflate(R.layout.item_listview,parent,false);
                        holder.iv = (ImageView) convertView.findViewById(R.id.item_imageview);
                        holder.tv = (TextView) convertView.findViewById(R.id.item_textview);
                        break;
                    case 1:
                        convertView = mInflater.inflate(R.layout.item_listview,parent,false);
                        holder.iv = (ImageView) convertView.findViewById(R.id.item_imageview);
                        holder.tv = (TextView) convertView.findViewById(R.id.item_textview);
                        break;
                    default:
                        break;
                }
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }
            switch (type) {
                case 0:
                    holder.iv.setImageResource(R.drawable.folder_123);
                    holder.tv.setText(entity.getFileName());
                    break;
                case 1:
                    holder.iv.setImageResource(R.drawable.file);
                    holder.tv.setText(entity.getFileName());
                    break;
                default:
                    break;
            }
            return convertView;
        }

    }
    class ViewHolder {
        ImageView iv;
        TextView tv;
    }
}
