package owner.yacer.contactapp.Models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import owner.yacer.contactapp.Fragments.RecentFragment
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


const val TABLE_CONTACTS = "contacts"
const val CONTACT_ID = "contact_id"
const val CONTACT_FIRSTNAME = "first_name"
const val CONTACT_LASTNAME = "last_name"
const val CONTACT_PHONE = "phone"
const val CONTACT_PHOTO = "profile_pic"
const val CONTACT_ADDRESS = "address"
const val CONTACT_CITY = "city"
const val CONTACT_FAVORITE = "favorite"
const val CONTACT_ADDED_AT = "added_at"
const val TABLE_CALLOG = "table_callLog"
const val CALLOG_PHONE = "callLog_phone"
const val CALLOG_TYPE = "callLog_type"
const val CALLOG_DATE = "callLog_date"
const val CALLOG_IMG = "callLog_img"
const val CALLOG_ID = "callLog_id"
const val TABLE_IMAGE = "table_img"
const val CALLOG_FULLNAME = "callog_fullName"
const val IMAGE_ID = "img_id"
const val IMAGE_DATA = "img_data"

class DbHelper(private val context: Context) :
    SQLiteOpenHelper(context, "contactsApp.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {

        val createQuery =
            "create table $TABLE_CONTACTS ($CONTACT_ID integer primary key autoincrement,$CONTACT_FIRSTNAME text" +
                    ",$CONTACT_LASTNAME text not null,$CONTACT_PHONE text not null,$CONTACT_ADDRESS text" +
                    ",$CONTACT_CITY text,$CONTACT_PHOTO blob not null ,$CONTACT_FAVORITE boolean not null" +
                    ",$CONTACT_ADDED_AT text not null );"
        val createCallLogQuery =
            "create table  $TABLE_CALLOG ($CALLOG_ID integer primary key autoincrement" +
                    ",$CALLOG_PHONE text ,$CALLOG_FULLNAME text default null" +
                    ",$CALLOG_DATE integer not null ,$CALLOG_TYPE integer not null);"
        val createImgTableQuery =
            "create table $TABLE_IMAGE ($IMAGE_ID text not null,$IMAGE_DATA blob ," +
                    "foreign key ($IMAGE_ID) references $TABLE_CALLOG ($CALLOG_PHONE));"
        db!!.execSQL(createCallLogQuery)
        db!!.execSQL(createQuery)
        db!!.execSQL(createImgTableQuery)
        db.execSQL("CREATE INDEX idx ON $TABLE_CALLOG($CALLOG_DATE) ;")


    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.needUpgrade(newVersion)

    }

    fun addContact(contact: Contact): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        val stream = ByteArrayOutputStream()
        contact.photo!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val photoByteArray = stream.toByteArray()
        cv.put(CONTACT_FIRSTNAME, contact.firstName)
        cv.put(CONTACT_LASTNAME, contact.lastName)
        cv.put(CONTACT_PHONE, contact.phone)
        cv.put(CONTACT_ADDRESS, contact.address)
        cv.put(CONTACT_CITY, contact.city)
        cv.put(CONTACT_PHOTO, photoByteArray)
        cv.put(CONTACT_FAVORITE, contact.favorite)
        cv.put(CONTACT_ADDED_AT, contact.addedAt)
        val result = db.insert(TABLE_CONTACTS, null, cv)
        val exist = existInCallLog(contact.phone)
        if (exist) {
            updateCallLog(contact)
        }
        db.close()
        return result > 0
    }

    fun existInCallLog(phone: String): Boolean {
        val query = "select rowid from $TABLE_CALLOG where $CALLOG_PHONE = ?;"
        val db = readableDatabase
        val cursor = db.rawQuery(query, arrayOf(phone))
        return cursor.moveToFirst()
    }

    fun updateCallLog(contact: Contact) {
        val db = writableDatabase
        val stream = ByteArrayOutputStream()
        contact.photo!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val cv = ContentValues()
        val cvImg = ContentValues()
        cv.put(CALLOG_FULLNAME, "${contact.firstName} ${contact.lastName}")
        cvImg.put(IMAGE_DATA, stream.toByteArray())
        val result = db.update(TABLE_CALLOG, cv, "$CALLOG_PHONE = '${contact.phone}'", null)
        val resultImg = db.update(TABLE_IMAGE, cvImg, "$IMAGE_ID = '${contact.phone}'", null)
        if (result > 0 && resultImg > 0) {
            Log.e("msgUpdate", "success")
        } else {
            Log.e("msgUpdate", "failed")
        }
    }

    fun deleteContact(contact: Contact): Boolean {
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $TABLE_CONTACTS where ;"
        val result = db.delete(TABLE_CONTACTS,"$CONTACT_ID = ?",arrayOf("${contact.id}"))
        val exist = existInCallLog(contact.phone)
        if (exist) {
            //val userImg = getImage(contact.phone)
            //if (userImg != null) {
                val cv = ContentValues()
                cv.putNull(CALLOG_FULLNAME)
                db.update(TABLE_CALLOG, cv, "$CALLOG_PHONE = '${contact.phone}'", null)
                val cvImg = ContentValues()
                val img = BitmapFactory.decodeResource(
                    context.resources,
                    RecentFragment.listIcons[RecentFragment.random.nextInt(RecentFragment.listIcons.size - 1)]
                )
                val stream = ByteArrayOutputStream()
                img.compress(Bitmap.CompressFormat.PNG, 100, stream)
                cvImg.put(IMAGE_DATA, stream.toByteArray())
                val insertImgRes =
                    db.update(TABLE_IMAGE, cvImg, "$IMAGE_ID = '${contact.phone}'", null)

        }

        return result > 0
    }

    fun editContact(contact: Contact): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        val stream = ByteArrayOutputStream()
        contact.photo!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val photoByteArray = stream.toByteArray()

        cv.put(CONTACT_FIRSTNAME, contact.firstName)
        cv.put(CONTACT_LASTNAME, contact.lastName)
        cv.put(CONTACT_PHONE, contact.phone)
        cv.put(CONTACT_ADDRESS, contact.address)
        cv.put(CONTACT_CITY, contact.city)
        cv.put(CONTACT_PHOTO, photoByteArray)
        cv.put(CONTACT_FAVORITE, contact.favorite)
        val result = db.update(TABLE_CONTACTS, cv, "$CONTACT_ID = ?", arrayOf("${contact.id}"))
        if (existInCallLog(contact.phone))
            updateCallLog(contact)
        return result > 0
    }

    fun getOneContact(id: Int): Contact? {
        val db = readableDatabase
        val query = "select * from $TABLE_CONTACTS where $CONTACT_ID = ? ;"
        val whereArgs = arrayOf("$id")
        val cursor = db.rawQuery(query, whereArgs)
        if (cursor.moveToFirst()) {
            val column_ID = cursor.getColumnIndexOrThrow(CONTACT_ID)
            val column_firstName = cursor.getColumnIndexOrThrow(CONTACT_FIRSTNAME)
            val column_lastName = cursor.getColumnIndexOrThrow(CONTACT_LASTNAME)
            val column_phone = cursor.getColumnIndexOrThrow(CONTACT_PHONE)
            val column_address = cursor.getColumnIndexOrThrow(CONTACT_ADDRESS)
            val column_city = cursor.getColumnIndexOrThrow(CONTACT_CITY)
            val column_photo = cursor.getColumnIndexOrThrow(CONTACT_PHOTO)
            val column_favorite = cursor.getColumnIndexOrThrow(CONTACT_FAVORITE)
            val column_addedAt = cursor.getColumnIndexOrThrow(CONTACT_ADDED_AT)

            val id = cursor.getIntOrNull(column_ID)
            val firstName = cursor.getString(column_firstName)
            val lastName = cursor.getStringOrNull(column_lastName)
            val phone = cursor.getString(column_phone)
            val address = cursor.getStringOrNull(column_address)
            val city = cursor.getStringOrNull(column_city)
            val photoByteArray = cursor.getBlob(column_photo)
            val photo = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)
            val favoriteInt = cursor.getInt(column_favorite)
            val fav = favoriteInt != 0
            val addedAt = cursor.getString(column_addedAt)
            return Contact(id, firstName, lastName, phone, photo, address, city, fav, addedAt)
        } else {
            Log.e("msg", "No Data found get One Contact")
            return null
        }
    }

    fun getContacts(): LinkedList<Contact> {
        val db = readableDatabase
        val query = "select * from $TABLE_CONTACTS order by $CONTACT_FIRSTNAME ;"
        val cursor = db.rawQuery(query, null)
        return loopThrowTheResult(cursor)
    }

    fun searchByName(inputName: String): LinkedList<Contact> {
        val db = readableDatabase
        val query =
            "select * from $TABLE_CONTACTS where $CONTACT_FIRSTNAME like ? or $CONTACT_LASTNAME like ? order by $CONTACT_FIRSTNAME;"
        val selectionArgs = arrayOf("%$inputName%", "%$inputName%")
        val cursor = db.rawQuery(query, selectionArgs)
        return loopThrowTheResult(cursor)
    }

    fun searchByPhoneNumber(phone: String): LinkedList<Contact> {
        val db = readableDatabase
        val query =
            "select * from $TABLE_CONTACTS where $CONTACT_PHONE like ? order by $CONTACT_FIRSTNAME;"
        val selectionArgs = arrayOf("%$phone%")
        val cursor = db.rawQuery(query, selectionArgs)
        return loopThrowTheResult(cursor)
    }

    fun updateFavorite(contact: Contact): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(CONTACT_FAVORITE, contact.favorite)
        val success = db.update(TABLE_CONTACTS, cv, "$CONTACT_ID = ?", arrayOf("${contact.id}"))
        return success > 0
    }

    private fun loopThrowTheResult(cursor: Cursor): LinkedList<Contact> {
        val list = LinkedList<Contact>()
        if (cursor.moveToFirst()) {
            val column_ID = cursor.getColumnIndexOrThrow(CONTACT_ID)
            val column_firstName = cursor.getColumnIndexOrThrow(CONTACT_FIRSTNAME)
            val column_lastName = cursor.getColumnIndexOrThrow(CONTACT_LASTNAME)
            val column_phone = cursor.getColumnIndexOrThrow(CONTACT_PHONE)
            val column_address = cursor.getColumnIndexOrThrow(CONTACT_ADDRESS)
            val column_city = cursor.getColumnIndexOrThrow(CONTACT_CITY)
            val column_photo = cursor.getColumnIndexOrThrow(CONTACT_PHOTO)
            val column_favorite = cursor.getColumnIndexOrThrow(CONTACT_FAVORITE)
            val cursor_addedAt = cursor.getColumnIndexOrThrow(CONTACT_ADDED_AT)
            do {
                val id = cursor.getIntOrNull(column_ID)
                val firstName = cursor.getString(column_firstName)
                val lastName = cursor.getStringOrNull(column_lastName)
                val phone = cursor.getString(column_phone)
                val address = cursor.getStringOrNull(column_address)
                val city = cursor.getStringOrNull(column_city)
                val photoByteArray = cursor.getBlob(column_photo)
                val photo = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)
                val favoriteInt = cursor.getInt(column_favorite)
                val fav = favoriteInt != 0
                val addedAt = cursor.getString(cursor_addedAt)
                val contact =
                    Contact(id, firstName, lastName, phone, photo, address, city, fav, addedAt)
                list.add(contact)
            } while (cursor.moveToNext())
        } else {
            Log.e("msg", "No Data Found looper")
        }
        return list
    }

    fun getFavorites(): List<Contact> {
        val db = readableDatabase
        val query = "select * from $TABLE_CONTACTS where $CONTACT_FAVORITE = 1"
        val cursor = db.rawQuery(query, null)
        return loopThrowTheResult(cursor)
    }

    fun addRecentContact(recentContact: RecentContact): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        val cvImg = ContentValues()
        cv.put(CALLOG_DATE, recentContact.date)
        cv.put(CALLOG_TYPE, recentContact.type)
        cv.put(CALLOG_PHONE, recentContact.phone)

        if (recentContact.fullName != null)
            cv.put(CALLOG_FULLNAME, recentContact.fullName)
        cvImg.put(IMAGE_ID, recentContact.phone)
        if (getImage(recentContact.phone!!) == null) {// if this phone number doesn't exist
            val stream = ByteArrayOutputStream()
            // sure recentContact here has a photo cuz of fragment check
            recentContact.img!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            cvImg.put(IMAGE_DATA, stream.toByteArray())
            //}
        }
        val resImg = db.insert(TABLE_IMAGE, null, cvImg)
        val resCallog = db.insert(TABLE_CALLOG, null, cv)
        //db.close()
        return resCallog > 0 && resImg > 0
    }

    fun getRecentContacts(): LinkedList<RecentContact> {
        val listRecentContacts = LinkedList<RecentContact>()
        val query = "SELECT * FROM $TABLE_CALLOG order by $CALLOG_DATE DESC;"
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val column_phone = cursor.getColumnIndexOrThrow(CALLOG_PHONE)
            val column_date = cursor.getColumnIndexOrThrow(CALLOG_DATE)
            val column_type = cursor.getColumnIndexOrThrow(CALLOG_TYPE)
            val column_fullName = cursor.getColumnIndexOrThrow(CALLOG_FULLNAME)
            val column_id = cursor.getColumnIndexOrThrow(CALLOG_ID)
            do {
                val id = cursor.getInt(column_id)
                val phone = cursor.getString(column_phone)
                val date = cursor.getLong(column_date)
                val type = cursor.getInt(column_type)
                val img = getImage(phone)
                val fullName = cursor.getString(column_fullName)
                val recentContact = RecentContact(fullName, phone, type, date, img, id)
                listRecentContacts.add(recentContact)
            } while (cursor.moveToNext())
        } else {
            Log.e("msg", "No Logs Found get recent contacts")
        }
        return listRecentContacts
    }

    fun getImage(phone: String): Bitmap? {
        val db = readableDatabase
        val query = "select $IMAGE_DATA from $TABLE_IMAGE where $IMAGE_ID = ? ;"
        val whereArgs = arrayOf(phone)
        val cursor = db.rawQuery(query, whereArgs)
        var img: Bitmap? = null
        if (cursor.moveToFirst()) {
            val column_data = cursor.getColumnIndexOrThrow(IMAGE_DATA)
            val bytes = cursor.getBlob(column_data)
            img = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        return img
    }

    fun getNotFavorites(): LinkedList<Contact> {
        val db = readableDatabase
        val query = "select * from $TABLE_CONTACTS where $CONTACT_FAVORITE = 0"
        val cursor = db.rawQuery(query, null)
        return loopThrowTheResult(cursor)
    }

    fun getLastRecentCaller(): Long {
        val query = "select $CALLOG_DATE from $TABLE_CALLOG order by $CALLOG_DATE DESC limit 1 ;"
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val column_date = cursor.getColumnIndexOrThrow(CALLOG_DATE)
            val date = cursor.getLong(column_date)
            return date
        }
        return 0
    }

    fun getOneRecentContact(rowid: Int): RecentContact? {
        val db = readableDatabase
        val query = "select * from $TABLE_CALLOG where rowid = ? ;"
        val cursor = db.rawQuery(query, arrayOf("$rowid"))
        if (cursor.moveToFirst()) {
            val fullName = cursor.getString(cursor.getColumnIndexOrThrow(CALLOG_FULLNAME))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(CALLOG_PHONE))
            val type = cursor.getInt(cursor.getColumnIndexOrThrow(CALLOG_TYPE))
            val date = cursor.getLong(cursor.getColumnIndexOrThrow(CALLOG_DATE))
            val id = cursor.getString(cursor.getColumnIndexOrThrow(CALLOG_ID))
            val img = getImage(phone)
            return RecentContact(fullName, phone, type, date, img, rowid)
        }
        return null
    }

}