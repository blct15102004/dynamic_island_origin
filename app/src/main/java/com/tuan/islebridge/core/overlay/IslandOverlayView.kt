package com.tuan.islebridge.core.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.tuan.islebridge.R
import com.tuan.islebridge.core.originisland.IslandCardModel

class IslandOverlayView(context: Context) : FrameLayout(context) {

    private val container: LinearLayout
    private val iconView: ImageView
    private val titleView: TextView
    private val subtextView: TextView
    private val progressBar: ProgressBar

    private var isExpanded = false
    private var currentCard: IslandCardModel? = null

    init {
        // Pill background
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setColor(Color.parseColor("#0F0F12"))
            setStroke(2, Color.parseColor("#222228"))
        }
        background = bg

        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(28, 12, 28, 12)
        }

        iconView = ImageView(context).apply {
            setImageResource(R.drawable.ic_launcher_foreground)
            layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                marginEnd = 16
            }
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        titleView = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            maxLines = 1
        }

        subtextView = TextView(context).apply {
            setTextColor(Color.parseColor("#9E9EA8"))
            textSize = 10f
            maxLines = 1
        }

        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(160, 8).apply {
                topMargin = 4
            }
        }

        textLayout.addView(titleView)
        textLayout.addView(subtextView)
        textLayout.addView(progressBar)

        container.addView(iconView)
        container.addView(textLayout)

        addView(container)

        setOnClickListener {
            toggleExpanded()
        }
    }

    fun bind(card: IslandCardModel) {
        currentCard = card
        titleView.text = card.capsuleText
        subtextView.text = card.capsuleSubtext ?: card.subtitle

        if (card.progress != null && card.maxProgress != null) {
            progressBar.visibility = View.VISIBLE
            progressBar.max = card.maxProgress
            progressBar.progress = card.progress
            progressBar.isIndeterminate = card.isIndeterminate
        } else {
            progressBar.visibility = View.GONE
        }
    }

    private fun toggleExpanded() {
        isExpanded = !isExpanded
        val card = currentCard ?: return
        if (isExpanded) {
            titleView.text = card.title
            subtextView.text = card.subtitle
            subtextView.maxLines = 3
        } else {
            titleView.text = card.capsuleText
            subtextView.text = card.capsuleSubtext ?: card.subtitle
            subtextView.maxLines = 1
        }
    }
}

