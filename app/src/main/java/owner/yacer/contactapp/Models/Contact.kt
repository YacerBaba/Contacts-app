package owner.yacer.contactapp.Models

import android.graphics.Bitmap

data class Contact (
    val id:Int? = -1,
    val firstName:String,
    val lastName:String?=null,
    val phone:String,
    val photo : Bitmap?=null,
    val address :String? = null,
    val city:String? = null,
    var favorite:Boolean = false
)
