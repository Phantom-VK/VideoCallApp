package com.app.videocallingapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import live.videosdk.rtc.android.VideoSDK

class JoinActivity: AppCompatActivity() {


    private var sampleToken = "78c36027-23ff-439f-bc25-0a678b890611"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        val btnCreate = findViewById<Button>(R.id.btnCreateMeeting)
        val btnJoin = findViewById<Button>(R.id.btnJoinMeeting)
        val etMeetingId = findViewById<EditText>(R.id.etMeetingId)

        btnCreate.setOnClickListener { v: View? ->
            createMeeting(sampleToken)
        }
        btnJoin.setOnClickListener { v: View? ->
            val intent = Intent(this@JoinActivity, MeetingActivity::class.java)
            intent.putExtra("token", sampleToken)
            intent.putExtra("meetingId", etMeetingId.text.toString())
            startActivity(intent)
        }
    }

    private fun createMeeting(token: String) {
        // we will explore this method in the next step
    }
}