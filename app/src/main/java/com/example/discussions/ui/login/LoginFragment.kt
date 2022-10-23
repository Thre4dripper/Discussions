package com.example.discussions.ui.login

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.databinding.FragmentLoginBinding
import com.example.discussions.viewModels.LoginViewModel

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        initLogin()
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = viewModel

        binding.loginBtn.setOnClickListener { login() }
        return binding.root
    }

    private fun initLogin() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Logging in...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        viewModel.checkLoginStatus()
        viewModel.isAuthenticated.observe(viewLifecycleOwner) {
            //initial case
            if (it == null) return@observe

            //login success
            if (it == LoginViewModel.API_SUCCESS) {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
            }
            //login failed
            else {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Logging in...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val username = binding.usernameEt.text.toString()
        val password = binding.passwordEt.text.toString()

        if (username.isEmpty()) {
            binding.usernameTil.error = "Username cannot be empty"
            binding.usernameEt.requestFocus()
            progressDialog.dismiss()
            return
        }

        if (password.isEmpty()) {
            binding.passwordTil.error = "Password cannot be empty"
            binding.passwordEt.requestFocus()
            progressDialog.dismiss()
            return
        }

        viewModel.login(requireActivity(), username, password, binding.rememberMeCb.isChecked)
    }

}