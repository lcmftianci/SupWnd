package com.lcmf.xll.screenrecorder;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.lcmf.xll.screenrecorder.SceenShot.RecordService;

/**
 * Created by Administrator on 2017/7/21 0021.
 */

public class ScreenApplication extends Application {
	private static ScreenApplication application;

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		application = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 启动 Marvel service
		startService(new Intent(this, RecordService.class));
	}

	public static ScreenApplication getInstance() {
		return application;
	}
}
