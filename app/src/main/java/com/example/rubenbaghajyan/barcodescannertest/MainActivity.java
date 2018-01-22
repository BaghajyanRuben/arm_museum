package com.example.rubenbaghajyan.barcodescannertest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity {

	TextView statusText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		statusText = findViewById(R.id.scanner_result);

		findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				statusText.setVisibility(View.GONE);
				Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
				startActivityForResult(intent, 123);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 123 && resultCode == CommonStatusCodes.SUCCESS && data != null) {
			Barcode barcode = data.getParcelableExtra("barcode");
			statusText.setVisibility(View.VISIBLE);
			statusText.setText(barcode.displayValue);
		}
	}
}
