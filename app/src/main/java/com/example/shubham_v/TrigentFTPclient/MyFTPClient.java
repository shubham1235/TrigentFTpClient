package com.example.shubham_v.TrigentFTPclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FileTransferClient;
import com.enterprisedt.net.ftp.FileTransferInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamException;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.id.list;

/**
 * Created by shubham_v on 06-06-2017.
 */

public class MyFTPClient {

    //Now, declare a public FTP client object.
    ProgressDialog progressDialog;
    private static final String TAG = "MyFTPClient";
    public FTPClient mFTPClient = null;
    FTPClient ftpClient;
    FileTransferClient ftp;
    MainActivity mainActivity;
    TextView loginStatusTextview;
    boolean successLogin = false;
    Handler loginHandler;

    Handler responseHandler = null;
    String srcFilePath = null;
    String desFilePath = null;

    final boolean[] success = {false};
    final boolean[] pingReply = {false};
    final long[] fileSize = {0};
    final long[] total = {0};

    //for rad and wite file
    ArrayList<String> sourceAddress = new ArrayList<String>();
    ArrayList<String> destinationAddress = new ArrayList<String>();
    ArrayList<String> wrongAddress = new ArrayList<String>();


    //for login method
    String _server;
    int _port;
    String _user;
    String _pass;
    String prgPathText;

    //Method to connect to FTP server:
    public boolean ftpConnect(String host, final String username, final String password, int port1, final TextView textView, MainActivity mActivity) {


        _server = host;
        _port = port1;
        _user = username;
        _pass = password;
        loginStatusTextview = textView;
        mainActivity = mActivity;
        final int[] replyCode = new int[1];
        ftpClient = new FTPClient();
        ftp = new FileTransferClient();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(_server, _port);
                    ftp.setRemoteHost(_server);
                    ftp.setRemotePort(_port);

                    showServerReply(ftpClient);
                    replyCode[0] = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(replyCode[0])) {
                        loginHandler.sendEmptyMessage(0);
                        return;
                    }
                    successLogin = ftpClient.login(_user, _pass);
                    ftp.setUserName(username);
                    ftp.setPassword(password);
                    ftp.connect();
                    showServerReply(ftpClient);
                    if (!successLogin) {
                        loginHandler.sendEmptyMessage(1);
                        return;
                    } else {
                        loginHandler.sendEmptyMessage(2);
                        ftpClient.enterLocalActiveMode();
                        //ftpClient.enterLocalPassiveMode();

                        try {
                            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (IOException ex) {
                    loginHandler.sendEmptyMessage(3);
                    ex.printStackTrace();
                } catch (FTPException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();



        loginHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    loginStatusTextview.setText("Operation failed. Server reply code: " + replyCode[0]);

                }
                if (msg.what == 1) {
                    loginStatusTextview.setText("Could not login to the server");
                }
                if (msg.what == 2) {
                    loginStatusTextview.setText("LOGGED IN SERVER");
                }
                if (msg.what == 3) {
                    loginStatusTextview.setText("Oops! Something wrong happened server is not working");
                }
            }
        };

