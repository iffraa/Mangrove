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
    private var packages: HashMap<String, ServicePackage> = hashMapOf()


    fun setVisitorList(newFamList: ArrayList<Visitor>, vPackages: HashMap<String, ServicePackage>) {
        /* prefsHelper = SharedPreferencesHelper(context)
         val data_string = prefsHelper.getData(Constants.USER_DATA)
         val gson = Gson()
         data = gson.fromJson(data_string, Data::class.java)

         dateTime =  visitorFrag.getDateTime()*/

        packages = vPackages
        enteredData.clear()
        visitorList.clear()
        visitorList.addAll(newFamList)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemVisitorInviteBinding.inflate(inflater,parent,false)
        return VisitorViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {

        val visitor: Visitor = visitorList.get(position)

        val et_name: EditText = holder.view.etVisitorsName
        val et_id: EditText = holder.view.etVisitorsId
        val et_mobile: EditText = holder.view.layoutMobile.etMobile
        val price: EditText = holder.view.etPay

        val cb_male: CheckBox = holder.view.checkboxMale
        val cb_female: CheckBox = holder.view.checkboxFemale
       // val cb_pay: CheckBox = holder.view.cbPay

        val btn_visitor: Button = holder.view.btnVisitor
        btn_visitor.setText(context.getString(R.string.visitor) + " " + (position + 1))

        price.setText(visitor.price)

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
                visitor.contact_no = et_mobile.text.toString()
            }

        et_id.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                if(!et_id.text.toString().isEmpty())
                    visitor.id_no = et_id.text.toString()
            }

        fetchGenderPackage(cb_female,cb_male,visitor,price)
       // visitor.who_will_pay = getWhoWillPay(cb_pay)
        enteredData.add(visitor)

    }

    /*   fun setGenderCheck(holder: VisitorViewHolder, visitor: Visitor, position: Int)
       {
           holder.view.checkboxFemale.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
               if (isChecked) {
                   onItemClick!!.onFCheck(visitor, holder.view.etPay);

               }

           })

           holder.view.checkboxMale.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
               if (isChecked) {
                   onItemClick!!.onMCheck(visitor, holder.view.etPay);

               }

           })

       }*/


    fun fetchGenderPackage(femaleChkBx: CheckBox, maleChkBx: CheckBox,visitor: Visitor, price: EditText): String {
        var gender = Constants.FEMALE
        var isChkBoxChkd = false

        femaleChkBx.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                maleChkBx.setChecked(false)
                gender = Constants.FEMALE
                visitor.gender = gender

                setVisitorPackage(gender, visitor, price)

                //  data.token?.let { viewModel.getPackages(it,gender,dateTime, data.user?.resort_id.toString()) }
                //  observeViewModel(viewModel,visitor,price, position, holder)
            }

        }

        maleChkBx.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                femaleChkBx.setChecked(false)
                gender = Constants.MALE
                visitor.gender = gender

                setVisitorPackage(gender, visitor, price)
            }
        }

        if(!isChkBoxChkd) {

            if(visitor.gender.isNullOrEmpty())
            {
                visitor.gender = gender
            }
            else  if(visitor.gender.equals(Constants.MALE))
            {
                gender = Constants.MALE
                femaleChkBx.setChecked(false)
                maleChkBx.setChecked(true)

            }
            else  if(visitor.gender.equals(Constants.FEMALE))
            {
                gender = Constants.FEMALE
                femaleChkBx.setChecked(true)
                maleChkBx.setChecked(false)

            }

            setVisitorPackage(gender, visitor,price)

        }

        return gender
    }

    private fun setVisitorPackage(gender: String, visitor: Visitor, etPrice: EditText)
    {
        val visitorPckg = packages.get(gender)
        if(visitorPckg != null) {
            visitor.package_id = visitorPckg.id.toString()
            visitor.servicePackage = visitorPckg
            visitor.price = visitorPckg.price

            etPrice.setText(visitor.price)
        }
    }

    fun getData(): ArrayList<Visitor>
    {
        return enteredData
    }


    fun getWhoWillPay(cb_pay: CheckBox): String
    {
        var who_will_pay = "sender"

        cb_pay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                who_will_pay = "visitor"
            }
        })

        return who_will_pay
    }

    fun clear() {

        enteredData.clear()
        val size: Int = visitorList.size
        if (size > 0) {
            for (i in 0 until size) {
                visitorList.removeAt(i)
            }

            notifyItemRangeRemoved(0, size)
            notifyDataSetChanged()
        }
    }
    override fun getItemCount() = visitorList.size

    class VisitorViewHolder(val view: ItemVisitorInviteBinding) : RecyclerView.ViewHolder(view.root)


}

