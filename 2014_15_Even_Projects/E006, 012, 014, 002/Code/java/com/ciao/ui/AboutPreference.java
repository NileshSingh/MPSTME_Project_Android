package com.ciao.ui;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

import com.ciao.utils.PhoneHelper;

public class AboutPreference extends Preference {
	public AboutPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setSummary();
	}

	public AboutPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		setSummary();
	}

    @Override
    protected void onClick() {
        super.onClick();
        final Intent intent = new Intent(getContext(), AboutActivity.class);
        getContext().startActivity(intent);
    }

    private void setSummary() {
		setSummary("Ciao " + PhoneHelper.getVersionName(getContext()));
	}
}

