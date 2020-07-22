package com.ncsu.imagc.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import kotlinx.android.synthetic.main.home_fragment.view.startButton

class HomeFragment : Fragment() {


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.home_fragment, container, false)
        root.startButton.setOnClickListener {
            (activity as MainActivity).let {
                if(it.account == null)
                    it.goToLogin()
                else {
                    root.findNavController().apply {
                        if(currentDestination?.id == R.id.nav_home) {
                            navigate(R.id.nav_camera)
                        }
                    }
                }
            }

        }
        if (allPermissionsGranted()) {
            root.startButton.isEnabled = true
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        return root
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startButton.isEnabled = true
            } else {
                Toast.makeText(context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            activity!!.baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

}