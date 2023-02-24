package owner.yacer.contactapp.Activities

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.text.TextPaint
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_create_new_contact_activty.*
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.shuffle

const val REQUEST_CODE = 1

class CreateNewContactActivity : AppCompatActivity() {
    lateinit var dbHelper: DbHelper
    var imageBitmap:Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_contact_activty)
        dbHelper = DbHelper(this)


        createAct_btn_save.setOnClickListener {
            var address:String? = null
            var city:String? = null
            if(createAct_et_contactFirstName.editText!!.text.toString().trim().isEmpty()){
                createAct_et_contactFirstName.error = "Enter the first name"
                createAct_et_contactFirstName.requestFocus()
                return@setOnClickListener
            }
            if(createAct_et_contactPhone.editText!!.text.toString().trim().isEmpty() ){
                createAct_et_contactPhone.error = "Enter the phone number"
                createAct_et_contactPhone.requestFocus()
                return@setOnClickListener
            }
            if(createAct_et_contactPhone.editText!!.text.toString().toDoubleOrNull() == null ){
                createAct_et_contactPhone.error = "Enter a valid phone number"
                createAct_et_contactPhone.requestFocus()
                return@setOnClickListener
            }
            if(createAct_et_contactAddress.editText!!.text.toString().isNotEmpty()){
                address = createAct_et_contactAddress.editText!!.text.toString()
            }
            if(createAct_et_contactCity.editText!!.text.toString().isNotEmpty()){
                city = createAct_et_contactCity.editText!!.text.toString()
            }
            val firstName = createAct_et_contactFirstName.editText!!.text.toString().trim()
            val lastName = createAct_et_contactLastName.editText!!.text.toString().trim()
            val phone  = createAct_et_contactPhone.editText!!.text.toString().trim()

            if(imageBitmap==null){
                imageBitmap = generateProfilePicture(firstName)
            }
            val date = Date()
            val format = SimpleDateFormat("MMM, d yyyy", Locale.getDefault())
            val addedAt = format.format(date)
            val contact = Contact(-1,firstName,lastName,phone,imageBitmap,address, city, addedAt = addedAt)
            val success = dbHelper.addContact(contact)
            if(success){
                Toast.makeText(this,"Contact added successfully",Toast.LENGTH_SHORT).show()
                finish()
            }else{
                Toast.makeText(this,"something went wrong",Toast.LENGTH_SHORT).show()
            }

        }

        createAct_selectPicture.setOnClickListener {
            openGallery()
        }

        createAct_btn_cancel.setOnClickListener {
            finish()
        }

    }

    private fun generateProfilePicture(name: String): Bitmap {
        val letter = name[0].toString().toUpperCase()
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // choose random color
        val colors = listOf("#ffbe0b", "#fb5607", "#ff006e", "#8338ec", "#3a86ff","#90e0ef","#ffb4a2",
            "#168aad","#8ac926")
        shuffle(colors)
        val random = Random()
        val randomColor = colors[random.nextInt(colors.size)]
        // Draw background color
        canvas.drawColor(Color.parseColor(randomColor))
        // Draw first letter
        val textPaint = TextPaint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 135f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textAlign = Paint.Align.CENTER
        val x = (canvas.width) / 2.0f
        val y = (canvas.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(letter, x, y, textPaint)
        return bitmap
    }

    private fun openGallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_CODE && resultCode == RESULT_OK && data!=null){
            val selectedImageUri = data.data!!
            Glide.with(this).load(selectedImageUri).into(createAct_selectPicture)
            val inputStream = contentResolver.openInputStream(selectedImageUri) // open an input stream to the Uri
            imageBitmap = BitmapFactory.decodeStream(inputStream) // decode the input stream to a bitmap
        }
    }
}