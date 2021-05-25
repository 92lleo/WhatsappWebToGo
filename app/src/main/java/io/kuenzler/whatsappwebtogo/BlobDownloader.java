package io.kuenzler.whatsappwebtogo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BlobDownloader {
    private Context context;

    public final static String JsInstance = "Downloader";

    public BlobDownloader(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data) throws IOException {
        convertBase64StringToFileAndStoreIt(base64Data);
    }

    public static String getBase64StringFromBlobUrl(String blobUrl) {
        if (blobUrl.startsWith("blob")) {
            return "javascript: var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobFile = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobFile);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            "+JsInstance+".getBase64FromBlobData(base64data);" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();";
        }
        return "javascript: console.log('It is not a Blob URL');";
    }

    private void convertBase64StringToFileAndStoreIt(String base64File) throws IOException {
        final int notificationId = 1;
        final String[] strings = base64File.split(",");
        Toast.makeText(context, base64File.toString(), Toast.LENGTH_LONG).show();
        for(String s : strings){
            Toast.makeText(context, s, Toast.LENGTH_LONG).show();
        }
        String extension = null;

        extension = MimeTypes.lookupExt(strings[0]);
        if (null == extension){
            if (strings.length > 0) {
                extension = strings[0];
                extension = "." + extension.substring(extension.indexOf('/') + 1, extension.indexOf(';'));
            } else {
                extension = ".file";
            }
        }

        @SuppressLint("SimpleDateFormat") //SDF is just fine for filename
        final String currentDateTime = new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date());
        final String dlFileName = "WAWTG_" + currentDateTime + extension;
        final File dlFilePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + dlFileName);
        final byte[] fileAsBytes = Base64.decode(base64File.replaceFirst(strings[0], ""), 0);
        final FileOutputStream os = new FileOutputStream(dlFilePath, false);
        os.write(fileAsBytes);
        os.flush();

        if (dlFilePath.exists()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri apkURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", dlFilePath);
            intent.setDataAndType(apkURI, MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            String CHANNEL_ID = "Downloads";
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_LOW);
                Notification notification = new Notification.Builder(context, CHANNEL_ID)
                        .setContentText(String.format(context.getString(R.string.notification_text_saved_as), dlFileName))
                        .setContentTitle(context.getString(R.string.notification_title_tap_to_open))
                        .setContentIntent(pendingIntent)
                        .setChannelId(CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .build();
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationManager.notify(notificationId, notification);
                }
            } else {
                NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(String.format(context.getString(R.string.notification_text_saved_as), dlFileName))
                        .setContentText(context.getString(R.string.notification_title_tap_to_open));
                if (notificationManager != null) {
                    notificationManager.notify(notificationId, b.build());
                    Handler h = new Handler();
                    long delayInMilliseconds = 1000;
                    h.postDelayed(new Runnable() {
                        public void run() {
                            notificationManager.cancel(notificationId);
                        }
                    }, delayInMilliseconds);
                }
            }
        }
        Toast.makeText(context, R.string.toast_saved_to_downloads_folder, Toast.LENGTH_SHORT).show();
    }
}
