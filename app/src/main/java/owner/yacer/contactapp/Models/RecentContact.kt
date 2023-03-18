package owner.yacer.contactapp.Models

import android.graphics.Bitmap

data class RecentContact(
    val fullName: String? = null,
    val phone: String? = null,
    val type: Int,
    val date: Long,
    val img: Bitmap? = null,
    val rowid :Int? = 0
)