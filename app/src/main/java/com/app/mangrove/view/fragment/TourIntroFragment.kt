package com.app.mangrove.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.mangrove.databinding.FragmentTourInfoBinding
import java.util.*

class TourIntroFragment : Fragment()  {

    private var _binding: FragmentTourInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTourInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root
     return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}