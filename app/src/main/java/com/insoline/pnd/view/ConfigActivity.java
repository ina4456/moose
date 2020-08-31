package com.insoline.pnd.view;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.insoline.pnd.R;
import com.insoline.pnd.SiteConstants;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.config.Constants;
import com.insoline.pnd.define.AppDefine;
import com.insoline.pnd.model.Popup;
import com.insoline.pnd.model.SelectionItem;
import com.insoline.pnd.remote.manager.ATCommandManager;
import com.insoline.pnd.utils.LogHelper;
import com.insoline.pnd.utils.StringUtil;
import com.insoline.pnd.view.custom.DebugWindow;
import com.thinkware.houston.externaldevice.service.IMainService;
import com.thinkware.houston.externaldevice.service.ITachoMeter;
import com.thinkware.houston.externaldevice.service.ITachoMeterCallback;
import com.thinkware.houston.externaldevice.service.IVacancyLight;
import com.thinkware.houston.externaldevice.service.IVacancyLightCallback;
import com.thinkware.houston.externaldevice.service.data.ServiceStatus;
import com.thinkware.houston.externaldevice.service.data.TachoMeterData;
import com.thinkware.houston.externaldevice.service.data.VacancyLightData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_CALL;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_COMPLEX;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_DRIVING;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_EXTRA_CHARGE;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_PAYMENT;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_PAYMENT_EXCLUDE_KUMHO;
import static com.thinkware.houston.externaldevice.service.data.TachoMeterData.STATUS_VACANCY;
import static com.thinkware.houston.externaldevice.service.data.VacancyLightData.DAY_OFF;
import static com.thinkware.houston.externaldevice.service.data.VacancyLightData.RESERVATION;
import static com.thinkware.houston.externaldevice.service.data.VacancyLightData.RIDDEN;
import static com.thinkware.houston.externaldevice.service.data.VacancyLightData.VACANCY;

public class ConfigActivity extends BaseActivity {

    public static final int REQUEST_CODE_CONFIG = 1000;

    private TextView tvTitle, tvAppVersion;
    private EditText etServiceNum, etCorporationCode, etCarNum;
    private RadioGroup rgCorporation;
    private RadioButton rbCorporationIndividual, rbCorporationCorp;

    private EditText etPst, etPsd, etRc, etRt, etCvt;
    private ToggleButton btnLs;

    private EditText etServerIP, etServerPort, etApiServerIP, etApiServerPort;

    private RadioGroup rgTachometerSignal;
    private RadioButton rbVacancyLight, rbTachometer;
    private Spinner spinnerTachometer;
    private TextView tvTachometerTest, tvVacancyLightTest;
    private Button btnTachometerTestInfo, btnTachometerTestRefresh, btnVacancyLightTest, btnVacancyLightTestReservation;
    private TextView tvModemNumber;
    private Button btnModemNumberTest;

    private Button btnSave, btnCancel;

    private ArrayAdapter spinnerTachometerAdapter;

    //service 관리
    private IMainService mExternalDeviceMainService = null;
    private ITachoMeter mTachometerService = null;
    private IVacancyLight mVacancyLightService = null;

