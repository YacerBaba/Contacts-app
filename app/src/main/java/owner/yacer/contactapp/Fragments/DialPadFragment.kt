package owner.yacer.contactapp.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_dial_pad.*
import kotlinx.android.synthetic.main.fragment_dial_pad.view.*
import owner.yacer.contactapp.Activities.MainActivity
import owner.yacer.contactapp.R


class DialPadFragment : Fragment(R.layout.fragment_dial_pad) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dial_btn_call.setOnClickListener {
            if (editTextTextPersonName.editText!!.text.toString().isNotEmpty()) {
                val number = editTextTextPersonName.editText!!.text.toString()
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                requireContext().startActivity(intent)
            }
        }
        button.setOnClickListener {
            onClick(it)
        }
        button2.setOnClickListener {
            onClick(it)
        }
        button3.setOnClickListener {
            onClick(it)
        }
        button4.setOnClickListener {
            onClick(it)
        }
        button5.setOnClickListener {
            onClick(it)
        }
        button6.setOnClickListener {
            onClick(it)
        }
        button7.setOnClickListener {
            onClick(it)
        }
        button8.setOnClickListener {
            onClick(it)
        }
        button9.setOnClickListener {
            onClick(it)
        }
        button10.setOnClickListener {
            onClick(it)
        }
        button11.setOnClickListener {
            onClick(it)
        }
        button12.setOnClickListener {
            onClick(it)
        }


    }

    private fun onClick(view: View) {
        val btn = (view as Button)
        editTextTextPersonName.editText!!.append(btn.text)
    }
}