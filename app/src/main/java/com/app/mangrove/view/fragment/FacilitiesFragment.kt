package com.app.mangrove.view.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.mangrove.databinding.FragmentFacilitiesBinding
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.DefaultSliderView

class FacilitiesFragment : Fragment() {

    private var _binding: FragmentFacilitiesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFacilitiesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }


    private fun setSlider() {
        val slider: SliderLayout = binding.slider
        for (i in 1..5) {
            val imgSliderView = DefaultSliderView(context)
            val imageName = "facility" + i
            val resources: Resources = requireContext().resources
            val resourceId: Int = resources.getIdentifier(
                imageName, "drawable",
                requireContext().packageName
            )
            imgSliderView.image(resourceId)
            slider.addSlider(imgSliderView)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSlider()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}