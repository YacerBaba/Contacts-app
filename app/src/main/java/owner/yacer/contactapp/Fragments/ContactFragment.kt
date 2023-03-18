package owner.yacer.contactapp.Fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import owner.yacer.contactapp.Activities.CreateNewContactActivity
import owner.yacer.contactapp.Activities.MainActivity
import owner.yacer.contactapp.Models.CALL_REQUEST_CODE
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.ContactAdapter
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.R
import java.util.*
import kotlin.system.measureTimeMillis


class ContactFragment : Fragment(R.layout.fragment_contact) {
    lateinit var adapter: ContactAdapter
    lateinit var dbHelper: DbHelper
    lateinit var observer: RecyclerView.AdapterDataObserver
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DbHelper(requireContext())

        setUpContactRecyclerView()
        floatingActionButton.setOnClickListener {
            val fragment = DialPadFragment()
            MainActivity.currentFragment = fragment
            requireFragmentManager().beginTransaction().apply {
                addToBackStack(ContactFragment::class.java.name)
                replace(R.id.fl_fragment, fragment)
                commit()
                (activity as MainActivity).bottom_app_bar.visibility = View.GONE
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val number = newText!!.toString().toDoubleOrNull()
                if (number == null) {
                    val filteredList = dbHelper.searchByName(newText)
                    adapter.setFilteredList(filteredList)
                } else {
                    val filteredList = dbHelper.searchByPhoneNumber(newText)
                    adapter.setFilteredList(filteredList)
                }
                return true
            }

        })


        layout_addContact.setOnClickListener {
            Intent(context, CreateNewContactActivity::class.java).also {
                it.putExtra("fragment", 0)
                startActivity(it)
                (context as Activity).overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
        }
    }

    private fun setUpContactRecyclerView() {
        CoroutineScope(Dispatchers.IO).launch {
            val listContacts: LinkedList<Contact>
            val time = measureTimeMillis {
                listContacts = dbHelper.getContacts()
            }
            Log.e("msgTimeContact", "$time")
            withContext(Dispatchers.Main) {
                adapter = ContactAdapter(requireContext(), listContacts)
                layout_noContacts.visibility = if(adapter.itemCount ==0) View.VISIBLE else View.GONE
                observer = object : RecyclerView.AdapterDataObserver(){
                    override fun onChanged() {
                        super.onChanged()
                        val count = adapter.itemCount
                        layout_noContacts.visibility = if(count ==0) View.VISIBLE else View.GONE
                    }
                }
                adapter.registerAdapterDataObserver(observer)
                val layoutManager = LinearLayoutManager(requireContext())
                main_rv_contacts.adapter = adapter
                main_rv_contacts.layoutManager = layoutManager
            }
        }


    }

    override fun onResume() {
        super.onResume()
        setUpContactRecyclerView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALL_REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

        }
    }
}