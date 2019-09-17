package com.sharedcalendar.ui

import `in`.goodiebag.carouselpicker.CarouselPicker
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.events.calendar.views.EventsCalendar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.adapters.RecyclerViewAdapter
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.EventsEvidence
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.checkInternetConnection
import com.sharedcalendar.utility.convertToday
import com.sharedcalendar.utility.toggleVisibility
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.enter_event_data_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity(), EventsCalendar.Callback {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var valueEventListener: ValueEventListener? = null
    private lateinit var loggedUser: String
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var imageCarousel: CarouselPicker
    private var eventsEvidence = EventsEvidence()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_calendar)
        runFirebase()
        checkLoggedUser()
        startActionBar(loggedUser)
        runRecyclerViewAdapter()
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {
        val pickedDate = selectedDate!!.time.convertToday()
        recycler_view_id.visibility = View.VISIBLE
        startVibration()
        startEnterDialog(pickedDate)
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        recycler_view_id.visibility = View.GONE
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        val pickedDate = selectedDate!!.time.convertToday()
        recycler_view_id.visibility = View.VISIBLE
        eventDetails(pickedDate)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            logoutUser()
            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onStart() {
        progressbar_calendar_activity.visibility = View.VISIBLE
        super.onStart()
        if (checkInternetConnection()) {
            val calendarListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val listOfCalendarDate =
                        dataSnapshot.child(Statics.FIREBASE_DATE).children.mapNotNull {
                            it.getValue<CalendarDate>(CalendarDate::class.java)
                        }
                    runCalendar()

                    val cal = Calendar.getInstance()

                    listOfCalendarDate.forEach {
                        cal.set(
                            it.date?.split("-")!![0].toInt(),
                            it.date?.split("-")!![1].toInt() - 1,
                            it.date?.split("-")!![2].toInt()
                        )
                        events_calendar_id.addEvent(cal)
                    }
                    progressbar_calendar_activity.visibility = View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        baseContext, getString(R.string.on_cancelled_toast_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            databaseReference.addValueEventListener(calendarListener)
            this.valueEventListener = calendarListener
        }
    }

    private fun runCalendar() {
        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        events_calendar_id.setSelectionMode(events_calendar_id.MULTIPLE_SELECTION)
            .setToday(today)
            .setMonthRange(today, end)
            .setWeekStartDay(Calendar.MONDAY, false)
            .setCurrentSelectedDate(today)
            .setIsBoldTextOnSelectionEnabled(true)
            .setWeekHeaderTypeface(Typeface.DEFAULT_BOLD)
            .setCallback(this)
            .build()
        calendar_background_id.visibility = View.VISIBLE
    }

    private fun startActionBar(string: String) {
        supportActionBar?.also {
            title = string
            it.setDisplayHomeAsUpEnabled(false)
            it.setDisplayHomeAsUpEnabled(false)
            it.setLogo(R.drawable.logout)
            it.setDisplayUseLogoEnabled(false)
        }
    }

    private fun runFirebase() {
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    private fun runRecyclerViewAdapter() {
        recyclerViewAdapter = RecyclerViewAdapter()
        recycler_view_id.adapter = recyclerViewAdapter
    }

    private fun logoutUser() {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun checkLoggedUser() {
        if (auth.currentUser != null) {
            loggedUser = " ${auth.currentUser?.email}"
        }
    }

    private fun eventDetails(calendar: String) {
        val calendarListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listOfCalendarDate =
                    dataSnapshot.child(Statics.FIREBASE_DATE).children.mapNotNull {
                        it.getValue<CalendarDate>(CalendarDate::class.java)
                    }
                val filteredList = listOfCalendarDate.filter { it.date!! == calendar }

                recyclerViewAdapter.updateItemList(filteredList)
                recyclerViewAdapter.selectedItem = {
                    removeEvent(it.id.toString())
                }
                recyclerViewAdapter.notifyDataSetChanged()
                runLayoutAnimation()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    baseContext, getString(R.string.on_cancelled_toast_text),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        progressbar_calendar_activity.visibility = View.GONE
        databaseReference.addValueEventListener(calendarListener)
        this.valueEventListener = calendarListener
    }

    private fun saveDate() {
        val calendarDate = CalendarDate.create()
        calendarDate.date = eventsEvidence.date
        if (eventsEvidence.time.isNullOrEmpty()) {
            calendarDate.time = getString(R.string.time_all_day)
        } else {
            calendarDate.time = eventsEvidence.time
            eventsEvidence.time = ""
        }

        if (eventsEvidence.event.isNullOrEmpty()) {
            calendarDate.event = getString(R.string.event_duty)
        } else {
            calendarDate.event = eventsEvidence.event
            eventsEvidence.event = ""
        }

        val newDate = databaseReference.child(Statics.FIREBASE_DATE).push()
        calendarDate.id = newDate.key
        newDate.setValue(calendarDate)
        StyleableToast.makeText(
            applicationContext,
            getString(R.string.add_event_text),
            Toast.LENGTH_LONG,
            R.style.myToastAdd
        ).show()

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 1)
        events_calendar_id.addEvent(cal)
        events_calendar_id.setSelectionMode(events_calendar_id.SINGLE_SELECTION)
    }

    private fun removeEvent(eventId: String) {
        val task = databaseReference.child(Statics.FIREBASE_DATE).child(eventId)
        task.removeValue()
        events_calendar_id.setSelectionMode(events_calendar_id.SINGLE_SELECTION)
        StyleableToast.makeText(
            applicationContext,
            getString(R.string.remove_event_text),
            Toast.LENGTH_LONG,
            R.style.myToastRemove
        ).show()
    }

    private fun startEnterDialog(calendar: String) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.enter_event_data_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        val mAlertDialog = mBuilder.create()
        imageCarousel = mDialogView.carousel_dialog_id
        createListOfCarousel(imageCarousel)
        mAlertDialog.run {
            imageCarousel = mDialogView.carousel_dialog_id
            createListOfCarousel(imageCarousel)
            show()
            window.setLayout(650, 920)
        }

        mDialogView.enter_dialog_data_id.text = calendar
        eventsEvidence.date = calendar

        mDialogView.time_picker_dialog_id.setOnTimeChangedListener { view, hour, minute ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            eventsEvidence.time = SimpleDateFormat("HH:mm").format(cal.time)
        }

        mDialogView.enter_dialog_cancel_button_id.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mDialogView.carousel_dialog_id.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> eventsEvidence.event = getString(R.string.add_event_text)
                    1 -> eventsEvidence.event = getString(R.string.event_clinic_text)
                    2 -> eventsEvidence.event = getString(R.string.event_visit_text)
                    3 -> eventsEvidence.event = getString(R.string.event_grafting_text)
                    4 -> eventsEvidence.event = getString(R.string.event_other_text)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        mDialogView.dialog_ok_button_id.setOnClickListener {
            mAlertDialog.dismiss()
            saveDate()
            progressbar_calendar_activity.toggleVisibility()
        }
    }

    private fun createListOfCarousel(imageCarousel: CarouselPicker) {
        val imageItems = ArrayList<CarouselPicker.PickerItem>()
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.duty))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.clinic))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.visit))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.grafting))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.other))
        val imageAdapter = CarouselPicker.CarouselViewAdapter(this, imageItems, 0)
        imageCarousel.adapter = imageAdapter as PagerAdapter?
    }

    private fun runLayoutAnimation() {
        val context = recycler_view_id.context
        val controller =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        recycler_view_id.layoutAnimation = controller
        recycler_view_id.adapter!!.notifyDataSetChanged()
        recycler_view_id.scheduleLayoutAnimation()
    }

    private fun startVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        300,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(300)
            }
        }
    }
}