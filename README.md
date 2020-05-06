# Our calendar

First version of calendar application that gives possibility to add an events. Main function of the application is access to the shared
calendar and to added events. All events added by users are stored on Firebase Realtime Database. Thanks to that solution, every user 
has an attempt to see, add or remove events on real time. All users using the same login have access to the shared calendar. Events added by one of the users will be available to all users.

#Sources:
- Firebase Realtime Database: https://firebase.google.com
- calendar library: https://github.com/tizisdeepan/EventsCalendar
- carousel picker library: https://github.com/GoodieBag/CarouselPicker

# Main screen:
This is the screen, where user can log in to application. Application check validation of email and password and if it's not in database, 
show proper message (second screen). New user can register theirs email by clicking the text below login button. Then user go next screen, where he can register (third screen). 


<img src="https://i.ibb.co/ChTjxdn/screen-1.png" width=250>  <img src="https://i.ibb.co/W682Y0R/screen-2.png" width=250>  <img src="https://i.ibb.co/nzfzK8C/screen-3.png" height=500 width=250>


After proper register user is automatically moved to screen with calendar.
User have to login only for the first time. Until he logout, every lunch application will moved user to calendar screen (forth screen).

# Calendar screen
This is the screen, where is the main calendar (forth screen). User can selected day and swipe left or right to change the month. 
Dots on calendars represents added events.


<img src="https://i.ibb.co/2cgPWK1/screen-4.png" width=250>  <img src="https://i.ibb.co/0QqmY8G/screen-5.png" width=250>  <img src="https://i.ibb.co/s3JstLG/screen-6.png" width=250>

When user long press day with no dot, it will open dialog, where he can add an event on that day (fifth screen). 
User can chose: evet by horizontal swipe carousel picker and time by vertical swipe begin and end time pickers.

When user press day with dot, uder the calendar he will see all events on picked day (sixth screen). At this screen user can also remove each event.
