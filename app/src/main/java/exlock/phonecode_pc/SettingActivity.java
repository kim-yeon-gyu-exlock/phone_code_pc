package exlock.phonecode_pc;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.net.Uri;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import exlock.phonecode_pc.Tools.JsonManager;
import exlock.phonecode_pc.Tools.LanguageProfile;
import exlock.phonecode_pc.Tools.LanguageProfileJsonReader;
import exlock.phonecode_pc.Tools.LanguageProfileMember;
import exlock.phonecode_pc.Tools.FilePath;

enum MenuList {
    TWO_SPACE, FOUR_SPACE, ONE_TAB, TWO_TAB
}

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final TextView indentSetting = findViewById(R.id.settingIndentTextView);
        TextView profileSetting = findViewById(R.id.settingLanguageProfileTextView);
        TextView addProfile = findViewById(R.id.settingAddLanguageProfileTextView);
        TextView credit = findViewById(R.id.credit);


        final String jsonString = getSharedPreferences("json", MODE_PRIVATE)
                        .getString("profileJson", "");

        indentSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indentSetDialog(jsonString).show();
            }
        });
        profileSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                i.setType("application/*");
                i.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(i,"Select the language profiies"), 43);
            }
        });
        addProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                i.setType("application/*");
                i.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(i,"Select the language profiies"), 44);
            }
        });
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> paths = getLanguageProfileDirectories();
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.setting_paths_information)).append("\n");
                if(paths.size() > 0){
                    sb.append("1: ")
                            .append(paths.get(0));
                }
                for (int i = 1; i < paths.size(); i++) {
                    sb.append("\n").append(i+1).append(": ").append(paths.get(i));
                }
                Toast.makeText(SettingActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void loadJson(String absolutePath){
        String jsonExtension = absolutePath.substring(absolutePath.length() - 4, absolutePath.length());
        if (!jsonExtension.equals("json")) {
            Toast.makeText(SettingActivity.this, getString(R.string.toast_not_json), Toast.LENGTH_SHORT).show();
            return;
        }
        String json = JsonManager.getJsonFromPath(absolutePath);

        if (LanguageProfileJsonReader.getProfileMembers(json) == null) {
            Toast.makeText(SettingActivity.this, getString(R.string.toast_wrong_json), Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences profileJson = getSharedPreferences("json", MODE_PRIVATE);
        SharedPreferences.Editor editor = profileJson.edit();
        editor.putString("profileJson", json);
        editor.apply();

        this.addLanguageProfileDirectory(absolutePath, true);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode == Activity.RESULT_OK) {//if set the language profiles
            if(requestCode == 43) {
                Uri uri;
                if (resultData != null) {
                    uri = resultData.getData();
                    String[] path = uri.getPath().split(":");
                    String absolutePath = Environment.getExternalStorageDirectory() + "/" + path[1];
                    this.loadJson(absolutePath);
                }

            }else if(requestCode == 44){
                Uri uri;
                if (resultData != null) {
                    uri = resultData.getData();
                    String selectedFile = uri.toString();
                    String jsonExtension = selectedFile.substring(selectedFile.length() - 4, selectedFile.length());

                    if (!jsonExtension.equals("json")) {
                        Toast.makeText(SettingActivity.this, getString(R.string.toast_not_json), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] path = uri.getPath().split(":");
                    String absolutePath = Environment.getExternalStorageDirectory() + "/" + path[1];
                    String newJson = JsonManager.getJsonFromPath(absolutePath);

                    if (LanguageProfileJsonReader.getProfileMembers(newJson) == null) {
                        Toast.makeText(SettingActivity.this, getString(R.string.toast_wrong_json), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SharedPreferences profileJson = getSharedPreferences("json", MODE_PRIVATE);
                    String jsonString = profileJson.getString("profileJson", "");
                    if(jsonString.equals("")){
                        this.loadJson(absolutePath);
                        return;
                    }
                    LanguageProfileMember oldLPM = LanguageProfileJsonReader.getProfileMembers(
                            jsonString
                    );
                    LanguageProfileMember currLPM = LanguageProfileJsonReader.getProfileMembers(newJson);

                    ArrayList<String> currCategories;

                    if(oldLPM!=null&&currLPM!=null){
                        currCategories = currLPM.categories;

                        LanguageProfile oldLangProfile = new Gson().fromJson(
                                jsonString,
                                LanguageProfile.class
                        );

                        LanguageProfileJsonReader currLPJR = new LanguageProfileJsonReader(currLPM);

                        ArrayList<String> temp = new ArrayList<>();

                        if(currCategories.size() > 0) {
                            temp.add(JsonManager.addJsonKeyToArray(
                                    oldLangProfile.getFunctions().toString(),
                                    currCategories.get(0),
                                    currLPJR.getFunctions(currCategories.get(0))).toString());
                            for (int i = 1; i < currCategories.size(); i++) {
                                String targetString = currCategories.get(i);

                                temp.add(JsonManager.addJsonKeyToArray(
                                        temp.get(i - 1),
                                        targetString,
                                        currLPJR.getFunctions(targetString)
                                ).toString());
                            }
                            SharedPreferences.Editor editor = profileJson.edit();
                            oldLangProfile.setFunctions(temp.get(temp.size()-1));
                            editor.putString("profileJson", new Gson().toJson(oldLangProfile));
                            editor.apply();
                            this.addLanguageProfileDirectory(absolutePath, false);
                        }
                    }else{
                        //error
                    }
                }
            }
        }
    }
    private Dialog indentSetDialog(final String jsonString) {
        final CharSequence[] cs = getResources().getStringArray(R.array.setting_indent_array);
        AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this)
                .setTitle("Set indent type")
                .setItems(cs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence selected = cs[which];
                        String result = "";
                        switch (MenuList.values()[which]){
                            case TWO_SPACE:
                                result = "  ";
                                break;
                            case FOUR_SPACE:
                                result = "    ";
                                break;
                            case ONE_TAB:
                                result = "\t";
                                break;
                            case TWO_TAB:
                                result = "\t\t";
                                break;
                        }
                        String modifiedJson = JsonManager.modifyJsonByKey(jsonString, "lang_informs", "indent", result);
                        SharedPreferences sp = getSharedPreferences("json", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("profileJson", modifiedJson);
                        editor.apply();
                    }
                })
                .create();
        return dialog;
    }
    private void addLanguageProfileDirectory(String absolutePath, Boolean isClear){
        SharedPreferences absolutePaths = getSharedPreferences("json", MODE_PRIVATE);

        SharedPreferences.Editor editor = absolutePaths.edit();

        JSONObject jObject = new JSONObject();
        JSONArray jArray = new JSONArray();
        String prevJson = absolutePaths.getString("language_profile_paths", "");
        if(!isClear) {
            ArrayList<String> paths = new Gson().fromJson(prevJson, FilePath.class).getPaths();
            for(int i = 0;i<paths.size();i++)
                jArray.put(paths.get(i));
        }
        jArray.put(absolutePath);
        try {
            jObject.put("paths", jArray);
        }catch(JSONException e){
            e.printStackTrace();
            return;
        }
        editor.putString("language_profile_paths", jObject.toString());
        editor.apply();
    }
    private ArrayList<String> getLanguageProfileDirectories(){
        SharedPreferences absolutePaths = getSharedPreferences("json", MODE_PRIVATE);
        String pathsJson = absolutePaths.getString("language_profile_paths", "");
        FilePath lpp = new Gson().fromJson(pathsJson, FilePath.class);
        return lpp.getPaths();
    }
}
