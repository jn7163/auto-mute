/*
 * Copyright (C) 2018 David Sommerich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.sommd.automute.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioPlaybackConfiguration
import androidx.content.systemService
import xyz.sommd.automute.App
import xyz.sommd.automute.R
import xyz.sommd.automute.settings.SettingsActivity
import xyz.sommd.automute.utils.log

class Notifications(private val context: Context) {
    companion object {
        const val STATUS_CHANNEL = "status"
        
        const val STATUS_ID = 1
        
        fun from(context: Context) = App.from(context).notifications
    }
    
    private val notifManager = context.systemService<NotificationManager>()
    private val res = context.resources
    
    fun createChannels() {
        val statusChannel = NotificationChannel(
                STATUS_CHANNEL,
                res.getText(R.string.notif_channel_status_name),
                NotificationManager.IMPORTANCE_LOW).apply {
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        
        notifManager.createNotificationChannel(statusChannel)
    }
    
    fun updateStatusNotification(muted: Boolean, playbackConfigs: Set<AudioPlaybackConfiguration>) {
        log("Updating status notification")
        
        notifManager.notify(STATUS_ID, createStatusNotification(muted, playbackConfigs))
    }
    
    fun createStatusNotification(muted: Boolean,
                                 playbackConfigs: Set<AudioPlaybackConfiguration> = emptySet()):
            Notification {
        val streamTypes = playbackConfigs.map { AutoMuteService.AudioType.from(it.audioAttributes) }
        val typeCounts = streamTypes.groupBy { it }.mapValues { it.value.size }
        val totalStreams = playbackConfigs.size
        val totalTypes = typeCounts.size
        
        return Notification.Builder(context, STATUS_CHANNEL).apply {
            setSmallIcon(
                    when {
                        muted -> R.drawable.ic_notif_status_muted
                        totalStreams == 0 -> R.drawable.ic_notif_status_unmuted
                        else -> R.drawable.ic_notif_status_playing
                    }
            )
            
            // Always the total number of streams playing
            setContentTitle(res.getQuantityString(R.plurals.notif_status_total_streams,
                                                  totalStreams, totalStreams))
            
            if (totalTypes == 1) {
                // Count of only audio type
                val type = streamTypes.first()
                setContentText(getTypeCountText(type, totalStreams))
            } else {
                // Number of different audio types
                setContentText(res.getQuantityString(R.plurals.notif_status_total_types,
                                                     totalTypes, totalTypes))
                
                if (totalTypes > 0) {
                    val style = Notification.InboxStyle()
                    
                    // Counts of each audio type
                    for (type in typeCounts.keys.sorted()) {
                        val count = typeCounts[type] ?: 0
                        style.addLine(getTypeCountText(type, count))
                    }
                    
                    setStyle(style)
                }
            }
            
            // Open SettingsActivity when clicked
            setContentIntent(PendingIntent.getActivity(context, 0, Intent(
                    context, SettingsActivity::class.java
            ), 0))
        }.build()
    }
    
    private fun getTypeCountText(type: AutoMuteService.AudioType, count: Int): CharSequence {
        val typeName = res.getStringArray(R.array.audio_type_names)[type.ordinal]
        return res.getQuantityString(R.plurals.notif_status_type_count, count, count, typeName)
    }
}