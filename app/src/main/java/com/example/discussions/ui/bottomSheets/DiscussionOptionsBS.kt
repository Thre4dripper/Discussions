package com.example.discussions.ui.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.discussions.MyApplication
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.adapters.interfaces.DiscussionMenuInterface
import com.example.discussions.databinding.BsDiscussionOptionsBinding
import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DiscussionOptionsBS(
    private var post: PostModel?,
    private var poll: PollModel?,
    private var discussionMenuInterface: DiscussionMenuInterface
) :
    BottomSheetDialogFragment() {
    private val TAG = "DiscussionOptionsBS"

    private lateinit var binding: BsDiscussionOptionsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BsDiscussionOptionsBinding.inflate(inflater, container, false)

        if (post != null) {
            if (post!!.username == MyApplication.username) {
                binding.optionEditDiscussionTv.visibility = View.VISIBLE
                binding.optionDeleteDiscussionTv.visibility = View.VISIBLE
            } else {
                binding.optionEditDiscussionTv.visibility = View.GONE
                binding.optionDeleteDiscussionTv.visibility = View.GONE
            }
        } else if (poll != null) {
            if (poll!!.username == MyApplication.username) {
                //edit not allowed for polls
                binding.optionEditDiscussionTv.visibility = View.GONE
                binding.optionDeleteDiscussionTv.visibility = View.VISIBLE
            } else {
                binding.optionEditDiscussionTv.visibility = View.GONE
                binding.optionDeleteDiscussionTv.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.optionEditDiscussionTv.setOnClickListener {
            if (post != null) {
                discussionMenuInterface.onMenuEdit(
                    post!!.postId,
                    null,
                    DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POST
                )
            }
            dismiss()
        }

        binding.optionDeleteDiscussionTv.setOnClickListener {
            if (post != null) {
                discussionMenuInterface.onMenuDelete(
                    post!!.postId,
                    null,
                    DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POST
                )
            }
            if (poll != null) {
                discussionMenuInterface.onMenuDelete(
                    null,
                    poll!!.pollId,
                    DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POLL
                )
            }
            dismiss()
        }

        binding.optionBookmarkDiscussionTv.setOnClickListener {
            //TODO bookmark
            dismiss()
        }

        binding.optionReportDiscussionTv.setOnClickListener {
            //TODO report
            dismiss()
        }

        binding.optionCancelDiscussionTv.setOnClickListener {
            dismiss()
        }
    }
}