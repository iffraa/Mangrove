package com.app.mangrove.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentServicesBinding
import com.app.mangrove.model.ServiceEntry
import com.app.mangrove.model.Status
import com.app.mangrove.util.*
import com.app.mangrove.view.ServiceListAdapter
import com.app.mangrove.viewmodel.ServicesViewModel
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [ServicesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ServicesFragment : Fragment() {
    private lateinit var binding: FragmentServicesBinding
    private lateinit var viewModel: ServicesViewModel
    private var prefsHelper = SharedPreferencesHelper()
    private val servicesAdapter = ServiceListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentServicesBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val data = prefsHelper.getObject(Constants.USER_DATA)
        val token = data?.token

        binding.progressBar.progressbar.visibility = View.VISIBLE

        setupViewModel()
        token?.let { viewModel.getServices(it,"100","1") }
        setServicesObserver()

        binding.btnCreate.setOnClickListener(View.OnClickListener {
            val action = ServicesFragmentDirections.actionServiceToForm()
            action?.let { Navigation.findNavController(view).navigate(it) }
        })

        val action = ServicesFragmentDirections.actionServiceToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)

    }

    private fun setServicesObserver() {
        viewModel.getServices().observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                   it.api_data?.let { it1 -> setServicesList(it1.data) }
                }
                Status.ERROR -> {
                    //Handle Error
                    binding.progressBar.progressbar.visibility = View.GONE

                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )

                }
            }
        })
    }


    private fun setServicesList(services: ArrayList<ServiceEntry>) {
        binding.rvServices.visibility = View.VISIBLE
        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvServices.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            adapter = servicesAdapter
            servicesAdapter.setServicesList(services,context)

        }


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
            .get(ServicesViewModel::class.java)
    }

}