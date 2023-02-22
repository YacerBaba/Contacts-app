package owner.yacer.contactapp.Models

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation


class Animations {
    companion object {
        fun toggleArrow(view: View, isExpanded: Boolean): Boolean {
            return if (isExpanded) {
                view.animate().setDuration(200).rotation(180F)
                true
            } else {
                view.animate().setDuration(200).rotation(0f)
                false
            }
        }

        fun expand(view: View,view1:View) {
            val animation = expandAction(view,view1)
            view.startAnimation(animation)
        }

        private fun expandAction(view: View,view1:View): Animation {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val actualheight = view.measuredHeight
            view.layoutParams.height = 0
            view.visibility = View.VISIBLE
            view1.visibility = View.VISIBLE
            val animation: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    view.layoutParams.height =
                        if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (actualheight * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }
            animation.duration = 150
            view.startAnimation(animation)
            return animation
        }

        fun collapse(view: View,view1:View) {
            val actualHeight = view.measuredHeight
            val animation: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    if (interpolatedTime == 1f) {
                        view.visibility = View.GONE
                        view1.visibility = View.GONE
                    } else {
                        view.layoutParams.height =
                            actualHeight - (actualHeight * interpolatedTime).toInt()
                        view.requestLayout()
                    }
                }
            }
            animation.duration =
                300
            view.startAnimation(animation)
        }
    }
}