package com.app.videocallingapp

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class DatabaseActivity {

    private val db = Firebase.firestore

    // Adds a meeting to the database and returns true if successful, false otherwise
    suspend fun addMeetingToDatabase(meeting: Meeting): Boolean {
        return try {
            val userMap = meeting.toMap().filterValues { it.toString().isNotEmpty() }
            db.collection("Meetings")
                .document(meeting.meetingId)
                .set(userMap).await()
            true
        } catch (e: Exception) {
            Log.e("DatabaseActivity", "Error adding meeting to database", e)
            false
        }
    }

    // Updates the status of a meeting and returns true if successful, false otherwise
    suspend fun updateMeetingStatus(meetingId: String, isAvailable: Boolean): Boolean {
        return try {
            db.collection("Meetings")
                .document(meetingId)
                .update("isAvailable", isAvailable)
                .await()
            true
        } catch (e: Exception) {
            Log.e("DatabaseActivity", "Error updating meeting status", e)
            false
        }
    }

    suspend fun updateParticipantCount(meetingId: String, participantCount: Int): Boolean {
        return try {db.collection("Meetings")
            .document(meetingId)
            .update("participantCount", participantCount)
            .await()
            true
        }catch (e:Exception){
            Log.e("DatabaseActivity", "Error updating participant count", e)
            false
        }
    }

    // Deletes a meeting from the database and returns true if successful, false otherwise
    suspend fun deleteMeetingFromDatabase(meetingId: String): Boolean {
        return try {
            db.collection("Meetings")
                .document(meetingId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("DatabaseActivity", "Error deleting meeting from database", e)
            false
        }
    }

    // Retrieves a random meeting ID from available meetings in the database
    suspend fun getRandomMeetingId(): String? {
        val availableMeetings = getAvailableMeetings()
        return if (availableMeetings.isNotEmpty()) {
            val randomIndex = Random.nextInt(availableMeetings.size)
            val randomMeetingId = availableMeetings[randomIndex].meetingId
            Log.d("RandomMeeting", "Meeting ID: $randomMeetingId")
            randomMeetingId
        } else {
            null
        }
    }

    // Retrieves a list of available meetings from the database
    private suspend fun getAvailableMeetings(): List<Meeting> {
        val meetings = mutableListOf<Meeting>()
        try {
            val result = db.collection("Meetings").get().await()
            for (document in result) {
                val meeting = document.toObject(Meeting::class.java)
                if (meeting.isAvailable) {
                    meetings.add(meeting)
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseActivity", "Error retrieving available meetings", e)
        }
        return meetings
    }

    // Converts a Meeting object to a map
    private fun Meeting.toMap(): Map<String, Any?> {
        return mapOf(
            "meetingId" to meetingId,
            "isAvailable" to isAvailable
        )
    }
}
