package com.app.mangrove.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.app.mangrove.R
import com.app.mangrove.databinding.ActivityDashboardBinding
import com.app.mangrove.util.Constants
import com.google.android.material.navigation.NavigationView

import android.view.View
import androidx.navigation.*


class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private var drawerLayout: DrawerLayout? = null
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  setActionBarTitle()
        setSupportActionBar(binding.appBarMain2.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations(back button will not be shown)
        appBarConfiguration = AppBarConfiguration(
            setOf( R.id.nav_invite_visitor, R.id.nav_application,R.id.nav_reservation,
                R.id.mainFragment, R.id.nav_login,  R.id.nav_tour, R.id.nav_about,R.id.nav_services
                , R.id.nav_contact, R.id.nav_dashboard , R.id.nav_visitor_list, R.id.nav_service_form), drawerLayout
        )
       //   appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        //show back button
      //  setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)
        navController.setGraph(R.navigation.nav_graph)

        changeNavIcon()
        hideLoginItems()

        setTitle("")
        changeTitle(navController)
    }

    private fun changeNavIcon() {
        getSupportActionBar()?.setHomeButtonEnabled(true);
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setHomeAsUpIndicator(R.drawable.nav_icon);
        getSupportActionBar()?.setTitle("")

    }

  /*  @SuppressLint("ResourceType")
    private fun setNavGraphStart(navController: NavController, isLogin: Boolean) {
        if (isLogin) {
            navController.setGraph(R.navigation.nav_login)
        } else {
            navController.setGraph(R.navigation.nav_apply)
        }

    }*/

    //this shows the overflow icon
    /*  override fun onCreateOptionsMenu(menu: Menu): Boolean {
          // Inflate the menu; this adds items to the action bar if it is present.
          menuInflater.inflate(R.menu.main_activity2, menu)
          return true
      }*/


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setActionBarTitle(title: String) {
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setSupportActionBar(binding.appBarMain2.toolbar)

        //    binding.appBarMain2.toolbar.findViewById<TextView>(R.id.toolbar_title).setText(title)
    }

    /* private fun clickOverflowIcon()
     {
         binding.appBarMain2.toolbar.setOnMenuItemClickListener({
             Navigation.findNavController(navView).navigate(R.id.nav_home);

             true
         })
     }

    fun makeUserDashboardStart() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.userDashboardFragment
            ), drawerLayout
        )

        //show back button
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.graph.startDestination = R.id.userDashboardFragment
        navView.setupWithNavController(navController)

    }*/

    //user is not logged in
    private fun hideLoginItems() {
        val menu: Menu = binding.navView.getMenu()
        val nav_logout = menu.findItem(R.id.nav_logout)
        nav_logout.setVisible(false)

        val nav_login = menu.findItem(R.id.nav_login)
        nav_login.setVisible(true)

         val nav_invite = menu.findItem(R.id.nav_invite_visitor)
         nav_invite.setVisible(false)

         val nav_profile = menu.findItem(R.id.nav_dashboard)
         nav_profile.setVisible(false)

         val nav_req_services = menu.findItem(R.id.nav_service_form)
         nav_req_services.setVisible(false)

    }


    //user is logged in
    private fun showLoginItems() {
        val menu: Menu = binding.navView.getMenu()

        val nav_login = menu.findItem(R.id.nav_login)
        nav_login.setVisible(false)

        val nav_logout = menu.findItem(R.id.nav_logout)
        nav_logout.setVisible(true)

         val nav_invite = menu.findItem(R.id.nav_invite_visitor)
          nav_invite.setVisible(true)

          val nav_profile = menu.findItem(R.id.nav_dashboard)
          nav_profile.setVisible(true)

          val nav_req_services = menu.findItem(R.id.nav_service_form)
          nav_req_services.setVisible(true)


    }

    fun changeLoginDisplay(isLogin: Boolean) {
        val menu: Menu = binding.navView.getMenu()

        if (isLogin) {
            showLoginItems()
        } else {
            hideLoginItems()
        }
    }

    fun changeMenuIcon() {
        navController.addOnDestinationChangedListener(NavController.OnDestinationChangedListener{ navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            if (navDestination.getId() == R.id.nav_tour
                || navDestination.getId() == R.id.nav_login
                || navDestination.getId() == R.id.mainFragment){
                getSupportActionBar()?.setHomeButtonEnabled(true);
                getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
                getSupportActionBar()?.setHomeAsUpIndicator(R.drawable.nav_icon);
            }
        })

    }

    private fun changeTitle(navController: NavController)
    {
        // setting title according to fragment
        navController.addOnDestinationChangedListener {
                controller, destination, arguments ->
            binding.appBarMain2.toolbar.title = navController.currentDestination?.label
        }
    }

}

