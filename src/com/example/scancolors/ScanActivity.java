package com.example.scancolors;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import com.example.scancolors.R;

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
import android.widget.FrameLayout;
import android.widget.TextView;

public class ScanActivity extends Activity {

	private static Camera mCamera;
	private CameraPreview mPreview;
	public Context context;

	private static final int colorTolerance = 40; // RGB values will be evaluated within this range!
	
	// limit for app to recognize color!
	// 20% of pixel color(or similar to it) has to match for colorNo1 and 20% for the colorNo2 on the screen
	//for the app to send sms
	private static final float COLOR_MATCH_LIMIT = (float) 0.2;
	private static final int mainFrameCountLimit = 6; // every mainFrameCountLimit-th frame to take!
	private final int REQUIRED_SIZE = 50; // to resize

	private static int red1, blue1, green1;
	private static int red2, blue2, green2;
	private static double resultColorNo1, resultColorNo2, rez_percent_first,
			rez_percent_second;
	public SharedPreferences settings;

	private int mainFrameCount = 0;

	public FrameLayout preview;

	private int picture_height, picture_width, only_one_instance = 1;
	private static double mainPixelCounter = 0;
	private static double colorNo1MatchPixelCounter = 0,
			colorNo2MatchPixelCounter = 0;

	public static final String PREFS_NAME = "SettingsFile";

