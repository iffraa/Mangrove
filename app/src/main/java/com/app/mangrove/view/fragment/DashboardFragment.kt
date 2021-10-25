package com.app.mangrove.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentDashboardBinding
import com.app.mangrove.model.Data
import com.app.mangrove.model.FamilyMember
import com.app.mangrove.model.Status
import com.app.mangrove.model.User
import com.app.mangrove.util.*
import com.app.mangrove.view.FMemberListAdapter
import com.app.mangrove.viewmodel.DashboardViewModel
import com.google.gson.Gson
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private var prefsHelper = SharedPreferencesHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDashboardBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        setupViewModel()

        val data: Data? = prefsHelper.getObject(Constants.USER_DATA)
        data?.token?.let { viewModel.getProfile(it) }

        setupObserver()

        val action = DashboardFragmentDirections.actionDashboardToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)

    }

    private fun setupObserver() {
        viewModel.getResponse()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressbar.visibility = View.GONE

                    val data: Data? = it.api_data?.data
                    data?.user?.let { user -> setProfileDetails(user)
                        user.members?.let { it1 -> setFamilyList(it1) }
                    }


                }
                Status.LOADING -> {
                    binding.progressbar.visibility = View.GONE
                }
                Status.ERROR -> {
                    //Handle Error
                    binding.progressbar.visibility = View.GONE

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
            .get(DashboardViewModel::class.java)
    }


    private fun setProfileDetails(user: User) {
        context?.let {
            user.profile_image?.let { it1 -> loadCircularImg(binding.ivProfile, it1, it) }
            user.qr_code?.let { it1 -> loadImage(binding.ivBarcode, it1, it) }
        }

        var unitNo = user.unit_no
        if (unitNo.isNullOrEmpty())
            unitNo = ""

        binding.tvName.setText(user.name)
        binding.tvRole.setText(user.role + " - " + getString(R.string.unit) + " " + unitNo)
        binding.tvId.setText(user.id.toString())
        binding.tvMobile.setText(user.contact_no)
        binding.tvEmail.setText(user.email)

        binding.tvDaysLeft.setText(user.contract_remaining_days)
        binding.tvExpiry.setText(user.contract_end_date)
    }

    private fun setFamilyList(familyMembers: ArrayList<FamilyMember>) {
        binding.rvFam.visibility = View.VISIBLE
        binding.rvFam.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvFam.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            val famListAdapter = FMemberListAdapter(arrayListOf())
            adapter = famListAdapter
            famListAdapter.setFamilyList(familyMembers)
        }

    }
}