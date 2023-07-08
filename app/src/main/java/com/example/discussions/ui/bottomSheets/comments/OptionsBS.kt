package com.example.discussions.ui.bottomSheets.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.discussions.MyApplication
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.databinding.BsCommentOptionsBinding
import com.example.discussions.models.CommentModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBS(
    private var comment: CommentModel,
    private var commentInterface: CommentInterface
) :
    BottomSheetDialogFragment() {
    private val TAG = "OptionsBottomSheet"

    private lateinit var binding: BsCommentOptionsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BsCommentOptionsBinding.inflate(inflater, container, false)

        if (comment.username != MyApplication.username) {
            binding.optionEditCommentTv.visibility = View.GONE
            binding.optionDeleteCommentTv.visibility = View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.optionEditCommentTv.setOnClickListener {
            commentInterface.onCommentEdit(comment.commentId, comment.comment)
            dismiss()
        }
        binding.optionDeleteCommentTv.setOnClickListener {
            commentInterface.onCommentDeleted(comment)
            dismiss()
        }
        binding.optionReplyCommentTv.setOnClickListener {
            commentInterface.onCommentReply(comment.commentId, comment.username)
            dismiss()
        }
        binding.optionCopyCommentTv.setOnClickListener {
            commentInterface.onCommentCopy(comment.comment)
            dismiss()
        }
        binding.optionCancelCommentTv.setOnClickListener {
            dismiss()
        }
    }
}