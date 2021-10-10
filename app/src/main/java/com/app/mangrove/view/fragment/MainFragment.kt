package com.app.mangrove.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.app.mangrove.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navigateToTour()
        navigateToLogin()
    }

    fun navigateToTour() {
        binding.btnTour.setOnClickListener(View.OnClickListener {
            val action = MainFragmentDirections.actionMainToTour()
            Navigation.findNavController(it).navigate(action)

        })
    }

    fun navigateToLogin() {
        binding.btnLogin.setOnClickListener(View.OnClickListener {
            val action = MainFragmentDirections.actionMainToLogin()
            Navigation.findNavController(it).navigate(action)

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


