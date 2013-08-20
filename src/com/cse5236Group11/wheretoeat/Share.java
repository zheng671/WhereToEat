package com.cse5236Group11.wheretoeat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Share extends Activity implements OnClickListener {
	// references:
	// http://developer.android.com/training/camera/photobasics.html
	// http://stackoverflow.com/questions/9598872/unable-to-share-text-and-image-to-facebook-via-android-intent
	// http://stackoverflow.com/questions/4455558/allow-user-to-select-camera-or-gallery-for-image
	// http://stackoverflow.com/questions/10838138/how-can-i-retrieve-the-path-from-an-image-in-the-android-gallery
	// http://stackoverflow.com/questions/7856959/android-file-chooser
	// http://stackoverflow.com/questions/4142090/how-do-you-to-retrieve-dimensions-of-a-view-getheight-and-getwidth-always-r/4406090#4406090
	// http://stackoverflow.com/questions/10530165/android-camera-orientation-issue

	private String mCurrentPhotoPath = "";
	private Boolean mMeasured;
	private Boolean isTextChecked;
	private String restaurantName = "", restaurantAddress = "";
	private String experience = "";
	private static String KEY_NAME = "name"; // name of the place
	private static String KEY_ADDRESS = "address"; // address of the place

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);

		// retrieve restaurant name and address from activity calling
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			restaurantName = extras.getString(KEY_NAME);
			restaurantAddress = extras.getString(KEY_ADDRESS);
		}

		// only set address and name if available
		if (restaurantName != null && restaurantAddress != null
				&& restaurantName.length() > 0
				&& restaurantAddress.length() > 0) {
			experience = "I just visited " + restaurantName + " located at "
					+ restaurantAddress + " and...";
		} else {
			// unable to get name and address, default to empty
			experience = "";
		}

		// mMeasured used for tracking resize of ImageView on orientation change
		mMeasured = false;
		// initialize to text only share option
		isTextChecked = true;

		View attachPictureButton = findViewById(R.id.attachPicture);
		attachPictureButton.setOnClickListener(this);
		attachPictureButton.setVisibility(View.INVISIBLE);

		View postButton = findViewById(R.id.postButton);
		postButton.setOnClickListener(this);
		postButton.setEnabled(false);

		View previewImage = findViewById(R.id.previewImage);
		previewImage.setVisibility(View.INVISIBLE);

		// when rotating the device, we need to know how much room we have for
		// resizing the image to. this cannot be done until the layout is
		// completely drawn so we know how much space is available. this
		// listener
		// recognizes when the image is ready to be set to the available image
		// view size
		previewImage.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {

						if (!mMeasured) {
							// Here your view is already laid out and measured
							// for the first time
							setPic();
							mMeasured = true; // flag to mark that we already
												// got the sizes
						}
					}
				});

		RadioButton rbText = (RadioButton) findViewById(R.id.textRadio);
		rbText.setOnClickListener(this);
		rbText.setChecked(true);

		RadioButton rbTextPicture = (RadioButton) findViewById(R.id.textAndPictureRadio);
		rbTextPicture.setOnClickListener(this);
		rbTextPicture.setChecked(false);

		TextView experienceText = (TextView) findViewById(R.id.experienceText);
		experienceText.setVisibility(View.VISIBLE);

		// text listener in order to save what has been placed in the text box
		// at all times for sharing and layout rebuilding
		experienceText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				TextView experienceText = (TextView) findViewById(R.id.experienceText);
				experience = experienceText.getText().toString();

				if (experience.length() > 0) {
					View postButton = findViewById(R.id.postButton);
					postButton.setEnabled(true);
				} else {
					View postButton = findViewById(R.id.postButton);
					postButton.setEnabled(false);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		experienceText.setText(experience);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Update the image view onResume only if there is no rotation going on,
		// if being rotated the
		// view will be set by the listener for the imageview global layout
		// listener

		if (mMeasured) {
			setPic();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// RESULT_OK means a picture was taken from Camera or selected from
		// Gallery
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				if (new File(mCurrentPhotoPath).exists()) {

					// Image taken from Camera, update Gallery
					galleryAddPic();
					View postButton = findViewById(R.id.postButton);
					postButton.setOnClickListener(this);
					postButton.setEnabled(true);
				} else {
					final String action = data.getAction();
					if (action == null) {
						// Image selected from gallery, and set path of selected
						// image
						Uri selectedImageUri = data.getData();
						mCurrentPhotoPath = getPath(selectedImageUri);
						View postButton = findViewById(R.id.postButton);
						postButton.setOnClickListener(this);
						postButton.setEnabled(true);
					}
				}

			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Unalbe to attach picture. Please try again",
					Toast.LENGTH_LONG).show();

			View postButton = findViewById(R.id.postButton);
			postButton.setEnabled(false);

			ImageView previewImage = (ImageView) findViewById(R.id.previewImage);
			previewImage.setImageDrawable(null);

		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// set new layout depending on landscape or portrait and set listeners &
		// values where necessary
		setContentView(R.layout.share);

		View attachPictureButton = findViewById(R.id.attachPicture);
		attachPictureButton.setOnClickListener(this);

		View postButton = findViewById(R.id.postButton);
		postButton.setOnClickListener(this);

		// set to false so once window for image view is drawn it can then set
		// the picture to it
		mMeasured = false;

		View previewImage = findViewById(R.id.previewImage);

		previewImage.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (!mMeasured) {
							// Here your view is already layed out and measured
							// for the first time
							setPic();
							mMeasured = true; // Some optional flag to mark,
												// that we already got the sizes
						}
					}
				});

		RadioButton rbText = (RadioButton) findViewById(R.id.textRadio);
		rbText.setOnClickListener(this);

		RadioButton rbTextPicture = (RadioButton) findViewById(R.id.textAndPictureRadio);
		rbTextPicture.setOnClickListener(this);

		TextView experienceText = (TextView) findViewById(R.id.experienceText);
		experienceText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				TextView experienceText = (TextView) findViewById(R.id.experienceText);
				experience = experienceText.getText().toString();

				if (experience.length() > 0) {
					View postButton = findViewById(R.id.postButton);
					postButton.setEnabled(true);
				} else {
					View postButton = findViewById(R.id.postButton);
					postButton.setEnabled(false);
				}

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		if (experience.length() > 0) {
			experienceText.setText(experience);
		}

		if (isTextChecked) {
			rbText.performClick();
		} else {
			rbTextPicture.performClick();
		}

	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.attachPicture:
			// check for available camera on device , add intents for available
			// camera applications
			final List<Intent> cameraIntents = new ArrayList<Intent>();
			final Intent captureIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			final PackageManager packageManager = getPackageManager();
			final List<ResolveInfo> listCam = packageManager
					.queryIntentActivities(captureIntent, 0);
			Uri uriForPic;
			try {
				uriForPic = createPicturePath();

				for (ResolveInfo res : listCam) {
					final String packageName = res.activityInfo.packageName;
					final Intent intent = new Intent(captureIntent);
					intent.setComponent(new ComponentName(
							res.activityInfo.packageName, res.activityInfo.name));
					intent.setPackage(packageName);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForPic);
					cameraIntents.add(intent);

				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// GALLERY , FILE EXPLORER, or similar for picture
			final Intent galleryIntent = new Intent();
			galleryIntent.setType("image/*");
			galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

			final Intent chooserIntent = Intent.createChooser(galleryIntent,
					"Picture from...");

			// Add the camera options.
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					cameraIntents.toArray(new Parcelable[] {}));

			startActivityForResult(chooserIntent, 1);
			break;
		case R.id.postButton:
			shareImage();
			break;
		case R.id.textRadio:

			isTextChecked = true;

			if (experience.length() > 0) {
				View postButton = findViewById(R.id.postButton);
				postButton.setEnabled(true);
			} else {
				View postButton = findViewById(R.id.postButton);
				postButton.setEnabled(false);
			}

			RadioButton textRadio = (RadioButton) findViewById(R.id.textRadio);
			RadioButton textPictureRadio = (RadioButton) findViewById(R.id.textAndPictureRadio);
			textRadio.setChecked(true);
			textPictureRadio.setChecked(false);

			View attachPictureButton = findViewById(R.id.attachPicture);
			attachPictureButton.setVisibility(View.INVISIBLE);

			View previewImage = findViewById(R.id.previewImage);
			previewImage.setVisibility(View.INVISIBLE);

			TextView experienceText = (TextView) findViewById(R.id.experienceText);
			experienceText.setVisibility(View.VISIBLE);

			break;

		case R.id.textAndPictureRadio:

			isTextChecked = false;

			File f = new File(mCurrentPhotoPath);

			if (mCurrentPhotoPath.length() > 0 && f.exists()) {
				View postButton = findViewById(R.id.postButton);
				postButton.setEnabled(true);
			} else {
				View postButton = findViewById(R.id.postButton);
				postButton.setEnabled(false);
			}

			textRadio = (RadioButton) findViewById(R.id.textRadio);
			textPictureRadio = (RadioButton) findViewById(R.id.textAndPictureRadio);
			textRadio.setChecked(false);
			textPictureRadio.setChecked(true);

			attachPictureButton = findViewById(R.id.attachPicture);
			attachPictureButton.setVisibility(View.VISIBLE);

			previewImage = findViewById(R.id.previewImage);
			previewImage.setVisibility(View.VISIBLE);

			experienceText = (TextView) findViewById(R.id.experienceText);
			experienceText.setVisibility(View.INVISIBLE);
			break;
		}

	}

	@SuppressLint("SimpleDateFormat")
	private Uri createPicturePath() throws IOException {
		// Create an image file name that is unique for this time and our app
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String filename = "Where-To-Eat_" + timeStamp + ".jpeg";
		// save the path for where this file should be saved to, in this case
		// the public pictures folder so the camera is capable of saving to it
		// and we will know where it is by this saved filename

		File exst = Environment.getExternalStorageDirectory();
		String exstPath = exst.getPath();

		File fooo = new File(exstPath + "/WhereToEat");
		fooo.mkdir();

		mCurrentPhotoPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separatorChar
				+ "WhereToEat"
				+ File.separatorChar + filename;

		return Uri.fromFile(new File(mCurrentPhotoPath));

	}

	private void galleryAddPic() {
		// notify the media scanner of our new image so it will show up in the
		// users photo gallery on their device
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.fromFile(new File(mCurrentPhotoPath))));
	}

	private void shareImage() {
		Intent share = new Intent(Intent.ACTION_SEND);

		RadioButton textRadio = (RadioButton) findViewById(R.id.textRadio);

		// set text for both options. some sharing applications allow both text
		// and image to be passed as intent, others only text. if the user
		// selects picture, they can only be guaranteed that the image will be
		// attached to post, similarly for the text
		TextView experienceText = (TextView) findViewById(R.id.experienceText);

		share.putExtra(Intent.EXTRA_TEXT, experienceText.getText().toString());

		if (textRadio.isChecked()) {
			share.setType("text/plain");

		} else {
			share.setType("image/*");

			Uri uri = Uri.fromFile(new File(mCurrentPhotoPath));
			share.putExtra(Intent.EXTRA_STREAM, uri);

		}
		// user now picks where to share the media to
		startActivity(Intent.createChooser(share, "Share Your Experience!!"));
	}

	private void setPic() {
		// update the imageview with the currenlty selected picture from the
		// camera or gallery
		if (mCurrentPhotoPath.length() > 0
				&& new File(mCurrentPhotoPath).exists()) {
			ImageView previewImage = (ImageView) findViewById(R.id.previewImage);
			// Get the dimensions of the View
			int targetW = previewImage.getWidth();

			int targetH = previewImage.getHeight();

			// Get the dimensions of the bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			// Determine how much to scale down the image
			int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,
					bmOptions);
			try {
				// adjust orientation of image and set image to image view
				int orientation = resolveBitmapOrientation(new File(
						mCurrentPhotoPath));
				previewImage.setImageBitmap(applyOrientation(bitmap,
						orientation));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// disable posting since picture doesn't exist only if on image
			// radio
			RadioButton rbText = (RadioButton) findViewById(R.id.textRadio);
			if (!(rbText.isChecked() && experience.length() > 0)) {
				View postButton = findViewById(R.id.postButton);
				postButton.setEnabled(false);
			}

			// if file doesn't exist, reset the image preview to be empty
			ImageView previewImage = (ImageView) findViewById(R.id.previewImage);
			previewImage.setImageDrawable(null);
		}
	}

	// method used to gather orientation of a specified image
	private int resolveBitmapOrientation(File bitmapFile) throws IOException {
		ExifInterface exif = null;
		exif = new ExifInterface(bitmapFile.getAbsolutePath());

		return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);
	}

	// method used to rotate image for displaying correctly in image view
	private Bitmap applyOrientation(Bitmap bitmap, int orientation) {
		int rotate = 0;
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate = 270;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate = 90;
			break;
		default:
			return bitmap;
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix mtx = new Matrix();
		mtx.postRotate(rotate);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	// gather the path to the image selected from the gallery (or other besides
	// camera) by parsing uri
	// from the data and obtain the path
	public String getPath(Uri uri) {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = getApplicationContext().getContentResolver().query(
						uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {

			}
		}

		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, Setting.class);
			startActivity(i);
			break;
		}
		return true;
	}

}
