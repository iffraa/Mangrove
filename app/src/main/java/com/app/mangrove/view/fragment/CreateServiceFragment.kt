package com.app.mangrove.view.fragment

import android.R.attr
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mangrove.BuildConfig
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentCreateServiceBinding
import com.app.mangrove.model.Data
import com.app.mangrove.model.ServicePackage
import com.app.mangrove.model.ServiceRequest
import com.app.mangrove.model.Status
import com.app.mangrove.util.*
import com.app.mangrove.view.PackageListAdapter
import com.app.mangrove.viewmodel.CreateServiceViewModel
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder
import java.io.File

import java.util.*
import okhttp3.RequestBody

import android.R.attr.category
import okhttp3.MediaType
import android.R.attr.category

import okhttp3.MultipartBody





/**
 * A simple [Fragment] subclass.
 * Use the [AboutUsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateServiceFragment : Fragment() {

    private lateinit var binding: FragmentCreateServiceBinding
    private lateinit var viewModel: CreateServiceViewModel
    private lateinit var packageListAdapter: PackageListAdapter
    private lateinit var serviceId: String
    private var packageId = "0"
    private lateinit var userData: Data
    private var prefsHelper = SharedPreferencesHelper()
    private var imgUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateServiceBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        getServices()
        selectPicture()

        binding.etDate.setOnClickListener(View.OnClickListener {
            showDateTime(requireContext(),binding.etDate)
        })

        binding.btnListing.setOnClickListener(View.OnClickListener {
            val action = CreateServiceFragmentDirections.actionServiceFormToListing()
            action?.let { Navigation.findNavController(view).navigate(it) }

        })

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            val details = binding.etComments.text.toString()
            val date = binding.etDate.text.toString()

            if(!packageId.equals("0") && !date.isNullOrEmpty() && !details.isNullOrEmpty()) {

                val token = userData.token
                val imageBody = imgUri?.let { it1 -> getImageBody(it1,requireContext()) }

                val request = ServiceRequest(serviceId, packageId,details,date, imageBody)

                token?.let { it1 -> viewModel.addService(it1,request) }
                setServiceCreationObserver()
            }
            else
            {
                showAlertDialog(
                    context as Activity,
                    getString(R.string.app_name),
                    getString(R.string.empty_fields)
                )

            }

        })

        val action = CreateServiceFragmentDirections.actionServiceToTour()
        onHomeIconClick(binding.rlFooter.ivHome, action)

    }

    private fun selectPicture() {
        binding.tvBrowse.setOnClickListener(View.OnClickListener {
           // pickImages.launch("image/*")
           /// val imgHelper = ImageHelper(requireActivity(), this, binding.tvBrowse,takePicture, pickImages)
         showImgChooserDialog()

        })
    }


    private fun getServices()
    {
        userData = prefsHelper.getObject(Constants.USER_DATA)!!
        userData.token?.let { viewModel.getServices(it) }
        setServicesObserver()
    }

    private fun getPackages(serviceId: String)
    {
        userData.token?.let { viewModel.getPackages(it, serviceId) }
        setPckgsObserver()
    }

    private fun setServicesObserver() {
        viewModel.getServices().observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    populateSpinners()
                }
                Status.LOADING -> {
                    binding.progressBar.progressbar.visibility = View.GONE
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
                Status.LOADING -> {
                    binding.progressBar.progressbar.visibility = View.GONE
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

    private fun populateSpinners() {

        var data: ArrayList<String>? = arrayListOf<String>()

        val services = viewModel.getServices().value?.api_data?.resort
        services?.forEachIndexed { index, e ->
            services.get(index).name?.let {
                if (data != null) {
                    data.add(it)
                }
            }
        }

        // Creating adapter for spinner
        activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item,
                data as MutableList<String>
            )
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spServices.adapter = adapter
                    onServiceSelection()

                }
        }


    }

    private fun onServiceSelection() {
        binding.spServices?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                val services = viewModel.getServices().value?.api_data?.resort
                services?.forEachIndexed { index, e ->
                    if (services.get(index).name?.equals(selectedItem) == true) {
                        serviceId = services.get(index).id.toString()
                        getPackages(serviceId)
                    }

                }
            }

        }

    }

    private fun setServiceCreationObserver() {
        viewModel.getServiceCreationResponse().observe(viewLifecycleOwner, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    val msg = it.api_data?.message
                    if (msg != null) {
                        showAlertDialog(
                            context as Activity,
                            getString(R.string.app_name),msg
                        )
                    }
                }
                Status.LOADING -> {
                    binding.progressBar.progressbar.visibility = View.GONE
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

    private fun setPackagesList(servicePackages: ArrayList<ServicePackage>) {
        binding.rvPckgs.visibility = View.VISIBLE
        binding.rvPckgs.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvPckgs.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            packageListAdapter = PackageListAdapter(arrayListOf(), object :
                PackageListAdapter.OnItemCheckListener {

                override fun onItemCheck(item: ServicePackage?) {
                    packageId = item?.id.toString()
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
            .get(CreateServiceViewModel::class.java)
    }


//    --------chosing image dialog-------------

    fun showImgChooserDialog() {
        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        myAlertDialog.setTitle("Upload Pictures Option")
        myAlertDialog.setMessage("How do you want to set your picture?")
        myAlertDialog.setPositiveButton("Gallery",
            DialogInterface.OnClickListener { arg0, arg1 ->

                pickImages.launch("image/*")

            })
        myAlertDialog.setNegativeButton("Camera",
            DialogInterface.OnClickListener { arg0, arg1 ->
                takePicture()
            })
        myAlertDialog.show()
    }

    fun takePicture() {
        val root =
            File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID + File.separator)
        root.mkdirs()
        val fname = "img_" + System.currentTimeMillis() + ".jpg"
        val sdImageMainDirectory = File(root, fname)
        imgUri = FileProvider.getUriForFile(
            Objects.requireNonNull(requireContext()),
            BuildConfig.APPLICATION_ID + ".provider", sdImageMainDirectory);
        //imgUri = FileProvider.getUriForFile(requireContext(),
         //   context?.applicationContext?.packageName + ".provider", sdImageMainDirectory)
        takePicture.launch(imgUri)
    }

    val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
      //  if (success) {
            // The image was saved into the given Uri -> do something with it
           // Picasso.get().load(viewModel.profileImageUri).resize(800,800).into(registerImgAvatar)
            val path = imgUri?.path
            binding.tvBrowse.setText(path)
      //  }
    }

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                // The image was saved into the given Uri -> do something with it
              //  imgUri = uri
                binding.tvBrowse.setText(it.path)
            }
        }


}


