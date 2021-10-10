package com.app.mangrove.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import java.util.*
import kotlin.collections.ArrayList
import com.hbb20.CountryCodePicker
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentUnitFormBinding
import com.app.mangrove.model.*
import com.app.mangrove.util.*
import com.app.mangrove.view.FamilyListAdapter
import com.app.mangrove.view.PackageListAdapter
import com.app.mangrove.viewmodel.UnitApplicationViewModel

import android.view.Gravity
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder


/**
 * A simple [Fragment] subclass.
 * Use the [UnitApplicationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UnitApplicationFragment() : Fragment() {

    private var _binding: FragmentUnitFormBinding? = null
    private val binding get() = _binding!!
    private var resortId = ""
    private lateinit var viewModel: UnitApplicationViewModel
    private val familyListAdapter = FamilyListAdapter(arrayListOf())

    //private val packageListAdapter = PackageListAdapter(arrayListOf())
    private lateinit var packageListAdapter: PackageListAdapter
    private var prefsHelper = SharedPreferencesHelper()
    private var roleId: String = ""
    private var service_id: String = ""
    private var package_id: String = ""
    private var marginTop = 0
    private var userObj: Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            marginTop = it.getInt(getString(R.string.margin_top))

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnitFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        prefsHelper.getObject(Constants.USER_DATA).also {
            if (it != null) {
                userObj = it
                roleId = userObj?.user?.role_id.toString()
            }
        }

        binding.progressBar.progressbar.visibility = View.VISIBLE

        setupViewModel()
        setLogoMargin()
        selectPicture()

        getFacilities()
        getRoles()

        binding.etDob.setOnClickListener(View.OnClickListener {
            showDate(requireContext(), binding.etDob)
        })

        getGender(binding.cbFemale, binding.cbMale)
        setFamilyList()
        applyForMembership()

        val action = UnitApplicationFragmentDirections.actionUnitAppToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)

    }

    private fun setLogoMargin() {
        val iv_logo = binding.ivLogo
        val lp = LinearLayout.LayoutParams(iv_logo.getLayoutParams())
        lp.setMargins(0, marginTop, 0, 0)
        lp.gravity = Gravity.CENTER_HORIZONTAL
        iv_logo.setLayoutParams(lp)
    }


    fun getRoles() {
        viewModel.getUserRoles()
        setRolesObserver()
    }

    fun getFacilities() {

        viewModel.getGuestResorts()
        setFacilityObserver()
    }

    private fun setFacilityObserver() {
        viewModel.getResorts()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    val response = it.api_data?.resort
                    if (response != null) {
                        populateSpinners(response, true)
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

    private fun setServicesObserver() {
        viewModel.getServices()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    val response = it.api_data?.resort
                    if (response != null) {
                        populateServices(response)
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


    private fun setRolesObserver() {
        viewModel.getRoles()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    val response = it.api_data?.resort
                    if (response != null) {
                        populateSpinners(response, false)
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


    private fun setPckgsObserver() {
        viewModel.getPackages()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    val response = it.api_data?.data
                    if (response != null) {
                        setPackagesList(response)
                    }

                }

                Status.ERROR -> {
                    //Handle Error
                    binding.progressBar.progressbar.visibility = View.GONE

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
            .get(UnitApplicationViewModel::class.java)
    }

    private fun getServices() {
        viewModel.getResortServices(resortId)
        setServicesObserver()
    }

    private fun populateSpinners(data: ArrayList<Resort>, isResort: Boolean) {

        var roleNames: ArrayList<String>? = arrayListOf<String>()

        data!!.forEachIndexed { index, e ->
            data!!.get(index).name?.let {
                if (!isResort) {
                    //only unit member can register
                    if (it.equals(Constants.UNIT_MEMBER)) {
                        roleNames?.add(it)
                    }
                } else {
                    roleNames?.add(it)
                }
            }
        }

        // Creating adapter for spinner
        activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item,
                roleNames as MutableList<String>
            )

                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    if (isResort)
                        binding.spFacility.adapter = adapter
                    else
                        binding.spRoles.adapter = adapter
                }
        }

        if (isResort)
            onFacilitySelected(data)
        else
            onRolesSelected(data)

    }

    private fun populateServices(data: ArrayList<Resort>) {

        var roleNames: ArrayList<String>? = arrayListOf<String>()

        data!!.forEachIndexed { index, e ->
            data!!.get(index).name?.let {
                roleNames?.add(it)
            }
        }

        // Creating adapter for spinner
        activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item,
                roleNames as MutableList<String>
            )

                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner

                    binding.spService.adapter = adapter
                }
        }

        onServiceSelected(data)

    }

    private fun onFacilitySelected(resorts: ArrayList<Resort>) {
        binding.spFacility?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                resorts!!.forEachIndexed { index, e ->
                    if (resorts!!.get(index).name?.equals(selectedItem) == true) {
                        resortId = resorts!!.get(index).id.toString()
                        getServices()
                    }

                }
            }


        }

    }

    private fun onServiceSelected(services: ArrayList<Resort>) {
        binding.spService?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                services!!.forEachIndexed { index, e ->
                    if (services!!.get(index).name?.equals(selectedItem) == true) {
                        service_id = services!!.get(index).id.toString()

                    }

                }
            }


        }

    }


    private fun onRolesSelected(roles: ArrayList<Resort>) {
        binding.spRoles?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                roles!!.forEachIndexed { index, e ->
                    if (roles!!.get(index).name?.equals(selectedItem) == true) {
                        roleId = roles!!.get(index).id.toString()
                        getPackages()

                    }

                }
            }


        }

    }

    private fun getPackages() {
        if (!resortId.isNullOrEmpty() && !roleId.isNullOrEmpty()) {
            viewModel.getPackages(resortId, roleId)
            setPckgsObserver()
        }

    }

    private fun setFamilyList() {
        binding.rvFamily.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = familyListAdapter
        }

        binding.rvFamily.addItemDecoration(
            DividerItemDecoration(
                binding.rvFamily.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.etFamily.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val noOfFam: String = binding.etFamily.text.toString()
                if (!noOfFam.isEmpty()) {
                    activity?.let { hideKeyboard(it) }

                    val no_of_fam_members: Int = noOfFam.toInt()
                    val members = ArrayList<FamilyMemberRequest>(no_of_fam_members)
                    for (i in 1..no_of_fam_members) {
                        val person = FamilyMemberRequest(0, "", "", "", "", "")
                        members.add(person)
                    }
                    binding.rvFamily.visibility = View.VISIBLE
                    familyListAdapter.setFamilyList(members, requireContext())

                }
            }

    }


    private fun setPackagesList(servicePackages: ArrayList<ServicePackage>) {
        binding.rvPckgs.visibility = View.VISIBLE
        binding.rvPckgs.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvFamily.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            packageListAdapter = PackageListAdapter(arrayListOf(), object :
                PackageListAdapter.OnItemCheckListener {

                override fun onItemCheck(item: ServicePackage?) {
                    package_id = item?.id.toString()
                    Log.e(
                        "check package",
                        item?.service_name + "  " + item?.id
                    )
                }

                override fun onItemUncheck(item: ServicePackage?) {
                    Log.e(
                        "uncheck package",
                        item?.service_name + "  " + item?.id
                    )
                }
            })
            adapter = packageListAdapter
            packageListAdapter.updatePckgList(servicePackages)

            /*    addOnItemTouchListener(RecyclerItemClickListenr(context, binding.rvPckgs, object : RecyclerItemClickListenr.OnItemClickListener {

                    override fun onItemClick(view: View, position: Int) {
                        val chkBox: CheckedTextView = view.findViewById(R.id.ctv_pckgs)
                        if(chkBox.isChecked){
                            val selected_package: ServicePackage = servicePackages.get(position)
                            Log.e(
                            "sel package",
                            selected_package?.service_name + "  " + selected_package?.id
                        )
                    }}
                }))*/
        }


    }


    private fun selectPicture() {
        binding.tvBrowse.setOnClickListener(View.OnClickListener {
            pickImages.launch("image/*")
        })

    }


    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                // The image was saved into the given Uri -> do something with it
                binding.tvBrowse.setText(it.path)
            }
        }


    private fun getSelectedCountry(cpp: CountryCodePicker): String {
        var selectedCountry = cpp.defaultCountryName
        Log.i("country", cpp.selectedCountryName)
        cpp.setOnCountryChangeListener { //Alert.showMessage(RegistrationActivity.this, ccp.getSelectedCountryCodeWithPlus());
            Log.i("country", cpp.selectedCountryName)
            selectedCountry = cpp.selectedCountryName
        }
        return selectedCountry
    }

    fun applyForMembership() {
        binding.btnApply.setOnClickListener(View.OnClickListener {
            val firstName: String = binding.etFname.text.toString()
            val lastName: String = binding.etLname.text.toString()
            val email: String = binding.etEmail.text.toString()
            val contact: String = binding.layoutMobile.etMobile.text.toString()
            var no_of_fam_member: String = binding.etFamily.text.toString()
            val city_offc: String = binding.etOffCity.text.toString()
            val city_home: String = binding.etHCity.text.toString()
            val country_offc = getSelectedCountry(binding.ccpOffice)
            val country_home = getSelectedCountry(binding.ccpHome)
            val gender: String = getGender(binding.cbFemale, binding.cbMale)
            val member_id: String = binding.etId.text.toString()
            val dob: String = binding.etDob.text.toString()
            if (no_of_fam_member.isNullOrEmpty()) {
                no_of_fam_member = "0"
            }

            val family_data: ArrayList<FamilyMemberRequest> = familyListAdapter.getData()
            Log.i("family_data", family_data.toString())

            val request = RegisterMemberRequest(
                roleId,
                resortId,
                service_id,
                package_id,
                firstName,
                lastName,
                member_id,
                email,
                dob,
                gender,
                getString(R.string.number_code) + contact,
                city_home,
                country_home,
                city_offc,
                country_offc,
                "",
                no_of_fam_member,
                family_data
            )

            if (!package_id.isEmpty()) {

                var errorMsg = getEmptyFieldsMsg()
                if (errorMsg.isNullOrEmpty()) {

                    binding.rlPB.visibility = View.VISIBLE
                    hideKeyboard(requireActivity())

                    viewModel.addUnitMember(request)
                    setRegistrationObserver()
                } else {
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name), errorMsg
                    )
                }

            } else {
                showAlertDialog(
                    context as Activity,
                    getString(R.string.app_name),
                    getString(R.string.no_pckg_error)
                )
            }

        })

    }

    private fun setRegistrationObserver() {
        viewModel.getAddMemberResponse()?.observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.rlPB.visibility = View.GONE
                    val response = it.api_data?.data

                    it.api_data?.message?.let { it1 ->
                        showAlertDialog(
                            context as Activity,
                            getString(R.string.app_name), it1
                        )
                    }

                }

                Status.ERROR -> {
                    //Handle Error
                    binding.rlPB.visibility = View.GONE

                    val errorJson = it.message
                    if (errorJson != null) {
                        displayServerErrors(errorJson, requireContext())

                    }

                }
            }
        })
    }


    private fun getEmptyFieldsMsg(): String {
        var errorMsg = ""
        val firstName = binding.etFname.text.toString()
        val lastName = binding.etLname.text.toString()
        val member_id: String = binding.etId.text.toString()
        val email = binding.etEmail.text.toString()
        val mobile = "9665" + binding.layoutMobile.etMobile.text.toString()

        //employee required fields
        if (mobile.isNullOrEmpty() || mobile.length < 8) {
            errorMsg = getString(R.string.contact_error)
        } else if (email.isNullOrEmpty() || !email.isEmailValid()) {
            errorMsg = getString(R.string.email_error)
        } else if (lastName.isNullOrEmpty()) {
            errorMsg = getString(R.string.lname_error)
        } else if (firstName.isNullOrEmpty()) {
            errorMsg = getString(R.string.fname_error)
        }


        return errorMsg
    }


}