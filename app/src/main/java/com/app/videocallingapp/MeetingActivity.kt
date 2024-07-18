package com.app.videocallingapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener

class MeetingActivity : AppCompatActivity() {
    // Declare variables to handle the meeting
    private var meeting: Meeting? = null
    private var micEnabled = true
    private var webcamEnabled = true

    // Initialize DatabaseActivity instance
    private val dbActivity: DatabaseActivity = DatabaseActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

        // Retrieve token and meetingId from the intent
        val token = intent.getStringExtra("token")
        val meetingId = intent.getStringExtra("meetingId")
        val participantName = "John Doe" // TODO: Get from user

        // Configure VideoSDK with the token
        VideoSDK.config(token)

        // Initialize VideoSDK Meeting
        meeting = VideoSDK.initMeeting(
            this, meetingId, participantName,
            micEnabled, webcamEnabled, null, null, false, null, null
        )

        // Add event listener for upcoming events
        meeting!!.addEventListener(meetingEventListener)

        // Join the VideoSDK Meeting
        meeting!!.join()

        // Set the meetingId text
        findViewById<TextView>(R.id.tvMeetingId).text = meetingId

        // Initialize RecyclerView for participants
        findViewById<RecyclerView>(R.id.rvParticipants)?.apply {
            layoutManager = GridLayoutManager(this@MeetingActivity, 2)
            adapter = ParticipantAdapter(meeting!!)
        }

        // Set action listeners for UI interactions
        setActionListeners()
    }

    // Creating the MeetingEventListener
    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()")
        }

        override fun onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()")
            meeting = null
            if (!isDestroyed) finish()
        }

        override fun onParticipantJoined(participant: Participant) {
            // Set joined meeting as not available in the database
            lifecycleScope.launch(Dispatchers.IO) {
                meeting?.meetingId?.let { dbActivity.updateMeetingStatus(it, false) }
            }
            Toast.makeText(
                this@MeetingActivity, "${participant.displayName} joined",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onParticipantLeft(participant: Participant) {
            // Set joined meeting as available in the database
            lifecycleScope.launch(Dispatchers.IO) {
                meeting?.meetingId?.let { dbActivity.updateMeetingStatus(it, true) }
            }
            Toast.makeText(
                this@MeetingActivity, "${participant.displayName} left",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Set action listeners for mic, webcam, and leave buttons
    private fun setActionListeners() {
        // Toggle mic
        findViewById<View>(R.id.btnMic).setOnClickListener {
            if (micEnabled) {
                meeting!!.muteMic()
                Toast.makeText(this, "Mic Muted", Toast.LENGTH_SHORT).show()
            } else {
                meeting!!.unmuteMic()
                Toast.makeText(this, "Mic Enabled", Toast.LENGTH_SHORT).show()
            }
            micEnabled = !micEnabled
        }

        // Toggle webcam
        findViewById<View>(R.id.btnWebcam).setOnClickListener {
            if (webcamEnabled) {
                meeting!!.disableWebcam()
                Toast.makeText(this, "Webcam Disabled", Toast.LENGTH_SHORT).show()
            } else {
                meeting!!.enableWebcam()
                Toast.makeText(this, "Webcam Enabled", Toast.LENGTH_SHORT).show()
            }
            webcamEnabled = !webcamEnabled
        }

        // Leave meeting
        findViewById<View>(R.id.btnLeave).setOnClickListener {
            // Update meeting status if the user is the last participant
            if ((meeting?.participants?.size ?: 0) <= 1) {
                lifecycleScope.launch(Dispatchers.IO) {
                    meeting?.meetingId?.let { dbActivity.updateMeetingStatus(it, true) }
                }
            }else if((meeting?.participants?.size ?: 0) == 0){
                lifecycleScope.launch(Dispatchers.IO) {
                    meeting?.meetingId?.let { dbActivity.deleteMeetingFromDatabase(it) }
                    meeting?.end()
                }
            }
            meeting!!.leave()
        }
    }
}