        return false;
    }

    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
                Log.i("SERVER: ", aReply);
            }
        }
    }
    //Method to disconnect from FTP server:

    public boolean ftpDisconnect() {
        try {
            mFTPClient.logout();
            mFTPClient.disconnect();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.");
        }

        return false;
    }

    //Method to get current working directory:

    public String ftpGetCurrentWorkingDirectory() {
        try {
            String workingDir = mFTPClient.printWorkingDirectory();
            return workingDir;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not get current working directory.");
        }

        return null;
    }

    //Method to change working directory:

    public boolean ftpChangeDirectory(String directory_path) {
        try {
            mFTPClient.changeWorkingDirectory(directory_path);
        } catch (Exception e) {
            Log.d(TAG, "Error: could not change directory to " + directory_path);
        }

        return false;
    }

    //Method to list all files in a directory:

    public void ftpPrintFilesList(String dir_path) {
        try {
            FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
            int length = ftpFiles.length;

            for (int i = 0; i < length; i++) {
                String name = ftpFiles[i].getName();
                boolean isFile = ftpFiles[i].isFile();

                if (isFile) {
                    Log.i(TAG, "File : " + name);
                } else {
                    Log.i(TAG, "Directory : " + name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Method to create new directory:

    public boolean ftpMakeDirectory(String new_dir_path) {
        try {
            boolean status = mFTPClient.makeDirectory(new_dir_path);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not create new directory named " + new_dir_path);
        }

        return false;
    }

    //Method to delete/remove a directory:

    public boolean ftpRemoveDirectory(String dir_path) {
        try {
            boolean status = mFTPClient.removeDirectory(dir_path);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not remove directory named " + dir_path);
        }

        return false;
    }

    //Method to delete a file:

    public boolean ftpRemoveFile(String filePath) {
        try {
            File file = new File(filePath);
            file.delete();
            if (file.exists()) {
                file.getCanonicalFile().delete();
                if (file.exists()) {
                    Toast.makeText(mainActivity, "you have no permision to detele this file", Toast.LENGTH_SHORT).show();
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    //Method to rename a file:

    public boolean ftpRenameFile(String from, String to) {
        try {
            boolean status = mFTPClient.rename(from, to);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Could not rename file: " + from + " to: " + to);
        }

        return false;
    }

    //Method to download a file from FTP server:

    /**
     * mFTPClient: FTP client connection object (see FTP connection example)
     * srcFilePath: path to the source file in FTP server
     * desFilePath: path to the destination file to be saved in sdcard
     */

    //Method to upload a file to FTP server:

    /**
     * mFTPClient: FTP client connection object (see FTP connection example)
     * srcFilePath: source file path in sdcard
     * desFileName: file name to be stored in FTP server
     * desDirectory: directory path where the file should be upload to
     */
    public boolean ftpUpload(String srcFilePath, String desFileName,
                             String desDirectory, Context context) {
        boolean status = false;
        try {
            // FileInputStream srcFileStream = new FileInputStream(srcFilePath);

            FileInputStream srcFileStream = context.openFileInput(srcFilePath);

            // change working directory to the destination directory
            //if (ftpChangeDirectory(desDirectory)) {
            status = mFTPClient.storeFile(desFileName, srcFileStream);
            //}

            srcFileStream.close();
            return status;
        } catch (Exception e) {
            Log.d(TAG, "upload failed: " + e);
        }

        return status;
    }

    public boolean ftpDownload(String srcFilePath1, String desFilePath1, final TextView DownloadStatustextView) {
        srcFilePath = srcFilePath1;
        desFilePath = desFilePath1;
        final Timer timer = new Timer();
        responseHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    if (msg.what == 0) {
                        DownloadStatustextView.setText("Ftp file successfully download.");
                        dismissProgressDialog();

                    }
                    if (msg.what == 1) {
                        showProgressDialog();
                        DownloadStatustextView.setText("Downloading start");
                    }
                    if (msg.what == 3) {
                        Toast.makeText(mainActivity, "your downloading is stop because your FTP server is not working", Toast.LENGTH_SHORT).show();
                    }

                    if (msg.what == 4) {
                        Toast.makeText(mainActivity, "Server is not working  ", Toast.LENGTH_SHORT).show();
                        dismissProgressDialog();
                    }
                    if (msg.what == 21) {
                        dismissProgressDialog();
                        total[0] = 0;
                    }
                    if (msg.what == 5) {
                        pingReply[0] = true;
                        dismissProgressDialog();
                        DownloadStatustextView.setText("please try again your Downloading is faile because your server is stoped.");
                        loginStatusTextview.setText("Please Login again ");
                        ftpRemoveFile(desFilePath);
                        timer.cancel();
                    }
                    if (msg.what == 8) {
                        Toast.makeText(mainActivity, "somting Wrong With your Source and destination text file", Toast.LENGTH_SHORT).show();
                    }
                    if (msg.what == 9) {
                        progressDialogin((int) (total[0] * 100 / fileSize[0]));
                    }
                    if (msg.what == 22) {
                        aleartDialog();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        if (successLogin) {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        String abc = ftpClient.getStatus();
                    } catch (IOException e) {
                        e.printStackTrace();
                        responseHandler.sendEmptyMessage(5);

                    }
                }
            };
            timer.schedule(timerTask, 1000, 10000);

            final Thread thread = new Thread() {
                @Override
                public void run() {
                    Boolean ReadAuthentication = socketStrem(srcFilePath, desFilePath);
                    if (ReadAuthentication) {
                        if (readUserSourceDstinationtextViaFile(desFilePath)) {
                            Iterator srcitr = sourceAddress.iterator();
                            Iterator desitr = destinationAddress.iterator();
                            while (srcitr.hasNext() && desitr.hasNext()) {
                                responseHandler.sendEmptyMessage(21);
                                socketStrem(srcitr.next().toString(), desitr.next().toString());
                                if (pingReply[0] == true) {
                                    break;
                                }

                            }
                        }

                    } else {
                        responseHandler.sendEmptyMessage(8);
                    }

                    responseHandler.sendEmptyMessage(22);
                }
            };
            thread.start();
        } else {
            DownloadStatustextView.setText("!First Click Login button");
        }
        return success[0];
    }

    void showProgressDialog() {
        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Path:- " + prgPathText);
        progressDialog.setTitle("Downloading files...");
        progressDialog.show();
    }

    void aleartDialog(){
        if(wrongAddress != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
            alertDialogBuilder.setTitle("Wrong Path's");
            final ScrollView s_view = new ScrollView(mainActivity);
            alertDialogBuilder.setView(s_view);
            Iterator itr=wrongAddress.iterator();
            StringBuffer str= new StringBuffer();
             while(itr.hasNext()){
                 str.append((String) itr.next()+"\n");
             }
            alertDialogBuilder.setMessage(str);
            alertDialogBuilder.show();
        }
        else {

        }
    }

    void dismissProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    void progressDialogin(int a) {
        progressDialog.setProgress(a);
    }

    boolean socketStrem(String srcFilePath2, String desFilePath2) {
        prgPathText =srcFilePath2;
        Boolean funReturn = false;
        float fSize = 0;
        String drivaddress = srcFilePath2;
        StringBuilder sb = new StringBuilder(drivaddress);

        int i;
        for (i = drivaddress.length() - 1; i <= drivaddress.length(); i--) {
            if (drivaddress.charAt(i) != '/') {
                sb.deleteCharAt(i);
            } else {
                break;
            }

        }


        File file = new File(sb.toString());
        FTPFile[] filesz = new FTPFile[0];
        try {
            filesz = ftpClient.listFiles(srcFilePath2);
            if (filesz.length == 1 && filesz[0].isFile()) {
                fSize = filesz[0].getSize();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (file.isDirectory() && fSize > 0 && srcFilePath2 != null && desFilePath2 != null) {
            try {
                //FTP.ASCII_FILE_TYPE
                fSize = 0;
                String remoteFilePath = srcFilePath2;
                File localfile = new File(desFilePath2);
                FTPFile[] files = ftpClient.listFiles(remoteFilePath);
                if (files.length == 1 && files[0].isFile()) {
                    fileSize[0] = files[0].getSize();
                }
                FileOutputStream outputStream = new FileOutputStream(localfile);
                FileTransferInputStream inputStream = ftp.downloadStream(srcFilePath2);
                try {
                    int read = 0;
                    byte[] bytes = new byte[1024];

                    responseHandler.sendEmptyMessage(1);
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                        total[0] += read;
                        if (pingReply[0] == true) {
                            break;
                        }
                        responseHandler.sendEmptyMessage(9);

                    }
                    if (read == -1) {
                        responseHandler.sendEmptyMessage(0);
                        funReturn = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (CopyStreamException e) {
                responseHandler.sendEmptyMessage(4);
            } catch (IOException e) {
                e.printStackTrace();
                responseHandler.sendEmptyMessage(4);
            } catch (FTPException e) {
                e.printStackTrace();
            }
        } else {
            wrongAddress.add(srcFilePath2 + " , " + desFilePath2 + "\n");
        }
        return funReturn;
    }

    boolean readUserSourceDstinationtextViaFile(String _fileName) {
        File srdDesfile = new File(_fileName);
        Boolean readStatus = false;
        if (srdDesfile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(_fileName))) {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    if (sCurrentLine.indexOf(",") != -1) {
                        String[] srcDesAddress = sCurrentLine.split(",");
                        sourceAddress.add(srcDesAddress[0]);
                        destinationAddress.add(srcDesAddress[1]);
                        srcDesAddress[0] = null;
                        srcDesAddress[1] = null;
                    } else {
                        wrongAddress.add("Not contain Spliter (,)  " + sCurrentLine+  "\n");
                    }
                }
                readStatus = true;
            } catch (IOException e) {
                e.printStackTrace();
                responseHandler.sendEmptyMessage(8);
            }
        }
        return readStatus;
    }


}



