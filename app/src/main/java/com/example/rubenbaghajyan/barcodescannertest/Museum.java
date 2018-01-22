package com.example.rubenbaghajyan.barcodescannertest;

/**
 * Created by rubenbaghajyan on 1/21/18.
 */

public class Museum {
	String id;
	String imagePath;
	String name;

	public Museum(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
