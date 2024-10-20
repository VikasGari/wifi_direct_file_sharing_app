# Wi-Fi Direct File Sharing App

## System Requirements
- **Minimum Android Version**: API 24 (Android 7.0 Nougat)
- **Target Android Version**: API 35 (Android 14)
- **Storage**: Sufficient free space to store received files.
- **Wi-Fi**: Enabled and functioning on the device.
- **Permissions**: The app requires location, storage, and Wi-Fi-related permissions.

## Features Overview
- **Enable/Disable Wi-Fi**: Manually toggle the device’s Wi-Fi.
- **Discover Devices**: Search for nearby devices that support Wi-Fi Direct.
- **Connect to Devices**: Establish a connection with the discovered device.
- **File Selection**: Select multiple files from your device to send.
- **File Transfer Progress**: Track the progress of file sending and receiving through progress bars.
- **Receive Files**: Automatically receive files from the connected device.

## Installation of the App
1. Download the APK file from my [GitHub](https://github.com/VikasGari/wifi_direct_file_sharing_app).
2. On your Android device, navigate to the APK file in the file manager.
3. Tap on the file to initiate the installation process.
4. If prompted, enable "Install from unknown sources" in your device’s settings.
5. Follow the on-screen instructions to complete the installation.

## App Setup and Permissions
Upon launching the app for the first time, you will be prompted to grant the following permissions:
1. **Location Access**: Needed to discover nearby devices over Wi-Fi Direct.
2. **Storage Access**: Required to read and send files from your storage and to save received files.
3. **Nearby Wi-Fi Devices**: For discovering other devices using Wi-Fi Direct.
4. **Manage External Storage** (for Android 11 and above): To fully access files in the internal/external storage.

Grant all permissions to ensure the app functions correctly.

## Using the App

### Enabling Wi-Fi
1. Launch the app, and you will see a button labeled **"Wi-Fi On/Off"**.
2. Tap the button to enable or disable Wi-Fi.
3. If Wi-Fi is disabled, the app will attempt to re-enable it after a short delay.
4. You can manually enable Wi-Fi by going to **Settings > Wi-Fi**.

### Discovering Devices
1. After enabling Wi-Fi, press the **"Discover"** button to begin scanning for nearby devices that support Wi-Fi Direct.
2. The app will notify you of the discovery status, displaying **"Discovery Started"** or an error message if discovery fails.
3. Available devices will be listed in the **"Discovered Devices"** section.

### Connecting to Devices
1. Once nearby devices are discovered, tap on the device name from the list to initiate a connection.
2. The app will display a **"Connected to [Device Name]"** message once the connection is successful.
3. You will then be ready to send or receive files.

### Sending Files
1. Click the **"Select Files to Send"** button.
2. This will open the file explorer where you can choose multiple files.
3. Once selected, the files will be prepared for transfer.
4. The **"Sending File"** progress bar will update in real time to show the status of the transfer.

### Receiving Files
1. Once connected to another device, the app will automatically begin receiving files.
2. You can track the transfer progress using the **"Receiving File"** progress bar.
3. Received files will be saved in the **Downloads/File Sharing App** folder.
4. A notification will appear upon successful transfer, showing the path to the received file.

## Troubleshooting

| **Issue**                               | **Solution**                                                                 |
|-----------------------------------------|-----------------------------------------------------------------------------|
| Wi-Fi won’t enable                      | Go to your device’s **Wi-Fi settings** and manually enable Wi-Fi.           |
| No devices found during discovery       | Ensure that Wi-Fi is enabled and permissions are granted.                   |
| Failed to connect to a device           | Restart Wi-Fi on both devices and try again. Ensure the device supports Wi-Fi Direct. |
| File transfer failed                    | Check if the devices are still connected. Restart the app if necessary.     |
| Files not showing up after receiving    | Ensure the app has storage permissions, or manually check the **Downloads/File Sharing App** folder. |

## FAQs

**Q1: Can I send multiple files at once?**  
Yes, the app allows selecting and sending multiple files at once.

**Q2: What types of files can I send?**  
You can send any file type supported by your Android device.

**Q3: Where are the received files stored?**  
Received files are saved in the **Downloads/File Sharing App** folder.

**Q4: Do both devices need to have the app installed?**  
Yes, both devices need the app installed and running to share files.

**Q5: What is Wi-Fi Direct, and why do I need it?**  
Wi-Fi Direct allows devices to connect directly to each other over Wi-Fi without requiring an internet connection, enabling fast file transfers.
