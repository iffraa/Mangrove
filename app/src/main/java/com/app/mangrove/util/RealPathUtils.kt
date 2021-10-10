package com.app.mangrove.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore

class RealPathUtils {

    fun getRealPathFromURI_API19(context: Context, uri: Uri): String {
        var filePath = "";
        val wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        val id = wholeID.split(":")[1];

        val column = arrayOf(MediaStore.Images.Media.DATA)


        // where id is equal to
        val sel = MediaStore.Images.Media._ID + "=?";

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column, sel, arrayOf(id), null
        )


        val columnIndex = cursor?.getColumnIndex(column[0]);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                filePath = columnIndex?.let { cursor.getString(it) }.toString();
            }
        }
        if (cursor != null) {
            cursor.close()
        };
        return filePath;
    }


  /*  @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
            context,
            contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
        = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
*/
}