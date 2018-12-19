package ljl.myapplication2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.util.FileUtils;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.angmarch.views.NiceSpinner;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private MyFileAdapter mAdapter;
    private Context mContext;
    private ArrayList<FileEntity> mList;
    private Handler mHandler;
    private PDFView pdfView;
    private String rootPath;
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
    private String[] dialogItem = new String[0];
    private int machineIndex = 0;
    private String machinePassword;
    private JSONArray machineViewList;
    private JSONArray napkinViewList;
    private JSONArray padViewList;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.file_activity);
        String user = "ljl";
        String pass = "Aa!123456";
        List<String> mdata = new ArrayList<>();
        for (int i = 0; i < 5;i++){
            mdata.add("test"+i);
        }
//        ListView listView1 = (ListView) findViewById(R.id.machineView);
//        listView1.setChoiceMode(listView1.CHOICE_MODE_SINGLE);
//        ListAdapter adapter = new ArrayAdapter<String>(this,R.layout.radio_item,mdata) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                final ChoiceView view;
//                if(convertView == null) {
//                    view = new ChoiceView(MainActivity.this);
//                }else {
//                    view = (ChoiceView)convertView;
//                }
//                view.setText(getItem(position));
//                return view;
//            }
//        };
//        listView1.setAdapter(adapter);
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
                        getMachineAndPassword(rootPath+"config.json");
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("访问密码")
                                .inputType(InputType.TYPE_MASK_VARIATION)
                                .input("", null, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                        String iString = input.toString();
                                        if (iString.equals(machinePassword)) {
                                            connectGetData();
                                        }else {
                                            Toast toast = Toast.makeText(MainActivity.this,"密码错误",Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER,0,0);
                                            toast.show();
                                        }
                                    }
                                })
                                .positiveText("确定")
                                .negativeText("取消")
                                .show();


                }
        });


        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, onDateSetListener, mYear, mMonth, mDay).show();
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
    }
    private void connectGetData() {
        getMachineListData(rootPath + "config.txt");
    }
    private void replaceMachineNum() {
        String ban = niceSpinner.getText().toString();
        String banPath;
        if (ban == "白班") {
            banPath = "D/";
        }else {
            banPath = "N/";
        }
        if (dialogItem.length > 0) {
            getData(rootPath + smb_Date + "/" + dialogItem[machineIndex] + "/" + banPath);
        }else {
            getData(rootPath + smb_Date + "/无/" + banPath );
        }
    }

    //从共享目录下载
    private static String smbGet(String remoteUrl,NtlmPasswordAuthentication auth) {
        InputStream in = null;
        OutputStream out = null;
        String newFilePath = null;
        try {
            Config.registerSmbURLHandler();
            SmbFile remoteFile = new SmbFile(remoteUrl,auth);
            if (remoteFile == null) {
                System.out.println("密码文件不存在");
                return "";
            }
            String fileName = remoteFile.getName();
            String sdRootPath = Environment.getExternalStorageDirectory().getPath();
            newFilePath = sdRootPath + File.separator + fileName;
            File localFile = new File(newFilePath);
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newFilePath;
    }

    private void getMachineAndPassword(final String jPath) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    String filePath = smbGet(jPath,auth);
                    String content = org.apache.commons.io.FileUtils.readFileToString(new File(filePath), "UTF-8");
                    JSONObject jsonObject = JSONObject.fromObject(content);
//                    JSONObject newObj = JSONObject.fromObject(json);
                    machinePassword = (String) jsonObject.get("password");
//                    JSONArray machineList = jsonObject.getJSONArray("machine");
                    machineViewList = jsonObject.getJSONArray("machine");
                    napkinViewList = jsonObject.getJSONArray("napkin");
                    padViewList = jsonObject.getJSONArray("pad");
                }catch (Exception e) {
                    Looper.prepare();
                    Toast toast = Toast.makeText(MainActivity.this, "获取服务器密码失败", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    Looper.loop();
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getMachineListData(final String nPath) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Config.registerSmbURLHandler();
                    SmbFile smbFile = new SmbFile(nPath,auth);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(smbFile)));
                    String line = reader.readLine();
                    dialogItem = line.split(",");
                    if (dialogItem.length > 0) {
                        mHandler.post(createAlertDialog);
                    }else {
                        Toast toast =Toast.makeText(MainActivity.this,"请查看配置文件或者联系管理员",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                }catch (Exception e){
                    mHandler.post(UpdateMachineNumError);
                    e.printStackTrace();
                }
            }
        }.start();
    }
    Runnable createAlertDialog = new Runnable() {
        @Override
        public void run() {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
            View myView = View.inflate(mContext,R.layout.dialog_item,null);
            List<String> mdata = new ArrayList<>();
            for (int i=0;i<5;i++) {
                mdata.add("test"+i);
            }
            ListView listView1 = myView.findViewById(R.id.machineView);
            listView1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            final SubjectAdapter adapter1 = new SubjectAdapter(machineViewList,MainActivity.this);
            listView1.setAdapter(adapter1);
            ListView listView2 = myView.findViewById(R.id.napkinView);
            listView2.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            final ListAdapter adapter2 = new ArrayAdapter<String>(MainActivity.this,R.layout.radio_item,napkinViewList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    final ChoiceView view;
                    if(convertView == null) {
                        view = new ChoiceView(MainActivity.this);
                    }else {
                        view = (ChoiceView)convertView;
                    }
                    view.setText(getItem(position));
                    return view;
                }
            };
            listView2.setAdapter(adapter2);

            ListView listView3 = myView.findViewById(R.id.padView);
            listView3.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            ListAdapter adapter3 = new ArrayAdapter<String>(MainActivity.this,R.layout.radio_item,padViewList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    final ChoiceView view;
                    if(convertView == null) {
                        view = new ChoiceView(MainActivity.this);
                    }else {
                        view = (ChoiceView)convertView;
                    }
                    view.setText(getItem(position));
                    return view;
                }
            };
            listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter1.SetClear();
                }
            });
            listView3.setAdapter(adapter3);
            alertBuilder.setTitle("机台设置");
            alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    machineNum = dialogItem[machineIndex];
                    mHandler.post(updateMachineNum);
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
            alertDialog2.setView(myView);
            alertDialog2.show();
        }
    };
    Runnable UpdateMachineNumError = new Runnable() {
        @Override
        public void run() {
            machineView.setText(machineNum);
            Toast toast = Toast.makeText(MainActivity.this,"发生异常错误，请联系管理员处理",Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    };

    Runnable updateMachineNum = new Runnable() {
        @Override
        public void run() {
            machineView.setText(machineNum);
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
            replaceMachineNum();
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
                            .onRender(new OnRenderListener() {
                                @Override
                                public void onInitiallyRendered(int nbPages, float pageWidth, float pageHeight) {
                                    pdfView.fitToWidth();
                                }
                            })
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
