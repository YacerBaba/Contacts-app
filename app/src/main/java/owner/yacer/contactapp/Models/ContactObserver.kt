package owner.yacer.contactapp.Models

import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import owner.yacer.contactapp.Fragments.RecentFragment
import owner.yacer.contactapp.R
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ContactObserver (val context:Context): ContentObserver(null) {
    private val listIcons = intArrayOf(
        R.drawable.user1,
        R.drawable.user2,
        R.drawable.user3,
        R.drawable.user4,
        R.drawable.user5,
        R.drawable.user6,
        R.drawable.user7,
        R.drawable.user8,
        R.drawable.user9
    )
    private val random = Random()
    override fun onChange(selfChange: Boolean) {
        val lastTime = RecentFragment.spLast.getLong("lastTime",0)
        Log.e("msg","spLast timestmp = $lastTime")
        val dbHelper = DbHelper(context)
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
            ),
            "${CallLog.Calls.DATE} > $lastTime",
            null,
            CallLog.Calls.DATE +" ASC"
        )
        val listRecent = LinkedList<RecentContact>()
        if(cursor == null){
            Log.e("msg","cursor null")
            return
        }
        if(cursor.moveToFirst()){
            val column_phone = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val column_type = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val column_date = cursor.getColumnIndex(CallLog.Calls.DATE)

            val format = SimpleDateFormat("EEE HH:mm")
            do {
                val phone = cursor.getString(column_phone)
                val type = cursor.getInt(column_type)
                val date = cursor.getString(column_date).toLong()
                var imgBitmap: Bitmap?
                val recentContact:RecentContact
                val searchResult = dbHelper.searchByPhoneNumber(phone)
                if(searchResult.isNotEmpty()){
                    val contact = searchResult[0]
                    val fullName = "${contact.firstName} ${contact.lastName}"
                    imgBitmap = contact.photo
                    recentContact = RecentContact(fullName,phone,type, date,imgBitmap)
                }else{
                    imgBitmap = BitmapFactory.decodeResource(
                        context.resources , listIcons[random.nextInt(listIcons.size-1)]
                    )
                    recentContact =
                        RecentContact(null,phone = phone, type = type, date = date, img = imgBitmap)
                }
                listRecent.add(recentContact)
            }while (cursor.moveToNext())
            RecentFragment.spLast.edit().putLong("lastTime",listRecent[0].date).apply()
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                for (recent in listRecent){
                    dbHelper.addRecentContact(recent)
                    //RecentFragment.recentAdapter.listRecent.addFirst(recent)
                }
                RecentFragment.recentAdapter.listRecent = dbHelper.getRecentContacts()
                RecentFragment.recentAdapter.notifyDataSetChanged()
            }
        }else{
            Log.e("msgObserver","No data found")
        }
    }

}