package com.njg.appratingdialog

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.support.v7.widget.AppCompatRatingBar
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Toast


class RatingDialog(private var mContext: Context) : DialogInterface.OnClickListener {
    private var APP_PNAME = mContext.applicationContext.packageName
    private var APP_TITLE = mContext.packageManager.getApplicationLabel(mContext.applicationInfo).toString()
    private var DAYS_UNTIL_PROMPT : Int = 3
    private var LAUNCHES_UNTIL_PROMPT = 3
    private val dialog : AlertDialog.Builder = AlertDialog.Builder(mContext)
    private var clickListener: DialogInterface.OnClickListener? = null
    private var ratingBar: AppCompatRatingBar? = null
    private var dialogBuilder : AlertDialog? = null
    private val ll = LinearLayout(mContext)
    private val prefs = mContext.getSharedPreferences("apprater", 0)

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val editor = prefs.edit()
        when(which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val uri = Uri.parse("market://details?=$APP_PNAME")
                val gotoMarket = Intent(Intent.ACTION_VIEW, uri)
                gotoMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
                val stars = prefs!!.getFloat("numStars", 0.0f)
                Log.d("test", "$stars")

                Toast.makeText(mContext, "Value : "+stars, Toast.LENGTH_SHORT).show()
                try {
                    mContext.startActivity(gotoMarket)

                } catch(e: ActivityNotFoundException){
                    mContext.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=$APP_PNAME"))
                    )
                }
                dialog?.dismiss() }

            DialogInterface.BUTTON_NEUTRAL -> {
                if(editor != null) {
                    editor.putBoolean("remindmelater", true)
                    var reset = 0L
                    editor.putLong("app_launch_count", reset)
                    editor.apply()

                }
                else {
                    dialog?.dismiss()
                }

            }
            DialogInterface.BUTTON_NEGATIVE -> {
                if(editor != null) {
                    editor.putBoolean("dontshowthisagain", true)
                    editor.commit()
                } else {
                    dialog?.dismiss()
                }

            }

        }
        if (clickListener != null) {
            clickListener!!.onClick(dialog, which)
        }
    }

    init {
        dialogBuilder = dialog.create()
        ratingBar = AppCompatRatingBar(mContext)
        val editor = prefs.edit()
        dialogBuilder?.setCanceledOnTouchOutside(true)
        dialogBuilder?.setButton(
            AlertDialog.BUTTON_POSITIVE, "Rate Now",
            setOnClickListener(clickListener))
        dialogBuilder?.setButton(
            AlertDialog.BUTTON_NEUTRAL, "Remind me Later"
            , setOnClickListener(clickListener))
        dialogBuilder?.setButton(
            AlertDialog.BUTTON_NEGATIVE, "Never"
            , setOnClickListener(clickListener))


        dialogBuilder!!.setOnCancelListener {
            if(editor != null) {
                editor.putBoolean("remindmelater", true)
                var reset = 0L
                editor.putLong("app_launch_count", reset)
                editor.apply()
            }
        }
    }

    fun setLaunchesBeforePrompt(launch: Int): RatingDialog{
        LAUNCHES_UNTIL_PROMPT = launch
        return this
    }
    fun setDaysUntilPrompt(days: Int): RatingDialog{
        DAYS_UNTIL_PROMPT = days
        return this
    }
    fun setAppTitle( title: String): RatingDialog{
        APP_TITLE = title
        return this
    }
    fun setDialogTitle(dialogTitle: String): RatingDialog{
        dialogBuilder?.setTitle(dialogTitle)
        return this
    }
    fun setMessage(message: String): RatingDialog {
        dialogBuilder?.setMessage(message)
        return this
    }
    fun setPositiveText(text: String):RatingDialog {
        dialogBuilder?.getButton(AlertDialog.BUTTON_POSITIVE)?.text = text
        return this
    }
    fun setNegativeText(text: String): RatingDialog {
        dialogBuilder?.getButton(AlertDialog.BUTTON_NEGATIVE)?.text = text
        return this
    }
    fun setNeutralText(text: String): RatingDialog {
        dialogBuilder?.getButton(AlertDialog.BUTTON_NEUTRAL)?.text = text
        return this
    }

    fun load() : RatingDialog{
        var new_app_launch_count: Long
        if (prefs.getBoolean("dontshowthisagain", false))
        {
            return this
        }
        val editor = prefs.edit()

        val app_launch_count = prefs.getLong("app_launch_count", 0) + 1
        editor.putLong("app_launch_count", app_launch_count)

        var date_app_firstLaunch : Long? = prefs.getLong("date_app_firstLaunch", 0)

        if(date_app_firstLaunch == 0L){
            date_app_firstLaunch = System.currentTimeMillis()
            editor.putLong("date_app_firstLaunch", date_app_firstLaunch)
        } else {


        }
        if(prefs.getBoolean("remindmelater", false)){
            new_app_launch_count = prefs.getLong("app_launch_count", 0) + 1
            editor.putLong("app_launch_count", new_app_launch_count)
            val remind_me_later_date: Long = System.currentTimeMillis()
            editor.putLong("date_app_firstLaunch", remind_me_later_date)
            editor.apply()
            if(new_app_launch_count >= LAUNCHES_UNTIL_PROMPT ||
                System.currentTimeMillis() >= remind_me_later_date!! + DAYS_UNTIL_PROMPT * 24* 60 * 60 * 100) {
                if(dialogBuilder != null){
                    showDefaultRatingDialog(editor)
                } else {
                }
            }
        } else {
            if(app_launch_count >= LAUNCHES_UNTIL_PROMPT ||
                System.currentTimeMillis() >= date_app_firstLaunch!! + DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {
                if(dialogBuilder != null){
                    showDefaultRatingDialog(editor)
                } else {
                }
            }
        }
        editor.apply()
        return this
    }
    fun showDefaultRatingDialog(editor: SharedPreferences.Editor?): RatingDialog{
        ll.orientation = LinearLayout.VERTICAL
        dialogBuilder?.setTitle("Rate us now")
        dialogBuilder?.setMessage("If you enjoy using $APP_TITLE, please take a moment to rate it. Thanks for your support!")
        dialogBuilder?.setView(ll)
        ratingBar?.numStars = 5
        ratingBar?.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        ll.setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
        ratingBar?.setPadding(5,15,5,0)
        ratingBar?.setOnRatingBarChangeListener { rate, rating, fromUser ->
            val numStars  = ratingBar?.rating!!.toFloat()
            editor?.putFloat("numStars", numStars)
            editor?.apply()
        }
        ll.addView(ratingBar)
        dialogBuilder?.show()
        return this
    }
    fun setOnClickListener(onClickListener: DialogInterface.OnClickListener?): RatingDialog {
        clickListener = onClickListener
        return this
    }
}