package com.ncsu.imagc.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import kotlinx.android.synthetic.main.login_fragment.*


class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        signInButton.setOnClickListener(activity as MainActivity)
    }

}