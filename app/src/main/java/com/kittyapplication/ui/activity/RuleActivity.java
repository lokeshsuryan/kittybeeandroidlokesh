package com.kittyapplication.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.services.CreateGroupIntentService;
import com.kittyapplication.ui.viewinterface.KittyRuleView;
import com.kittyapplication.ui.viewmodel.KittyRulesViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Pintu Riontech on 6/8/16.
 */
public class RuleActivity extends BaseActivity implements KittyRuleView {
    private static final String TAG = RuleActivity.class.getSimpleName();
    private KittyRulesViewModel mViewModel;
    //private CustomEditTextNormal mEdtHost;
    private EditText mEdtKittyAmount, mEdtKittyTime, mEdtHost, mEdtKittyDate,
            mEdtFineAmount;
    private EditText mEdtFoodAmount, mEdtDrinkAmount, mEdtPuncAmountOne, mEdtPuncAmountTwo;
    private EditText mEdtPuncTimeOne, mEdtPuncTimeTwo, mEdtNote, mEdtGameAmount, mEdtTambolaAmount;
    private CheckBox mCbDestinantion;
    private Spinner mSpnDays, mSpnWeek, mSpnFood, mSpnDrink,
            mSpnTambola, mSpnGames, mSpnPuncType;
    private Spinner mSpnHost;
    private TextView mTxtSubmit;
    private ImageView mImgHeder;
    private LinearLayout mLLlayout;
    private boolean isDisplay = false;
    private TextView mTxtOptional;
    private boolean isDiaryData = false;
    private boolean isFromChat = false;
    private boolean isFromSetting = false;

    private View viewDividerPuncTime, viewDividerPuncTimeTwo,
            viewDividerPuncAmount, viewDividerPuncAmountTwo,
            viewDividerFoodAmount, viewDividerDrinkAmount,
            viewDividerTamBolaAmount, viewDividerGamesAmount;

