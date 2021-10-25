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
import com.app.mangrove.databinding.FragmentVisitorsBinding
import com.app.mangrove.model.Status
import com.app.mangrove.model.Visitor
import com.app.mangrove.util.*
import com.app.mangrove.view.VisitorListAdapter
import com.app.mangrove.viewmodel.ServicesViewModel
import com.app.mangrove.viewmodel.VisitorsViewModel
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [VisitorsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorsFragment : Fragment() {
    private lateinit var binding: FragmentVisitorsBinding
    private lateinit var viewModel: VisitorsViewModel
    private var prefsHelper = SharedPreferencesHelper()
    private val visitorAdapter = VisitorListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVisitorsBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val data = prefsHelper.getObject(Constants.USER_DATA)
        val token = data?.token

        binding.progressBar.progressbar.visibility = View.VISIBLE

        setupViewModel()
        token?.let { viewModel.getVisitors(it) }
        setVisitorsObserver()

        val action = ServicesFragmentDirections.actionServiceToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)

    }

    private fun setVisitorsObserver() {
        viewModel.getVisitorResponse().observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    it.api_data?.let {
                        val visitorResponse = it.data.invitees.data
                        val visitors = visitorResponse
                        var visitorList = arrayListOf<Visitor>()

                        for (data in visitors) {
                            val price = data.total_price
                            val invitations = data.visitors
                            for (visitor in invitations) {
                                visitor.price = price.toString()
                                visitorList.add(visitor)

                            }
                        }
                        setVisitorsList(visitorList)
                    }
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


    private fun setVisitorsList(visitors: ArrayList<Visitor>) {
        binding.rvVisitors.visibility = View.VISIBLE
        binding.rvVisitors.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvVisitors.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            adapter = visitorAdapter
            visitorAdapter.setVisitorsList(visitors, context)

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
            .get(VisitorsViewModel::class.java)
    }

}