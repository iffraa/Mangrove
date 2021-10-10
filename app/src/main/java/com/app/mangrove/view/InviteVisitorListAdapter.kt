package com.app.mangrove.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.app.mangrove.R
import com.app.mangrove.databinding.ItemVisitorInviteBinding
import com.app.mangrove.model.Data
import com.app.mangrove.model.ServicePackage
import com.app.mangrove.model.Status
import com.app.mangrove.model.Visitor
import com.app.mangrove.util.Constants
import com.app.mangrove.util.SharedPreferencesHelper
import com.app.mangrove.util.ViewModelFactory
import com.app.mangrove.util.showAlertDialog
import com.app.mangrove.view.fragment.VisitorInviteFragment
import com.app.mangrove.viewmodel.VisitorInviteViewModel

import com.jakewharton.rxbinding4.widget.textChanges
import com.mindorks.kotlinFlow.data.api.ApiHelperImpl
import com.mindorks.kotlinFlow.data.api.RetrofitBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class InviteVisitorListAdapter(
    val visitorList: ArrayList<Visitor>,
    mContext: Context,
    visitorInviteFragment: VisitorInviteFragment
) :
    RecyclerView.Adapter<InviteVisitorListAdapter.VisitorViewHolder>() {

    private var _binding: ItemVisitorInviteBinding? = null
    private val binding get() = _binding!!
    private val enteredData: ArrayList<Visitor> = arrayListOf()
    private val context: Context = mContext
    private val visitorFrag: VisitorInviteFragment = visitorInviteFragment
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var data: Data
    private lateinit var dateTime: String
    private lateinit var servicePackage: ServicePackage
    private lateinit var viewModel: VisitorInviteViewModel


    fun setVisitorList(newFamList: ArrayList<Visitor>) {
        prefsHelper = SharedPreferencesHelper(context)
        data = prefsHelper.getObject(Constants.USER_DATA)!!

        dateTime = visitorFrag.getDateTime()
        //dateTime = "2021-08-02 04:01 PM"

        enteredData.clear()
        visitorList.clear()
        visitorList.addAll(newFamList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemVisitorInviteBinding.inflate(inflater)
        return VisitorViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {

        val visitor: Visitor = visitorList.get(position)

        val et_name: EditText = holder.view.etVisitorsName
        val et_id: EditText = holder.view.etVisitorsId
        val et_mobile: EditText = holder.view.layoutMobile.etMobile

        val cb_male: CheckBox = holder.view.checkboxMale
        val cb_female: CheckBox = holder.view.checkboxFemale
        val cb_pay: CheckBox = holder.view.cbPay

        val btn_visitor: Button = holder.view.btnVisitor
        btn_visitor.setText(context.getString(R.string.visitor) + " " + (position + 1))

        et_name.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                visitor.name = et_name.text.toString()
            }

        et_mobile.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                visitor.contact_no = context.getString(R.string.sa_code) + et_mobile.text.toString()
            }

        et_id.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                if (!et_id.text.toString().isEmpty())
                    visitor.id_no = et_id.text.toString().toInt().toString()
            }


        fetchGenderPackage(cb_female, cb_male, visitor)
        visitor.who_will_pay = getWhoWillPay(cb_pay)
        enteredData.add(visitor)
    }

    fun fetchGenderPackage(femaleChkBx: CheckBox, maleChkBx: CheckBox, visitor: Visitor): String {
        var gender = Constants.MALE
        var isChkBoxChkd = false
        setupViewModel()

        femaleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                maleChkBx.setChecked(false)
                gender = Constants.FEMALE
                visitor.gender = gender

                binding.progressBar.progressbar.visibility = View.GONE
                data?.token?.let {
                    viewModel.getPackages(
                        it,
                        dateTime,
                        gender,
                        data?.user?.resort_id.toString()
                    )
                }
                setupObserver(visitor)
                //  data.token?.let { viewModel.getPackages(it,gender,dateTime, data.user?.resort_id.toString()) }
                //  observeViewModel(viewModel,visitor)
            }

        })

        maleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                femaleChkBx.setChecked(false)
                gender = Constants.MALE
                visitor.gender = gender

                binding.progressBar.progressbar.visibility = View.GONE
                data?.token?.let {
                    viewModel.getPackages(
                        it,
                        dateTime,
                        gender,
                        data?.user?.resort_id.toString()
                    )
                }
                setupObserver(visitor)
                // data.token?.let { viewModel.getPackages(it,gender,dateTime, data.user?.resort_id.toString()) }
                // observeViewModel(viewModel,visitor)
            }
        })

        if (!isChkBoxChkd) {
            visitor.gender = gender
            data?.token?.let {
                viewModel.getPackages(
                    it,
                    dateTime,
                    gender,
                    data?.user?.resort_id.toString()
                )
            }
            setupObserver(visitor)

        }

        return gender
    }


    fun getData(): ArrayList<Visitor> {
        return enteredData
    }

    fun getWhoWillPay(cb_pay: CheckBox): String {
        var who_will_pay = "visitor"

        cb_pay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                who_will_pay = "sender"
            }
        })

        return who_will_pay
    }

    fun getPackage() = servicePackage

    override fun getItemCount() = visitorList.size

    class VisitorViewHolder(val view: ItemVisitorInviteBinding) : RecyclerView.ViewHolder(view.root)


    private fun setupObserver(visitor: Visitor) {
        viewModel.getPackages()?.observe(visitorFrag, Observer {

            when (it.status) {
                Status.SUCCESS -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    servicePackage = it.api_data?.data!!
                    visitor.package_id = servicePackage.id.toString()

                }
                Status.LOADING -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                }
                Status.ERROR -> {
                    binding.progressBar.progressbar.visibility = View.GONE
                    //Handle Error
                    showAlertDialog(
                        context as Activity,
                        context.getString(R.string.app_name),
                        context.getString(R.string.pckgs_error)
                    )

                }
            }
        })
    }

    private fun setupViewModel() {

        viewModel = ViewModelProviders.of(
            visitorFrag,
            (context as Activity).application?.let {
                ViewModelFactory(
                    ApiHelperImpl(RetrofitBuilder.apiService), it
                )

            }
        )
            .get(VisitorInviteViewModel::class.java)
    }

}

