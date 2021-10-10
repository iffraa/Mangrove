package com.app.mangrove.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.core.app.ActivityCompat.startActivityForResult

import android.provider.MediaStore

import android.content.Intent

import android.content.DialogInterface
import android.net.Uri
import android.os.Environment
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.databinding.library.BuildConfig
import androidx.fragment.app.Fragment
import java.io.File
import java.util.*



class ImageHelper(mActivity: Activity, mFragment: Fragment, mTextView: TextView, mTakePicture: ActivityResultLauncher<Uri>
,mPickPicture: ActivityResultLauncher<String>
) {

    private val activity: Activity = mActivity
    private val fragment: Fragment = mFragment
    private val textView = mTextView
    private val takePicture = mTakePicture
    private val pickPicture = mPickPicture


    fun showImgChooserDialog() {

        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        myAlertDialog.setTitle("Upload Pictures Option")
        myAlertDialog.setMessage("How do you want to set your picture?")
        myAlertDialog.setPositiveButton("Gallery",
            DialogInterface.OnClickListener { arg0, arg1 ->


                pickPicture.launch("image/*")

            })
        myAlertDialog.setNegativeButton("Camera",
            DialogInterface.OnClickListener { arg0, arg1 ->
                takePicture()
            })
        myAlertDialog.show()
    }

    lateinit var imgUri: Uri
    fun takePicture() {
     /*   val root =
            File(
                Environment.getExternalStorageDirectory(),
                BuildConfig.APPLICATION_ID + File.separator
            )
        root.mkdirs()
        val fname = "img_" + System.currentTimeMillis() + ".jpg"
        val sdImageMainDirectory = File(root, fname)
        imgUri = FileProvider.getUriForFile(
            Objects.requireNonNull(activity),
            BuildConfig.APPLICATION_ID + ".provider", sdImageMainDirectory
        );
        //imgUri = FileProvider.getUriForFile(requireContext(),
        //   context?.applicationContext?.packageName + ".provider", sdImageMainDirectory)
        takePicture.launch(imgUri)*/
    }

 /*   val takePicture =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            //  if (success) {
            // The image was saved into the given Uri -> do something with it
            // Picasso.get().load(viewModel.profileImageUri).resize(800,800).into(registerImgAvatar)
            val path = imgUri.path
            textView.setText(path)
            //  }
        }

    private val pickImages =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                // The image was saved into the given Uri -> do something with it
                textView.setText(it.path)
            }
        }
*/

}