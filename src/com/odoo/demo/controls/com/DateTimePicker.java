package com.odoo.demo.controls.com;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.odoo.util.ODate;

public class DateTimePicker {

	public enum Type {
		Date, DateTime
	}

	private Context mContext = null;
	private Builder mBuilder;
	private DatePicker mDatePicker;
	private TimePicker mTimePicker;

	public DateTimePicker() {

	}

	public DateTimePicker(Context context, Builder builder) {
		mContext = context;
		mBuilder = builder;
	}

	public void show() {
		mDatePicker = new DatePicker(mContext);
		mDatePicker.setPickerCallback(callBack);
		mDatePicker.show();
	}

	PickerCallBack callBack = new PickerCallBack() {

		@Override
		public void onTimePick(String time) {
			mBuilder.getCallBack().onTimePick(time);
			mTimePicker.dismiss();
		}

		@Override
		public void onDatePick(String date) {
			mDatePicker.dismiss();
			if (mBuilder.getType() == Type.DateTime) {
				mTimePicker = new TimePicker(mContext);
				mTimePicker.setPickerCallback(callBack);
				mTimePicker.show();
			}
			mBuilder.getCallBack().onDatePick(date);
		}
	};

	public static class Builder {
		private Context mContext;
		private Type mType = Type.DateTime;
		private PickerCallBack mCallback;
		private String mDialogTitle = null;

		public Builder(Context context) {
			mContext = context;
		}

		public Builder setType(Type type) {
			mType = type;
			return this;
		}

		public Type getType() {
			return mType;
		}

		public Builder setCallBack(PickerCallBack callback) {
			mCallback = callback;
			return this;
		}

		public PickerCallBack getCallBack() {
			return mCallback;
		}

		public Builder setTitle(String title) {
			mDialogTitle = title;
			return this;
		}

		public Builder setTitle(int res_id) {
			mDialogTitle = mContext.getResources().getString(res_id);
			return this;
		}

		public String getDialogTitle() {
			return mDialogTitle;
		}

		public DateTimePicker build() {
			DateTimePicker picker = new DateTimePicker(mContext, this);
			return picker;
		}
	}

	public class DatePicker implements DatePickerDialog.OnDateSetListener,
			OnCancelListener {

		private PickerCallBack mCallback;
		private boolean called = false;
		private Dialog mDialog;

		public DatePicker(Context context) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			mDialog = new DatePickerDialog(context, this, year, month, day);
			mDialog.setOnCancelListener(this);
		}

		@Override
		public void onCancel(DialogInterface dialog) {

		}

		@Override
		public void onDateSet(android.widget.DatePicker view, int year,
				int monthOfYear, int dayOfMonth) {
			if (mCallback != null && !called) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				cal.set(Calendar.YEAR, year);
				Date now = cal.getTime();
				String date = new SimpleDateFormat(ODate.DEFAULT_DATE_FORMAT)
						.format(now);
				mCallback.onDatePick(date);
				called = true;
			}
		}

		public void setPickerCallback(PickerCallBack callback) {
			mCallback = callback;
		}

		public void show() {
			mDialog.show();
		}

		public void dismiss() {
			mDialog.dismiss();
		}
	}

	public class TimePicker implements TimePickerDialog.OnTimeSetListener,
			OnCancelListener {

		private PickerCallBack mCallback;
		private boolean called = false;
		private TimePickerDialog mDialog = null;

		public TimePicker(Context context) {

			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			mDialog = new TimePickerDialog(context, this, hour, minute, false);
			mDialog.setOnCancelListener(this);
		}

		@Override
		public void onTimeSet(android.widget.TimePicker view, int hourOfDay,
				int minute) {
			if (mCallback != null && !called) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				cal.set(Calendar.MILLISECOND, 0);
				Date now = cal.getTime();
				String time = new SimpleDateFormat(ODate.DEFAULT_TIME_FORMAT)
						.format(now);
				mCallback.onTimePick(time);
				called = true;
			}
		}

		@Override
		public void onCancel(DialogInterface dialog) {
		}

		public void setPickerCallback(PickerCallBack callback) {
			mCallback = callback;
		}

		public void show() {
			mDialog.show();
		}

		public void dismiss() {
			mDialog.dismiss();
		}
	}

	public interface PickerCallBack {
		public void onDatePick(String date);

		public void onTimePick(String time);
	}
}
