package com.example.filesharingapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btnOnOff, btnDiscover, btnSelectFile;
    ListView listView;
    TextView connectionStatus,receivingFileNameTextView,sendingFileNameTextView;
    ProgressBar receivingProgressBar, sendingProgressBar;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    static final int FILE_REQUEST_CODE = 2;

    ServerClass serverClass;
    ClientClass clientClass;

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;
    private static final int PERMISSIONS_REQUEST_CODE_READ_STORAGE = 1002;
    private static final int PERMISSIONS_REQUEST_CODE_WRITE_STORAGE = 1003;
    private static final int REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 1004;
    private static final int REQUEST_CODE_NEARBY_DEVICES = 1005;
    public static final int SEND_PORT = 9998;
    public static final int RECEIVE_PORT = 9999;

    Socket sendSocket;
    Socket receiveSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initialWork();
        checkPermissions();
        exqListner();
    }

    private void checkPermissions() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        }

        // Check storage permissions for Android versions before R
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE_READ_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE_WRITE_STORAGE);
            }
        }

        // Check permission for nearby Wi-Fi devices for Android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.NEARBY_WIFI_DEVICES},
                        REQUEST_CODE_NEARBY_DEVICES);
            }
        }

        // Check and request MANAGE_EXTERNAL_STORAGE permission for Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_MANAGE_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION:
                handleLocationPermissionResult(grantResults);
                break;

            case PERMISSIONS_REQUEST_CODE_READ_STORAGE:
                handleStoragePermissionResult(grantResults, "Read storage permission is required to access files.");
                break;

            case PERMISSIONS_REQUEST_CODE_WRITE_STORAGE:
                handleStoragePermissionResult(grantResults, "Write storage permission is required to save files.");
                break;

            case REQUEST_CODE_NEARBY_DEVICES:
                handleNearbyDevicesPermissionResult(grantResults);
                break;

            // Handle other permission request codes if needed
        }
    }

    private void handleLocationPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with location-related functionality
        } else {
            // Permission denied, show a message to the user
            showToast("Location permission is required for this feature.");
            redirectToAppSettings();
        }
    }

    private void handleStoragePermissionResult(int[] grantResults, String message) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with storage-related functionality
        } else {
            // Permission denied, show a message to the user
            showToast(message);
            redirectToAppSettings();
        }
    }

    private void handleNearbyDevicesPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with nearby device functionality
        } else {
            // Permission denied, redirect to settings
            showToast("Nearby devices permission is needed to discover devices.");
            redirectToAppSettings();
        }
    }

    private void showToast(String message) {
        runOnUiThread(()->Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private void redirectToAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void exqListner() {
        btnOnOff.setOnClickListener(v -> {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                btnOnOff.setText("Disabling Wi-Fi...");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    boolean wifiReEnabled = wifiManager.setWifiEnabled(true);

                    if (wifiReEnabled) {
                        btnOnOff.setText("Re-enabling Wi-Fi...");
                        wifiManager.disconnect();
                    } else {
                        btnOnOff.setText("Failed to enable Wi-Fi. Click to enable it manually.");
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                        if (wifiManager.isWifiEnabled()){
                            btnOnOff.setText("Wi-Fi is enabled");
                        }
                    }
                }, 2000);

            } else {
                boolean wifiEnabled = wifiManager.setWifiEnabled(true);

                if (wifiEnabled) {
                    btnOnOff.setText("Wi-Fi is now enabled");
                    wifiManager.disconnect();
                } else {
                    btnOnOff.setText("Failed to enable Wi-Fi. Click to enable it manually.");
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                    if (wifiManager.isWifiEnabled()){
                        btnOnOff.setText("Wi-Fi is enabled");
                    }
                }
            }
        });


        btnDiscover.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            }
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    connectionStatus.setText("Discovery Started");
                    Toast.makeText(MainActivity.this, "Peer discovery started", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    connectionStatus.setText("Discovery Failed");
                    String message = "";
                    switch (reason) {
                        case WifiP2pManager.BUSY:
                            message = "Wi-Fi Direct is busy. Try again.";
                            break;
                        case WifiP2pManager.ERROR:
                            message = "Internal Error. Try restarting Wi-Fi.";
                            break;
                        case WifiP2pManager.P2P_UNSUPPORTED:
                            message = "Wi-Fi Direct is not supported on this device.";
                            break;
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            final WifiP2pDevice device = deviceArray[i];
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    connectionStatus.setText("Connected to " + device.deviceName);
                    Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();

                    new ServerClass().start();
                    new ClientClass(device.deviceAddress).start();
                }

                @Override
                public void onFailure(int reason) {
                    connectionStatus.setText("Failed to connect to " + device.deviceName);
                    Toast.makeText(getApplicationContext(), "Failed to connect to " + device.deviceName, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Select file button click
        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple file selection
            startActivityForResult(intent, FILE_REQUEST_CODE);
        });
    }

    // Handle file selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ClipData clipData = data.getClipData();
            List<Uri> fileUris = new ArrayList<>();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    fileUris.add(clipData.getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                fileUris.add(data.getData());
            }

            if (!fileUris.isEmpty() && sendSocket != null) {
                FileSender fileSender = new FileSender(sendSocket);
                fileSender.sendFile(fileUris);
            }
        }
    }


    private void initialWork() {
        btnOnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnSelectFile = findViewById(R.id.selectFileButton); // For file selection
        listView = findViewById(R.id.peerListView);
        receivingFileNameTextView = findViewById(R.id.receivingFileNameTextView);
        sendingFileNameTextView = findViewById(R.id.sendingFileNameTextView);
        receivingProgressBar = findViewById(R.id.receivingProgressBar);
        sendingProgressBar = findViewById(R.id.sendingProgressBar);
        connectionStatus = findViewById(R.id.connectionStatus);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                deviceNameArray = new String[peers.size()];
                deviceArray = new WifiP2pDevice[peers.size()];

                int index = 0;
                for (WifiP2pDevice device : peers) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                connectionStatus.setText("Host");
                serverClass = new ServerClass();
                serverClass.start();
            } else if (wifiP2pInfo.groupFormed) {
                connectionStatus.setText("Client");
                clientClass = new ClientClass(groupOwnerAddress.getHostAddress());
                clientClass.start();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }

    private class ServerClass extends Thread {
        private ServerSocket sendServerSocket;
        private ServerSocket receiveServerSocket;

        @Override
        public void run() {
            try {
                // Set up two server sockets: one for receiving, one for sending
                sendServerSocket = new ServerSocket(SEND_PORT); // Port for receiving files
                receiveServerSocket = new ServerSocket(RECEIVE_PORT); // Port for sending files

                // Accept connection for receiving file from client
                receiveSocket = receiveServerSocket.accept();
                FileReceiver fileReceiver = new FileReceiver(receiveSocket);
                fileReceiver.receiveFile(); // Start receiving files

                // Accept connection for sending file to client
                sendSocket = sendServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientClass extends Thread {
        private String hostAddress;

        public ClientClass(String hostAddress) {
            this.hostAddress = hostAddress;
        }

        @Override
        public void run() {
            try {
                // Connect to server on two different ports: one for sending, one for receiving
                receiveSocket = new Socket(hostAddress, SEND_PORT); // Connect for receiving files
                sendSocket = new Socket(hostAddress, RECEIVE_PORT); // Connect for sending files

                // Start receiving files from server
                FileReceiver fileReceiver = new FileReceiver(receiveSocket);
                fileReceiver.receiveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class FileSender{
        private Socket socket;
        private OutputStream outputStream;
        public FileSender(Socket socket) {
            this.socket = socket;

            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void sendFile(List<Uri> fileUris) {
            if (fileUris == null || fileUris.isEmpty()) {
                Log.e("SendFile", "File URI list is null or empty. Cannot send file.");
                return;
            }
            Log.d("SelectedFiles", "Number of selected files: " + fileUris.size());

            new Thread(() -> {
                for (Uri fileUri : fileUris) {
                    try {
                        Thread.sleep(1000);
                        // Same logic for opening and sending files as before
                        ContentResolver contentResolver = getApplicationContext().getContentResolver();
                        InputStream fileInputStream = contentResolver.openInputStream(fileUri);

                        if (fileInputStream == null) {
                            Log.e("SendFile", "Unable to open InputStream for fileUri: " + fileUri);
                            return;
                        }
                        Log.d("SendFile", "Processing URI: " + fileUri.toString());
                        Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
                        String fileName = "unknown_file";
                        long fileSize = 0;

                        if (cursor != null) {
                            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            if (nameIndex != -1 && cursor.moveToFirst()) {
                                fileName = cursor.getString(nameIndex);
                                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                                fileSize = cursor.getLong(sizeIndex);
                                Log.d("SendFile", "Sending file: " + fileName + " Size: " + fileSize);
                            } else {
                                Log.e("SendFile", "Cursor does not contain valid data for URI: " + fileUri);
                                return;
                            }
                        } else {
                            Log.e("SendFile", "Cursor is null for URI: " + fileUri);
                            return;
                        }


                        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
                        byte[] magicNumber = new byte[] {0x12, 0x34, 0x56, 0x78};
                        outputStream.write(magicNumber);
                        outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(fileNameBytes.length).array());
                        outputStream.write(fileNameBytes);
                        outputStream.write(ByteBuffer.allocate(Long.BYTES).putLong(fileSize).array());

                        long finalFileSize = fileSize;
                        runOnUiThread(()->sendingProgressBar.setMax((int) finalFileSize));
                        String finalFileName = fileName;
                        runOnUiThread(()->sendingFileNameTextView.setText("Sending File: "+finalFileName));
                        runOnUiThread(()->sendingProgressBar.setProgress(0));

                        byte[] buffer = new byte[1024 * 1024];
                        int bytesRead;
                        int progress =0;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            progress+=bytesRead;
                            int finalProgress = progress;
                            runOnUiThread(()->sendingProgressBar.setProgress(finalProgress));
                        }
                        fileInputStream.close();
                        outputStream.flush();
                        Log.d("SendFile", "File sent successfully.");

                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (IOException e) {
                        Log.e("SendFile", "Error sending file", e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "All files sent", Toast.LENGTH_SHORT).show());
            }).start();
        }
    }
    public class FileReceiver {
        private Socket socket;
        private InputStream inputStream;
        public FileReceiver(Socket socket) {
            this.socket = socket;

            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void receiveFile() {
            new Thread(() -> {
                FileOutputStream fileOutputStream = null;
                try {
                    while (true) { // Keep listening for files
                        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File shareAppDir = new File(downloadsDir, "File Sharing App");
                        if (!shareAppDir.exists()) {
                            shareAppDir.mkdirs(); // Create the directory if it doesn't exist
                        }

                        DataInputStream dataInputStream = new DataInputStream(inputStream);
                        byte[] magicNumber = new byte[4];
                        boolean foundMagicNumber = false;
                        byte[] tempbuffer = new byte[4];

                        // Read byte-by-byte until magic number is found
                        while (!foundMagicNumber) {
                            tempbuffer[0] = tempbuffer[1]; // Shift bytes for checking magic number
                            tempbuffer[1] = tempbuffer[2];
                            tempbuffer[2] = tempbuffer[3];
                            tempbuffer[3] = (byte) dataInputStream.read(); // Read the next byte

                            // Check for magic number
                            if (tempbuffer[0] == 0x12 && tempbuffer[1] == 0x34 && tempbuffer[2] == 0x56 && tempbuffer[3] == 0x78) {
                                foundMagicNumber = true;
                            }
                        }

                        // Read the file name length
                        int fileNameLength = dataInputStream.readInt();
                        Log.d("ReceiveFile", "File name length: " + fileNameLength);
                        if (fileNameLength <= 0) { // Add a sanity check
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Invalid file name length.", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // Read the file name
                        byte[] fileNameBuffer = new byte[fileNameLength];
                        dataInputStream.readFully(fileNameBuffer);
                        String originalFileName = new String(fileNameBuffer, StandardCharsets.UTF_8);

                        // Create a file with the timestamp and original name
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String newFileName = timeStamp + "_" + originalFileName;
                        File file = new File(shareAppDir, newFileName);

                        // Read the file size
                        long fileSize = dataInputStream.readLong();
                        Log.d("ReceiveFile", "Received file size: " + fileSize);

                        // Reading file
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer

                        runOnUiThread(()->receivingProgressBar.setMax((int) fileSize));
                        runOnUiThread(()->receivingFileNameTextView.setText("Receiving File: "+originalFileName));
                        runOnUiThread(()->receivingProgressBar.setProgress(0));

                        int bytesRead;
                        int totalBytesRead = 0;
                        while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            int finalTotalBytesRead = totalBytesRead;
                            runOnUiThread(()->receivingProgressBar.setProgress(finalTotalBytesRead));
                        }
                        fileOutputStream.close();
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "File received: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    Log.e("ReceiveFile", "Error receiving file", e);
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            Log.e("ReceiveFile", "Error closing file output stream", e);
                        }
                    }
                }
            }).start();
        }
    }
}
