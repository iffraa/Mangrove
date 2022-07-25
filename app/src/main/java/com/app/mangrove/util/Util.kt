package com.app.mangrove.util

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.app.mangrove.R
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import java.text.SimpleDateFormat
import java.util.*

import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import okhttp3.MediaType
import okhttp3.MultipartBody

import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


fun loadImage(view: ImageView, url: String, context: Context) {
    Glide.with(context)
        .load(url)
        .into(view)
    view.clipToOutline = true

}

fun loadCircularImg(view: ImageView, url: String, context: Context) {
    Glide.with(context)
        .load(url).circleCrop()
        .into(view)
    view.clipToOutline = true

}


fun showAlertDialog(activity: Activity, title: String, msg: String) {
    val builder: AlertDialog.Builder? = activity?.let {
        AlertDialog.Builder(it)
    }

    builder?.setMessage(msg)
        ?.setTitle(title)?.setPositiveButton(R.string.ok,
            { dialog, id ->
            })
    builder?.create()?.show()

}

fun CharSequence?.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()


fun ImageView.loadImage(uri: String?) {
    val options = RequestOptions()
        .error(R.mipmap.ic_launcher)

    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

@BindingAdapter("android:imageUrl")
fun loadViewImage(view: ImageView, url: String?) {
    view.loadImage(url)
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}


fun selectDOB(context: Context, dobField: EditText) {
    hideKeyboard(context as Activity)

    val newCalendar: Calendar = Calendar.getInstance()
    var mDatePickerDialog: DatePickerDialog
    mDatePickerDialog = context?.let {
        DatePickerDialog(
            it,
            { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                val sd = SimpleDateFormat("dd-MM-yyyy")

                val startDate: Date = newDate.getTime()
                val fdate: String = sd.format(startDate)
                dobField.setText(fdate)
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)


        )
    }!!

    mDatePickerDialog?.getDatePicker()//?.setMaxDate(System.currentTimeMillis())
    mDatePickerDialog.show()
}

fun getGender(femaleChkBx: CheckBox, maleChkBx: CheckBox): String {
    var gender = Constants.MALE

    femaleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            maleChkBx.setChecked(false)
            gender = Constants.FEMALE
        }

    })

    maleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            femaleChkBx.setChecked(false)
            gender = Constants.MALE
        }
    })
    return gender
}


fun showDateTime(context: Context, editText: EditText) {

    hideKeyboard(context as Activity)

    val d = Date()
    val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

    dateDialog.title(context.getString(R.string.select_date))
        .titleTextColor(context.getResources().getColor(R.color.white))
        .minutesStep(1)
        .minDateRange(d)
        .backgroundColor(context.getResources().getColor(R.color.white))
        .mainColor(
            context.getResources().getColor(
                R.color.blue_text
            )
        )
        .listener { date ->
            val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm aa"
            val sdf = SimpleDateFormat(DATE_TIME_FORMAT)
            val sdate = sdf.format(date)
            editText.setText(sdate)
        }.display()

}

fun onHomeIconClick(homeIcon: ImageView, action: NavDirections) {
    homeIcon.setOnClickListener(View.OnClickListener {
        Navigation.findNavController(it).navigate(action)

    })

}


fun showDate(context: Context, editText: EditText) {

    hideKeyboard(context as Activity)

    val d = Date()
    val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

    dateDialog.title(context.getString(R.string.select_date))
        .titleTextColor(context.getResources().getColor(R.color.white))
        .displayHours(false)
        .displayMinutes(false)
        // .minDateRange(d)

        .displayDays(false)
        .displayMonth(true)
        .displayYears(true)
        .displayDaysOfMonth(true)

        .backgroundColor(context.getResources().getColor(R.color.white))
        .mainColor(context.getResources().getColor(R.color.blue_text))
        .listener { date ->
            val DATE_FORMAT = "yyyy-MM-dd"
            var sdf = SimpleDateFormat(DATE_FORMAT)
            val sdate = sdf.format(date)
            editText.setText(sdate)
        }.display()

}


fun displayServerErrors(errorJson: String, context: Context) {
    var message = ""
    try {
        val json = JSONObject(errorJson)
        var errors: ArrayList<String> = arrayListOf()
        val iter: Iterator<String> = json.keys()
        while (iter.hasNext()) {
            val key = iter.next()
            try {
                var value = json.get(key)

                val filtered = "[]\""
                value = value.toString().filterNot { filtered.indexOf(it) > -1 }

                errors.add(value)
            } catch (e: JSONException) {
                // Something went wrong!
            }
        }

        for (error in errors) {
            message += error + "\n"
        }

        showAlertDialog(
            context as Activity,
            context.getString(R.string.app_name), message
        )
    } catch (ex: JSONException) {
        showAlertDialog(
            context as Activity,
            context.getString(R.string.app_name), ex.toString()
        )
    }
}

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}


fun getGalleryImgPath(selectedImageUri: Uri, context: Context): String? {

    val contentResolver = context.contentResolver ?: return null

    // Create file path inside app's data dir
    val filePath = (context.applicationInfo.dataDir.toString() + File.separator
            + System.currentTimeMillis())

    val file = File(filePath)
    try {
        val inputStream = contentResolver.openInputStream(selectedImageUri) ?: return null
        val outputStream: OutputStream = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()
    } catch (ignore: IOException) {
        return null
    }

    return file.absolutePath
}
