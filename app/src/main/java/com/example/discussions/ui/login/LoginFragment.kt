package com.example.discussions.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.databinding.FragmentLoginBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var loadingDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        initLoadingDialog()
        initLogin()
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = viewModel

        binding.loginBtn.setOnClickListener { login() }
        return binding.root
    }

    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root)
            .setCancelable(false).show()
    }

    private fun initLogin() {
        loadingDialog.show()
        viewModel.checkLoginStatus()
        viewModel.isAuthenticated.observe(viewLifecycleOwner) {
            //initial case
            if (it == null) return@observe

            //login success
            if (it == LoginViewModel.API_SUCCESS) {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
            }
            //login failed
            else {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login() {
        loadingDialog.show()
        val username = binding.usernameEt.text.toString()
        val password = binding.passwordEt.text.toString()

        if (username.isEmpty()) {
            binding.usernameEt.error = "Username is required"
            binding.usernameEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            //clearing error
            binding.usernameEt.error = null
        }
        if (password.isEmpty()) {
            binding.passwordTil.error = "Password is required"
            binding.passwordEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            //clearing error
            binding.passwordTil.error = null
        }



        viewModel.login(requireActivity(), username, password, binding.rememberMeCb.isChecked)
    }

}