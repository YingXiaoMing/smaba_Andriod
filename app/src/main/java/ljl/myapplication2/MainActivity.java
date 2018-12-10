package ljl.myapplication2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private PermissionHandler mHandler;
    private Button b1,b2;
    private Button smbBtn;
    private ListView listView_main_news;
    private String[] data;
    private Handler mainHeader = null;
    private List<Map<String,String>> list = null;
    private static  final  int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        MainActivityPermissionsDispatcher.
        //设置横屏
        MainActivityPermissionsDispatcher.handleReadAndWriteSDPermissionWithCheck(this);
        mainHeader = new Handler();
//        yunXing();
        b1 = findViewById(R.id.btn_pdfFile);
        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,MainActivity2.class);
                startActivity(intent);
            }
        });
        b2 = findViewById(R.id.btn_File);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,FileActivity.class);
                startActivity(intent);
            }
        });
        smbBtn = findViewById(R.id.btn_readFile);
        smbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,smbActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
    /**
     * 请求读写SD卡权限
     *
     * @param permissionHandler
     */
    protected void  requestReadAndWriteSDPermission(PermissionHandler permissionHandler) {
        this.mHandler = permissionHandler;
        MainActivityPermissionsDispatcher.handleReadAndWriteSDPermissionWithCheck(this);
    }

    @NeedsPermission(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void handleReadAndWriteSDPermission() {
        if (mHandler != null) {
            mHandler.onGranted();
        }
    }

    @OnPermissionDenied(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void deniedReadAndWriteSDPermission() {
        if (mHandler != null)
            mHandler.onDenied();
    }

    @OnNeverAskAgain(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void OnReadAndWriteSDNeverAskAgain() {
        showDialog("[存储空间]");
    }

    public void showDialog(String permission) {
        new AlertDialog.Builder(this).setMessage("权限申请").setMessage("在设置-应用-Test-权限中开启"+ permission + "权限，以正常使用Test").setPositiveButton("去开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mHandler != null);
                    mHandler.onDenied();
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }



    //权限回调接口
    public abstract class PermissionHandler {
        public abstract  void onGranted();
        public void onDenied() {
            finish();
        }
    }
    private List<String> getData() {
        List<String> data = new ArrayList<String>();
        data.add("测试数据1");
        data.add("测试数据2");
        data.add("测试数据3");
        data.add("测试数据4");
        return data;
    }

    private void ConnectSMBServer() {

    }


    private void yunXing() {
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

                    }
//                    mainHeader.post(runnableUi);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            listView_main_news = (ListView) findViewById(R.id.lv_files);
            listView_main_news.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,data));
            listView_main_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(MainActivity.this, "Click item" + id, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

}
