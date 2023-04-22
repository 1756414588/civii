package com.game.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

public class XProperties extends Properties {
	private static final long serialVersionUID = -7234946412423331719L;
	private static final Charset PROPERTIES_ENCODING = Charset.forName("UTF-8");

	public XProperties() {

	}

	public boolean loadFile(String path) {
		return loadFile(new File(path));
	}

	public boolean loadFile(File file) {
		InputStreamReader is = null;
		try {
			is = new InputStreamReader(new FileInputStream(file), PROPERTIES_ENCODING);
			this.load(is);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
//			e.printStackTrace();
			return false;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	public String getString(String key, String defaultValue) {
		String value = getProperty(key, defaultValue);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	public String getString(String key) {
		return getString(key, "");
	}

	public Integer getInteger(String key, Integer defaltValue) {
		try {
			String value = getProperty(key);
			if (value == null) {
				return defaltValue;
			}

			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			// TODO: handle exception
			e.printStackTrace();
			throw e;
		}
	}

	public Boolean getBoolean(String key, Boolean defaltValue) {
		Integer value = getInteger(key, null);
		if (value != null) {
			return value != 0;
		}

		return Boolean.parseBoolean(getProperty(key));
	}

	@Override
	public String getProperty(String key) {
		// TODO Auto-generated method stub
		String value = super.getProperty(key);
		return value == null ? null : value.trim();
	}
}
