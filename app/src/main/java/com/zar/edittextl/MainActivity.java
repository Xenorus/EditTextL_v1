package com.zar.edittextl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    public EditText editText;
    public String filename = null;
    private String path = Environment.getExternalStorageDirectory().toString() + "/files/";

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 10001;
    private static final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //создание стартового окна
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.editText); //объявление элемента editText, имеющегося
        // в первом окне, для компилятора

        // проверяем разрешения: если они уже есть,
        // то приложение продолжает работу в нормальном режиме
        if (isPermissionGranted(READ_EXTERNAL_STORAGE_PERMISSION)) {
            Toast.makeText(this, "Разрешения есть, можно работать", Toast.LENGTH_SHORT).show();
        } else {
            // иначе запрашиваем разрешение у пользователя
            requestPermission(READ_EXTERNAL_STORAGE_PERMISSION, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Разрешения получены", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Разрешения не получены", Toast.LENGTH_LONG).show();
                showPermissionDialog(MainActivity.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission(String permission, int requestCode) {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }
    private void showPermissionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = getResources().getString(R.string.app_name);
        builder.setTitle(title);
        builder.setMessage(title + " требует разрешение на доступ к медиафайлам");

        String positiveText = "Настройки";
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppSettings();
            }
        });

        String negativeText = "Выход";
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            requestApplicationConfig();
        }
    }

    private void requestApplicationConfig() {
        if (isPermissionGranted(READ_EXTERNAL_STORAGE_PERMISSION)) {
            Toast.makeText(MainActivity.this, "Теперь уже разрешения получены", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Пользователь снова не дал нам разрешение", Toast.LENGTH_LONG).show();
            requestPermission(READ_EXTERNAL_STORAGE_PERMISSION, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //создание меню
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() { //определяет сохраненные настройки
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //размер текста
        float fSize = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_size), "20"));
        editText.setTextSize(fSize);


        //стиль текста
        String regular = sharedPreferences.getString(getString(R.string.pref_style), "");
        int typeface = Typeface.NORMAL;
        if (regular.contains("Полужирный")) {
            typeface += Typeface.BOLD;
        }
        if (regular.contains("Курсив")) {
            typeface += Typeface.ITALIC;
        }
        if (regular.contains("Полужирный курсив")) {
            typeface += Typeface.BOLD_ITALIC;
        }
        editText.setTypeface(null, typeface);

        int color = Color.BLACK;
        if (sharedPreferences.getBoolean(getString(R.string.pref_color_red), false)) {
            color += Color.RED;
        }
        if (sharedPreferences.getBoolean(getString(R.string.pref_color_green), false)) {
            color += Color.GREEN;
        }
        if (sharedPreferences.getBoolean(getString(R.string.pref_color_blue), false)) {
            color += Color.BLUE;
        }

        editText.setTextColor(color);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                editText.setText("");
                Toast.makeText(getApplicationContext(), "Очищено!", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_open:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Имя файла");
                builder.setMessage("Введите имя файла для открытия");
                EditText input = new EditText(this); //поле ввода имени
                builder.setView(input);
                builder.setPositiveButton("Открыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editText.setText("");
                        String value = input.getText().toString(); //преобразуем в тип String тк editText
                        //возвращает не строки, а специальный тип Editable
                        filename = value;
                        File file = new File(path, filename);

                        if(file.exists() && file.isFile()) { //здесь всегда срабатывет else,
                            //а даже если убрать, просто ничего не происходит
                            editText.setText(openFile(path, filename));
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Файл не существует.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
                return true;

            case R.id.action_save:

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Имя файла");
                alertDialog.setMessage("Введите имя файла:");

                final EditText input_save = new EditText(this);
                alertDialog.setView(input_save);
                alertDialog.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = input_save.getText().toString();
                        filename = value;
                        saveFile(MainActivity.this, filename, editText.getText().toString());
                    }
                });
                alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Не сохранено", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private String openFile(String path, String filename) {

        StringBuilder text = new StringBuilder();
        try {
            File file = new File((path + "/files/"), filename);
            if (!file.exists()) {
                file.mkdirs(); //вот это просто игнорируется, я хз, что делать
            }

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line=bufferedReader.readLine())!= null) {
                text.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private void saveFile(AppCompatActivity app, String filename, String body) {

        try {
            File file = new File(app.getFilesDir(), filename);
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.append(body);
                writer.flush();
                writer.close();
                Toast.makeText(MainActivity.this, "Сохранено!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Файл с таким именем уже существует!", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Не сохранено!", Toast.LENGTH_SHORT).show();
        }
    }
}