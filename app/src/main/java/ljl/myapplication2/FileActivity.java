package ljl.myapplication2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.util.ArrayList;


public class FileActivity extends AppCompatActivity {
    private ListView mListView;
    private MyFileAdapter mAdapter;
    private Context mContext;
    private Handler mHandler;
    private ArrayList<FileEntity> mList;
    private File currentFile;
    private PDFView pdfView;
    private String machineNum;
    String sdRootPath;

    public FileActivity() {
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected  void onCreate(Bundle savedInstanceState) {
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
//        sdRootPath = Environment.getDataDirectory().getPath();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            System.out.println("成功获取到SD设备");
            sdRootPath = Environment.getExternalStorageDirectory().getPath();
//            sdRootPath = Environment.getRootDirectory().getAbsolutePath();
        }
//        sdRootPath = Environment.getRootDirectory().getAbsolutePath();
//        sdRootPath = sdRootPath.substring(0,sdRootPath.length()-1);
        System.out.println("内置SD卡位置:"+sdRootPath);
        pdfView = findViewById(R.id.pdfView);
        currentFile = new File(sdRootPath);
        initView();
        getData(sdRootPath);
    }
    private  void initView() {
        mListView = (ListView)findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FileEntity entity = mList.get(position);
                if (entity.getFileType() == FileEntity.Type.FILE.Floder) {
                    currentFile = new File(entity.getFilePath());
                    getData(entity.getFilePath());
                }else if (entity.getFileType() == FileEntity.Type.FILE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentFile = new File(entity.getFilePath());
                            pdfView.fromFile(currentFile)
                                    .defaultPage(0)
                                    .enableAnnotationRendering(true)
                                    .load();
//                            Toast.makeText(mContext, entity.getFilePath()+"  "+entity.getFileName(), 1).show();
                        }
                    });
                }
            }
        });
    }
    //查找path地址下所有文件
    public  void findAllFiles(String path) {
        mList.clear();
        if (path == null || path.equals("")){
            return;
        }
        File fatherFile = new File(path);
        File[] files = fatherFile.listFiles();
        if (!sdRootPath.equals(currentFile.getAbsolutePath())) {
            FileEntity entity2 = new FileEntity();
            entity2.setFileType(FileEntity.Type.Floder);
            entity2.setFileName("...");
            String parentPath = currentFile.getParent();
            entity2.setFilePath(parentPath);
            mList.add(entity2);
        }
        if (files != null && files.length > 0) {
            for (int i =0;i<files.length;i++) {
                FileEntity entity = new FileEntity();
                boolean isDirectory = files[i].isDirectory();
                if(isDirectory == true){
                    entity.setFileType(FileEntity.Type.Floder);
                }else {
                    entity.setFileType(FileEntity.Type.FILE);
                }
                entity.setFileName(files[i].getName().toString());
                entity.setFilePath(files[i].getAbsolutePath());
                entity.setFileSize(files[i].length()+"");
                mList.add(entity);
            }
        }
        mHandler.sendEmptyMessage(1);
    }
    private  void getData(final String path) {
        new Thread() {
            @Override
            public  void run() {
                super.run();
                findAllFiles(path);
            }
        }.start();
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
