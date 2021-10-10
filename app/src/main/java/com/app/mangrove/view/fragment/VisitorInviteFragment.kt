package com.app.mangrove.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentVisitorInviteBinding
import com.app.mangrove.model.*
import com.app.mangrove.util.*
import com.app.mangrove.view.InviteVisitorListAdapter
import com.app.mangrove.viewmodel.VisitorInviteViewModel
import com.jakewharton.rxbinding4.widget.textChanges
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [VisitorInviteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorInviteFragment : Fragment() {

    private lateinit var binding: FragmentVisitorInviteBinding
    private lateinit var viewModel: VisitorInviteViewModel
    private var prefsHelper = SharedPreferencesHelper()
    private lateinit var userData: Data
    private lateinit var visitorListAdapter: InviteVisitorListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVisitorInviteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        userData = prefsHelper.getObject(Constants.USER_DATA)!!

        binding.etVisitorsTime.setOnClickListener(View.OnClickListener {
            showDateTime(requireContext(), binding.etVisitorsTime)
        })

        binding.btnVisitors.setOnClickListener(View.OnClickListener {
            val action = VisitorInviteFragmentDirections.actionInviteToList()
            Navigation.findNavController(it).navigate(action)
        })

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            val visitors = binding.etVisitorsNum.text
            if (visitors.isNullOrEmpty() || visitors.equals("0")) {
                showAlertDialog(
                    context as Activity,
                    getString(R.string.app_name),
                    getString(R.string.visitor_error)
                )
            } else {
                binding.rlInclude.visibility = View.VISIBLE
                setupViewModel()
                addVisitor()
                setupObserver()
            }

        })

        setVisitorList()
        val action = VisitorInviteFragmentDirections.actionVisitorToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)

    }

    private fun setVisitorList() {

        visitorListAdapter = context?.let { InviteVisitorListAdapter(arrayListOf(), it, this) }!!

        binding.rvVisitor.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = visitorListAdapter
        }

        binding.rvVisitor.addItemDecoration(
            DividerItemDecoration(
                binding.rvVisitor.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.etVisitorsNum.textChanges()
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val noOfVisitors: String = binding.etVisitorsNum.text.toString()
                if (!noOfVisitors.isEmpty()) {

                    activity?.let { hideKeyboard(it) }

                    val no_of_visitors: Int = noOfVisitors.toInt()
                    if (no_of_visitors > 0) {
                        val visitors = ArrayList<Visitor>(no_of_visitors)
                        for (i in 1..no_of_visitors) {
                            val person = Visitor("0", "", "", "", "", "")
                            visitors.add(person)
                        }
                        binding.rvVisitor.visibility = View.VISIBLE
                        binding.btnSubmit.visibility = View.VISIBLE
                        visitorListAdapter?.setVisitorList(
                            visitors
                        )

                    } else {
                        binding.rvVisitor.visibility = View.GONE
                        binding.btnSubmit.visibility = View.GONE

                    }
                }


            }

    }

    fun addVisitor() {
        val visitors: ArrayList<Visitor>? = visitorListAdapter.getData()
        val servicePackage: ServicePackage = visitorListAdapter.getPackage()

        val token = userData?.token
        val no_of_visitors = binding.etVisitorsNum.text.toString()
        val resort_id = userData?.user?.resort_id.toString()
        val visiting_date_time = binding.etVisitorsTime.text.toString()
        val sub_total = ""
        val total_price = servicePackage.price
        val discount_percentage = servicePackage.discount_percentage
        val visitorReq = visitors?.let {
            VisitorRequest(
                no_of_visitors,
                resort_id,
                visiting_date_time,
                discount_percentage,
                sub_total,
                "",
                total_price,
                it
            )
        }

        token?.let {
            if (visitorReq != null) {
                viewModel.addVisitors(it, visitorReq)
            }
        }
    }


    fun getDateTime(): String {
        val dateTime = binding.etVisitorsTime.text.toString()

        if (dateTime.isNullOrEmpty()) {
            activity?.let {
                showAlertDialog(
                    it,
                    getString(R.string.app_name),
                    getString(R.string.missing_date)
                )
            }
        } else {
            return dateTime
        }

        return ""
    }

    private fun setupObserver() {

        viewModel.getVisitorResponse()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {

                        binding.rlInclude.visibility = View.GONE
                        it.api_data?.message?.let { it1 ->
                            showAlertDialog(
                                context as Activity, requireContext().getString(R.string.app_name),
                                it1
                            )
                        }

                }
                Status.LOADING -> {
                    binding.rlInclude.visibility = View.GONE
                }
                Status.ERROR -> {
                    //Handle Error
                    binding.rlInclude.visibility = View.GONE
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.add_visitor_error)
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
            .get(VisitorInviteViewModel::class.java)
    }

}