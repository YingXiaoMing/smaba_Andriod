package ljl.myapplication2;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class MainActivity extends AppCompatActivity {
    private Button b1,b2;
    private ListView listView_main_news;
    private List<Map<String,String>> list = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        yunXing();
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
    }
    private List<String> getData() {
        List<String> data = new ArrayList<String>();
        data.add("测试数据1");
        data.add("测试数据2");
        data.add("测试数据3");
        data.add("测试数据4");
        return data;
    }
    private void yunXing() {
        final List<String> newList = new ArrayList<>();
        new Thread() {
            public void run() {
                try {
                    Config.registerSmbURLHandler();
                    String user = "ljl";
                    String pass = "Aa!123456";
                    String path="smb://192.168.16.40/program1/";
                    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("",user, pass);
                    SmbFile smbFile = new SmbFile(path,auth);
                    String[] data = smbFile.list();
                    List<String> strsToList1 = Arrays.asList(data);
                    listView_main_news = (ListView) findViewById(R.id.lv_files);
                    listView_main_news.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,data));
                    listView_main_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(MainActivity.this, "Click item" + id, Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


}
