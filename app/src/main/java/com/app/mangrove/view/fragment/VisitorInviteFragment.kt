package com.app.mangrove.view.fragment

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentVisitorInviteBinding
import com.app.mangrove.model.*
import com.app.mangrove.util.*
import com.app.mangrove.view.InviteVisitorListAdapter
import com.app.mangrove.viewmodel.VisitorInviteViewModel
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.jakewharton.rxbinding4.widget.textChanges
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    private var packages: HashMap<String, ServicePackage> =  hashMapOf()

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

        binding.etVisitorsTime.setOnClickListener {
            showDateTime()
        }

        binding.btnVisitors.setOnClickListener {
            viewVisitorList(it)
        }

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            val visitors = binding.etVisitorsNum.text
            if (visitors.isNullOrEmpty() || visitors.equals("0")) {
                showAlertDialog(
                    context as Activity,
                    getString(R.string.app_name),
                    getString(R.string.visitor_error)
                )
            } else {
                hideKeyboard(requireActivity())
                binding.rlInclude.visibility = View.VISIBLE

                addVisitor()
                setupObserver(it)
            }

        })

        setVisitorList()
        val action = VisitorInviteFragmentDirections.actionVisitorToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)

    }

    private fun showDateTime() {

        hideKeyboard(requireActivity())

        val d = Date()
        val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

        dateDialog.title(getString(R.string.select_date))
            .titleTextColor(getResources().getColor(R.color.white))
            .minutesStep(1)
            .minDateRange(d)
            .backgroundColor(getResources().getColor(R.color.white))
            .mainColor(getResources().getColor(R.color.blue_text))
            .listener { date ->
                val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm aa"
                val sdf = SimpleDateFormat(DATE_TIME_FORMAT)
                val sdate = sdf.format(date)
                binding.etVisitorsTime.setText(sdate)

                setupViewModel()
                getPackages()
            }.display()

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
                            val person = Visitor("0", "", "", "", "",
                                "",null, "")
                            visitors.add(person)
                        }
                        binding.rvVisitor.visibility = View.VISIBLE
                        binding.btnSubmit.visibility = View.VISIBLE
                        visitorListAdapter?.setVisitorList(
                            visitors,packages
                        )

                    } else {
                        binding.rvVisitor.visibility = View.GONE
                        binding.btnSubmit.visibility = View.GONE

                    }
                }


            }

    }

    private fun addVisitor() {
        val visitors: ArrayList<Visitor> = visitorListAdapter.getData()
        var (total, subTotal, discount) = getPriceInfo(visitors)

        val token = userData?.token
        val no_of_visitors = binding.etVisitorsNum.text.toString()
        val resort_id = userData?.user?.resort_id.toString()
        val visiting_date_time = binding.etVisitorsTime.text.toString()
        val customDiscount = userData.user?.invite_visitor_discount_percentage
        val visitorReq = visitors?.let {
            VisitorRequest(
                no_of_visitors,
                resort_id,
                visiting_date_time,
                customDiscount,
                subTotal,
                discount,
                total,
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

    private fun setupObserver(view: View) {

        viewModel.getVisitorResponse()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {

                    binding.rlInclude.visibility = View.GONE
                    showSuccessDialog(view)

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

    private fun showSuccessDialog(view: View) {
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }

        builder?.setMessage(getString(R.string.visitor_invite_success))
            ?.setTitle(getString(R.string.app_name))?.setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    viewVisitorList(view)
                })
        builder?.create()?.show()
    }

    private fun viewVisitorList(view: View) {
        val action = VisitorInviteFragmentDirections.actionInviteToList()
        Navigation.findNavController(view).navigate(action)

    }

    private fun getPriceInfo(visitors: ArrayList<Visitor>): Triple<String, String, String>
    {
        var discount = 0
        var total = 0
        var subTotal = 0
        val serverDiscount = userData.user?.invite_visitor_discount_percentage
        var customDiscount: Int = serverDiscount?.toInt()!!

        for(visitor in visitors)
        {
            val servicePackage = visitor.servicePackage
            val price = servicePackage?.price
            subTotal += price?.toInt() ?: 0
        }

        if (customDiscount != 0) {
            discount = (customDiscount/100) * subTotal
        }
        total = subTotal - discount
        return Triple(total.toString(), subTotal.toString(), discount.toString())
    }


    private fun getPackages() {

        val visitingDate = binding.etVisitorsTime.text.toString()

        userData.token?.let {
            viewModel.getMalePackages(
                it, visitingDate,
                userData.user?.resort_id.toString()
            )
            observeMalePckgVM()


            viewModel.getFemalePackages(
                it,
                visitingDate,
                userData.user?.resort_id.toString()
            )
            observeFemalePckgVM()

        }
    }


    private fun observeMalePckgVM() {

        viewModel.getMalePackages()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    it.api_data?.let { it1 -> packages.put(Constants.MALE, it1.data) }

                }

                Status.ERROR -> {
                    //Handle Error
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.pckgs_error)
                    )

                }
            }
        })

    }


    private fun observeFemalePckgVM() {

        viewModel.femalePackage?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    it.api_data?.let { it1 -> packages.put(Constants.FEMALE, it1.data) }

                }

                Status.ERROR -> {
                    //Handle Error
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.pckgs_error)
                    )

                }
            }
        })

    }
}