package com.example.discussions.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.databinding.FragmentLoginBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.store.LoginStore
import com.example.discussions.ui.home.HomeActivity
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
        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

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
        loadingDialog.dismiss()
    }

    private fun initLogin() {
        loadingDialog.show()
        viewModel.checkLoginStatus(requireContext())
        viewModel.isAuthenticated.observe(viewLifecycleOwner) {
            //initial case
            if (it == null) {
                loadingDialog.dismiss()
                return@observe
            }

            //login success
            if (it == Constants.API_SUCCESS) {
                loadingDialog.dismiss()
                homeActivityCallback.launch(Intent(requireContext(), HomeActivity::class.java))
                Toast.makeText(requireContext(), "Welcome", Toast.LENGTH_SHORT).show()
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
        val username = binding.loginUsernameEt.text.toString()
        val password = binding.loginPasswordEt.text.toString()

        //checking for username field
        if (username.isEmpty()) {
            binding.loginUsernameEt.error = "Username is required"
            binding.loginUsernameEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            //clearing error
            binding.loginUsernameEt.error = null
        }

        //checking for password field (error should be displayed on text input layout in password field)
        if (password.isEmpty()) {
            binding.loginPasswordTil.error = "Password is required"
            binding.loginPasswordEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            //clearing error
            binding.loginPasswordTil.error = null
        }


        viewModel.login(requireActivity(), username, password, binding.rememberMeCb.isChecked)
    }

    /**
     * Home Activity launch callback
     */
    private var homeActivityCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Constants.RESULT_LOGOUT) {
                //delete jwt and clear login status on logout
                LoginStore.saveLoginStatus(requireContext(), false)
                LoginStore.saveJWTToken(requireContext(), null)
            } else if (it.resultCode == Constants.RESULT_CLOSE) {
                requireActivity().finish()
            }
        }
}