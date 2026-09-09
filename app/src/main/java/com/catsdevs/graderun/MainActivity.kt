package com.catsdevs.graderun

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "GradleRun\n\nGradle project created successfully!"
            textSize = 22f
            setPadding(32, 32, 32, 32)
        })
    }
}
