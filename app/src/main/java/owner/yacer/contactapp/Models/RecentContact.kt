package owner.yacer.contactapp.Models

import android.graphics.Bitmap

data class RecentPerson(
    var contactID:Int? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val type: Int,
    val date: String,
    val img: Bitmap? = null
)