package com.example.rubenbaghajyan.barcodescannertest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

	static final String GEO_LOC_LINK = "https://www.google.com/maps/search/";
	SurfaceView cameraPreview;
	BarcodeDetector barcodeDetector;
	CameraSource cameraSource;

	List<Museum> museums = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner);


		cameraPreview = findViewById(R.id.scanner_view);
		barcodeDetector = new BarcodeDetector.Builder(this).build();


		museums.add(new Museum("40.19025826324782,44.51328635215759", "Aram Khachatryan Museum"));
		museums.add(new Museum("40.18303736176448,44.50966000556946", "Eghishe Charents House and Museum"));
		museums.add(new Museum("40.17854542574362,44.51381206512451", "History Museum of Armenia"));
		museums.add(new Museum("40.19107784026421,44.5130181312561", "Avetik Isahakyan House-Museum"));
		museums.add(new Museum("40.17878980536867,44.5001669973135", "Sergey Paradjanov Museum"));
		museums.add(new Museum("40.18775849217955,44.509949684143066", "Hovhanes Tumanyan Museum"));


		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels;
		int height = displayMetrics.heightPixels;
		cameraSource = new CameraSource.Builder(this, barcodeDetector)
				.setAutoFocusEnabled(true)
				.setRequestedPreviewSize(width, height)
				.build();
		createCameraSource();

	}


	private void createCameraSource() {


		cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder surfaceHolder) {
				checkCameraPermissionAndStart();
			}

			@Override
			public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
				cameraSource.stop();
			}
		});

		barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
			@Override
			public void release() {

			}

			@Override
			public void receiveDetections(Detector.Detections<Barcode> detections) {
				SparseArray<Barcode> barcodes = detections.getDetectedItems();


				if (barcodes.size() > 0) {
					barcodeDetector.setProcessor(null);
					Intent intent = new Intent(ScannerActivity.this, MapActivity.class);
					intent.putExtra("barcode", barcodes.valueAt(0));
					Barcode barcode = barcodes.valueAt(0);
					String value = barcode.displayValue;
					if (!TextUtils.isEmpty(value) && value.startsWith(GEO_LOC_LINK)) {
						String geoLoc = value.substring(value.lastIndexOf("/") + 1, value.length());
						String[] latLong = geoLoc.split(",");
						double latitude = Double.parseDouble(latLong[0]);
						double longitude = Double.parseDouble(latLong[1]);

						intent.putExtra("latitude", latitude);
						intent.putExtra("longitude", longitude);
						intent.putExtra("marker_title", getNameById(geoLoc));
						startActivity(intent);
						finish();
					}
					setResult(CommonStatusCodes.SUCCESS, intent);
					finish();
				}
			}
		});

	}

	private void checkCameraPermissionAndStart() {
		try {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(ScannerActivity.this, new String[]{Manifest.permission.CAMERA}, 1234);
				return;
			}
			cameraSource.start(cameraPreview.getHolder());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == 1234 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			checkCameraPermissionAndStart();
		}
	}

	private String getNameById(String id){
		String name = " Unknown";
		for (Museum museum : museums) {
			if (museum.id.equals(id)){
				name = museum.name;
			}
		}
		return name;
	}
}
