package com.pictorytale.messenger.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

/**
 * Created by Marcin on 13.02.2018.
 */

public class Utils {
    public static AlertDialog.Builder BuildAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(title)
                .setMessage(message)
                //                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                //                    public void onClick(DialogInterface dialog, int which) {
                //                        // continue with delete
                //                    }
                //                })
                //                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                //                    public void onClick(DialogInterface dialog, int which) {
                //                        // do nothing
                //                    }
                //                })
                .setIcon(android.R.drawable.ic_dialog_alert);
        //.show();
        ;
        return builder;
    }
}
