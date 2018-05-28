package ru.raznometov.ma_launcher_v2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private ListView lView;
    private ArrayList results = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        lView = (ListView) findViewById(R.id.list1);
        PackageManager pm = this.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
        for (ResolveInfo rInfo : list) {
            results.add(rInfo.activityInfo.applicationInfo.loadLabel(pm).toString());
            Log.i("Installed Applications", rInfo.activityInfo.applicationInfo.loadLabel(pm).toString());
        }
        lView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, results));

    }
}
