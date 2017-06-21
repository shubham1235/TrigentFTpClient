package com.example.shubham_v.TrigentFTPclient;

import android.content.Context;
import android.net.wifi.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;

import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;


import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {


    TextView FTPLoing_textView, Download_textView_status;
    Button FTPLoing_Button, Download_button;
    public WifiManager wifiManager;
    MyFTPClient myFTPClient;
    int _network_Socket_Port_Number = 2221;
    String _host_Ip = "192.168.43.1";
    String _netWork_UserName = "shubham";
    String _netWrok_Password = "shubham";
    String _sourcePath = "/storage/sdcard0/Download/WTBDATA/shubham.txt";
    String _destinationPath = "/storage/sdcard0/Download/WTBDATA/shubham.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FTPLoing_textView = (TextView) findViewById(R.id.Ftp_server_login_status_id);
        Download_textView_status = (TextView) findViewById(R.id.download_status_textView_id);
        myFTPClient = new MyFTPClient();
        //wifi connection
        final Boolean wifiConnectionStatus = WifiReConnection();

        //ftp loing button and login functionality
        FTPLoing_Button = (Button) findViewById(R.id.Ftp_Login_button_id);
        FTPLoing_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wifiConnectionStatus) {
                    myFTPClient.ftpConnect(_host_Ip, _netWork_UserName, _netWrok_Password, _network_Socket_Port_Number, FTPLoing_textView, MainActivity.this);
                } else {
                    FTPLoing_textView.setText("Please try again");
                }
            }

        });

        Download_button = (Button) findViewById(R.id.Download_button_id);
        Download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myFTPClient.ftpDownload(_sourcePath, _destinationPath, Download_textView_status);
            }
        });

    }

    public Boolean WifiReConnection() {
        final Boolean[] wifireconnectstatus = {false};
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() == false) {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            Boolean _wifiStatus = wifiManager.setWifiEnabled(true);

            if (_wifiStatus) {
                Toast.makeText(this, "now wifi is enable ", Toast.LENGTH_SHORT).show();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        wifireconnectstatus[0] = WifiP2PConfiguration.ConnectionWithWifi(MainActivity.this);
                    }
                }, 4000);

            }
        } else {
            Toast.makeText(this, "wifi is already enable  ", Toast.LENGTH_SHORT).show();
            wifireconnectstatus[0] = WifiP2PConfiguration.ConnectionWithWifi(MainActivity.this);
        }
        return wifireconnectstatus[0];

    }

}


