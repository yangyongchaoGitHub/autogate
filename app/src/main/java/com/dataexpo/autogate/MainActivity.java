package com.dataexpo.autogate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.dataexpo.autogate.model.gate.ReportData;
import com.rfid.api.ADReaderInterface;
import com.rfid.api.GFunction;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;

public class MainActivity extends Activity implements OnClickListener
{
	final static int GET_REPORT_DATA = 1;
	final static int GET_REPORT_END = 2;
	private EditText ed_ipaddr = null;
	private EditText ed_port = null;
	private Button btn_open = null;
	private Button btn_close = null;
	private Button btn_get_dev_info = null;
	private Button btn_get_passing_counter = null;
	private Button btn_reset_passing_counter = null;
	private Button btn_reverse_direction = null;
	private Button btn_get_system_time = null;
	private Button btn_set_system_time = null;
	private Button btn_start_get_record = null;
	private Button btn_stop_get_record = null;
	private Button btn_clear_record = null;
	private Button btn_set_output = null;
	private TextView tv_report_cnt = null;
	private boolean bGetReportThrd = false;
	private Thread mGetReportThrd = null;// new Thread(new GetReportThrd());
	private TabHost myTabhost = null;
	private int[] layRes = { R.id.tab_lsg, R.id.tab_command, R.id.tab_report };
	private String[] layTittle = { "Device", "Command", "Report" };
	static ADReaderInterface m_reader = new ADReaderInterface();
	private List<ReportData> reportList = new ArrayList<ReportData>();
	private ReportAdapter reportAdapter = null;
	private ListView listReport = null;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		myTabhost = findViewById(R.id.tabhost);
		ed_ipaddr = findViewById(R.id.ed_ipaddr);
		ed_port = findViewById(R.id.ed_port);
		btn_open = findViewById(R.id.btn_open);
		btn_close = findViewById(R.id.btn_close);
		btn_get_dev_info = findViewById(R.id.btn_get_dev_info);
		btn_get_passing_counter = findViewById(R.id.btn_get_passing_counter);
		btn_reset_passing_counter = findViewById(R.id.btn_reset_passing_counter);
		btn_reverse_direction = findViewById(R.id.btn_reverse_direction);
		btn_get_system_time = findViewById(R.id.btn_get_system_time);
		btn_set_system_time = findViewById(R.id.btn_set_system_time);
		btn_start_get_record = findViewById(R.id.btn_start_get_record);
		btn_stop_get_record = findViewById(R.id.btn_stop_get_record);
		btn_clear_record = findViewById(R.id.btn_clear_record);
		btn_set_output = findViewById(R.id.btn_set_output);
		listReport = findViewById(R.id.list_record);
		tv_report_cnt = findViewById(R.id.tv_record_cnt);

		myTabhost.setup();
		for (int i = 0; i < layRes.length; i++)
		{
			TabSpec myTab = myTabhost.newTabSpec("tab" + i);
			myTab.setIndicator(layTittle[i]);
			myTab.setContent(layRes[i]);
			myTabhost.addTab(myTab);
		}
		myTabhost.setCurrentTab(0);

		btn_open.setOnClickListener(this);
		btn_close.setOnClickListener(this);
		btn_get_dev_info.setOnClickListener(this);
		btn_get_passing_counter.setOnClickListener(this);

		btn_reset_passing_counter.setOnClickListener(this);
		btn_reverse_direction.setOnClickListener(this);
		btn_get_system_time.setOnClickListener(this);
		btn_set_system_time.setOnClickListener(this);
		btn_start_get_record.setOnClickListener(this);
		btn_stop_get_record.setOnClickListener(this);
		btn_clear_record.setOnClickListener(this);
		btn_set_output.setOnClickListener(this);

		ViewGroup InventorytableTitle = (ViewGroup) findViewById(R.id.report_title);
		InventorytableTitle.setBackgroundColor(Color.rgb(255, 100, 10));

		reportAdapter = new ReportAdapter(this, reportList);
		listReport.setAdapter(reportAdapter);

