package ljl.myapplication2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import java.nio.FloatBuffer;

public class MainActivity2 extends AppCompatActivity {
    private PDFView pdfView;
    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);
        pdfView = findViewById(R.id.pdfView);
        pdfView.fromAsset("andriod.pdf")
                    .enableSwipe(true)
                    .swipeHorizontal(true)
                    .load();
    }
}
