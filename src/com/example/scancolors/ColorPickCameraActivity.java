package com.example.scancolors;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class ColorPickCameraActivity extends Activity {
	private static Camera mCamera;
	private CameraPreview mPreview;
	public static final String TAG = "AACamera";
	public static final String FOLDER_NAME = "Kamera";
	public Context context;
	public int[] rgbArr;
	public int red_colorNo1, green_colorNo1, blue_colorNo1, red_colorNo2,
			green_colorNo2, blue_colorNo2;
	private static final int mainFrameCountLimit = 1; // every frame
	private double mainFrameCount = 0;
	public static final String PREFS_NAME = "SettingsFile";
	public static int color_button_clicked;

	public FrameLayout preview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.color_pick_camera);

		// for colorPicker to know which color (colorNo1/colorNo2) to take.
		color_button_clicked = getIntent().getExtras().getInt("color");

		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera, previewCb);
		preview = (FrameLayout) findViewById(R.id.camera_preview_color_picker);
		preview.addView(mPreview);

		Button btn_pick_color = (Button) findViewById(R.id.btn_pick_color);
		btn_pick_color.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				if (color_button_clicked == 1) {
					red_colorNo1 = rgbArr[0];
					green_colorNo1 = rgbArr[1];
					blue_colorNo1 = rgbArr[2];
					Log.w("aplikacija boja crvena:",
							String.valueOf(red_colorNo1));
					Log.w("aplikacija boja zelena:",
							String.valueOf(green_colorNo1));
					Log.w("aplikacija boja plava:",
							String.valueOf(blue_colorNo1));
					editor.putInt("red1", red_colorNo1);
					editor.putInt("green1", green_colorNo1);
					editor.putInt("blue1", blue_colorNo1);
					// Commit the edits!
					editor.commit();
				} else if (color_button_clicked == 2) {
					red_colorNo2 = rgbArr[0];
					green_colorNo2 = rgbArr[1];
					blue_colorNo2 = rgbArr[2];
					Log.w("aplikacija boja crvena:",
							String.valueOf(red_colorNo2));
					Log.w("aplikacija boja zelena:",
							String.valueOf(green_colorNo2));
					Log.w("aplikacija boja plava:",
							String.valueOf(blue_colorNo2));
					editor.putInt("red2", red_colorNo2);
					editor.putInt("green2", green_colorNo2);
					editor.putInt("blue2", blue_colorNo2);
					// Commit the edits!
					editor.commit();
				}

				// releaseCamera();
				Intent i = new Intent(getApplicationContext(),
						MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});

	}

	/**
	 * Taking byte data from camera. Approx. 12-18 frames per second.
	 * This PreviewCallback gets called for every frame camera sends,
	 * but gets only mainFrameCountLimit-th frame. In this class,
	 * it gets every second frame to get the center pixel from it to save
	 * for color to be scanned.
	 * Yuv format is coming from camera, we convert it to jpeg, load it
	 * and get Bitmap from it. Then we can position to the center (original
	 * bitmap has some offset and it doesn't give center pixel), crop it and
	 * take center pixel to be used as selected colorno1/no2.
	 */
	PreviewCallback previewCb = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {

			if (mainFrameCount == mainFrameCountLimit && mainFrameCount != 0) {
				Camera.Parameters parameters = camera.getParameters();
				int width = parameters.getPreviewSize().width;
				int height = parameters.getPreviewSize().height;
				YuvImage yuv = new YuvImage(data,
						parameters.getPreviewFormat(), width, height, null);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

				byte[] bytes = out.toByteArray();
				final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);
				final Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,
						(width / 2) - 50, (height / 2) - 50, 100, 100);

				int rgb = croppedBitmap.getPixel(50, 50);
				rgbArr = getRGBArr(rgb);

				/*
				 * To see the difference between captured image and the 
				 * cropped one. Be sure to set mainFrameCountLimit to 50 or >
				 * for slowing down the process of saving frames to file.
				 * 
				Camera.Parameters parameters = camera.getParameters();
				int width = parameters.getPreviewSize().width;
				int height = parameters.getPreviewSize().height;
				YuvImage yuv = new YuvImage(data,
						parameters.getPreviewFormat(), width, height, null);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

				byte[] bytes = out.toByteArray();
				final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);
				String path = Environment.getExternalStorageDirectory()
						.toString() + "/Pictures/";
				OutputStream fOut = null;
				File file = new File(path, "OriginalPhoto.png");
				try {
					fOut = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
				int rgb = bitmap.getPixel(height / 2, width / 2);

				Bitmap resizedbitmap1 = Bitmap.createBitmap(bitmap, (width / 2)-50,
						(height / 2)-50, 100, 100);
				OutputStream fOut1 = null;
				File file2 = new File(path, "CropedImage.png");
				try {
					fOut1 = new FileOutputStream(file2);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				resizedbitmap1.compress(Bitmap.CompressFormat.PNG, 90, fOut1);
				rgbArr = getRGBArr(rgb); 
				 
				 */
			}
			mainFrameCount++;
		}
	};

	public static int[] getRGBArr(int pixel) {

		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		return new int[] { red, green, blue };

	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseCamera();

	}

	@Override
	public void onResume() {
		super.onResume();
		mCamera = getCameraInstance();
		if (mPreview == null) {
			mPreview = new CameraPreview(this, mCamera, previewCb);
			preview = (FrameLayout) findViewById(R.id.camera_preview_color_picker);
			preview.addView(mPreview);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		releaseCamera();
	}

	public static void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

}
