package ljl.myapplication2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

import org.angmarch.views.NiceSpinner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;


public class smbActivity extends AppCompatActivity {
    private ListView mListView;
    private MyFileAdapter mAdapter;
    private Context mContext;
    private ArrayList<FileEntity> mList;
    private Handler mHandler;
    private PDFView pdfView;
    private String rootPath;
    private String currentPath;
    private NtlmPasswordAuthentication auth;
    private ProgressDialog progressDialog;
    private int mYear,mMonth,mDay;
    private TextView btn_date;
    private String smb_Date;
    private String machineNum;
    private TextView machineView;
    private NiceSpinner niceSpinner;
    private TextView btn_setting;
    private AlertDialog alertDialog2; //单选框
    private String[] dialogItem;
    private int machineIndex = 0;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.file_activity);
        String user = "ljl";
        String pass = "Aa!123456";
        auth = new NtlmPasswordAuthentication("",user, pass);
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
        btn_date = findViewById(R.id.btn_date);
        machineView = (TextView) findViewById(R.id.machineNum);
        rootPath = "smb://192.168.16.40/ftp/";
        Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        transferNewDate(mYear,mMonth,mDay);
        btn_setting = findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogItem.length > 0) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(smbActivity.this);
                    alertBuilder.setTitle("机台设置");
                    alertBuilder.setSingleChoiceItems(dialogItem, machineIndex, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            machineIndex = which;
                        }
                    });
                    alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            machineNum = dialogItem[machineIndex];
                            mHandler.post(updateMachineNum);
//                        machineView.setText(dialogItem[machineIndex]);
                            replaceMachineNum();
                            alertDialog2.dismiss();
                        }
                    });
                    alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog2.dismiss();
                        }
                    });
                    alertDialog2 = alertBuilder.create();
                    alertDialog2.show();
                }else {

                }

            }
        });


        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(smbActivity.this, onDateSetListener, mYear, mMonth, mDay).show();
            }
        });
        niceSpinner = (NiceSpinner)findViewById(R.id.nice_spinner);
        List<String> dataList = new ArrayList<>();
        dataList.add("白班");
        dataList.add("晚班");
        niceSpinner.attachDataSource(dataList);
        niceSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ban = niceSpinner.getText().toString();
                machineNum = machineView.getText().toString();
                String banPath;
                if (ban == "白班") {
                    banPath = "N/";
                }else {
                    banPath = "D/";
                }
                getData(rootPath + smb_Date + '/' + machineNum + '/' + banPath);
            }
        });
        initView();
        connectGetData();
    }

    private void connectGetData() {
        getMachineListData(rootPath + smb_Date + '/');
    }

    private void replaceMachineNum() {
        String ban = niceSpinner.getText().toString();
        String banPath;
        if (ban == "白班") {
            banPath = "D/";
        }else {
            banPath = "N/";
        }
        getData(rootPath + smb_Date + '/' + dialogItem[machineIndex] + '/' + banPath);
    }

    private void getMachineListData(final String nPath) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Config.registerSmbURLHandler();
                    SmbFile smbFile = new SmbFile(nPath,auth);
                    dialogItem = smbFile.list();
                    String appendText;
                    if (dialogItem.length > 0){
                        machineNum = dialogItem[0];
                        mHandler.post(updateMachineNum);
                        appendText = dialogItem[0];
                    }else {
                        machineNum = "无";
                        mHandler.post(updateMachineNum);
                        appendText = "无";
                    }
                    String ban = niceSpinner.getText().toString();
                    String banPath;
                    if (ban == "白班") {
                        banPath = "D/";
                    }else {
                        banPath = "N/";
                    }
                    getData(rootPath + smb_Date + '/' + appendText + '/' + banPath);
                }catch (Exception e){
                    machineView.setText("无");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable updateMachineNum = new Runnable() {
        @Override
        public void run() {
            machineView.setText(machineNum);
        }
    };
    Runnable updateDate = new Runnable() {
        @Override
        public void run() {

        }
    };

    private void transferNewDate(int nYear,int nMonth, int nDay) {
        String days;
        if (mMonth + 1 <10) {
            if (mDay < 10) {
                days = new StringBuffer().append(nYear).append("年").append("0").append(nMonth + 1)
                        .append("月").append(0).append(nDay).append("日").toString();
                smb_Date = new StringBuffer().append(nYear).append("0").append(nMonth + 1).append(0).append(nDay).toString();
            } else {
                days = new StringBuffer().append(nYear).append("年").append("0").append(nMonth + 1)
                        .append("月").append(nDay).append("日").toString();
                smb_Date = new StringBuffer().append(nYear).append("0").append(nMonth + 1).append(nDay).toString();
            }
        }else {
            if (mDay < 10) {
                days = new StringBuffer().append(nYear).append("年").append(nMonth+1)
                        .append("月").append(0).append(nDay).append("日").toString();
                smb_Date = new StringBuffer().append(nYear).append(nMonth+1).append(0).append(nDay).toString();
            } else {
                days = new StringBuffer().append(nYear).append("年").append(nMonth+1)
                        .append("月").append(nDay).append("日").toString();
                smb_Date = new StringBuffer().append(nYear).append(nMonth+1).append(nDay).toString();
            }
        }
        btn_date.setText(days);
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mYear = year;
            mMonth = month;
            mDay = dayOfMonth;
            transferNewDate(mYear,mMonth,mDay);
            connectGetData();
        }
    };
    private void getData(final String newPath) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                connectSmbFile(newPath);
            }
        }.start();
    }

    public void showProgressDialog(Context mContext, String text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(text);
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
            }
        },60000);
    }
    public Boolean dismissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                return true;
            }
        }
        return false;
    }
    public void connectSmbFile(final String smbPath) {
        mList.clear();
        new Thread() {
            public void run() {
                try {
                    Config.registerSmbURLHandler();
                    SmbFile smbFile = new SmbFile(smbPath,auth);
                    SmbFile[] files = smbFile.listFiles();
                    if (files.length > 0) {
                        for (SmbFile f : files) {
                            FileEntity entity2 = new FileEntity();
                            boolean isDir =  f.isDirectory();
                            if (isDir == true) {
                                entity2.setFileType(FileEntity.Type.Floder);
                                entity2.setFileName(f.getName().substring(0,f.getName().length()-1));
                            }else {
                                entity2.setFileType(FileEntity.Type.FILE);
                                entity2.setFileName(f.getName());
                            }

                            entity2.setFilePath(f.getCanonicalPath());
                            entity2.setFileSize(f.length()+"");
                            mList.add(entity2);
                        }
                    }else {
                        pdfView.fromAsset("test.txt").load();
                    }
                    mHandler.sendEmptyMessage(1);
//                    mainHeader.post(runnableUi);
                }catch(Exception e) {
                    pdfView.fromAsset("test.txt").load();
                    mHandler.sendEmptyMessage(1);
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
                if (entity.getFileType() == FileEntity.Type.Floder) {
                    getData(entity.getFilePath());
                }else if (entity.getFileType() == FileEntity.Type.FILE) {
                    showProgressDialog(mContext,"加载中...");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            yunXing(entity.getFilePath());
                        }
                    });
                }
            }
        });

    }
    private void yunXing(final String smbPath) {
        new Thread() {
            public void run() {
                try {
                    Config.registerSmbURLHandler();
                    SmbFile smbFile = new SmbFile(smbPath, auth);
                    InputStream contentIn = smbFile.getInputStream();
                    pdfView.fromStream(contentIn)
                            .enableSwipe(true)
                            .onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int nbPages) {
                                    dismissProgressDialog();
                                }
                            })
                            .load();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
