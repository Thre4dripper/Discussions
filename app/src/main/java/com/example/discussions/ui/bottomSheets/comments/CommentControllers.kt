package com.example.discussions.ui.bottomSheets.comments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.discussions.Constants
import com.example.discussions.models.CommentModel
import com.example.discussions.viewModels.CommentsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommentControllers {
    companion object {
        var commentType = ""
        var commentId: String? = null

        /**
         * METHODS FOR COMMENT OBSERVERS
         */
        fun setupCommentObservers(
            context: Context,
            viewModel: CommentsViewModel,
            cardView: View,
            progressBar: View,
            btn: View,
            lifecycleOwner: LifecycleOwner,
            restoreCommentType: () -> Unit,
        ) {
            //setting add comment observer only once
            addCommentObserver(
                context,
                viewModel,
                cardView,
                progressBar,
                btn,
                lifecycleOwner
            ) { restoreCommentType() }

            //setting edit comment observer only once
            editCommentObserver(
                context,
                viewModel,
                cardView,
                progressBar,
                btn,
                lifecycleOwner
            ) { restoreCommentType() }

            //setting delete comment observer only once
            deleteCommentObserver(
                context, viewModel, lifecycleOwner
            )

            //setting like comment observer only once
            likeCommentObserver(
                context, viewModel, lifecycleOwner
            )
        }

        /***
         * METHODS FOR OBSERVING COMMENT DATA
         */
        private fun addCommentObserver(
            context: Context,
            viewModel: CommentsViewModel,
            commentActionsCv: View,
            commentAddProgressBar: View,
            addCommentBtn: View,
            lifecycleOwner: LifecycleOwner,
            restoreCommentType: () -> Unit,
        ) {
            viewModel.isCommentAdded.observe(lifecycleOwner) {
                if (it != null) {
                    if (it == Constants.API_SUCCESS) {
                        Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show()
                        commentActionsCv.visibility = View.GONE
                        restoreCommentType()
                    } else {
                        Toast.makeText(context, "Error Adding Comment", Toast.LENGTH_SHORT).show()
                    }
                    commentAddProgressBar.visibility = View.GONE
                    addCommentBtn.visibility = View.VISIBLE
                }
            }
        }

        private fun editCommentObserver(
            context: Context,
            viewModel: CommentsViewModel,
            commentActionsCv: View,
            commentAddProgressBar: View,
            addCommentBtn: View,
            lifecycleOwner: LifecycleOwner,
            restoreCommentType: () -> Unit,
        ) {
            viewModel.isCommentEdited.observe(lifecycleOwner) {
                if (it != null) {
                    if (it == Constants.API_SUCCESS) {
                        Toast.makeText(context, "Comment Updated", Toast.LENGTH_SHORT).show()
                        commentActionsCv.visibility = View.GONE
                        restoreCommentType()
                    } else {
                        Toast.makeText(context, "Error Editing Comment", Toast.LENGTH_SHORT).show()
                    }
                    commentAddProgressBar.visibility = View.GONE
                    addCommentBtn.visibility = View.VISIBLE
                }
            }
        }

        private fun deleteCommentObserver(
            context: Context, viewModel: CommentsViewModel, lifecycleOwner: LifecycleOwner
        ) {
            viewModel.isCommentDeleted.observe(lifecycleOwner) {
                if (it != null && it != Constants.API_SUCCESS) Toast.makeText(
                    context, "Error deleting comment", Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun likeCommentObserver(
            context: Context, viewModel: CommentsViewModel, lifecycleOwner: LifecycleOwner
        ) {
            viewModel.isCommentLikedChanged.observe(lifecycleOwner) {
                if (it != null && it != Constants.API_SUCCESS) Toast.makeText(
                    context, "Error liking comment", Toast.LENGTH_SHORT
                ).show()
            }
        }

        /***
         * METHODS FOR HANDLING COMMENT ACTIONS
         */
        fun addCommentHandler(
            context: Context,
            progressBar: View,
            commentAddBtn: ImageView,
            addCommentEt: EditText,
            id: String,
            viewModel: CommentsViewModel,
        ) {
            //preconfiguring add comment button
            commentAddBtn.apply {
                isEnabled = false
                drawable.alpha = 100
                setOnClickListener {
                    createEditComment(
                        context, addCommentEt.text.toString(), progressBar, commentAddBtn,
                        commentType, id, commentId, viewModel
                    )
                    addCommentEt.text.clear()
                }
            }

            //controlling add comment button based on text
            addCommentEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {
                        commentAddBtn.apply {
                            isEnabled = false
                            drawable.alpha = 100
                        }
                    } else {
                        commentAddBtn.apply {
                            isEnabled = true
                            drawable.alpha = 255
                        }
                    }
                }

            })
        }

        /**
         * METHOD FOR CREATING OR EDITING COMMENT
         */
        private fun createEditComment(
            context: Context,
            content: String,
            progressBar: View,
            commentAddBtn: View,
            commentType: String,
            id: String,
            commentId: String?,
            viewModel: CommentsViewModel
        ) {
            progressBar.visibility = View.VISIBLE
            commentAddBtn.visibility = View.GONE

            when (commentType) {
                Constants.COMMENT_TYPE_POST -> viewModel.createComment(
                    context, postId = id, null, null, content
                )
                Constants.COMMENT_TYPE_POLL -> viewModel.createComment(
                    context, null, pollId = id, null, content
                )
                Constants.COMMENT_TYPE_REPLY -> viewModel.createComment(
                    context, null, null, commentId = commentId, content
                )
                Constants.COMMENT_TYPE_EDIT -> viewModel.editComment(
                    context, commentId!!, content
                )
            }
        }

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