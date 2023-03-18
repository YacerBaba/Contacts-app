package owner.yacer.contactapp.Activities

import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import owner.yacer.contactapp.Fragments.ContactFragment
import owner.yacer.contactapp.Fragments.FavoriteFragment
import owner.yacer.contactapp.Fragments.RecentFragment
import owner.yacer.contactapp.R

class MainActivity : AppCompatActivity() {
    companion object{
        var currentFragment:Fragment? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contactFragment = ContactFragment()
        currentFragment = contactFragment
        if(!hasCallPhonePermission()){
            requestPermission()
        }
        setFragment(contactFragment)

        layout_contact.setOnClickListener {
            if(contactFragment.javaClass != currentFragment?.javaClass){
                setFragment(contactFragment)
                setDefaultBg()
                main_contact_bg.background  =ContextCompat.getDrawable(this,R.drawable.bottom_app_focused)
                main_tv_contact.typeface = Typeface.DEFAULT_BOLD
                currentFragment = contactFragment
            }
        }
        layout_recent.setOnClickListener {
            val recentFragment = RecentFragment()
            if(recentFragment.javaClass != currentFragment?.javaClass){
                setFragment(recentFragment)
                setDefaultBg()
                main_recent_bg.background  =ContextCompat.getDrawable(this,R.drawable.bottom_app_focused)
                main_tv_recent.typeface = Typeface.DEFAULT_BOLD
                currentFragment = recentFragment
            }
        }


        layout_favorite.setOnClickListener {
            val favoriteFragment = FavoriteFragment()
            if(favoriteFragment.javaClass != currentFragment?.javaClass){
                setFragment(favoriteFragment)
                setDefaultBg()
                main_fav_bg.background  =ContextCompat.getDrawable(this,R.drawable.bottom_app_focused)
                main_tv_favorite.typeface = Typeface.DEFAULT_BOLD
                currentFragment = favoriteFragment
            }
        }
    }

    private fun setDefaultBg() {
        main_contact_bg.setBackgroundColor(ContextCompat.getColor(this,R.color.bottom_color))
        main_tv_contact.typeface = Typeface.DEFAULT
        main_recent_bg.setBackgroundColor(ContextCompat.getColor(this,R.color.bottom_color))
        main_tv_recent.typeface = Typeface.DEFAULT
        main_fav_bg.setBackgroundColor(ContextCompat.getColor(this,R.color.bottom_color))
        main_tv_favorite.typeface = Typeface.DEFAULT
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_fragment, fragment)
            commit()
        }
    }

    private fun hasCallPhonePermission():Boolean{
        return ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CALL_PHONE), 101
        )
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack();
            bottom_app_bar.visibility = View.VISIBLE
            return
        }
        super.onBackPressed()
    }
}