    private TextView txtPuncTimeOne, txtPuncTimeTwo, txtPuncAmountOne,
            txtPuncAmountTwo, txtlabelFoodAmount, txtlabelDrinkAmount,
            txtTamBolaAmount, txtGamesAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(RuleActivity.this).inflate(
                R.layout.activity_kitty_rules, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        mViewModel = new KittyRulesViewModel(this);
        view.findViewById(R.id.layout_rule_container).requestFocus();
        setView(view);

        // Comes from kitty Creation means create new group
        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_KITTY_DATA)) {
            String data = getIntent().getStringExtra(AppConstant.INTENT_KITTY_DATA);
            mViewModel.getData(data);
        }

        // Comes from Diary Data
        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_DIARY_DATA)) {
            isDiaryData = true;
            String data = getIntent().getStringExtra(AppConstant.INTENT_DIARY_DATA);
            mViewModel.getDiaryData(data);
        }

        // Comes from Setting
        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.SETTING_DATA)) {
            isFromSetting = true;
            String data = getIntent().getStringExtra(AppConstant.SETTING_DATA);
            mViewModel.getDiaryData(data);
        }

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.EXTRA_IS_CREATE_KITTY)) {
            isFromChat = true;
        }
    }

    private void setView(View view) {
        mEdtKittyAmount = (EditText) view.findViewById(R.id.edtKittyRuleKittyAmount);
//        Utils.hideKeyboard(this, mEdtKittyAmount);
        mEdtKittyTime = (EditText) view.findViewById(R.id.edtKittyRuleFirstKittyTime);
        mEdtKittyDate = (EditText) view.findViewById(R.id.edtKittyRuleFirstKittyDate);
        mEdtFoodAmount = (EditText) view.findViewById(R.id.edtKittyRuleKittyFoodAmount);
        mEdtDrinkAmount = (EditText) view.findViewById(R.id.edtKittyRuleKittyDrinkAmount);
        mEdtPuncAmountOne = (EditText) view.findViewById(R.id.edtKittyRuleKittyPunctualityAmountOne);
        mEdtPuncAmountTwo = (EditText) view.findViewById(R.id.edtKittyRuleKittyPunctualityAmountTwo);
        mEdtPuncTimeOne = (EditText) view.findViewById(R.id.edtKittyRuleKittyPunctualityAmountTimeOne);
        mEdtPuncTimeTwo = (EditText) view.findViewById(R.id.edtKittyRuleKittyPunctualityAmountTimeTwo);
        mEdtNote = (EditText) view.findViewById(R.id.edtKittyRuleNote);
        mEdtGameAmount = (EditText) view.findViewById(R.id.edtKittyRuleKittyGamesAmount);
        mEdtTambolaAmount = (EditText) view.findViewById(R.id.edtKittyRuleKittyTambolaAmount);
        mCbDestinantion = (CheckBox) view.findViewById(R.id.cbKittyRuleDestination);
        mTxtSubmit = (TextView) view.findViewById(R.id.txtKittyRuleSubmit);
        mImgHeder = (ImageView) view.findViewById(R.id.imgKittyRule);
        mSpnDays = (Spinner) view.findViewById(R.id.spnKittyRulesDays);
        mSpnWeek = (Spinner) view.findViewById(R.id.spnKittyRulesWeek);
        mSpnFood = (Spinner) view.findViewById(R.id.spnKittyRulesFood);
        mSpnDrink = (Spinner) view.findViewById(R.id.spnKittyRulesDrink);
        mSpnTambola = (Spinner) view.findViewById(R.id.spnKittyRulesTamBola);
        mSpnGames = (Spinner) view.findViewById(R.id.spnKittyRulesGames);
        mSpnPuncType = (Spinner) view.findViewById(R.id.spnKittyRulesPunctuality);
        mSpnHost = (Spinner) view.findViewById(R.id.spnNoOfHost);
        mSpnHost.setSelection(0);
        mEdtFineAmount = (EditText) view.findViewById(R.id.edtKittyRuleKittyFinePunctuality);

        mTxtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDiaryData && !isFromSetting) {
                    mViewModel.submit();
                } else {
                    mViewModel.submitDiaryData();
                }
            }
        });

        viewDividerPuncAmount = view.findViewById(R.id.viewDividerFineAmountOne);
        viewDividerPuncTime = view.findViewById(R.id.viewDividerFineTimeOne);
        viewDividerPuncAmountTwo = view.findViewById(R.id.viewDividerFineAmountTwo);
        viewDividerPuncTimeTwo = view.findViewById(R.id.viewDividerFineTimeTwo);

        txtPuncTimeOne = (TextView) view.findViewById(R.id.txtLabelFoodTimeOne);
        txtPuncTimeTwo = (TextView) view.findViewById(R.id.txtLabelFoodTimeTwo);
        txtPuncAmountOne = (TextView) view.findViewById(R.id.txtLabelFoodAmountOne);
        txtPuncAmountTwo = (TextView) view.findViewById(R.id.txtLabelFoodAmountTwo);


        mViewModel.setOnDatePickerOpenListener(mEdtKittyTime);
        mViewModel.setOnDatePickerOpenListener(mEdtKittyDate);
        mViewModel.setOnDatePickerOpenListener(mEdtPuncTimeOne);
        mViewModel.setOnDatePickerOpenListener(mEdtPuncTimeTwo);
        mViewModel.getDateFromDayAndWeek(mSpnWeek, mSpnDays);
        mSpnWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewModel.getDateFromDayAndWeek(mSpnWeek, mSpnDays);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpnDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewModel.getDateFromDayAndWeek(mSpnWeek, mSpnDays);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        viewDividerPuncAmount = view.findViewById(R.id.viewDividerFineAmountOne);
        viewDividerPuncTime = view.findViewById(R.id.viewDividerFineTimeOne);
        viewDividerPuncAmountTwo = view.findViewById(R.id.viewDividerFineAmountTwo);
        viewDividerPuncTimeTwo = view.findViewById(R.id.viewDividerFineTimeTwo);

        txtPuncTimeOne = (TextView) view.findViewById(R.id.txtLabelFoodTimeOne);
        txtPuncTimeTwo = (TextView) view.findViewById(R.id.txtLabelFoodTimeTwo);
        txtPuncAmountOne = (TextView) view.findViewById(R.id.txtLabelFoodAmountOne);
        txtPuncAmountTwo = (TextView) view.findViewById(R.id.txtLabelFoodAmountTwo);


        viewDividerDrinkAmount = view.findViewById(R.id.viewDividerDrinkAmount);
        viewDividerFoodAmount = view.findViewById(R.id.viewDividerFoodAmount);

        txtlabelDrinkAmount = (TextView) view.findViewById(R.id.txtLabelDrinkAmount);
        txtlabelFoodAmount = (TextView) view.findViewById(R.id.txtLabelFoodAmount);


        viewDividerTamBolaAmount = view.findViewById(R.id.viewDividerTambolaAmount);
        viewDividerGamesAmount = view.findViewById(R.id.viewDividerGamesAmount);

        txtTamBolaAmount = (TextView) view.findViewById(R.id.txtLabelTambolaAmount);
        txtGamesAmount = (TextView) view.findViewById(R.id.txtLabelGamesAmount);

        mSpnPuncType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mEdtPuncAmountOne.setEnabled(false);
                    mEdtPuncAmountTwo.setEnabled(false);
                    mEdtPuncTimeOne.setEnabled(false);
                    mEdtPuncTimeTwo.setEnabled(false);

                    txtPuncTimeOne.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    txtPuncAmountOne.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));

                    txtPuncTimeTwo.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    txtPuncAmountTwo.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));

                    viewDividerPuncAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    viewDividerPuncTime.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    viewDividerPuncAmountTwo.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    viewDividerPuncTimeTwo.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));


                } else if (position == 1) {
                    mEdtPuncAmountOne.setEnabled(true);
                    mEdtPuncTimeOne.setEnabled(true);
                    txtPuncTimeOne.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    txtPuncAmountOne.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));

                    mEdtPuncTimeTwo.setEnabled(false);
                    mEdtPuncAmountTwo.setEnabled(false);

                    txtPuncTimeTwo.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    txtPuncAmountTwo.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));


                    viewDividerPuncAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    viewDividerPuncTime.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    viewDividerPuncAmountTwo.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    viewDividerPuncTimeTwo.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));


                } else {
                    mEdtPuncAmountOne.setEnabled(true);
                    mEdtPuncAmountTwo.setEnabled(true);

                    mEdtPuncTimeOne.setEnabled(true);
                    mEdtPuncTimeTwo.setEnabled(true);


                    txtPuncTimeOne.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    txtPuncAmountOne.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));

                    txtPuncTimeTwo.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    txtPuncAmountTwo.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));


                    viewDividerPuncAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    viewDividerPuncTime.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    viewDividerPuncAmountTwo.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    viewDividerPuncTimeTwo.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpnFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    mEdtFoodAmount.setEnabled(false);
                    viewDividerFoodAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    txtlabelFoodAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                } else {
                    mEdtFoodAmount.setEnabled(true);
                    viewDividerFoodAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    txtlabelFoodAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpnDrink.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    mEdtDrinkAmount.setEnabled(false);
                    viewDividerDrinkAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    txtlabelDrinkAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                } else {
                    mEdtDrinkAmount.setEnabled(true);
                    viewDividerDrinkAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    txtlabelDrinkAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpnGames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mEdtGameAmount.setEnabled(false);
                    viewDividerGamesAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    txtGamesAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                } else {
                    mEdtGameAmount.setEnabled(true);
                    viewDividerGamesAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    txtGamesAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mSpnTambola.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mEdtTambolaAmount.setEnabled(false);
                    viewDividerTamBolaAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                    txtTamBolaAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_unselect_color));
                } else {
                    mEdtTambolaAmount.setEnabled(true);
                    viewDividerTamBolaAmount.setBackgroundColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));
                    txtTamBolaAmount.setTextColor(ContextCompat.getColor(RuleActivity.this, R.color.rule_select_color));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mLLlayout = (LinearLayout) view.findViewById(R.id.llKittyRule);
        mTxtOptional = (TextView) view.findViewById(R.id.txtshowOptionalItem);
        //hideShowOptionalMenu();
        hideShowView();
        mTxtOptional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShowView();
            }
        });


        //Default Kitty time set
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date date = cal.getTime();
        mEdtKittyTime.setText(DateTimeUtils.getServerTimeFormat().format(date));


    }


    @Override
    protected String getActionTitle() {
        return "test";
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    @Override
    public CreateGroup getData() {
        CreateGroup groupData = new CreateGroup();
        groupData.setAmount(Utils.getText(mEdtKittyAmount));
        groupData.setHosts((String) mSpnHost.getSelectedItem());
        groupData.setNoOfHost((String) mSpnHost.getSelectedItem());

        final SimpleDateFormat displayFormat = DateTimeUtils.getPickerTimeFormat();
        final SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        try {
            Date date = parseFormat.parse(Utils.getText(mEdtKittyTime));
            groupData.setKittyTime(displayFormat.format(date));
        } catch (ParseException e) {
            groupData.setKittyTime(displayFormat.format(new Date()));
        }

        groupData.setFine("Yes");
        groupData.setFineAmount(Utils.getText(mEdtFineAmount));
        if (mCbDestinantion.isChecked()) {
            groupData.setDestination("Yes");
        } else {
            groupData.setDestination("No");
        }
        groupData.setFoodamount(Utils.getText(mEdtFoodAmount));
        groupData.setPunctualityTime(Utils.getText(mEdtPuncTimeOne));
        groupData.setPunctualityTimeTwo(Utils.getText(mEdtPuncTimeTwo));
        groupData.setPunctualityAmount(Utils.getText(mEdtPuncAmountOne));
        groupData.setPunctualityAmountTwo(Utils.getText(mEdtPuncAmountTwo));
        groupData.setTambolaprice(Utils.getText(mEdtTambolaAmount));
        groupData.setDrinkAmount(Utils.getText(mEdtDrinkAmount));

        switch (mSpnWeek.getSelectedItemPosition() + 1) {
            case 1:
                groupData.setWeek("first");
                break;
            case 2:
                groupData.setWeek("second");
                break;
            case 3:
                groupData.setWeek("third");
                break;
            case 4:
                groupData.setWeek("fourth");
                break;
        }


        groupData.setDays(String.valueOf(mSpnDays.getSelectedItem()).toLowerCase());
        groupData.setDrink(String.valueOf(mSpnDrink.getSelectedItem()));
        groupData.setFoodBy(String.valueOf(mSpnFood.getSelectedItem()));
        //groupData.setGameType(String.valueOf(mSpnGames.getSelectedItem()));
        groupData.setGameType("Tambola + Normal");
        groupData.setTambola(String.valueOf(mSpnTambola.getSelectedItem()));
        groupData.setPunctuality("0" + String.valueOf(mSpnPuncType.getSelectedItem()));
        groupData.setGame("Yes");
        groupData.setFirst_kitty(Utils.getText(mEdtKittyDate));
        groupData.setQuickGroupId(PreferanceUtils.getQucikBloxIdToPreferance(this));
        groupData.setNote(Utils.getText(mEdtNote));
        //groupData.setGroupIMG("");
        groupData.setNormal("1");
        groupData.setNormalprice("");
//        groupData.setFineAmount(Utils.getText(mEdtFineAmount));
        return groupData;
    }


    @Override
    public void setData(CreateGroup data) {
//        mTxtSubmit.setText(getResources().getString(R.string.update));

        mEdtKittyAmount.setText(data.getAmount());
        //mEdtHost.setText(data.getHosts());
        mEdtKittyTime.setText(data.getKittyTime());
        mEdtFineAmount.setText(data.getFineAmount());

        //Checkbox Selection
        if (data.getDestination().equalsIgnoreCase("Yes")) {
            mCbDestinantion.setChecked(true);
        } else {
            mCbDestinantion.setChecked(false);
        }

        mEdtFoodAmount.setText(data.getFoodamount());
        mEdtPuncTimeOne.setText(data.getPunctualityTime());
        mEdtPuncTimeTwo.setText(data.getPunctualityTimeTwo());
        mEdtPuncAmountOne.setText(data.getPunctualityAmount());
        mEdtPuncAmountTwo.setText(data.getPunctualityAmountTwo());
        mEdtTambolaAmount.setText(data.getTambolaprice());
        mEdtDrinkAmount.setText(data.getDrinkAmount());
        mEdtNote.setText(data.getNote());

        //Week Selection
        if (data.getWeek().equalsIgnoreCase("first")) {
            mSpnWeek.setSelection(0);
        } else if (data.getWeek().equalsIgnoreCase("second")) {
            mSpnWeek.setSelection(1);
        } else if (data.getWeek().equalsIgnoreCase("third")) {
            mSpnWeek.setSelection(2);
        } else if (data.getWeek().equalsIgnoreCase("fourth")) {
            mSpnWeek.setSelection(3);
        }

        //Days Selection
        String[] daysArray = getResources().getStringArray(R.array.days);
        for (int i = 0; i < daysArray.length; i++) {
            if (data.getDays().equalsIgnoreCase(daysArray[i])) {
                mSpnDays.setSelection(i);
                break;
            }
        }

        //Drink And Food Selection
        String[] food_drinkArray = getResources().getStringArray(R.array.food_drink);
        for (int i = 0; i < food_drinkArray.length; i++) {
            if (data.getDrink().equalsIgnoreCase(food_drinkArray[i])) {
                mSpnDrink.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < food_drinkArray.length; i++) {
            if (data.getFoodBy().equalsIgnoreCase(food_drinkArray[i])) {
                mSpnFood.setSelection(i);
                break;
            }
        }

        //Tambola And Punctuality And Game Selection
        String[] gameTypeArray = getResources().getStringArray(R.array.game_type);
        for (int i = 0; i < gameTypeArray.length; i++) {
            if (data.getTambola().equalsIgnoreCase(gameTypeArray[i])) {
                mSpnTambola.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < gameTypeArray.length; i++) {
            if (data.getPunctuality().equalsIgnoreCase(gameTypeArray[i])) {
                mSpnPuncType.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < gameTypeArray.length; i++) {
            if (data.getGame().equalsIgnoreCase(gameTypeArray[i])) {
                mSpnGames.setSelection(i);
                break;
            }
        }

        //Host Selection
        String[] hostSelectionArray = getResources().getStringArray(R.array.host_selection);
        for (int i = 0; i < hostSelectionArray.length; i++) {
            if (data.getHosts().equalsIgnoreCase(hostSelectionArray[i])) {
                mSpnHost.setSelection(i);
                break;
            }
        }

        Utils.setKittyRuleImage(data.getCategory(), this, mImgHeder);
    }

    @Override
    public void setImageViewData(String type) {
        Utils.setKittyRuleImage(type, this, mImgHeder);
    }

    @Override
    public void getKittyDate(String date) {
        mEdtKittyDate.setText(DateTimeUtils.getKittyRuleDateFormat(date));
    }


    private void hideShowOptionalMenu() {
        for (int i = 8; i < mLLlayout.getChildCount(); i++) {
            if (!isDisplay) {
                mLLlayout.getChildAt(i).setVisibility(View.GONE);
                isDisplay = true;
            } else {
                mLLlayout.getChildAt(i).setVisibility(View.VISIBLE);
                isDisplay = false;
            }
        }
    }

    private void hideShowView() {
        if (!isDisplay) {
            mLLlayout.getChildAt(9).setVisibility(View.GONE);
            mLLlayout.getChildAt(10).setVisibility(View.GONE);
            mLLlayout.getChildAt(11).setVisibility(View.GONE);
            mLLlayout.getChildAt(12).setVisibility(View.GONE);
            mLLlayout.getChildAt(13).setVisibility(View.GONE);
            mLLlayout.getChildAt(14).setVisibility(View.GONE);
            mLLlayout.getChildAt(15).setVisibility(View.GONE);
            mLLlayout.getChildAt(16).setVisibility(View.GONE);
            mLLlayout.getChildAt(17).setVisibility(View.GONE);
            mLLlayout.getChildAt(18).setVisibility(View.GONE);
            isDisplay = true;
            mTxtOptional.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        } else {
            mLLlayout.getChildAt(9).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(10).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(11).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(12).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(13).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(14).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(15).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(16).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(17).setVisibility(View.VISIBLE);
            mLLlayout.getChildAt(18).setVisibility(View.VISIBLE);
            isDisplay = false;
            mTxtOptional.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (hasDrawer()) {
                    toggle();
                } else {
                    onBackPressed();
                }
                break;

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    public void hideSubmitButton(ChatData data) {
        mTxtOptional.setEnabled(true);
        if (data.getIsAdmin().equalsIgnoreCase("1")) {
            mTxtSubmit.setVisibility(View.VISIBLE);
            enableDisableView(true);
        } else {
            mTxtSubmit.setVisibility(View.GONE);
            enableDisableView(false);
        }
    }

    public void enableDisableView(boolean flag) {
        mEdtKittyAmount.setEnabled(flag);
        mEdtKittyTime.setEnabled(flag);
        mEdtKittyDate.setEnabled(flag);
        mEdtFoodAmount.setEnabled(flag);
        mEdtDrinkAmount.setEnabled(flag);
        mEdtPuncAmountOne.setEnabled(flag);
        mEdtPuncAmountTwo.setEnabled(flag);
        mEdtPuncTimeOne.setEnabled(flag);
        mEdtPuncTimeTwo.setEnabled(flag);
        mEdtNote.setEnabled(flag);
        mEdtGameAmount.setEnabled(flag);
        mEdtTambolaAmount.setEnabled(flag);
        mEdtFineAmount.setEnabled(flag);

        mCbDestinantion.setEnabled(flag);
        mTxtSubmit.setEnabled(flag);
        mImgHeder.setEnabled(flag);


        mSpnDays.setEnabled(flag);
        mSpnWeek.setEnabled(flag);
        mSpnFood.setEnabled(flag);
        mSpnDrink.setEnabled(flag);
        mSpnTambola.setEnabled(flag);
        mSpnGames.setEnabled(flag);
        mSpnPuncType.setEnabled(flag);
        mSpnHost.setEnabled(flag);
    }

    public boolean isFromChat() {
        return isFromChat;
    }

    public boolean isDiaryData() {
        return isDiaryData;
    }

    public void disableHostAndKittyDate() {
        mSpnHost.setEnabled(false);
        mEdtKittyDate.setEnabled(false);
    }

    public boolean isFromSetting() {
        return isFromSetting;
    }

    public void startService() {
        startService(new Intent(getApplicationContext(), CreateGroupIntentService.class));
    }


    /**
     * get image string from path
     *
     * @param path
     * @return
     */
    public String getStringFromURIPath(String path) {
        String img = "";
        if (Utils.isValidString(path)) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                img = Utils.getImageInString(bitmap);
            }
        }
        return img;
    }
}