		String sRemoteIp = GetHistory("LSGIP");
		String sRemotePort = GetHistory("LSGPORT");
		if ("".equals(sRemoteIp))
		{
			sRemoteIp = "192.168.0.222";
		}
		if ("".equals(sRemotePort))
		{
			sRemotePort = "6012";
		}
		ed_ipaddr.setText(sRemoteIp);
		ed_port.setText(sRemotePort);
		EnableAllCtrol(false);
	}

	private void MessageBox(String msg)
	{
		new AlertDialog.Builder(this).setTitle("").setMessage(msg)
				.setPositiveButton("OK", null).show();
	}

	private void saveHistory(String sKey, String val)
	{
		@SuppressLint("WorldReadableFiles")
		@SuppressWarnings("deprecation")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(sKey, val);
		editor.commit();
	}

	private String GetHistory(String sKey)
	{
		@SuppressWarnings("deprecation")
		@SuppressLint("WorldReadableFiles")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_WORLD_READABLE);
		return preferences.getString(sKey, "");
	}

	private void OpenDevice()
	{
		String sRemoteIp = ed_ipaddr.getText().toString();
		String sRemotePort = ed_port.getText().toString();
		String conStr = String.format(
				"RDType=LS405;CommType=NET;RemoteIp=%s;RemotePort=%s",
				sRemoteIp, sRemotePort);
		int iret = m_reader.RDR_Open(conStr);
		if (iret != ApiErrDefinition.NO_ERROR)
		{
			Toast.makeText(this, "Failed to open the deviece. err: " + iret,
					Toast.LENGTH_SHORT).show();
			return;
		}
		EnableAllCtrol(true);
		Toast.makeText(this, "Open the device successfully.",
				Toast.LENGTH_SHORT).show();

		saveHistory("LSGIP", sRemoteIp);
		saveHistory("LSGPORT", sRemotePort);
	}

	private void EnableAllCtrol(boolean isOpen)
	{
		ed_ipaddr.setEnabled(!isOpen);
		ed_port.setEnabled(!isOpen);
		btn_open.setEnabled(!isOpen);
		btn_close.setEnabled(isOpen);
		btn_get_dev_info.setEnabled(isOpen);
		btn_get_passing_counter.setEnabled(isOpen);
		btn_reset_passing_counter.setEnabled(isOpen);
		btn_reverse_direction.setEnabled(isOpen);
		btn_get_system_time.setEnabled(isOpen);
		btn_set_system_time.setEnabled(isOpen);
		btn_start_get_record.setEnabled(isOpen);
		btn_stop_get_record.setEnabled(false);
		btn_clear_record.setEnabled(isOpen);
	}

	private void CloseDevice()
	{
		if (mGetReportThrd != null && mGetReportThrd.isAlive())
		{
			MessageBox("Please stop the thread of getting the report first.");
			return;
		}
		m_reader.RDR_Close();
		EnableAllCtrol(false);
	}

	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		if (mGetReportThrd != null && mGetReportThrd.isAlive())
		{
			// stop the thread
			bGetReportThrd = false;
			try
			{
				mGetReportThrd.join();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

		if (m_reader.isReaderOpen())
		{
			m_reader.RDR_Close();
		}
		super.onDestroy();
	}

	private void GetDeviceInfo()
	{
		StringBuffer buffer = new StringBuffer();
		int iret = m_reader.RDR_GetReaderInfor(buffer);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			MessageBox(buffer.toString());
		}
		else
		{
			MessageBox("Failed to get the device information.");
		}
	}

	@SuppressLint("DefaultLocale")
	private void GetPassingCounter()
	{
		long[] inFlow = new long[1];
		long[] outFlow = new long[1];
		int iret = m_reader.RDR_GetPassingCounter(inFlow, outFlow);
		if (iret != ApiErrDefinition.NO_ERROR)
		{
			MessageBox("Failed to get the passing counter.");
			return;
		}
		String strInfo = String.format("In:%d;Out:%d", inFlow[0], outFlow[0]);
		MessageBox(strInfo);
	}

	private void ResetPassingCounter()
	{
		int iret = m_reader.RDR_ResetPassingCounter(3);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			MessageBox("Reset passing counter successfully");
		}
		else
		{
			MessageBox("Failed to reset passing counter.");
		}
	}

	private void ReverseDirection()
	{
		int iret = m_reader.RDR_ReverseInOutDirection();
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			MessageBox("Reverse the direction successfully");
		}
		else
		{
			MessageBox("Failed to reverse the direction.");
		}
	}

	private void SetSysTime()
	{
		TimeZone timeZone = TimeZone.getTimeZone("GMT+8"); // ����Ϊ������
		Calendar c = Calendar.getInstance();
		c.setTimeZone(timeZone);

		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int date = c.get(Calendar.DATE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);
		int iret = m_reader.RDR_SetSysTime(year, month, date, hour, min, sec);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			MessageBox("Set system time successfully.");
		}
		else
		{
			MessageBox("Failed to set the system time.");
		}
	}

	private void GetSysTime()
	{
		StringBuffer sTime = new StringBuffer();
		int iret = m_reader.RDR_GetSysTime(sTime);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			MessageBox(sTime.toString());
		}
		else
		{
			MessageBox("Failed to get the system time.");
		}
	}

	private void StartGetReport()
	{
		ClearList();
		btn_get_dev_info.setEnabled(false);
		;
		btn_get_passing_counter.setEnabled(false);
		btn_reset_passing_counter.setEnabled(false);
		btn_reverse_direction.setEnabled(false);
		btn_get_system_time.setEnabled(false);
		btn_set_system_time.setEnabled(false);

		mGetReportThrd = new Thread(new GetReportThrd());
		btn_start_get_record.setEnabled(false);
		btn_stop_get_record.setEnabled(true);
		btn_clear_record.setEnabled(false);
		mGetReportThrd.start();
	}

	private void ClearList()
	{
		reportList.clear();
		reportAdapter.notifyDataSetChanged();
		tv_report_cnt.setText("Record count:0");
	}

	private void SetOutput()
	{
		Object dnOutputOper = m_reader.RDR_CreateSetOutputOperations();
		m_reader.RDR_AddOneOutputOperation(dnOutputOper, (byte) 1, (byte) 3, 1,
				100, 100);
		int iret = m_reader.RDR_SetOutput(dnOutputOper);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			MessageBox("Set output successfully.");
		}
		else
		{
			MessageBox("Failed to set output.");
		}
	}

	private void StopGetReport()
	{
		if (mGetReportThrd != null && mGetReportThrd.isAlive())
		{
			bGetReportThrd = false;
			try
			{
				mGetReportThrd.join();
			}
			catch (InterruptedException e)
			{
			}
		}
		btn_get_dev_info.setEnabled(true);
		btn_get_passing_counter.setEnabled(true);
		btn_reset_passing_counter.setEnabled(true);
		btn_reverse_direction.setEnabled(true);
		btn_get_system_time.setEnabled(true);
		btn_set_system_time.setEnabled(true);
		btn_start_get_record.setEnabled(true);
		btn_stop_get_record.setEnabled(false);
		btn_clear_record.setEnabled(true);
		mGetReportThrd = null;
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_open:
			OpenDevice();
			break;
		case R.id.btn_close:
			CloseDevice();
			break;
		case R.id.btn_get_dev_info:
			GetDeviceInfo();
			break;
		case R.id.btn_get_passing_counter:
			GetPassingCounter();
			break;
		case R.id.btn_reset_passing_counter:
			ResetPassingCounter();
			break;
		case R.id.btn_reverse_direction:
			ReverseDirection();
			break;
		case R.id.btn_set_system_time:
			SetSysTime();
			break;
		case R.id.btn_get_system_time:
			GetSysTime();
			break;
		case R.id.btn_start_get_record:
			StartGetReport();
			break;
		case R.id.btn_stop_get_record:
			StopGetReport();
			break;
		case R.id.btn_clear_record:
			ClearList();
			break;
		case R.id.btn_set_output:
			SetOutput();
			break;
		default:
			break;
		}
	}

	private Handler mHandler = new MyHandler(this);

	private static class MyHandler extends Handler
	{
		private final WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity)
		{
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MainActivity pt = mActivity.get();
			if (pt == null)
			{
				return;
			}
			switch (msg.what)
			{
			case GET_REPORT_DATA:// get the report
				@SuppressWarnings("unchecked")
				Vector<ReportData> reports = (Vector<ReportData>) msg.obj;
				for (ReportData report : reports)
				{
					pt.reportList.add(new ReportData(report.getStrData(),
							report.getStrDirection(), report.getStrTime()));
				}
				pt.reportAdapter.notifyDataSetChanged();
				pt.tv_report_cnt.setText(String.format("Record count:%d",
						pt.listReport.getCount()));
				break;
			case GET_REPORT_END:// the thread end
				break;
			default:
				break;
			}
		}
	}

	static public class ReportAdapter extends BaseAdapter
	{
		private List<ReportData> list;
		private LayoutInflater inflater;

		public ReportAdapter(Context context, List<ReportData> list)
		{
			this.list = list;
			inflater = LayoutInflater.from(context);
		}

		public int getCount()
		{
			return list.size();
		}

		public Object getItem(int position)
		{
			return list.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			ReportData mReport = (ReportData) this.getItem(position);
			ViewHolder viewHolder;
			if (convertView == null)
			{
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.report_tittle, null);
				viewHolder.mTextData = (TextView) convertView
						.findViewById(R.id.tv_serial_number);
				viewHolder.mTextDirection = (TextView) convertView
						.findViewById(R.id.tv_direction);
				viewHolder.mTextTime = (TextView) convertView
						.findViewById(R.id.tv_report_time);
				convertView.setTag(viewHolder);
			}
			else
			{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.mTextData.setText(mReport.getStrData());
			viewHolder.mTextDirection.setText(mReport.getStrDirection());
			viewHolder.mTextTime.setText(mReport.getStrTime());

			return convertView;
		}

		private class ViewHolder
		{
			public TextView mTextData;
			public TextView mTextDirection;
			public TextView mTextTime;
		}
	}

	private class GetReportThrd implements Runnable
	{
		public void run()
		{
			bGetReportThrd = true;
			int iret;
			byte flag = 0;
			while (bGetReportThrd)
			{
				iret = m_reader.RDR_BuffMode_FetchRecords(flag);
				if (iret == 0)
				{
					flag = 1;
					int nCount = m_reader.RDR_GetTagReportCount();
					if (nCount > 0)
					{
						Vector<ReportData> mReports = new Vector<ReportData>();
						Object hReport = m_reader
								.RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
						while (hReport != null)
						{
							byte[] reportBuf = new byte[64];
							int[] nSize = new int[1];
							iret = ADReaderInterface.RDR_ParseTagDataReportRaw(
									hReport, reportBuf, nSize);
							if (iret == 0)
							{
								if (nSize[0] < 9)
								{
									hReport = m_reader
											.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
									continue;
								}
								// byte evntType;
								byte direction;
								byte[] time = new byte[6];
								int dataLen;

								// evntType = reportBuf[0];
								direction = reportBuf[1];
								for (int i = 0; i < 6; i++)
								{
									time[i] = reportBuf[2 + i];
								}
								dataLen = reportBuf[8];
								if (dataLen < 0)
								{
									dataLen += 256;
								}
								nSize[0] -= 9;
								if (nSize[0] < dataLen)
								{
									hReport = m_reader
											.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
									continue;
								}

								byte[] byData = new byte[dataLen];
								System.arraycopy(reportBuf, 9, byData, 0,
										dataLen);
								ReportData report = new ReportData();
								report.setStrData(GFunction.encodeHexStr(byData));
								if (direction == 1)
								{
									report.setStrDirection("In");
								}
								else if (direction == 2)
								{
									report.setStrDirection("Out");
								}
								else
								{
									report.setStrDirection("null");
								}
								report.setStrTime(GFunction.encodeHexStr(time));
								mReports.add(report);
								hReport = m_reader
										.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
							}
						}

						Message msg = mHandler.obtainMessage();
						msg.what = GET_REPORT_DATA;
						msg.obj = mReports;
						mHandler.sendMessage(msg);
					}
				}
			}
			Message msg = mHandler.obtainMessage();
			msg.what = GET_REPORT_END;
			mHandler.sendMessage(msg);
		}

	}
}
