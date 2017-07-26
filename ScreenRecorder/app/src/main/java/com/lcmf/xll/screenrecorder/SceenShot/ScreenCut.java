package com.lcmf.xll.screenrecorder.SceenShot;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

/**
 * Created by Administrator on 2017/7/27 0027.
 */

public class ScreenCut {
//	final MediaProjectionManager projectionManager = (MediaProjectionManager)
//			getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//	Intent intent = projectionManager.createScreenCaptureIntent();
//	startActivityForResult(intent, REQUEST_CODE);
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		handleScreenShotIntent(resultCode, data);
//	}
//	private void handleScreenShotIntent(int resultCode, Intent data) {
//
//		onScreenshotTaskBegan();
//		final MediaProjectionManager projectionManager = (MediaProjectionManager)
//				getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//		final MediaProjection mProjection = projectionManager.getMediaProjection(resultCode, data);
//		Point size = Utils.getScreenSize(this);
//		final int mWidth = size.x;
//		final int mHeight = size.y;
//		final ImageReader mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat
//				.RGBA_8888, 2);
//		final VirtualDisplay display = mProjection.createVirtualDisplay("screen-mirror", mWidth,
//				mHeight, DisplayMetrics.DENSITY_MEDIUM,
//				DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, mImageReader.getSurface(),
//				null, null);
//
//		mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//			@Override
//			public void onImageAvailable(ImageReader mImageReader) {
//
//				Image image = null;
//				try {
//					image = mImageReader.acquireLatestImage();
//					if (image != null) {
//						final Image.Plane[] planes = image.getPlanes();
//						if (planes.length > 0) {
//							final ByteBuffer buffer = planes[0].getBuffer();
//							int pixelStride = planes[0].getPixelStride();
//							int rowStride = planes[0].getRowStride();
//							int rowPadding = rowStride - pixelStride * mWidth;
//
//
//							// create bitmap
//							Bitmap bmp = Bitmap.createBitmap(mWidth + rowPadding / pixelStride,
//									mHeight, Bitmap.Config.ARGB_8888);
//							bmp.copyPixelsFromBuffer(buffer);
//
//							Bitmap croppedBitmap = Bitmap.createBitmap(bmp, 0, 0, mWidth, mHeight);
//
//							saveBitmap(croppedBitmap);//保存图片
//
//							if (croppedBitmap != null) {
//								croppedBitmap.recycle();
//							}
//							if (bmp != null) {
//								bmp.recycle();
//							}
//						}
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				} finally {
//					if (image != null) {
//						image.close();
//					}
//					if (mImageReader != null) {
//						mImageReader.close();
//					}
//					if (display != null) {
//						display.release();
//					}
//
//					mImageReader.setOnImageAvailableListener(null, null);
//					mProjection.stop();
//
//					onScreenshotTaskOver();
//				}
//
//			}
//		}, getBackgroundHandler());
//	}

}
