package com.example.discussions.ui.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.databinding.CommentOptionsBsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBottomSheet(
    private var commentId: String,
    private var commentInterface: CommentInterface
) :
    BottomSheetDialogFragment() {
    private lateinit var binding: CommentOptionsBsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CommentOptionsBsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.optionEditCommentTv.setOnClickListener {
            commentInterface.onCommentEdit(commentId)
            dismiss()
        }
        binding.optionDeleteCommentTv.setOnClickListener {
            commentInterface.onCommentDeleted(commentId)
            dismiss()
        }
        binding.optionReplyCommentTv.setOnClickListener {
            commentInterface.onCommentReply(commentId)
            dismiss()
        }
        binding.optionCopyCommentTv.setOnClickListener {
            commentInterface.onCommentCopy(commentId)
            dismiss()
        }
        binding.optionCancelCommentTv.setOnClickListener {
            dismiss()
        }
    }
}