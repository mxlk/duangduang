package danmu.eric.com.duangduang;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getFragmentManager().beginTransaction().replace(R.id.container, new SettingFrag()).commit();
    }

    public static class SettingFrag extends PreferenceFragment{
        SharedPreferences sharedPreferences;
        Set<String> stringSet;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()){
                case "toggle":
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                    break;
                case "xiaomi":
                    new AlertDialog.Builder(getActivity()).setTitle("小米手机使用如下：").setMessage("1. 按Home键（“房子”键）返回桌面\n\n2. 按住Home键，出现程序列表" +
                            "\n\n3. 长按DuangDuang\n\n4. 进入应用信息，选择显示悬浮窗，点击确认").show();
                    break;
                case "notifi_list":
//                    new AlertDialog.Builder(getActivity()).setMessage("点击右下方悬浮球添加想要显示弹幕的应用程序哦").show();
                    List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();

                    Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
                    mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> resolveInfos = getActivity().getPackageManager().queryIntentActivities(mIntent, 0);
                    for (ResolveInfo resolveInfo : resolveInfos){
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("name", resolveInfo.loadLabel(getActivity().getPackageManager()).toString());
                        map.put("package", resolveInfo.activityInfo.packageName);
                        mapList.add(map);
                    }

                    stringSet = sharedPreferences.getStringSet("white_list", new HashSet<String>());

                    String[] title_list = new String[mapList.size()];
                    final String[] package_list = new String[mapList.size()];
                    boolean[] check_list = new boolean[mapList.size()];
                    int i = 0;
                    String package_name;
                    for(Map<String, Object> one : mapList){
                        title_list[i] = (String) one.get("name");
                        package_name = (String) one.get("package");
                        Log.d("package_name",  package_name);
                        package_list[i] = package_name;
                        if(stringSet.contains(package_name)){
                            check_list[i] = true;
                        }
                        i++;
                    }
                    Log.d("Packages", package_list.toString());


                    new AlertDialog.Builder(getActivity()).setTitle("请选择应用程序").setMultiChoiceItems(title_list, check_list, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if(isChecked == true){
                                if(stringSet != null){
                                    stringSet.add(package_list[which]);
                                }
                            }else{
                                if(stringSet != null){
                                    stringSet.remove(package_list[which]);
                                }
                            }
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Log.d("StringSet", stringSet.toString());
                            sharedPreferences.edit().putStringSet("white_list", stringSet).commit();
                            getActivity().startService(new Intent(getActivity(), NotificationListener.class));
                        }
                    }).show();
                    break;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

}
