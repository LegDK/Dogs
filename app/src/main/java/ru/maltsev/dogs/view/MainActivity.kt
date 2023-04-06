package ru.maltsev.dogs.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentController
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import ru.maltsev.dogs.R
import ru.maltsev.dogs.util.PERMISSION_SEND_SMS
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = requireNotNull(supportFragmentManager.findFragmentById(R.id.fragment))
        navController = navHostFragment.findNavController()
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

    fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                AlertDialog.Builder(this)
                    .setTitle("Send sms permissions")
                    .setMessage("This app need access to send sms")
                    .setPositiveButton("Ask me") {
                        dialog, which -> requestSmsPermission()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        notifyDetailFragment(false)
                    }
                    .show()
            } else {
                requestSmsPermission()
            }
        } else {
            notifyDetailFragment(true)
        }
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), PERMISSION_SEND_SMS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_SEND_SMS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notifyDetailFragment(true)
            }
        }
    }

    private fun notifyDetailFragment(permissionGranted: Boolean) {
        val fragment = supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.primaryNavigationFragment

        if (fragment is DetailFragment) {
            fragment.onPermissionResult(permissionGranted)
        }
    }
}