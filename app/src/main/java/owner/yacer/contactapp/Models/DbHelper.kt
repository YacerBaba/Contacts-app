package owner.yacer.contactapp.Models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import java.io.ByteArrayOutputStream
import java.util.*



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

class DbHelper(private val context: Context) :
    SQLiteOpenHelper(context, "contactsApp.db", null,  2) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createQuery =
            "create table $TABLE_CONTACTS ($CONTACT_ID integer primary key autoincrement,$CONTACT_FIRSTNAME text" +
                    ",$CONTACT_LASTNAME text not null,$CONTACT_PHONE text not null,$CONTACT_ADDRESS text" +
                    ",$CONTACT_CITY text,$CONTACT_PHOTO blob not null ,$CONTACT_FAVORITE boolean not null" +
                    ",$CONTACT_ADDED_AT text not null );"
        db!!.execSQL(createQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.needUpgrade(newVersion)
        if(oldVersion<newVersion){
            db?.execSQL("alter table $TABLE_CONTACTS add column $CONTACT_ADDED_AT text default ''")
        }
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
        cv.put(CONTACT_FAVORITE,contact.favorite)
        cv.put(CONTACT_ADDED_AT,contact.addedAt)
        val result = db.insert(TABLE_CONTACTS, null, cv)
        db.close()
        return result > 0
    }

    fun deleteContact(contact: Contact): Boolean {
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $TABLE_CONTACTS where $CONTACT_ID = ?;"
        val cursor = db.rawQuery(deleteQuery, arrayOf("${contact.id}"))
        val result = cursor.moveToFirst()
        cursor.close()
        db.close()
        return result
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
        cv.put(CONTACT_FAVORITE,contact.favorite)
        val result = db.update(TABLE_CONTACTS, cv, "$CONTACT_ID = ?", arrayOf("${contact.id}"))
        return result > 0
    }
    fun getOneContact(id:Int):Contact?{
        val db = readableDatabase
        val query = "select * from $TABLE_CONTACTS where $CONTACT_ID = ? ;"
        val whereArgs = arrayOf("$id")
        val cursor = db.rawQuery(query,whereArgs)
        if(cursor.moveToFirst()){
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
            return Contact(id, firstName, lastName, phone, photo, address, city,fav,addedAt)
        }else{
            Log.e("msg","No Data found")
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

    fun setFavorite(contact: Contact):Boolean{
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(CONTACT_FAVORITE,contact.favorite)
        val success = db.update(TABLE_CONTACTS, cv,"$CONTACT_ID = ?",arrayOf("${contact.id}"))
        return success > 0
    }

    private fun loopThrowTheResult(cursor: Cursor):LinkedList<Contact>{
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
                val contact = Contact(id, firstName, lastName, phone, photo, address, city,fav,addedAt)
                list.add(contact)
            } while (cursor.moveToNext())
        } else {
            Log.e("msg", "No Data Found")
        }
        return list
    }


}