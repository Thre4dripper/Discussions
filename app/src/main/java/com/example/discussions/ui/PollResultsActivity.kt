package com.example.discussions.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityPollResultsBinding
import com.example.discussions.viewModels.PollResultsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class PollResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPollResultsBinding
    private lateinit var viewModel: PollResultsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_poll_results)
        viewModel = ViewModelProvider(this)[PollResultsViewModel::class.java]

        setPollData()
    }

    private fun setPollData() {
        val pollId = intent.getStringExtra(Constants.POLL_ID)
        viewModel.getPollDetails(pollId!!)

        binding.pollResultsQuestionTv.text = viewModel.pollQuestion
        setPieChart()
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
        dataSet.selectionShift = 8f

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
    }
}