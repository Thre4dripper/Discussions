package com.example.discussions.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.discussions.R
import com.example.discussions.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding

    companion object {
        const val RESULT_LOGOUT = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        binding.settingsBackBtn.setOnClickListener { finish() }
        binding.settingsLogoutBtn.setOnClickListener { logoutDialog() }
    }

    /**
     * METHOD FOR LOGGING OUT AND DELETE JWT TOKEN
     */
    private fun logoutDialog() {
        MaterialAlertDialogBuilder(this).setTitle("Logout")
            .setMessage("Are you sure you want to logout?").setPositiveButton("Yes") { _, _ ->
                setResult(RESULT_LOGOUT)
                finish()
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}