    private boolean isVacancyLight = true;
    private String meterDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initView();
    }

    private void initView() {
        tvTitle = findViewById(R.id.tvTitle);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        etServiceNum = findViewById(R.id.etServiceNum); //서비스번호
        etCorporationCode = findViewById(R.id.etCorporationCode);   //회사 코드
        etCarNum = findViewById(R.id.etCarNum); //차량 번호

        //택시 종류 (개인/법인)
        rgCorporation = findViewById(R.id.rgCorporation);
        rbCorporationIndividual = findViewById(R.id.rbCorporationIndividual);
        rbCorporationCorp = findViewById(R.id.rbCorporationCorp);

        etPst = findViewById(R.id.etPst);
        etPsd = findViewById(R.id.etPsd);
        etRc = findViewById(R.id.etRc);
        etRt = findViewById(R.id.etRt);
        etCvt = findViewById(R.id.etCvt);
        btnLs = findViewById(R.id.btnLs);

        //콜서버정보
        etServerIP = findViewById(R.id.etServerIP);
        etServerPort = findViewById(R.id.etServerPort);
        //API 서버 정보
        etApiServerIP = findViewById(R.id.etApiServerIP);
        etApiServerPort = findViewById(R.id.etApiServerPort);

        //미터기 신호
        rgTachometerSignal = findViewById(R.id.rgTachometerSignal);
        rbVacancyLight = findViewById(R.id.rbVacancyLight);
        rbTachometer = findViewById(R.id.rbTachometer);

        //미터기 종류
        spinnerTachometer = findViewById(R.id.spinnerTachometer);
        //미터기 값
        tvTachometerTest = findViewById(R.id.tvTachometerTest);
        btnTachometerTestInfo = findViewById(R.id.btnTachometerTestInfo);
        btnTachometerTestRefresh = findViewById(R.id.btnTachometerTestRefresh);

        //빈차등 값
        tvVacancyLightTest = findViewById(R.id.tvVacancyLightTest);
        btnVacancyLightTest = findViewById(R.id.btnVacancyLightTest);
        btnVacancyLightTestReservation = findViewById(R.id.btnVacancyLightTestReservation);

        //모뎀번호
        tvModemNumber = findViewById(R.id.tvModemNumber);
        btnModemNumberTest = findViewById(R.id.btnModemNumberTest);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);


        //radioGroup
        rgCorporation.setOnCheckedChangeListener(onCheckedChangeListener);
        rgTachometerSignal.setOnCheckedChangeListener(onCheckedChangeListener);

        //spinner
        spinnerTachometerAdapter = ArrayAdapter.createFromResource(this, R.array.items_tachometer_type, R.layout.item_spinner);
        spinnerTachometerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTachometer.setAdapter(spinnerTachometerAdapter);
        spinnerTachometer.setOnItemSelectedListener(onItemSelectedListener);

        btnTachometerTestInfo.setOnClickListener(this);
        btnTachometerTestRefresh.setOnClickListener(this);
        btnVacancyLightTest.setOnClickListener(this);
        btnVacancyLightTestReservation.setOnClickListener(this);
        btnModemNumberTest.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        tvTitle.setOnClickListener(this);
        if (getScenarioService() != null)
            tvAppVersion.setOnTouchListener(mTouchListener);

        etServiceNum.setOnClickListener(this);
        etCorporationCode.setOnClickListener(this);
        etCarNum.setOnClickListener(this);
        etPst.setOnClickListener(this);
        etPsd.setOnClickListener(this);
        etRc.setOnClickListener(this);
        etRt.setOnClickListener(this);
        etCvt.setOnClickListener(this);
        etServerIP.setOnClickListener(this);
        etServerPort.setOnClickListener(this);
        etApiServerIP.setOnClickListener(this);
        etApiServerPort.setOnClickListener(this);

        initConfig();
    }

    //환경 설정 파일
    private void initConfig() {
        tvAppVersion.setText(getString(R.string.app_version, getConfigLoader().getAppVersion()));

        etServiceNum.setText(String.valueOf(getConfigLoader().getServiceNumber())); //서비스번호
        etCorporationCode.setText(String.valueOf(getConfigLoader().getCorporationCode()));   //회사 코드
        etCarNum.setText(String.valueOf(getConfigLoader().getCarId())); //차량 번호

        //택시 종류 (개인/법인)
        if (getConfigLoader().isCorporation()) {
            rgCorporation.check(R.id.rbCorporationCorp);
        } else {
            rgCorporation.check(R.id.rbCorporationIndividual);
        }

        etPst.setText(String.valueOf(getConfigLoader().getPst()));
        etPsd.setText(String.valueOf(getConfigLoader().getPsd()));
        etRc.setText(String.valueOf(getConfigLoader().getRc()));
        etRt.setText(String.valueOf(getConfigLoader().getRt()));
        etCvt.setText(String.valueOf(getConfigLoader().getCvt()));
        btnLs.setChecked(getConfigLoader().isLs());

        //콜서버정보
        etServerIP.setText(getConfigLoader().getCallServerIp());
        etServerPort.setText(String.valueOf(getConfigLoader().getCallServerPort()));
        //API 서버 정보
        etApiServerIP.setText(getConfigLoader().getApiServerIp());
        etApiServerPort.setText(String.valueOf(getConfigLoader().getApiServerPort()));

        //미터기 신호
        isVacancyLight = getConfigLoader().isVacancyLight();
        if (isVacancyLight) {
            rgTachometerSignal.check(R.id.rbVacancyLight);
            spinnerTachometer.setEnabled(false);

            tvTachometerTest.setVisibility(View.INVISIBLE);
            btnTachometerTestInfo.setEnabled(false);
            btnTachometerTestRefresh.setEnabled(false);

            tvVacancyLightTest.setVisibility(View.VISIBLE);
            btnVacancyLightTest.setEnabled(true);
            btnVacancyLightTestReservation.setEnabled(true);

        } else {
            rgTachometerSignal.check(R.id.rbTachometer);
            spinnerTachometer.setEnabled(true);

            tvTachometerTest.setVisibility(View.VISIBLE);
            btnTachometerTestInfo.setEnabled(true);
            btnTachometerTestRefresh.setEnabled(true);

            tvVacancyLightTest.setVisibility(View.INVISIBLE);
            btnVacancyLightTest.setEnabled(false);
            btnVacancyLightTestReservation.setEnabled(false);
        }

        //미터기 종류
        meterDeviceType = getConfigLoader().getMeterDeviceType();
        if (meterDeviceType != null) {
            int spinnerPosition = spinnerTachometerAdapter.getPosition(meterDeviceType);
            spinnerTachometer.setSelection(spinnerPosition);
        }

        //모뎀번호
        tvModemNumber.setText(getConfigLoader().getModemNumber());
    }

    @Override
    protected void onResume() {
        LogHelper.e("onResume()");
        super.onResume();

        bindAllServices();
    }

    @Override
    protected void onDestroy() {
        LogHelper.e("onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogHelper.e("onNewIntent()");
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        LogHelper.e("onBackPressed()");
//        super.onBackPressed();
        cancelProc();
    }

    private void cancelProc() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.tvTitle:
                showPopupCorporation();
                break;

            case R.id.btnSave:    //수정
                saveConfig();
                break;

            case R.id.btnCancel:    //취소
                cancelProc();
                break;

            case R.id.btnTachometerTestInfo:    //미터기 현재값
                tvTachometerTest.setText("");
                requestTachometerData();
                break;

            case R.id.btnTachometerTestRefresh:    //미터기 초기화
                tvTachometerTest.setText("");
                bindAllServices();
                break;

            case R.id.btnVacancyLightTest:    //빈차등 빈차
                setVacancyLightStatus(false);
                break;

            case R.id.btnVacancyLightTestReservation:    //빈차등 예약
                setVacancyLightStatus(true);
                break;

            case R.id.btnModemNumberTest:    //모뎀 재시도
                requestModemNumber();
                break;

            case R.id.etServiceNum:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_SERVICE_NUM
                        , getString(R.string.configuration_vehicle_service_num)
                        , etServiceNum.getText().toString(), true);
                break;

            case R.id.etCorporationCode:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_CORPORATION_CODE
                        , getString(R.string.configuration_vehicle_corporation_code)
                        , etCorporationCode.getText().toString(), true);
                break;

            case R.id.etCarNum:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_CAR_NUM
                        , getString(R.string.configuration_vehicle_number)
                        , etCarNum.getText().toString(), true);
                break;

            case R.id.etPst:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_PST
                        , getString(R.string.configuration_pst)
                        , etPst.getText().toString(), true);
                break;

            case R.id.etPsd:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_PSD
                        , getString(R.string.configuration_psd)
                        , etPsd.getText().toString(), true);
                break;

            case R.id.etRc:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_RC
                        , getString(R.string.configuration_rc)
                        , etRc.getText().toString(), true);
                break;

            case R.id.etRt:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_RT
                        , getString(R.string.configuration_rt)
                        , etRt.getText().toString(), true);
                break;

            case R.id.etCvt:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_CVT
                        , getString(R.string.configuration_cvt)
                        , etCvt.getText().toString(), true);
                break;

            case R.id.etServerIP:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_SERVER_IP
                        , getString(R.string.configuration_service_server_ip)
                        , etServerIP.getText().toString(), false);
                break;

            case R.id.etServerPort:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_SERVER_PORT
                        , getString(R.string.configuration_service_server_port)
                        , etServerPort.getText().toString(), true);
                break;

            case R.id.etApiServerIP:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_API_SERVER_IP
                        , getString(R.string.configuration_api_server_ip)
                        , etApiServerIP.getText().toString(), false);
                break;

            case R.id.etApiServerPort:
                showPopupDialog(Constants.DIALOG_TAG_CONFIG_API_SERVER_PORT
                        , getString(R.string.configuration_api_server_port)
                        , etApiServerPort.getText().toString(), true);
                break;
        }
    }

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            LogHelper.e("onCheckedChanged()");

            switch (checkedId) {
                case R.id.rbCorporationIndividual:  //개인
                    LogHelper.e("onCheckedChanged() : 개인 ");
                    break;

                case R.id.rbCorporationCorp:    //법인
                    LogHelper.e("onCheckedChanged() : 법인 ");
                    break;

                case R.id.rbVacancyLight:   //빈차등
                    spinnerTachometer.setEnabled(false);

                    tvTachometerTest.setVisibility(View.INVISIBLE);
                    btnTachometerTestInfo.setEnabled(false);
                    btnTachometerTestRefresh.setEnabled(false);

                    tvVacancyLightTest.setVisibility(View.VISIBLE);
                    btnVacancyLightTest.setEnabled(true);
                    btnVacancyLightTestReservation.setEnabled(true);
                    break;

                case R.id.rbTachometer: //미터기
                    spinnerTachometer.setEnabled(true);

                    tvTachometerTest.setVisibility(View.VISIBLE);
                    btnTachometerTestInfo.setEnabled(true);
                    btnTachometerTestRefresh.setEnabled(true);

                    tvVacancyLightTest.setVisibility(View.INVISIBLE);
                    btnVacancyLightTest.setEnabled(false);
                    btnVacancyLightTestReservation.setEnabled(false);
                    break;
            }
        }
    };

    //미터기 종류 변경
    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String meterType = parent.getItemAtPosition(position).toString();
            LogHelper.e("onItemSelected : " + position + "/" + meterType);
            changeTachometerType(meterType);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    @Override
    public void onDismissPopupDialog(String tag, Intent intent) {
        LogHelper.e("onDismissPopupDialog() : " + tag);

        hideInputKeyboard();
        if (intent != null) {
            String inputValue = intent.getStringExtra(Constants.DIALOG_INTENT_KEY_EDIT_TEXT);
            if (inputValue != null)
                inputValue = inputValue.trim();

            switch (tag) {
                case Constants.DIALOG_TAG_CONFIG_SERVICE_NUM:
                    etServiceNum.setText(inputValue);
                    break;

                case Constants.DIALOG_TAG_CONFIG_CORPORATION_CODE:
                    etCorporationCode.setText(inputValue);
                    break;

                case Constants.DIALOG_TAG_CONFIG_CAR_NUM:
                    if (inputValue != null && inputValue.length() == SiteConstants.LIMIT_LENGTH_CAR_NUMBER) {
                        etCarNum.setText(inputValue);
                    } else {
                        showPopupDialog(getString(R.string.popup_msg_invalid_car_number_s));
                    }
                    break;

                case Constants.DIALOG_TAG_CONFIG_PST:
                    etPst.setText(inputValue);
                    break;

                case Constants.DIALOG_TAG_CONFIG_PSD:
                    etPsd.setText(inputValue);
                    break;

                case Constants.DIALOG_TAG_CONFIG_RC:
                    etRc.setText(inputValue);
                    break;

                case Constants.DIALOG_TAG_CONFIG_RT:
                    etRt.setText(inputValue);
                    break;

                case Constants.DIALOG_TAG_CONFIG_CVT:
                    etCvt.setText(inputValue);
                    break;

                case Constants.DIALOG_TAG_CONFIG_SERVER_IP:
                    if (inputValue != null && !StringUtil.isEmptyString(inputValue)) {
                        etServerIP.setText(inputValue);
                    } else {
                        showPopupDialog(getString(R.string.popup_msg_invalid_server_ip));
                    }
                    break;

                case Constants.DIALOG_TAG_CONFIG_SERVER_PORT:
                    if (inputValue != null && !StringUtil.isEmptyString(inputValue)) {
                        etServerPort.setText(inputValue);
                    } else {
                        showPopupDialog(getString(R.string.popup_msg_invalid_server_port));
                    }
                    break;

                case Constants.DIALOG_TAG_CONFIG_API_SERVER_IP:
                    if (inputValue != null && !StringUtil.isEmptyString(inputValue)) {
                        etApiServerIP.setText(inputValue);
                    } else {
                        showPopupDialog(getString(R.string.popup_msg_invalid_api_server_ip));
                    }
                    break;

                case Constants.DIALOG_TAG_CONFIG_API_SERVER_PORT:
                    if (inputValue != null && !StringUtil.isEmptyString(inputValue)) {
                        etApiServerPort.setText(inputValue);
                    } else {
                        showPopupDialog(getString(R.string.popup_msg_invalid_api_server_port));
                    }
                    break;

                case Constants.DIALOG_TAG_CONFIG_CORPORATION:   //회사 선택
                    inputValue = intent.getStringExtra(Constants.DIALOG_INTENT_KEY_RADIO_BUTTON);
                    LogHelper.e("DIALOG_TAG_CONFIG_CORPORATION : " + inputValue);

                    String[] splitValue = inputValue.split("/");
                    if (splitValue[1].trim().equals(getString(R.string.configuration_vehicle_corporation_corp))) {
                        rbCorporationCorp.setChecked(true);
                    } else {
                        rbCorporationIndividual.setChecked(true);
                    }

                    etServiceNum.setText(splitValue[2].trim());
                    etCorporationCode.setText(splitValue[3].trim());
                    etServerIP.setText(splitValue[4].trim());
                    etServerPort.setText(splitValue[5].trim());
                    etApiServerIP.setText(splitValue[6].trim());
                    etApiServerPort.setText(splitValue[7].trim());
                    break;

                case Constants.DIALOG_TAG_CONFIG_ADMIN:    //디버깅 윈도우 호출
                    //디버깅 창 표시 암호(MMdd33)
                    SimpleDateFormat formatter = new SimpleDateFormat("MMdd", Locale.KOREA);
                    Date currentTime = new Date();
                    String debugWindowPassword = formatter.format(currentTime) + "33";
                    if (debugWindowPassword.equals(inputValue)) {
                        showDebugWindow(false);
                    } else {
                        showPopupDialog(getString(R.string.fail_cert));
                    }
                    break;
            }
        } else {
            switch (tag) {
                case Constants.DIALOG_TAG_CONFIG_RESTART:
                    //환경설정 수정시 앱 재실행
                    logout();
                    finish();
                    break;
            }
        }
    }

    //디버깅 윈도우 호출
    private long mDown;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mDown = System.currentTimeMillis();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - mDown > 1000) {
                    checkPermission();
                }
                return true;
            }
            return false;
        }
    };


    //디버깅 윈도우
    public void showDebugWindow(boolean moveLogList) {
        DebugWindow window = new DebugWindow(getScenarioService(), getConfigLoader());
        window.show(moveLogList);
    }


    /**
     * 환경 설정 저장 - 로그아웃 -> 앱재실행
     */
    private void saveConfig() {

        //정보 저장
        getConfigLoader().setServiceNumber(Integer.parseInt(etServiceNum.getText().toString())); //서비스번호
        getConfigLoader().setCorporationCode(Integer.parseInt(etCorporationCode.getText().toString()));   //회사 코드
        getConfigLoader().setCarId(Integer.parseInt(etCarNum.getText().toString())); //차량 번호

        //택시 종류 (개인/법인) - 법인인 경우 자동로그인 false
        if (rgCorporation.getCheckedRadioButtonId() == R.id.rbCorporationCorp) {
            getConfigLoader().setCorporation(true);
            getConfigLoader().setAutoLogin(false);
        } else {
            getConfigLoader().setCorporation(false);
        }

        getConfigLoader().setPst(Integer.parseInt(etPst.getText().toString()));
        getConfigLoader().setPsd(Integer.parseInt(etPsd.getText().toString()));
        getConfigLoader().setRc(Integer.parseInt(etRc.getText().toString()));
        getConfigLoader().setRt(Integer.parseInt(etRt.getText().toString()));
        getConfigLoader().setCvt(Integer.parseInt(etCvt.getText().toString()));

        boolean isLogging = getConfigLoader().isLs();
        getConfigLoader().setLs(btnLs.isChecked());

        //콜서버정보
        getConfigLoader().setCallServerIp(etServerIP.getText().toString());
        getConfigLoader().setCallServerPort(Integer.parseInt(etServerPort.getText().toString()));
        //API 서버 정보
        getConfigLoader().setApiServerIp(etApiServerIP.getText().toString());
        getConfigLoader().setApiServerPort(Integer.parseInt(etApiServerPort.getText().toString()));

        //미터기 신호 / 미터기 종류
        if (rgTachometerSignal.getCheckedRadioButtonId() == R.id.rbVacancyLight) {
            getConfigLoader().setSerialPortType(SiteConstants.SERIAL_PORT_VACANCYLIGHT);
            getConfigLoader().setVacancyLight(true);
            getConfigLoader().setMeterDeviceType(null);
        } else {
            getConfigLoader().setSerialPortType(SiteConstants.SERIAL_PORT_TACHOMETER);
            getConfigLoader().setVacancyLight(false);
            getConfigLoader().setMeterDeviceType(spinnerTachometer.getSelectedItem().toString());
        }

        //저장시에는 개인/법인 구분없이 자동로그인 처리 안함 (인증 후 개인인경우 자동로그인 처리)
        getConfigLoader().setAutoLogin(false);

        //설정 저장
        getConfigLoader().save();

        if (isLogging != getConfigLoader().isLs()) {
            getConfigLoader().mkLoggingDir();
        }

        //환경설정 수정시 앱 재실행 안내
        Popup popup = new Popup
                .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_CONFIG_RESTART)
                .setTitle(getString(R.string.popup_title_alert))
                .setContent(getString(R.string.popup_msg_config_needed_restart))
                .setDismissSecond(5)
                .build();
        showPopupDialog(popup);

        setResult(RESULT_OK);
    }

    //에러 팝업
    private void showPopupDialog(String strContent) {
        Popup popup = new Popup
                .Builder(Popup.TYPE_ONE_BTN_NORMAL, Constants.DIALOG_TAG_CONFIG_INVALID)
                .setTitle(getString(R.string.popup_title_alert))
                .setContent(strContent)
                .build();
        showPopupDialog(popup);
    }

    //입력 팝업
    private void showPopupDialog(String strTag, String strTitle, String strContent, boolean isInteger) {
        Popup popup = new Popup
                .Builder(Popup.TYPE_TWO_BTN_EDIT_TEXT, strTag)
                .setTitle(strTitle)
                .setContent(strContent)
                .setIsHiddenStatusBar(true)
                .setIsIntegerForEditText(isInteger)
                .build();
        showPopupDialog(popup);
    }

    private void showPopupCorporation() {
        ArrayList<SelectionItem> corporationList = new ArrayList<>();
        List<String> list = Arrays.asList(getResources().getStringArray(R.array.items_configuration_corporation));
        for (String content : list) {
            SelectionItem item = new SelectionItem();
            item.setItemContent(content);
            corporationList.add(item);
        }

        Popup popup = new Popup
                .Builder(Popup.TYPE_RADIO_TWO_BTN_LIST, Constants.DIALOG_TAG_CONFIG_CORPORATION)
                .setTitle(getString(R.string.configuration_vehicle_corporation_code))
                .setSelectionItems(corporationList)
                .setIsHiddenStatusBar(false)
                .build();
        showPopupDialog(popup);
    }

    //디버깅 창 표시를 위한 암호를 입력 받는다.
    private void showPopupDebugPass() {
        Popup popup = new Popup
                .Builder(Popup.TYPE_TWO_BTN_EDIT_TEXT, Constants.DIALOG_TAG_CONFIG_ADMIN)
                .setTitle(getString(R.string.configuration_debug_admin_password))
                .setIsHiddenStatusBar(true)
                .setIsSecureTextType(true)
                .build();
        showPopupDialog(popup);
    }

    /**
     * ACTION_MANAGE_OVERLAY_PERMISSION
     */
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_PERMISSION_OVERLAY);
            } else {
                showPopupDebugPass();
            }
        } else {
            showPopupDebugPass();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_OVERLAY) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리
                showPopupDialog("권한 동의 필수");
            } else {
                showPopupDebugPass();
            }
        }
    }


    /**
     * 모뎀 전화번호를 가져온다.
     */
    private void requestModemNumber() {
        //초기화
        tvModemNumber.setText("");
        ATCommandManager.getInstance().request(
                ATCommandManager.CMD_MODEM_NO,
                new ATCommandManager.IModemListener() {
                    @Override
                    public void onModemResult(String result) {
                        LogHelper.e("modem result : " + result);
                        if (result != null && result.contains(ATCommandManager.CMD_MODEM_NO_CONTAINS)) {
                            String modemNumber = ATCommandManager.getInstance().parseModemNumber(result);
                            modemNumber = StringUtil.changePhonePrefixToDomestic(modemNumber);
                            LogHelper.e("modem number : " + modemNumber);

                            getConfigLoader().setModemNumber(modemNumber);
                            if (!StringUtil.isEmptyString(modemNumber)) {
                                tvModemNumber.setText(modemNumber);
                            } else {
                                tvModemNumber.setText(getString(R.string.fail_modem_number));
                            }
                        }
                    }
                });
    }

    public void bindAllServices() {
        mExternalDeviceMainService = null;
        mTachometerService = null;
        mVacancyLightService = null;

        try {
            unbindService(externalDeviceMainConnection);
            unbindService(tachometerConnection);
            unbindService(vacancyLightConnection);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
        }

        bindServices();
        bindCallbackServices();
    }


    private void bindServices() {
        LogHelper.d("bindServices()");
        if (mExternalDeviceMainService == null) {
            Intent intent = new Intent();
            intent.setAction(AppDefine.PKG_NAME_EXTERNAL_DEVICE_MAIN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                intent.setPackage(AppDefine.PKG_NAME_EXTERNAL_DEVICE);

            intent.putExtra(Constants.INTENT_KEY_PORT_PATH_VACANCY, SiteConstants.SERIAL_PORT_VACANCYLIGHT);
            intent.putExtra(Constants.INTENT_KEY_PORT_PATH_METER, SiteConstants.SERIAL_PORT_TACHOMETER);
            intent.putExtra(Constants.INTENT_KEY_METER_TYPE, spinnerTachometer.getSelectedItem().toString());

            startService(intent); //초기화 및 시작을 위한 스타티드 서비스 연결
            bindService(intent, externalDeviceMainConnection, BIND_AUTO_CREATE); //포트, 미터기 변경을 위한 위한 바운드 서비스 연결
        }
    }

    private void bindCallbackServices() {
        LogHelper.d("bindCallbackServices()");
        if (mTachometerService == null) {
            Intent intent = new Intent();
            intent.setAction(AppDefine.PKG_NAME_EXTERNAL_DEVICE_TACHOMETER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                intent.setPackage(AppDefine.PKG_NAME_EXTERNAL_DEVICE);
            bindService(intent, tachometerConnection, Context.BIND_AUTO_CREATE);
        }

        if (mVacancyLightService == null) {
            Intent intent = new Intent();
            intent.setAction(AppDefine.PKG_NAME_EXTERNAL_DEVICE_VACANCYLIGHT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                intent.setPackage(AppDefine.PKG_NAME_EXTERNAL_DEVICE);
            bindService(intent, vacancyLightConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * ExternalDevices
     */
    private ServiceConnection externalDeviceMainConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogHelper.e("ExternalDevice MainService onServiceConnected()");
            mExternalDeviceMainService = IMainService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogHelper.e("ExternalDevice MainService onServiceDisconnected()");
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService = null;
            }
        }
    };

    /**
     * ExternalDevices aidl 연동 외부 메서드
     */
    // 시리얼 포트 변경
    public void changeSerialPort(String serialPortPath) {
        LogHelper.write("#### changeSerialPort : " + serialPortPath);
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.changeSerialPortPath(serialPortPath);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 미터기 타입 변경
    public void changeTachometerType(String tachometerType) {
        LogHelper.write("#### changeTachometerType : " + tachometerType);
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.changeTachometerType(tachometerType);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 미터기 데이터 요청
    public void requestTachometerData() {
        LogHelper.write("#### requestTachometerData");
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.sendInitDataToTachometer();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 빈차등 상태 변경
    public void setVacancyLightStatus(boolean isReservation) {
        LogHelper.write("#### setVacancyLightStatus reservation ? : " + isReservation);
        try {
            if (mExternalDeviceMainService != null) {
                mExternalDeviceMainService.setVacancyLightStatus(isReservation);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Tachometer callback service
    private ServiceConnection tachometerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogHelper.e("tachometerConnection onServiceConnected()");
            if (service != null) {
                mTachometerService = ITachoMeter.Stub.asInterface(service);
                if (mTachometerService != null) {
                    try {
                        mTachometerService.registerCallback(tachometerCallback);
                        tachometerCallback.onServiceStatus(ServiceStatus.SERVICE_LAUNCHED);
                        if (mTachometerService.getCurrentData() != null) {
                            tachometerCallback.onReceive(mTachometerService.getCurrentData());
                        }
                        LogHelper.e("tachometer service callback registered");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogHelper.e("TachoMeterService onServiceDisconnected()");
            if (mTachometerService != null) {
                try {
                    tachometerCallback.onServiceStatus(ServiceStatus.SERVICE_NOT_LAUNCHED);
                    mTachometerService.unregisterCallback(tachometerCallback);
                } catch (RemoteException e) {
//                    e.printStackTrace();
                    LogHelper.e("RemoteException : " + e.toString());
                } finally {
                    mTachometerService = null;
                }
            }
        }
    };

    // Tachometer callback
    private ITachoMeterCallback tachometerCallback = new ITachoMeterCallback.Stub() {
        @Override
        public void onReceive(final TachoMeterData data) {
            LogHelper.i("tachometer data : " + data.toString());

            String strStatus = "null";
            if (data != null) {
                LogHelper.i("미터기 명령: 0x%x, 상태: %d, 요금: %d, 거리: %d",
                        data.getCommand(), data.getStatus(), data.getFare(), data.getMileage());

                strStatus = getStrTachometerStatus(data.getStatus()) + " / " + data.getStatus();
            }

            setTachometerTest(strStatus);
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d(">> 미터기 서비스 상태: " + status);
            if (status != ServiceStatus.NO_ERROR
                    && status != ServiceStatus.SERVICE_NOT_LAUNCHED
                    && status != ServiceStatus.SERVICE_LAUNCHED) {
                LogHelper.write("#### 미터기 ->  에러");

                setTachometerTest("Error");
            }
        }
    };


    // Vacancy Light callback service
    private ServiceConnection vacancyLightConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                LogHelper.e("vacancyLightService onServiceConnected()");
                mVacancyLightService = IVacancyLight.Stub.asInterface(service);
                try {
                    mVacancyLightService.registerCallback(vacancyLightCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogHelper.e("VacancyLightService onServiceDisconnected()");
            if (mVacancyLightService != null) {
                try {
                    mVacancyLightService.unregisterCallback(vacancyLightCallback);
                } catch (RemoteException e) {
//                    e.printStackTrace();
                    LogHelper.e("RemoteException : " + e.toString());
                }
                mVacancyLightService = null;
            }
        }
    };

    // VacancyLight callback
    private IVacancyLightCallback vacancyLightCallback = new IVacancyLightCallback.Stub() {
        @Override
        public void onReceive(VacancyLightData data) throws RemoteException {
            LogHelper.i("vacancyLight data : " + data.toString());

            String strStatus = "null";
            if (data != null) {
                LogHelper.i("빈차등 명령: " + data.getStatus());
                strStatus = getStrVacancyLightStatus(data.getStatus()) + " / " + data.getStatus();
            }

            setVacancyLightTest(strStatus);
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.e(">> 빈차등 서비스 상태: " + status);
            // FIXME: 2019-10-04 아래 주석 해제시 externalDevice NPE가 발생함. 원인파악 필요
            if (status != ServiceStatus.NO_ERROR
                    && status != ServiceStatus.SERVICE_NOT_LAUNCHED
                    && status != ServiceStatus.SERVICE_LAUNCHED
                    && status != 200) { //FINISHED_WRITE_DATA 데이터 쓰기 완료

                setVacancyLightTest("Error");
            }
        }
    };

    private void setTachometerTest(final String str) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                tvTachometerTest.setText(str);
            }
        }, 100);
    }

    private void setVacancyLightTest(final String str) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                tvVacancyLightTest.setText(str);
            }
        }, 100);
    }


    public String getStrTachometerStatus(int status) {
        LogHelper.e("getStrTachometerStatus : " + status);
        String strStatus = "";
        switch (status) {
            case STATUS_VACANCY:
                LogHelper.i("@@ 미터기 : 빈차 (STATUS_VACANCY)");
                strStatus = Constants.STATUS_VACANCY;
                break;

            case STATUS_DRIVING:
                LogHelper.i("@@ 미터기 : 주행 (STATUS_DRIVING)");
                strStatus = Constants.STATUS_DRIVING;
                break;

            case STATUS_EXTRA_CHARGE:
                LogHelper.i("@@ 미터기 : 할증 (STATUS_EXTRA_CHARGE)");
                strStatus = Constants.STATUS_EXTRA_CHARGE;
                break;

            case STATUS_CALL:
                LogHelper.i("@@ 미터기 : 호출 (STATUS_CALL)");
                strStatus = Constants.STATUS_CALL;
                break;

            case STATUS_PAYMENT:
                LogHelper.i("@@ 미터기 : 지불 (STATUS_PAYMENT) / 금호,알파엠");
                strStatus = Constants.STATUS_PAYMENT;
                break;

            case STATUS_PAYMENT_EXCLUDE_KUMHO:
                LogHelper.i("@@ 미터기 : 지불 (STATUS_PAYMENT) / 한국,광신");
                strStatus = Constants.STATUS_PAYMENT;
                break;

            case STATUS_COMPLEX:
                LogHelper.i("@@ 미터기 : 복합/시외 (STATUS_OUT_CITY_OR_COMPLEX)");
                strStatus = Constants.STATUS_OUT_CITY_OR_COMPLEX;
                break;
        }
        return strStatus;
    }


    public String getStrVacancyLightStatus(int status) {
        LogHelper.e("setStrVacancyLightStatus : " + status);
        String strStatus = "";
        switch (status) {
            case VACANCY:
                LogHelper.i("@@ 빈차등 : 빈차 (STATUS_VACANCY)");
                strStatus = Constants.STATUS_VACANCY;
                break;

            case RIDDEN:
                LogHelper.i("@@ 빈차등 : 주행 (STATUS_DRIVING)");
                strStatus = Constants.STATUS_DRIVING;
                break;

            case RESERVATION:
                LogHelper.i("@@ 빈차등 : 예약 (STATUS_RESERVATION)");
                strStatus = Constants.STATUS_RESERVATION;
                break;

            case DAY_OFF:
                LogHelper.i("@@ 빈차등 : 휴무 (STATUS_DAYOFF)");
                strStatus = Constants.STATUS_DAYOFF;
                break;
        }
        return strStatus;
    }

}
