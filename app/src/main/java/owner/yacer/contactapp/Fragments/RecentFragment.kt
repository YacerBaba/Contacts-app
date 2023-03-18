package owner.yacer.contactapp.Fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_recent.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import owner.yacer.contactapp.Models.*
import owner.yacer.contactapp.R
import java.util.*
import kotlin.system.measureTimeMillis


class RecentFragment : Fragment(R.layout.fragment_recent) {
    lateinit var dbHelper: DbHelper
    lateinit var contactObserver: ContactObserver

    companion object {
        lateinit var recentAdapter: RecentAdapter
        lateinit var spLast: SharedPreferences
        val random = Random()
        val listIcons = intArrayOf(
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
    }

    lateinit var listRecent: LinkedList<RecentContact>
    private var lastDateDB = 0L
    lateinit var sharedPref: SharedPreferences
    lateinit var progressBox: ProgressDialog
    lateinit var observer: RecyclerView.AdapterDataObserver

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spLast = requireContext().getSharedPreferences("lastPhoneCall", Context.MODE_PRIVATE)
        progressBox = ProgressDialog(requireContext())
        with(progressBox) {
            setMessage("Loading Contacts ...")
            setCancelable(false)
        }
        dbHelper = DbHelper(requireContext())

        sharedPref = requireContext().getSharedPreferences("mySharedPref", Context.MODE_PRIVATE)
        listRecent = LinkedList<RecentContact>()
        progressBox.show()
        Log.e("msgVisible", "${progressBox.isShowing}")
        if (hasReadCallLogPermission()) {
            CoroutineScope(Dispatchers.IO).launch {
                getData()
                Log.e("msg", "this will be first")
                withContext(Dispatchers.Main) {
                    setupRecentRv()
                    if (!EnterAppFirstTime.observerInit) {
                        contactObserver = ContactObserver(requireContext())
                        requireActivity().contentResolver.registerContentObserver(
                            CallLog.Calls.CONTENT_URI, true, contactObserver
                        )
                        EnterAppFirstTime.observerInit = true
                    }
                    progressBox.dismiss()
                }
            }
        } else {
            requestPermission()
            progressBox.dismiss()
        }

        recent_searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    val number = newText!!.toString().toDoubleOrNull()
                    val list = LinkedList<RecentContact>()
                    if (number == null) {
                        for (i in listRecent.indices) {
                            if (listRecent[i].fullName.toString().lowercase()
                                    .contains(newText.lowercase())
                            ) {
                                list.add(listRecent[i])
                            }
                        }
                        recentAdapter.setFilteredList(list)
                    } else {
                        for (i in listRecent.indices) {
                            if (listRecent[i].phone!!.contains(newText)) {
                                list.add(listRecent[i])
                            }
                        }
                        recentAdapter.setFilteredList(list)
                    }
                    return true
                }

            })

    }


    private fun needUpdate(): Boolean {
        val search = requireContext().contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.DATE),
            null,
            null,
            "${CallLog.Calls.DATE} DESC LIMIT 1"
        )
        search?.moveToFirst()
        val column_date = search?.getColumnIndexOrThrow(CallLog.Calls.DATE)
        val lastDate = search?.getLong(column_date!!)
        lastDateDB = dbHelper.getLastRecentCaller()
        return lastDate!! > lastDateDB
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getData() {
        if (sharedPref.getString("firstTime", "") == "yes") {
            // get data from device callog and save them to callog Table
            listRecent = getRecentCallsFromCallLog()
            for (recent in listRecent) {
                dbHelper.addRecentContact(recent)
            }
            sharedPref.edit().putString("firstTime", "no").apply()
        }
        if (EnterAppFirstTime.enteredApp) {
            Log.e("msgApp", "first time")
            val boolean = needUpdate()
            // update table CallLog
            if (boolean) {
                val listNotUpdatedItems = getRecentCallsFromCallLog(lastDateDB)
                for (recent in listNotUpdatedItems) {
                    dbHelper.addRecentContact(recent)
                }
            }
            EnterAppFirstTime.enteredApp = false
        }

        val time = measureTimeMillis {
            listRecent = dbHelper.getRecentContacts()
        }
        Log.e("msgTime", "$time")
        Log.e("msgList", listRecent.toString())
        Log.e("msg", "before rv")
    }

    private fun hasReadCallLogPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.READ_CALL_LOG), 1
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)

    private fun getRecentCallsFromCallLog(lastTime: Long = 0L): LinkedList<RecentContact> {
        val listRecent = LinkedList<RecentContact>()
        requireActivity().contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
            ),
            if (lastTime != 0L) "${CallLog.Calls.DATE} > $lastTime" else null,
            null,
            CallLog.Calls.DATE + " DESC" + " limit 10"
        )?.use { cursor ->
            val column_phone = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val column_type = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val column_date = cursor.getColumnIndex(CallLog.Calls.DATE)

            if (cursor.moveToFirst()) {
                val timeStmp = cursor.getLong(column_date)
                do {
                    val phone = cursor.getString(column_phone)
                    val type = cursor.getInt(column_type)
                    val time = cursor.getLong(column_date)
                    var imgBitmap: Bitmap? = null
                    if (dbHelper.getImage(phone) == null)
                        imgBitmap = BitmapFactory.decodeResource(
                            resources, listIcons[random.nextInt(listIcons.size - 1)]
                        )
                    val recentContact =
                        RecentContact(phone = phone, type = type, date = time, img = imgBitmap)

                    listRecent.add(recentContact)

                } while (cursor.moveToNext())
                Log.e("msg", "timestmp of last call : $timeStmp")
                spLast.edit().putLong("lastTime", timeStmp).apply()
            }
        }
        return listRecent
    }


    private fun setupRecentRv() {
        recentAdapter = RecentAdapter(requireContext(), listRecent, this)

        recent_rv_contacts.adapter = recentAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        recent_rv_contacts.layoutManager = layoutManager
        recentAdapter!!.notifyDataSetChanged()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // mark that the user gives us the permission to read the callLog
            progressBox.show()
            sharedPref.edit().apply {
                putString("firstTime", "yes")
                apply()
            }
            CoroutineScope(Dispatchers.IO).launch {
                getData()
                withContext(Dispatchers.Main) {
                    setupRecentRv()
                    if (!EnterAppFirstTime.observerInit) {
                        contactObserver = ContactObserver(requireContext())
                        requireActivity().contentResolver.registerContentObserver(
                            CallLog.Calls.CONTENT_URI, true, contactObserver
                        )
                        EnterAppFirstTime.observerInit = true
                    }
                    progressBox.dismiss()
                }

            }
        }

    }
}