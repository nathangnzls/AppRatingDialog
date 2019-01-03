package com.example.nathan.apprating

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.njg.appratingdialog.RatingDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RatingDialog(this)
            .setLaunchesBeforePrompt(1)
            .setDaysUntilPrompt(1)
            .load()
            .setDialogTitle("Title")
            .setPositiveText("Rate Button")
            .setMessage("This is a test")
            .setOnClickListener(
                DialogInterface.OnClickListener
                { dialog: DialogInterface, which: Int ->
                    when(which) {
                        DialogInterface.BUTTON_NEUTRAL ->
                        {
                            Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show()
                        }
                    }
                })




    }
}
