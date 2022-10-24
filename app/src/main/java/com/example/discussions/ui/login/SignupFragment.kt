package com.example.discussions.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.databinding.FragmentSignupBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class SignupFragment(private var tabLayout: TabLayout) : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var loadingDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

        initLoadingDialog()
        initSignup()
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = viewModel

        binding.signupBtn.setOnClickListener { signup() }
        return binding.root
    }

    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()
    }

    private fun initSignup() {
        viewModel.isRegistered.observe(viewLifecycleOwner) {
            //initial case
            if (it == null) return@observe

            //login success
            if (it == LoginViewModel.API_SUCCESS) {
                loadingDialog.dismiss()
                tabLayout.selectTab(tabLayout.getTabAt(0))
                Toast.makeText(requireContext(), "Welcome", Toast.LENGTH_SHORT).show()
            }
            //login failed
            else {
                loadingDialog.dismiss()
                handleSignupErrors(it)
            }
        }
    }

    private fun handleSignupErrors(message: String) {
        //showing toast for other errors and returning
        if (message == LoginViewModel.API_ERROR) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return
        }

        //username already exists error
        binding.usernameEt.error =
            if (message == LoginViewModel.API_ERROR_USERNAME || message == LoginViewModel.API_ERROR_USERNAME_EMAIL) {
                binding.usernameEt.requestFocus()
                "Username already exists"
            } else null

        //email already exists error
        binding.emailEt.error =
            if (message == LoginViewModel.API_ERROR_EMAIL || message == LoginViewModel.API_ERROR_USERNAME_EMAIL) {
                binding.emailEt.requestFocus()
                "Email already exists"
            } else null

    }

    private fun signup() {
        loadingDialog.show()
        val username = binding.usernameEt.text.toString()
        val email = binding.emailEt.text.toString()
        val password = binding.passwordEt.text.toString()

        //checking for username field
        if (username.isEmpty()) {
            binding.usernameEt.error = "Username is required"
            binding.usernameEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            binding.usernameEt.error = null
        }

        //checking for email field
        if (email.isEmpty()) {
            binding.emailEt.error = "Email is required"
            binding.emailEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            binding.emailEt.error = null
        }

        //checking for password field (error should be displayed on text input layout in password field)
        if (password.isEmpty()) {
            binding.passwordTil.error = "Password is required"
            binding.passwordEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            binding.passwordTil.error = null
        }

        viewModel.signup(requireActivity(), username, email, password)
    }
}