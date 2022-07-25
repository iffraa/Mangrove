package com.app.mangrove.view.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.lifecycle.ViewModelProviders
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentGuestReservationBinding
import com.app.mangrove.model.*
import com.app.mangrove.util.*
import com.app.mangrove.view.GuestListAdapter
import com.app.mangrove.viewmodel.GHReservationViewModel
import com.app.mangrove.viewmodel.LoginViewModel
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder


/**
 * A simple [Fragment] subclass.
 * Use the [GHReservationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GHReservationFragment : Fragment() {

    private lateinit var viewModel: GHReservationViewModel
    private var _binding: FragmentGuestReservationBinding? = null

    private val guestListAdapter = GuestListAdapter()
    private val binding get() = _binding!!
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var resortId = ""
    private lateinit var obj: Data
    private lateinit var selectedUnit: AvailableUnit
    private var guests_limit = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGuestReservationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()

        binding.progressBar.progressbar.visibility = View.VISIBLE
        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        obj = prefsHelper.getObject(Constants.USER_DATA)!!

        getResorts()
        setReservationDates()
        setGuestList()

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            binding.progressBar.progressbar.visibility = View.VISIBLE
            addGuests()
            addGuestObserver()
        })

        val action = GHReservationFragmentDirections.actionGuestToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)
    }

    fun addGuests() {
        val guests: ArrayList<Guest>? = guestListAdapter.getData()

        val no_of_guests = binding.etGuestsNo.text.toString()
        val setup_unit_id = selectedUnit.id
        val discount = selectedUnit.discount
        val package_id = selectedUnit.package_id
        val sub_total = selectedUnit.sub_total
        val total_price = selectedUnit.total_price
        val custom_discount_percentage = selectedUnit.discount_percentage
        val reservation_date = binding.etChkIn.text.toString()
        val check_out_date = binding.etChkOut.text.toString()

        val reservationReq = guests?.let {
            GHReservationRequest(
                setup_unit_id.toString(),
                discount,
                no_of_guests,
                package_id,
                resortId,
                sub_total,
                total_price,
                reservation_date,
                check_out_date,
                custom_discount_percentage,
                it
            )
        }

        obj?.token?.let {
            reservationReq?.let { it1 ->
                viewModel.addGHReservation(
                    it,
                    it1, requireContext()
                )
            }
        }
    }


    private fun setReservationDates() {


        binding.etChkIn.setOnClickListener(View.OnClickListener {
            showDateTime(binding.etChkIn, Constants.CHKIN_TIME)
        })

        binding.etChkOut.setOnClickListener(View.OnClickListener {
            showDateTime(binding.etChkOut, Constants.CHKOUT_TIME)
        })

    }

    fun showDateTime(editText: EditText, showTime: String) {

        hideKeyboard(context as Activity)

        val d = Date()
        SingleDateAndTimePickerDialog.Builder(context as Activity)
            .title(getString(R.string.select_date))
            .titleTextColor(getResources().getColor(R.color.white))
            .displayMinutes(false)
            .displayHours(false)
            .minDateRange(d)
            .backgroundColor(getResources().getColor(R.color.white))
            .mainColor(getResources().getColor(R.color.blue_text))
            .listener { date ->
                val DATE_FORMAT = "yyyy-MM-dd"
                var sdf = SimpleDateFormat(DATE_FORMAT)

                val sdate = sdf.format(date)
                editText.setText(sdate + " " + showTime)

                if(!binding.etChkIn.text.toString().isNullOrEmpty() && !binding.etChkOut.text.toString().isNullOrEmpty())
                {
                    getAvailableUnits()
                }
            }.display()


    }

    private fun setDateTimeField(etField: EditText, appendTxt: String) {

        val newCalendar: Calendar = Calendar.getInstance()
        var mDatePickerDialog: DatePickerDialog
        mDatePickerDialog = context?.let {
            DatePickerDialog(
                it,
                { view, year, monthOfYear, dayOfMonth ->
                    val newDate: Calendar = Calendar.getInstance()
                    newDate.set(year, monthOfYear, dayOfMonth)
                    var sd = SimpleDateFormat("dd-MM-yyyy")
                    val startDate: Date = newDate.getTime()
                    val fdate: String = sd.format(startDate)

                    etField.setText(fdate + " at " + appendTxt)
                    hideKeyboard(context as Activity)

                    getAvailableUnits()
                },
                newCalendar.get(Calendar.YEAR),
                newCalendar.get(Calendar.MONTH),
                newCalendar.get(Calendar.DAY_OF_MONTH)
            )
        }!!
        mDatePickerDialog?.getDatePicker()//?.setMaxDate(System.currentTimeMillis())
        mDatePickerDialog.show()
    }

    private fun getResorts() {

        obj.token?.let { viewModel.getCustomerResorts(it) }

        resortsObserver()
    }

    private fun getAvailableUnits() {
        val chk_in_date = binding.etChkIn.text.toString()
        val chk_out_date = binding.etChkOut.text.toString()

        if (!chk_in_date.isNullOrEmpty() && !chk_out_date.isNullOrEmpty()) {

            binding.progressBar.progressbar.visibility = View.VISIBLE
            obj.token?.let { viewModel.getAvailableUnits(it, resortId, chk_in_date, chk_out_date) }
            unitsObserver()
        }
    }

    private fun populateSpinners(isUnits: Boolean) {

        var data: ArrayList<String>? = arrayListOf<String>()

        if (isUnits) {
            val units = viewModel.getUnits().value?.api_data?.data
             units?.forEachIndexed { index, e ->
                units.get(index).unit?.let {
                    if (data != null) {
                        data.add(it)
                    }
                }
            }

        } else {
            val resorts = viewModel.getResorts().value?.api_data?.resort
            resorts?.forEachIndexed { index, e ->
                resorts.get(index).name?.let {
                    if (resorts.get(index).name.equals(Constants.MANGROVE_UNITS)) {
                        data?.add(it)
                    }
                }
            }
        }

        // Creating adapter for spinner
        activity?.let {
            ArrayAdapter<String>(
                it,
                R.layout.item_spinner,
                data as MutableList<String>
            )

                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    if (isUnits) {
                        binding.spUnits.adapter = adapter
                        onUnitsSelection()
                    } else {
                        binding.spResorts.adapter = adapter
                        onResortSelection()
                    }
                }
        }


    }

    private fun onResortSelection() {
        binding.spResorts?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                val resorts = viewModel.getResorts().value?.api_data?.resort
                resorts?.forEachIndexed { index, e ->
                    if (resorts.get(index).name?.equals(selectedItem) == true) {
                        resortId = resorts.get(index).id.toString()
                    }

                }
            }


        }

    }

    private fun onUnitsSelection() {
        binding.spUnits?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                val units = viewModel.getUnits().value?.api_data?.data
                units?.forEachIndexed { index, e ->
                    if (units.get(index).unit?.equals(selectedItem) == true) {
                        selectedUnit = units.get(index)
                        setUnitDetails()
                    }

                }
            }

        }
    }

    private fun setUnitDetails() {
        binding.tvPrice.setText(selectedUnit.price)
        binding.tvMaxGuests.setText(
            getString(R.string.max) + " " + selectedUnit.no_of_guest + " " + getString(
                R.string.max_guests
            )
        )
        guests_limit = selectedUnit.no_of_guest.toInt()
        binding.tvPayment.setText(getString(R.string.payable) + " " + selectedUnit.total_price)
    }

    private fun setGuestList() {

        binding.rvGuest.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestListAdapter
        }

        binding.rvGuest.addItemDecoration(
            DividerItemDecoration(
                binding.rvGuest.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.etGuestsNo.textChanges()
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val enteredGuestTxt: String = binding.etGuestsNo.text.toString()
                if (!enteredGuestTxt.isEmpty()) {

                    activity?.let { hideKeyboard(it) }

                    val no_of_entered_guests: Int = enteredGuestTxt.toInt()
                    if (no_of_entered_guests > 0 && no_of_entered_guests < guests_limit) {
                        val visitors = ArrayList<Guest>(no_of_entered_guests)
                        for (i in 1..no_of_entered_guests) {
                            val person = Guest("", "", "", "")
                            visitors.add(person)
                        }
                        binding.rvGuest.visibility = View.VISIBLE
                        binding.btnSubmit.visibility = View.VISIBLE
                        guestListAdapter?.setGuestList(
                            visitors, requireContext()
                        )

                    } else if (no_of_entered_guests > guests_limit) {
                        showAlertDialog(
                            requireContext() as Activity,
                            getString(R.string.app_name),
                            getString(R.string.guests_exceeded)
                        )
                        binding.rvGuest.visibility = View.GONE
                        binding.btnSubmit.visibility = View.GONE

                    } else {
                        binding.rvGuest.visibility = View.GONE
                        binding.btnSubmit.visibility = View.GONE

                    }
                }


            }

    }

    private fun unitsObserver() {
        viewModel.getUnits()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE

                    val units = it.api_data?.data
                    if(!units.isNullOrEmpty()) {
                        populateSpinners(true)
                    }
                    else
                    {
                        showAlertDialog(
                            context as Activity,
                            getString(R.string.app_name),
                            getString(R.string.no_units)
                        )

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

    private fun setupViewModel() {

        viewModel = ViewModelProviders.of(
            this,
            (context as Activity).application?.let {
                ViewModelFactory(
                    ApiHelperImpl(RetrofitBuilder.apiService), it
                )

            }
        )
            .get(GHReservationViewModel::class.java)
    }

    private fun resortsObserver() {
        viewModel.getResorts().observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    populateSpinners(false)
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


    private fun addGuestObserver() {
        viewModel.getGHResponse().observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    val msg = it.api_data?.message
                    binding.progressBar.progressbar.visibility = View.GONE
                    if (msg != null) {
                        showAlertDialog(
                            context as Activity,
                            getString(R.string.app_name),msg
                        )
                    }


                }

                Status.ERROR -> {
                    //Handle Error
                    binding.progressBar.progressbar.visibility = View.GONE

                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.adding_guest_error)
                    )

                }
            }
        })
    }


}