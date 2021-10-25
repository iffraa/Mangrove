package com.app.mangrove.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentLoginBinding
import com.app.mangrove.model.Data
import com.app.mangrove.viewmodel.LoginViewModel

import com.app.mangrove.model.Status
import com.app.mangrove.util.*
import com.app.mangrove.view.activity.DashboardActivity
import com.google.gson.Gson
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var prefsHelper = SharedPreferencesHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        setupViewModel()
        binding.etEmail.removeUnderline()
        binding.etPwd.removeUnderline()


        binding.btnLogin.setOnClickListener(View.OnClickListener {

            if (context?.let { it1 -> ConnectivityUtils.isConnected(it1) } == true) {

                binding.etEmail.setText("warsi")
                binding.etPwd.setText("12345678")

                val user_name = binding.etEmail.text.toString()
                val pwd = binding.etPwd.text.toString()

                if (!user_name.isNullOrEmpty() && !pwd.isNullOrEmpty()) {
                    binding.rlInclude.visibility = View.VISIBLE

                    viewModel.login(user_name, pwd)
                    setupObserver(it)
                } else {
                    activity?.let { it1 ->
                        showAlertDialog(
                            it1,
                            getString(R.string.app_name),
                            getString(R.string.empty_fields)
                        )
                    }
                }
            } else {
                activity?.let { it1 ->
                    showAlertDialog(
                        it1,
                        getString(R.string.app_name),
                        getString(R.string.connectivity_error)
                    )
                }
            }
        })

    }

    private fun setupObserver(view: View) {
        viewModel.getResponse()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.rlInclude.visibility = View.GONE

                    val api_data: Data? = it.api_data?.data

                    val gson = Gson()
                    val json = gson.toJson(api_data)
                    prefsHelper.saveData(json, Constants.USER_DATA)

                    navigateToDashboard(view)

                }

                Status.ERROR -> {
                    //Handle Error
                    binding.rlInclude.visibility = View.GONE

                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.invalid_credentials)
                    )

                }
            }
        })
    }

    private fun setupViewModel() {

        viewModel = ViewModelProviders.of(
            this,
            (context as Activity).application?.let {
                ViewModelFactory(
                    ApiHelperImpl(RetrofitBuilder.apiService), it
                )

            }
        )
            .get(LoginViewModel::class.java)
    }

    private fun navigateToDashboard(view: View) {

        activity?.let { hideKeyboard(it) }
        (activity as DashboardActivity).changeLoginDisplay(true)

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_login, true)
            .build()

        val action = LoginFragmentDirections.actionLoginToDashboard()
        action?.let { Navigation.findNavController(view).navigate(it, navOptions) }
    }

    fun EditText.removeUnderline() {
        val paddingBottom = this.paddingBottom
        val paddingStart = ViewCompat.getPaddingStart(this)
        val paddingEnd = ViewCompat.getPaddingEnd(this)
        val paddingTop = this.paddingTop
        ViewCompat.setBackground(this, null)
        ViewCompat.setPaddingRelative(this, paddingStart, paddingTop, paddingEnd, paddingBottom)
    }


}