package com.example.discussions.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.PollResultsRecyclerAdapter
import com.example.discussions.databinding.ActivityPollResultsBinding
import com.example.discussions.viewModels.PollResultsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class PollResultsActivity : AppCompatActivity() {
    private val TAG = "PollResultsActivity"

    private lateinit var binding: ActivityPollResultsBinding
    private lateinit var viewModel: PollResultsViewModel

    private lateinit var pollResultsAdapter: PollResultsRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_poll_results)
        viewModel = ViewModelProvider(this)[PollResultsViewModel::class.java]

        binding.pollResultsBackBtn.setOnClickListener { finish() }

        binding.pollResultsRv.apply {
            pollResultsAdapter = PollResultsRecyclerAdapter()
            adapter = pollResultsAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@PollResultsActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        setPollData()
    }

    private fun setPollData() {
        val pollId = intent.getStringExtra(Constants.POLL_ID)
        viewModel.getPollDetails(pollId!!)

        binding.pollResultsQuestionTv.text = viewModel.pollQuestion
        setPieChart()

        viewModel.votedByList.observe(this) {
            if (it.isNotEmpty()) {
                pollResultsAdapter.submitList(it)
            }
        }
    }

    private fun setPieChart() {
        val entries = ArrayList<PieEntry>()
        for (option in viewModel.pollOptions) {
            entries.add(PieEntry(option.votes.toFloat(), option.content))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = mutableListOf(
            getColor(R.color.pie_chart_color_1),
            getColor(R.color.pie_chart_color_2),
            getColor(R.color.pie_chart_color_3),
            getColor(R.color.pie_chart_color_4),
            getColor(R.color.pie_chart_color_5),
            getColor(R.color.pie_chart_color_6)
        )

        dataSet.valueTextColor = R.color.white
        dataSet.valueTextSize = 14f
        dataSet.sliceSpace = 5f
        dataSet.selectionShift = 12f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChart))
        binding.pieChart.data = data

        val legend: Legend = binding.pieChart.legend
        legend.textSize = 12f
        legend.isWordWrapEnabled = true
        legend.mTextWidthMax = 10f
        legend.xEntrySpace = 10f
        legend.yEntrySpace = 5f
        legend.setDrawInside(false)

        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.animateXY(1500, 1500, Easing.EaseInOutQuad)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.invalidate()


        // on pie chart value selected
        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val index = h?.x?.toInt()

                if (index != null) {
                    binding.pollResultsOptionTv.text = viewModel.pollOptions[index].content
                    binding.pollResultsVotesCountTv.text = resources.getQuantityString(
                        R.plurals.poll_result_option_votes_text,
                        viewModel.pollOptions[index].votes,
                        viewModel.pollOptions[index].votes
                    )

                    //get all voted by list for selected option
                    viewModel.getVotedByList(index)
                }
            }

            override fun onNothingSelected() {
                binding.pollResultsOptionTv.text =
                    resources.getString(R.string.poll_result_option_text)
                binding.pollResultsVotesCountTv.text =
                    resources.getQuantityString(R.plurals.poll_result_option_votes_text, 0, 0)

                //clear all voted by list
                pollResultsAdapter.submitList(mutableListOf())
            }
        })
    }
}