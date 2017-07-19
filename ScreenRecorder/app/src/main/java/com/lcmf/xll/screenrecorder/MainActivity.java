package com.lcmf.xll.screenrecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.lcmf.xll.screenrecorder.SuspendWindow.SuspendWindowService;
import com.lcmf.xll.screenrecorder.ViewFragment.InnerFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

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
	public final static int SCREEN_SHOT = 8;
	public static  int nHaz = 0;
	public final static String TAG = "MainActivity";
	NotificationBroadcastReceiver mReceiver;

	MediaProjection mediaProjection;
	MediaProjectionManager projectionManager;
	VirtualDisplay virtualDisplay;
	int mResultCode;
	Intent mData;
	ImageReader imageReader;

	int width;
	int height;
	int dpi;

	String imageName;
	Bitmap bitmap;
//	ImageView imageView;


	private TextView mTextMessage;

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.navigation_home:
					mTextMessage.setText(R.string.title_home);
					if(fragment1 != null){
						transaction = manager.beginTransaction();
						transaction.hide(fragment1);
						IsVisible = 0;
						transaction.commit();
					}
					return true;
				case R.id.navigation_dashboard:
					mTextMessage.setText(R.string.title_dashboard);
					if(IsVisible == 0){
						showTips("---可显示---");
						if(nHaz == 1){
							transaction = manager.beginTransaction();
							transaction.show(fragment1);
							showTips("---显示fragment---");
						}
						if(nHaz == 0) {
							manager = getSupportFragmentManager();
							transaction = manager.beginTransaction();
							fragment1 = new InnerFragment();
							nHaz = 1;
							transaction.add(R.id.content, fragment1);
							showTips("---可添加---");
						}
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

		if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		}

		//截屏获取屏幕的信息
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		width = metric.widthPixels;
		height = metric.heightPixels;
		dpi = metric.densityDpi;

		//imageView = (ImageView) findViewById(R.id.image);
		projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
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
		remoteViews.setTextViewText(R.id.tv_up, "录屏精灵");
		remoteViews.setTextViewText(R.id.tv_down, "通知栏控制");

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
		builder.setTicker("录屏精灵");
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
						if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
							ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
						}else {
							GetandSaveCurrentImage();
						}
						break;
					case INTENT_BTN_STOP:
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
							StartScreenShot();
						else
							showTips("安卓系统版本低！");
						break;
				}
			}
		}
	}

	public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
	public enum MAGEINFO
	{
		MAGEINFO_OVERLAY, MAGEINFO_RECORDE, WED, THU, FRI, SAT, SUN;
	}

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
			if( requestCode == OVERLAY_PERMISSION_REQ_CODE){
				if (!Settings.canDrawOverlays(this)) {
					// SYSTEM_ALERT_WINDOW permission not granted...
					Toast.makeText(this, "Permission Denieddd by user.Please Check it in Settings", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "Permission Allowed", Toast.LENGTH_SHORT).show();
					// Already hold the SYSTEM_ALERT_WINDOW permission, do addview or something.
				}
		}else if(requestCode == SCREEN_SHOT){
				if(resultCode == RESULT_OK){
					mResultCode = resultCode;
					mData = data;
					setUpMediaProjection();
					setUpVirtualDisplay();
					startCapture();
				}
			}
	}


	/*
	* 截屏方式一
	*   View view = activity.getWindow().getDecorView();
		//      Enables or disables the drawing cache
        view.setDrawingCacheEnabled(true);
		//      will draw the view in a bitmap
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
	* */
	//截屏功能正在实现
	private void GetandSaveCurrentImage(){
		//隐藏程序窗口
		//get_root_view(MainActivity.this).setVisibility(View.GONE);
		//1.构建Bitmap
//		WindowManager windowManager = getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		int w = display.getWidth();
//		int h = display.getHeight();
		WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		int w = windowManager.getDefaultDisplay().getWidth();
		int h = windowManager.getDefaultDisplay().getHeight();

		Log.v("---MainActivity---", "开始截取图片");
		Log.v("---MainActivity---", String.valueOf(w) + String.valueOf(h));
		Bitmap Bmp = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888 );

		//2.获取屏幕
		View decorview = this.getWindow().getDecorView();
		decorview.setDrawingCacheEnabled(true);
		Bmp = decorview.getDrawingCache();

		String SavePath = getSDCardPath()+"/ScreenCap/ScreenImage";
		Log.v("---MainActivity---", SavePath);
		//3.保存Bitmap
		try {
			File path = new File(SavePath);
			//获取当前时间
			String strDate = getDateTime();

			//文件
			String filepath = SavePath + "/Screen" + "-" + getDateTime()+".png";
			Log.v("---MainActivity---", filepath);
			File file = new File(filepath);
			if(!path.exists()){
				path.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
				Log.v("---MainActivity---", "创建文件");
			}

			FileOutputStream fos = null;
			fos = new FileOutputStream(file);
			if (null != fos) {
				Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
				Toast.makeText(MainActivity.this, "截屏文件保存至" + SavePath + "下", Toast.LENGTH_LONG).show();
				//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取SDCard的目录路径功能
	 * @return
	 */
	private String getSDCardPath(){
		File sdcardDir = null;
		//判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(sdcardExist){
			sdcardDir = Environment.getExternalStorageDirectory();
		}
		return sdcardDir.toString();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode){
			case 1:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					GetandSaveCurrentImage();
				}else{
					Toast.makeText(MainActivity.this, "权限申请失败", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}

	/*
		创建隐藏与现实view的函数
		get_root_view(context).setVisibility(View.GONE);  //影藏 view
		get_root_view(context).setVisibility(View.VISIBLE);  //显示view
	*/
	private static View get_root_view(Activity context)
	{
		return ((ViewGroup)context.findViewById(android.R.id.content)).getChildAt(0);
	}

	/*
		获取当前系统时间
	 */
	public static String getDateTime(){
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String  date = sDateFormat.format(new java.util.Date());
		return date;
	}

//	moveTaskToBack(true);

	/*
	*
	* 截屏方法二
	* */

//	@TargetApi(Build.VERSION_CODES.M)
//	public void screenRecord(){
//		//1、获取MediaProjectionManager系统服务
//		MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
//
//		//2、获取Intent
//		startActivityForResult(projectionManager.createScreenCaptureIntent(),SCREEN_SHOT);
//	}
	@TargetApi(Build.VERSION_CODES.M)
	private void startCapture() {
		SystemClock.sleep(1000);
		imageName = System.currentTimeMillis() + ".png";
		Image image = imageReader.acquireNextImage();
		if (image == null) {
			Log.e(TAG, "image is null.");
			return;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		final Image.Plane[] planes = image.getPlanes();
		final ByteBuffer buffer = planes[0].getBuffer();
		int pixelStride = planes[0].getPixelStride();
		int rowStride = planes[0].getRowStride();
		int rowPadding = rowStride - pixelStride * width;
		bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(buffer);
		image.close();

		String SavePath = getSDCardPath()+"/ScreenCap/ScreenImage";
		Log.v("---MainActivity---", SavePath);
		//3.保存Bitmap
		try {
			File path = new File(SavePath);
			//获取当前时间
			String strDate = getDateTime();

			//文件
			String filepath = SavePath + "/Screen" + "-" + getDateTime()+".png";
			Log.v("---MainActivity---", filepath);
			File file = new File(filepath);
			if(!path.exists()){
				path.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
				Log.v("---MainActivity---", "创建文件");
			}

			FileOutputStream fos = null;
			fos = new FileOutputStream(file);
			if (null != fos) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
				Toast.makeText(MainActivity.this, "截屏文件保存至" + SavePath + "下", Toast.LENGTH_LONG).show();
				//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			//get_root_view(MainActivity.this).setVisibility(View.VISIBLE);
		}

//		if (bitmap != null) {
//			imageView.setImageBitmap(bitmap);
//		}
	}
	@TargetApi(Build.VERSION_CODES.M)
	private void setUpVirtualDisplay() {
		imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
//		mediaProjection.createVirtualDisplay("ScreenShout",
//				width,height,dpi,
//				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//				imageReader.getSurface(),null,null);

				mediaProjection.createVirtualDisplay("ScreenShout",
				width,height,dpi,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
				imageReader.getSurface(),null,null);
	}
	@TargetApi(Build.VERSION_CODES.M)
	private void setUpMediaProjection(){
		mediaProjection = projectionManager.getMediaProjection(mResultCode,mData);
	}
	@TargetApi(Build.VERSION_CODES.M)
	public void StartScreenShot() {
		startActivityForResult(projectionManager.createScreenCaptureIntent(),SCREEN_SHOT);
	}
}
