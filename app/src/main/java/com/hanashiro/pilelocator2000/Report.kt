package com.hanashiro.pilelocator2000

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a single report.
 *
 * @property id Unique identifier for the report.
 * @property latitude The latitude of the report's location.
 * @property longitude The longitude of the report's location.
 * @property photoUri The URI of the report's photo.
 * @property description A description of the report.
 * @property trashType The type of trash in the report.
 * @property status The current status of the report (e.g., "Pending", "Finished").
 */
@Parcelize
data class Report(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String,
    val description: String,
    val trashType: String,
    var status: String = "Pending"
) : Parcelable