	TextView tv_postotak_first_color, tv_postotak_second_color;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.scan_activity);

		mCamera = getCameraInstance();

		settings = getSharedPreferences(PREFS_NAME, 0);
		red1 = settings.getInt("red1", 1);
		blue1 = settings.getInt("blue1", 1);
		green1 = settings.getInt("green1", 1);
		red2 = settings.getInt("red2", 1);
		blue2 = settings.getInt("blue2", 1);
		green2 = settings.getInt("green2", 1);

		tv_postotak_first_color = (TextView) findViewById(R.id.tv_postotak_first_color);
		tv_postotak_second_color = (TextView) findViewById(R.id.tv_postotak_second_color);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera, previewCb);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

	}
	
	/**
	 * Taking byte data from camera. Approx. 12-18 frames per second.
	 * This PreviewCallback gets called for every frame camera sends,
	 * but gets only mainFrameCountLimit-th frame. In this class,
	 * it gets every second frame to get the center pixel from it to save
	 * for color to be scanned.
	 * Yuv format is coming from camera, we convert it to jpeg, load it
	 * and get Bitmap from it. Then we resize it, loop through the pixels
	 * and send SMS if colorNo1 and colorNo2 are >= COLOR_MATCH_LIMIT
	 * on that frame.
	 */
	PreviewCallback previewCb = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			//Log.w("ScanColors - ScanActivity: Frame number: ", mainFrameCount);

			if (mainFrameCount == mainFrameCountLimit && mainFrameCount != 0) {
				mainFrameCount = 0;
				mainPixelCounter = 0;
				colorNo1MatchPixelCounter = 0;
				colorNo2MatchPixelCounter = 0;
				Camera.Parameters parameters = camera.getParameters();
				int width = parameters.getPreviewSize().width;
				int height = parameters.getPreviewSize().height;

				/* Custom convert function. It is to slow.
				 * Better to use yum->jpeg->Bitmap native conversion. Much faster.
				  int[] pixeli_slike =
				  convertYUV420_NV21toRGB8888(data, width, height); Bitmap
				  bitmap = Bitmap.createBitmap(pixeli_slike, width, height,
				  Bitmap.Config.ARGB_8888);
				 */

				YuvImage yuv = new YuvImage(data,
						parameters.getPreviewFormat(), width, height, null);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

				byte[] bytes = out.toByteArray();
				final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);

				if (checkFrameForMatch(bitmap)) {
					finish();
					Intent i = new Intent(getApplicationContext(),
							SecondActivity.class);
					i.putExtra("only_one_instance", only_one_instance);

					// to be sure to send sms only once! There is a case when we take
					// every 5 and lower frames, activity can start even a few times!
					only_one_instance++;
					startActivity(i);
				}
			}
			mainFrameCount++;
		}
	};

	/**
	 * Given bitmap is resized, it's pixel goes to loop
	 * matching given colorNo1/No2 and returning true if
	 * matched pixels are in range >= COLOR_MATCH_LIMIT.
	 * 
	 * For now there are two colors that needs to be in that
	 * range for app to send sms on given number.
	 * 
	 * @param bitmap
	 * @return
	 */
	private boolean checkFrameForMatch(Bitmap bitmap) {
		try {
			final Bitmap resizedBitmap = resizeBitmap(bitmap);
			picture_height = resizedBitmap.getHeight();
			picture_width = resizedBitmap.getWidth();
			// Log.w("ScanColors - ScanActivity: picture height: ", String.valueOf(picture_height));
			// Log.w("ScanColors - ScanActivity: picture width: ", String.valueOf(picture_width)));

			for (int i = 0; i < picture_width; i++) {

				for (int j = 0; j < picture_height; j++) {

					int rgb = resizedBitmap.getPixel(i, j);
					int[] rgbArr = getRGBArr(rgb);

					if (isSimilarToColors(rgbArr, 1)) {
						colorNo1MatchPixelCounter++;
					}
					if (isSimilarToColors(rgbArr, 2)) {
						colorNo2MatchPixelCounter++;
					}
					mainPixelCounter++;
				}
			}
			/*Log.w("ScanColors - ScanActivity: Number of pixels",
					String.valueOf(mainPixelCounter));
			Log.w("ScanColors - ScanActivity: Number of pixel matched colorNo1: ",
					String.valueOf(colorNo1MatchPixelCounter));
			Log.w("ScanColors - ScanActivity: Number of pixel matched colorNo2: ",
					String.valueOf(colorNo2MatchPixelCounter));
			*/

			resultColorNo1 = (colorNo1MatchPixelCounter / mainPixelCounter);
			resultColorNo2 = (colorNo2MatchPixelCounter / mainPixelCounter);
			rez_percent_first = resultColorNo1 * 100;
			rez_percent_second = resultColorNo2 * 100;
			tv_postotak_first_color.setText(String.format("%.2f",
					rez_percent_first) + "%");
			tv_postotak_second_color.setText(String.format("%.2f",
					rez_percent_second) + "%");
			if (resultColorNo1 >= COLOR_MATCH_LIMIT
					&& resultColorNo2 >= COLOR_MATCH_LIMIT) {
				return true;
			} else {
				return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Method for resizing given bitmap.
	 * Comented code: To see the difference between original picture from camera frame
	 * and the Resized one for pixel comparison.
	 * Be sure to set mainFrameCountLimit to 50 or >
	 * for slowing down the process of saving frames to file.
	 * 
	 * @param bitmap
	 * @return resizedBitmap
	 * @throws FileNotFoundException
	 */
	private Bitmap resizeBitmap(Bitmap bitmap) throws FileNotFoundException {
		/*
		 String path = Environment.getExternalStorageDirectory().toString() +
		 "/Pictures/"; OutputStream fOut = null; File file = new File(path,
		 "OriginalPhoto.png"); fOut = new FileOutputStream(file);
		 bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
		*/

		int width_tmp = bitmap.getWidth(), height_tmp = bitmap.getHeight();
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
		}

		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width_tmp,
				height_tmp, false);

		/*
		 File file2 = new File(path, "ResizedPhoto.png"); fOut = new
		 FileOutputStream(file2);
		 resizedBitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
		*/

		return resizedBitmap;
	}

	public static int[] getRGBArr(int pixel) {

		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		return new int[] { red, green, blue };

	}

	// http://www.rapidtables.com/web/color/RGB_Color.htm
	/**
	 * Current algorithm for color recognition. Needs big improvement.
	 * 
	 * @param rgbArr
	 * @param boja
	 * @return true if color is similar
	 */
	public static boolean isSimilarToColors(int[] rgbArr, int boja) {
		int[] colorNumber1 = { red1, green1, blue1 };
		int[] colorNumber2 = { red2, green2, blue2 };
		int rrDif, rgDif, rbDif;

		if (boja == 1) {
			rrDif = rgbArr[0] - colorNumber1[0];
			rgDif = rgbArr[1] - colorNumber1[1];
			rbDif = rgbArr[2] - colorNumber1[2];

			if ((rrDif >= (-colorTolerance / 2) && (rrDif <= (colorTolerance / 2)))
					&& ((rgDif >= (-colorTolerance / 2) && (rgDif <= (colorTolerance / 2))) && ((rbDif >= (-colorTolerance / 2) && (rbDif <= (colorTolerance / 2)))))) {
				return true;
			} else {
				return false;
			}
		} else {
			rrDif = rgbArr[0] - colorNumber2[0];
			rgDif = rgbArr[1] - colorNumber2[1];
			rbDif = rgbArr[2] - colorNumber2[2];

			if ((rrDif >= (-colorTolerance / 2) && (rrDif <= (colorTolerance / 2)))
					&& ((rgDif >= (-colorTolerance / 2) && (rgDif <= (colorTolerance / 2))) && ((rbDif >= (-colorTolerance / 2) && (rbDif <= (colorTolerance / 2)))))) {
				return true;
			} else {
				return false;
			}
		}
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
		only_one_instance = 1;
		mCamera = getCameraInstance();
		red1 = settings.getInt("red1", 1);
		blue1 = settings.getInt("blue1", 1);
		green1 = settings.getInt("green1", 1);
		red2 = settings.getInt("red2", 1);
		blue2 = settings.getInt("blue2", 1);
		green2 = settings.getInt("green2", 1);

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

	/*
	public static int[] convertYUV420_NV21toRGB8888(byte[] data, int width,
			int height) {
		int size = width * height;
		int offset = size;
		int[] pixels = new int[size];
		int u, v, y1, y2, y3, y4;

		// i percorre os Y and the final pixels
		// k percorre os pixles U e V
		for (int i = 0, k = 0; i < size; i += 2, k += 2) {
			y1 = data[i] & 0xff;
			y2 = data[i + 1] & 0xff;
			y3 = data[width + i] & 0xff;
			y4 = data[width + i + 1] & 0xff;

			u = data[offset + k] & 0xff;
			v = data[offset + k + 1] & 0xff;
			u = u - 128;
			v = v - 128;

			pixels[i] = convertYUVtoRGB(y1, u, v);
			pixels[i + 1] = convertYUVtoRGB(y2, u, v);
			pixels[width + i] = convertYUVtoRGB(y3, u, v);
			pixels[width + i + 1] = convertYUVtoRGB(y4, u, v);

			if (i != 0 && (i + 2) % width == 0)
				i += width;
		}
		return pixels;
	}

	private static int convertYUVtoRGB(int y, int u, int v) {
		int r, g, b;

		r = y + (int) 1.402f * v;
		g = y - (int) (0.344f * u + 0.714f * v);
		b = y + (int) 1.772f * u;
		r = r > 255 ? 255 : r < 0 ? 0 : r;
		g = g > 255 ? 255 : g < 0 ? 0 : g;
		b = b > 255 ? 255 : b < 0 ? 0 : b;
		return 0xff000000 | (b << 16) | (g << 8) | r;
	}
	*/

}