package com.example.recipegpt.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.recipegpt.R

class CustomProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val progressBar: ProgressBar
    private val progressText: TextView

    init {
        View.inflate(context, R.layout.view_custom_progress, this)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
    }

    fun setProgress(value: Int) {
        progressBar.progress = value
        progressText.text = "$value%"
    }
}
