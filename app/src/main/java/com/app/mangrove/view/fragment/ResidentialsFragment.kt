package com.app.mangrove.view.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.mangrove.databinding.FragmentResidentialsBinding
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.DefaultSliderView
import kotlin.collections.ArrayList

class ResidentialsFragment : Fragment() {

    private var _binding: FragmentResidentialsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentResidentialsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var imgNames: ArrayList<String> = arrayListOf("bedroom4_1","bedroom4_2","bedroom4_3")
        setSlider(imgNames,binding.sl4bedroom)

        imgNames = arrayListOf("bedroom3_1","bedroom3_2","bedroom3_3")
        setSlider(imgNames,binding.sl3bedroom)

        imgNames = arrayListOf("bra3_1","bra3_2","bra3_3")
        setSlider(imgNames,binding.sl3brAparts)

        imgNames = arrayListOf("br2aparts1","br2aparts2")
        setSlider(imgNames,binding.sl2brAparts)

        imgNames = arrayListOf("br2_bunglow1","br2_bunglow2")
        setSlider(imgNames,binding.sl2brBanglows)


    }


    private fun setSlider(imgNAme: ArrayList<String>, slider: SliderLayout) {
         val resources: Resources = requireContext().resources
         for (image in imgNAme) {
             val imgSliderView = DefaultSliderView(context)
             val resourceId: Int = resources.getIdentifier(
                 image, "drawable",
                 requireContext().packageName
             )

             if(resourceId != 0) {
                 imgSliderView.image(resourceId)
                 slider.addSlider(imgSliderView)
             }
         }


     }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}