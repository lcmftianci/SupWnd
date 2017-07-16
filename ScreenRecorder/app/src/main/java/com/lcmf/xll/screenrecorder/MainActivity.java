package com.lcmf.xll.screenrecorder;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.lcmf.xll.screenrecorder.SuspendWindow.SuspendWindowService;
import com.lcmf.xll.screenrecorder.ViewFragment.InnerFragment;

import static com.lcmf.xll.screenrecorder.ViewFragment.InnerFragment.IsVisible;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	//主页内容成员
	FragmentManager manager;
	FragmentTransaction transaction;
	InnerFragment fragment1;

	//通知栏按钮成员
	public final static String ACTION_BTN = "com.lcmf.xll.btn.screenrecorder.start";
	public final static String INTENT_NAME = "btnid";
	public final static int INTENT_BTN_START = 1;
	public final static int INTENT_BTN_PAUSE = 2;
	public final static int INTENT_BTN_STOP = 3;
	NotificationBroadcastReceiver mReceiver;

	private TextView mTextMessage;

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.navigation_home:
					mTextMessage.setText(R.string.title_home);
					if(fragment1 != null){
//						transaction.remove(fragment1);
						transaction = manager.beginTransaction();
						transaction.hide(fragment1);
						IsVisible = 0;
						transaction.commit();
					}
					else
					{
						manager = getSupportFragmentManager();
						transaction = manager.beginTransaction();
						fragment1 = new InnerFragment();
						transaction.add(R.id.content, fragment1);
						transaction.remove(fragment1);
						IsVisible = 0;
						transaction.commit();
					}
					return true;
				case R.id.navigation_dashboard:
					mTextMessage.setText(R.string.title_dashboard);
					if(IsVisible == 0){
						manager = getSupportFragmentManager();
						transaction = manager.beginTransaction();
						fragment1 = new InnerFragment();
						transaction.add(R.id.content, fragment1);
						IsVisible = 1;
						transaction.commit();
					}
					return true;
			}
			return false;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		mTextMessage = (TextView) findViewById(R.id.message);
		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		//通知栏显示
		notification();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			// 使用api11 新加 api
			requestDrawOverLays();
		}
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_camera) {
			// Handle the camera action
			showTips("已打开悬浮窗");
			Intent intent = new Intent(MainActivity.this, SuspendWindowService.class);
			startService(intent);
//			finish();
		} else if (id == R.id.nav_gallery) {
			showTips("gallery");
		} else if (id == R.id.nav_slideshow) {

		} else if (id == R.id.nav_manage) {

		} else if (id == R.id.nav_share) {

		} else if (id == R.id.nav_send) {
			showTips("send");
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	public void showTips(String strInfo){
		Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_SHORT).show();
	}

	/*
	* 通知栏函数集合
	* */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregeisterReceiver();
	}

	private void notification() {
		unregeisterReceiver();
		intiReceiver();

		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
		remoteViews.setTextViewText(R.id.tv_up, "首都机场精品无线");
		remoteViews.setTextViewText(R.id.tv_down, "已免费接入");

		Intent intentStart= new Intent(ACTION_BTN);
		intentStart.putExtra(INTENT_NAME, INTENT_BTN_START);
		PendingIntent intentStartpi = PendingIntent.getBroadcast(this, 1, intentStart, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_start, intentStartpi);

		Intent intentPause = new Intent(ACTION_BTN);
		intentPause.putExtra(INTENT_NAME, INTENT_BTN_PAUSE);
		PendingIntent intentPausepi = PendingIntent.getBroadcast(this, 2, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_pause, intentPausepi);

		Intent intentStop = new Intent(ACTION_BTN);
		intentStop.putExtra(INTENT_NAME, INTENT_BTN_STOP);
		PendingIntent intentStoppi = PendingIntent.getBroadcast(this, 3, intentStop, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_stop, intentStoppi);

		Intent intent2 = new Intent();
		intent2.setClass(this, MainActivity.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent intentContent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		builder.setOngoing(false);
		builder.setAutoCancel(false);
		builder.setContent(remoteViews);
		builder.setTicker("正在使用首都机场无线");
		builder.setSmallIcon(R.drawable.id_airport);

		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.flags = Notification.FLAG_NO_CLEAR;
		notification.contentIntent = intentContent;

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
	}

	private void intiReceiver() {
		mReceiver = new NotificationBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_BTN);
		getApplicationContext().registerReceiver(mReceiver, intentFilter);
	}

	private void unregeisterReceiver() {
		if (mReceiver != null) {
			getApplicationContext().unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	class NotificationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_BTN)) {
				int btn_id = intent.getIntExtra(INTENT_NAME, 0);
				switch (btn_id) {
					case INTENT_BTN_START:
						Toast.makeText(MainActivity.this, "从通知栏点登录", Toast.LENGTH_SHORT).show();
						unregeisterReceiver();
						NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
						notificationManager.cancel(0);
						break;
					case INTENT_BTN_PAUSE:
						Toast.makeText(MainActivity.this, "你好", Toast.LENGTH_SHORT).show();
						break;
					case INTENT_BTN_STOP:
						Toast.makeText(MainActivity.this, "你真好", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}
	}

	public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

	@TargetApi(Build.VERSION_CODES.M)
	public void requestDrawOverLays() {
		if (!Settings.canDrawOverlays(MainActivity.this)) {
			Toast.makeText(this, "can not DrawOverlays", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MainActivity.this.getPackageName()));
			startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
		} else {
			// Already hold the SYSTEM_ALERT_WINDOW permission, do addview or something.
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
			if (!Settings.canDrawOverlays(this)) {
				// SYSTEM_ALERT_WINDOW permission not granted...
				Toast.makeText(this, "Permission Denieddd by user.Please Check it in Settings", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Permission Allowed", Toast.LENGTH_SHORT).show();
				// Already hold the SYSTEM_ALERT_WINDOW permission, do addview or something.
			}
		}
	}
}
