package com.app.mangrove.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.mangrove.R
import com.app.mangrove.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import android.widget.LinearLayout
import androidx.navigation.Navigation
import com.app.mangrove.util.onHomeIconClick


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTabs()
        hideFirstTab()
    }


    private fun setTabs() {
        val types = arrayOf(
            "Hide",
            getString(R.string.facilities),
            getString(R.string.residential),
            getString(R.string.apply)

        )
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        viewPager.isUserInputEnabled = false

        val adapter =
            ViewPagerAdapter(this.childFragmentManager, lifecycle, requireContext(), binding)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = types[position]
        }.attach()


    }

    private fun hideFirstTab() {
        (binding.tabLayout.getTabAt(0)?.view as LinearLayout).visibility = View.GONE
        binding.rlFooter.ivHome.visibility = View.GONE
    }

    class ViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        mContext: Context,
        binding: FragmentHomeBinding
    ) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        private val NUM_TABS = 4
        var context: Context? = mContext
        val binding = binding

        override fun getItemCount(): Int {
            return NUM_TABS
        }

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> {
                    binding.rlFooter.ivHome.visibility = View.GONE
                    binding.rlFooter.tvWelcome.visibility = View.VISIBLE
                    return TourIntroFragment()
                }
                1 -> {
                    setFooter()
                    return FacilitiesFragment()
                }
                2 -> {
                    setFooter()
                    return ResidentialsFragment()
                }


            }

            setFooter()
            return UnitApplicationFragment()
        }


        private fun setFooter()
        {
            binding.rlFooter.ivHome.visibility = View.VISIBLE
            binding.rlFooter.tvWelcome.visibility = View.GONE
            val action = HomeFragmentDirections.actionHomeToTour()

            onHomeIconClick(binding.rlFooter.ivHome, action)

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}