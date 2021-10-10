package com.app.mangrove.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.app.mangrove.R
import com.app.mangrove.util.Constants
import com.app.mangrove.util.SharedPreferencesHelper
import com.app.mangrove.view.activity.DashboardActivity

class LogoutFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.logout_msg))
            .setPositiveButton(getString(R.string.yes)) { _,_ ->
                (activity as DashboardActivity).changeLoginDisplay(false)
                clearData()

              //  val intent = Intent(activity, MainActivity::class.java)
               // startActivity(intent)

            }
            .setNegativeButton(getString(R.string.no)) { _,_ ->
                dismiss()
            }
            .create()

    private fun clearBackStack() {
        val fragmentManager: FragmentManager = requireActivity().getSupportFragmentManager()
        for (fragment in fragmentManager.getFragments()) {
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit()
            }
        }
    }

    private fun clearData()
    {
        val prefsHelper = context?.let { SharedPreferencesHelper(it) }
        prefsHelper?.clearPrefs()
        Constants.isLoggedIn = false

    }
}