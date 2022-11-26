package com.example.discussions.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.discussions.R
import com.example.discussions.databinding.ActivityPollResultsBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class PollResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPollResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_poll_results)

        setPieChart()
    }

    private fun setPieChart() {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(50f, "18-dutdtdkdtykdtkdutftdytdlufd25"))
        entries.add(PieEntry(26.7f, "26-rynymydukukdukddukmdm35"))
        entries.add(PieEntry(24.0f, "36-dtmtdmuddkdkdukutdu45"))
        entries.add(PieEntry(30.8f, "46-tudjtdkdtuhdfhdfjfytjfyjfyhlhlhiiljfyjfyjukdukkdtukduk55"))
        entries.add(PieEntry(30.8f, "56-tdkdtukdtukdtukdtukdtuk65"))
        entries.add(PieEntry(30.8f, "66-tdkdtukdtukdtukdtukdtuk75"))


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