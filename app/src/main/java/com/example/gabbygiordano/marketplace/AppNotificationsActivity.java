package com.example.gabbygiordano.marketplace;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AppNotificationsActivity extends AppCompatActivity {

    AppNotificationAdapter appNotificationAdapter;
    ArrayList<AppNotification> appNotifications;
    RecyclerView rvNotifications;

    BottomNavigationView bottomNavigationView;

    int ADD_ITEM_REQUEST = 10;

    Context mContext;
    Context context;

    Date lastNotif;

    Intent mServiceIntent;

    // Create a handler which can run code periodically
    static final int POLL_INTERVAL = 1000; // milliseconds
    Handler myHandler = new Handler();  // android.os.Handler
    Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            myHandler.postDelayed(this, POLL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        getSupportActionBar().setTitle("Notifications");

        context = this;

        lastNotif = new Date(0);
        Log.e("AppNotifications", lastNotif.toString());

        rvNotifications = (RecyclerView) findViewById(R.id.rvNotifications);
        appNotifications = new ArrayList<>();
        appNotificationAdapter = new AppNotificationAdapter(appNotifications, getApplicationContext());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvNotifications.setLayoutManager(linearLayoutManager);
        rvNotifications.setAdapter(appNotificationAdapter);
        rvNotifications.setHasFixedSize(true);

        MyDividerItemDecoration dividerItemDecoration = new MyDividerItemDecoration(rvNotifications.getContext());
        rvNotifications.addItemDecoration(dividerItemDecoration);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuitem = menu.getItem(2);
        menuitem.setChecked(true);

        mContext = ItemAdapter.getContext();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.action_home:
                        Intent i_home = new Intent(AppNotificationsActivity.this, HomeActivity.class);
                        startActivity(i_home);
                        finish();
                        break;

                    case R.id.action_maps:
                        Intent i_maps = new Intent(AppNotificationsActivity.this, MapsActivity.class);
                        startActivity(i_maps);
                        finish();
                        break;

                    case R.id.action_notifications:
                        Toast.makeText(AppNotificationsActivity.this, "Notifications Tab Selected", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.action_profile:
                        Intent i_profile = new Intent(AppNotificationsActivity.this, ProfileActivity.class);
                        startActivity(i_profile);
                        finish();
                        break;
                }

                return false;
            }
        });

        // make initial query
        ParseQuery<AppNotification> query = ParseQuery.getQuery(AppNotification.class);
        query.include("owner");
        query.include("buyer");
        query.include("item");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());
        query.orderByDescending("_created_at");
        query.findInBackground(new FindCallback<AppNotification>() {
            public void done(List<AppNotification> notificationsList, ParseException e) {
                if (e == null) {
                    if (notificationsList != null && !notificationsList.isEmpty()) {
                        lastNotif = (Date) notificationsList.get(0).get("date");

                        for (int i = 0; i < notificationsList.size(); i++) {
                            appNotifications.add(notificationsList.get(i));
                            appNotificationAdapter.notifyItemInserted(appNotifications.size()-1);
                        }
                    }
                } else {
                    Log.d("NotificationsActivity", e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_notifications, menu);
        MenuItem sortItem = menu.findItem(R.id.action_sort);

        sortItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                appNotifications.clear();
                appNotificationAdapter.notifyDataSetChanged();

                ParseQuery<AppNotification> query = ParseQuery.getQuery(AppNotification.class);
                query.include("owner");
                query.include("buyer");
                query.include("item");
                query.whereEqualTo("owner", ParseUser.getCurrentUser());
                query.orderByDescending("item_name");
                query.findInBackground(new FindCallback<AppNotification>() {
                    public void done(List<AppNotification> notificationsList, ParseException e) {
                        if (e == null) {
                            if (notificationsList != null && !notificationsList.isEmpty()) {
                                lastNotif = (Date) notificationsList.get(0).get("date");

                                if (notificationsList.size() > 0) {
                                    Collections.sort(notificationsList, new Comparator<AppNotification>() {
                                        @Override
                                        public int compare(final AppNotification notif1, final AppNotification notif2) {
                                            return notif1.getItem().getItemName().compareTo(notif2.getItem().getItemName());
                                        }
                                    });
                                }

                                for (int i = 0; i < notificationsList.size(); i++) {
                                    appNotifications.add(notificationsList.get(i));
                                    appNotificationAdapter.notifyItemInserted(appNotifications.size()-1);
                                }
                            }
                        } else {
                            Log.d("NotificationsActivity", e.getMessage());
                        }
                    }
                });
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    void refreshMessages() {
        // make the query
        ParseQuery<AppNotification> parseQuery = ParseQuery.getQuery(AppNotification.class);;

        parseQuery.include("owner");
        parseQuery.include("buyer");
        parseQuery.include("item");
        parseQuery.whereEqualTo("owner", ParseUser.getCurrentUser());
        parseQuery.whereGreaterThan("date", lastNotif);
        parseQuery.orderByDescending("_created_at");
        parseQuery.findInBackground(new FindCallback<AppNotification>() {
            public void done(List<AppNotification> notificationsList, ParseException e) {
                if (e == null) {
                    if (notificationsList != null && !notificationsList.isEmpty()) {
                        for (int i = 0; i < notificationsList.size(); i++) {
                            if (((Date) notificationsList.get(i).get("date")).after(lastNotif)) {
                                appNotifications.add(notificationsList.get(i));
                                appNotificationAdapter.notifyDataSetChanged();

                                // make push notification here
                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(context)
                                                .setSmallIcon(R.drawable.home_icon)
                                                .setContentTitle("New item request!")
                                                .setAutoCancel(true)
                                                .setContentText("Tap to view");

                                Intent resultIntent = new Intent(context, AppNotificationsActivity.class);
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                // Adds the back stack
                                stackBuilder.addParentStack(AppNotificationsActivity.class);
                                // Adds the Intent to the top of the stack
                                stackBuilder.addNextIntent(resultIntent);

                                mBuilder.setContentIntent(resultPendingIntent);

                                // Sets an ID for the notification
                                int mNotificationId = 1;
                                // Gets an instance of the NotificationManager service
                                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                // Builds the notification and issues it.
                                mNotifyMgr.notify(mNotificationId, mBuilder.build());
                            }
                        }
                        lastNotif = notificationsList.get(0).getCreatedAt();
                        Log.e("AppNotifications", lastNotif.toString());
                    }
                } else {
                    Log.d("AppNotifications", e.getMessage());
                }
            }
        });
    }

    public void addItem(View view) {
        Intent i_add = new Intent(context, AddItemActivity.class);
        ((HomeActivity) mContext).startActivityForResult(i_add, ADD_ITEM_REQUEST);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent i_home = new Intent(AppNotificationsActivity.this, HomeActivity.class);
        i_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i_home);
    }


}
