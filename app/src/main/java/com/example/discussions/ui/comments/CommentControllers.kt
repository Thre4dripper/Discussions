package com.example.discussions.ui.comments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.discussions.Constants
import com.example.discussions.databinding.CommentsBsBinding
import com.example.discussions.models.CommentModel
import com.example.discussions.viewModels.CommentsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommentControllers {
    companion object {
        /***
         * METHODS FOR OBSERVING COMMENT DATA
         */
        fun addCommentObserver(
            context: Context,
            viewModel: CommentsViewModel,
            binding: CommentsBsBinding,
            viewLifecycleOwner: LifecycleOwner
        ) {
            viewModel.isCommentAdded.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it == Constants.API_SUCCESS) {
                        Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show()
                        binding.commentActionsCv.visibility = View.GONE
                        //close keyboard
                        val imm =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.addCommentEt.windowToken, 0)
                    } else {
                        Toast.makeText(context, "Error Adding Comment", Toast.LENGTH_SHORT).show()
                    }
                    binding.commentAddProgressBar.visibility = View.GONE
                    binding.addCommentBtn.visibility = View.VISIBLE
                }
            }
        }

        fun editCommentObserver(
            context: Context,
            viewModel: CommentsViewModel,
            binding: CommentsBsBinding,
            viewLifecycleOwner: LifecycleOwner
        ) {
            viewModel.isCommentEdited.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it == Constants.API_SUCCESS) {
                        Toast.makeText(context, "Comment Updated", Toast.LENGTH_SHORT).show()
                        binding.commentActionsCv.visibility = View.GONE
                        //close keyboard
                        val imm =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.addCommentEt.windowToken, 0)
                    } else {
                        Toast.makeText(context, "Error Editing Comment", Toast.LENGTH_SHORT).show()
                    }
                    binding.commentAddProgressBar.visibility = View.GONE
                    binding.addCommentBtn.visibility = View.VISIBLE
                }
            }
        }

        fun deleteCommentObserver(
            context: Context, viewModel: CommentsViewModel, viewLifecycleOwner: LifecycleOwner
        ) {
            viewModel.isCommentDeleted.observe(viewLifecycleOwner) {
                if (it != null && it != Constants.API_SUCCESS) Toast.makeText(
                    context,
                    "Error deleting comment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /***
         * METHODS FOR HANDLING COMMENT ACTIONS
         */
        fun commentDeleteHandler(
            context: Context, viewModel: CommentsViewModel, comment: CommentModel
        ) {
            MaterialAlertDialogBuilder(context).setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Yes") { _, _ ->
                    Toast.makeText(context, "Comment Deleted", Toast.LENGTH_SHORT).show()
                    viewModel.deleteComment(context, comment)
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        fun commentCopyHandler(
            context: Context, content: String
        ) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("comment", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}