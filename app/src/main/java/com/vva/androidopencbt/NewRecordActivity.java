package com.vva.androidopencbt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vva.androidopencbt.db.DbRecord;

import org.w3c.dom.Text;

import java.util.Date;

import static android.view.View.GONE;

public class NewRecordActivity extends AppCompatActivity {

    RecordsViewModel viewModel;
    EditText thoughtEditText;
    EditText rationalEditText;
    EditText situationEditText;
    EditText emotionEditText;
    EditText feelingsEditText;
    EditText actionsEditText;

    SeekBar intensitySeekBar;
    TextView percentTextView;

    CheckBox allOrNothingCheckBox;
    CheckBox overgeneralizingCheckBox;
    CheckBox filteringCheckBox;
    CheckBox disqualCheckBox;
    CheckBox jumpCheckBox;
    CheckBox magnMinCheckBox;
    CheckBox emoReasonCheckBox;
    CheckBox mustCheckBox;
    CheckBox labelingCheckBox;
    CheckBox personCheckBox;
    long id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewModel = new ViewModelProvider(this).get(RecordsViewModel.class);

        // инициализация контроллов
        thoughtEditText = findViewById(R.id.thoughtEditText);
        rationalEditText = findViewById(R.id.rationalEditText);
        emotionEditText = findViewById(R.id.emotionEditText);
        situationEditText = findViewById(R.id.situationEditText);
        feelingsEditText = findViewById(R.id.feelingsEditText);
        actionsEditText = findViewById(R.id.actionsEditText);

        intensitySeekBar = findViewById(R.id.intensitySeekBar);
        intensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentTextView.setText(seekBar.getProgress() + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        percentTextView = findViewById(R.id.percentsTextView);

        allOrNothingCheckBox = findViewById(R.id.allOrNothingCheckBox);
        overgeneralizingCheckBox = findViewById(R.id.overgeneralizingCheckBox);
        filteringCheckBox = findViewById(R.id.filteringCheckBox);
        disqualCheckBox = findViewById(R.id.disqualCheckBox);
        jumpCheckBox = findViewById(R.id.jumpCheckBox);
        magnMinCheckBox = findViewById(R.id.magnMinCheckBox);
        emoReasonCheckBox = findViewById(R.id.emoReasonCheckBox);
        mustCheckBox = findViewById(R.id.mustCheckBox);
        labelingCheckBox = findViewById(R.id.labelingCheckBox);
        personCheckBox = findViewById(R.id.personCheckBox);

        Bundle bundle = getIntent().getExtras();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(bundle!=null)
        {
            id = bundle.getLong("ID");
            if(id > 0) //существующая запись
            {
                //---покажем кнопку "Удалить"
                Button deleteButton = findViewById(R.id.deleteButton);
                deleteButton.setVisibility(View.VISIBLE);
                //---

//                adapter.open();
//                Record record = adapter.getEvent(id);
//                adapter.close();

                viewModel.getRecordById(id).observe(this, record -> {
                    if (record == null)
                        return;

                    if (!record.getThoughts().isEmpty() || prefs.getBoolean("enable_thoughts", true))
                        thoughtEditText.setText(record.getThoughts());
                    else {
                        findViewById(R.id.nr_thoughtTextView).setVisibility(GONE);
                        thoughtEditText.setVisibility(GONE);
                    }

                    if (!record.getRational().isEmpty() || prefs.getBoolean("enable_rational", true))
                        rationalEditText.setText(record.getRational());
                    else {
                        findViewById(R.id.nr_rationalTextView).setVisibility(GONE);
                        rationalEditText.setVisibility(GONE);
                    }

                    if (!record.getEmotions().isEmpty() || prefs.getBoolean("enable_emotions", true))
                        emotionEditText.setText(record.getEmotions());
                    else {
                        findViewById(R.id.nr_emotionTextView).setVisibility(GONE);
                        emotionEditText.setVisibility(GONE);
                    }


                    if (!record.getSituation().isEmpty() || prefs.getBoolean("enable_situation", true))
                        situationEditText.setText(record.getSituation());
                    else {
                        findViewById(R.id.nr_situationTextView).setVisibility(GONE);
                        situationEditText.setVisibility(GONE);
                    }

                    if (!record.getFeelings().isEmpty() || prefs.getBoolean("enable_feelings", true))
                        feelingsEditText.setText(record.getFeelings());
                    else {
                        findViewById(R.id.nr_feelingsTextView).setVisibility(GONE);
                        feelingsEditText.setVisibility(GONE);
                    }

                    if (!record.getActions().isEmpty() || prefs.getBoolean("enable_actions", true))
                        actionsEditText.setText(record.getActions());
                    else {
                        findViewById(R.id.nr_actionsTextView).setVisibility(GONE);
                        actionsEditText.setVisibility(GONE);
                    }

                    if (record.getIntensity() != 0 || prefs.getBoolean("enable_intensity", true)) {
                        intensitySeekBar.setProgress(record.getIntensity());
                        percentTextView.setText(record.getIntensity() + "%");
                    } else {
                        findViewById(R.id.nr_intensityTextView).setVisibility(GONE);
                        intensitySeekBar.setVisibility(GONE);
                        percentTextView.setVisibility(GONE);
                    }

                    if (record.getDistortions() != 0 || prefs.getBoolean("enable_distortions", true)) {
                        int dist = record.getDistortions();
                        //---сделать разбор dist и отобразить CheckBox-ы
                        allOrNothingCheckBox.setChecked((dist & Record.ALL_OR_NOTHING) != 0);
                        overgeneralizingCheckBox.setChecked((dist & Record.OVERGENERALIZING) != 0);
                        filteringCheckBox.setChecked((dist & Record.FILTERING) != 0);
                        disqualCheckBox.setChecked((dist & Record.DISQUAL_POSITIVE) != 0);
                        jumpCheckBox.setChecked((dist & Record.JUMP_CONCLUSION) != 0);
                        magnMinCheckBox.setChecked((dist & Record.MAGN_AND_MIN) != 0);
                        emoReasonCheckBox.setChecked((dist & Record.EMOTIONAL_REASONING) != 0);
                        mustCheckBox.setChecked((dist & Record.MUST_STATEMENTS) != 0);
                        labelingCheckBox.setChecked((dist & Record.LABELING) != 0);
                        personCheckBox.setChecked((dist & Record.PERSONALIZATION) != 0);
                    } else {
                        allOrNothingCheckBox.setVisibility(GONE);
                        overgeneralizingCheckBox.setVisibility(GONE);
                        filteringCheckBox.setVisibility(GONE);
                        disqualCheckBox.setVisibility(GONE);
                        jumpCheckBox.setVisibility(GONE);
                        magnMinCheckBox.setVisibility(GONE);
                        emoReasonCheckBox.setVisibility(GONE);
                        mustCheckBox.setVisibility(GONE);
                        labelingCheckBox.setVisibility(GONE);
                        personCheckBox.setVisibility(GONE);
                        findViewById(R.id.nr_distortionTextView).setVisibility(GONE);
                    }
                });

            }
        }
        else
        {
            // свежая запись, смотрим какие поля показывать

            if(prefs.getBoolean("enable_thoughts",true)==false)
            {
                thoughtEditText.setVisibility(GONE);
                findViewById(R.id.nr_thoughtTextView).setVisibility(GONE);
            }

            if(prefs.getBoolean("enable_situation",true)==false)
            {
                situationEditText.setVisibility(GONE);
                findViewById(R.id.nr_situationTextView).setVisibility(GONE);
            }

            if(prefs.getBoolean("enable_emotions",true)==false)
            {
                emotionEditText.setVisibility(GONE);
                findViewById(R.id.nr_emotionTextView).setVisibility(GONE);
            }

            if(prefs.getBoolean("enable_intensity",true)==false)
            {
                intensitySeekBar.setVisibility(GONE);
                findViewById(R.id.nr_intensityTextView).setVisibility(GONE);
                percentTextView.setVisibility(GONE);
            }

            if(prefs.getBoolean("enable_rational",true)==false)
            {
                rationalEditText.setVisibility(GONE);
                findViewById(R.id.nr_rationalTextView).setVisibility(GONE);
            }

            if(prefs.getBoolean("enable_feelings",true)==false)
            {
                feelingsEditText.setVisibility(GONE);
                findViewById(R.id.nr_feelingsTextView).setVisibility(GONE);
            }

            if(prefs.getBoolean("enable_actions",true)==false)
            {
                actionsEditText.setVisibility(GONE);
                findViewById(R.id.nr_actionsTextView).setVisibility(GONE);
            }

            if(prefs.getBoolean("enable_distortions",true)==false)
            {
                allOrNothingCheckBox.setVisibility(GONE);
                overgeneralizingCheckBox.setVisibility(GONE);
                filteringCheckBox.setVisibility(GONE);
                disqualCheckBox.setVisibility(GONE);
                jumpCheckBox.setVisibility(GONE);
                magnMinCheckBox.setVisibility(GONE);
                emoReasonCheckBox.setVisibility(GONE);
                mustCheckBox.setVisibility(GONE);
                labelingCheckBox.setVisibility(GONE);
                personCheckBox.setVisibility(GONE);
                findViewById(R.id.nr_distortionTextView).setVisibility(GONE);
            }

        }
    }

    public void delete(View v)
    {
        viewModel.deleteRecord(id);
        finish();
    }

    public void save(View v)
    {
        String thought = thoughtEditText.getText().toString();
        String disput = rationalEditText.getText().toString();
        String situation = situationEditText.getText().toString();
        String emotion = emotionEditText.getText().toString();
        String feelings = feelingsEditText.getText().toString();
        String actions = actionsEditText.getText().toString();

        short intensity = (short)intensitySeekBar.getProgress();
        int dist = 0x0;

        if(allOrNothingCheckBox.isChecked()) { dist|=Record.ALL_OR_NOTHING;}
        if(overgeneralizingCheckBox.isChecked()) { dist|=Record.OVERGENERALIZING;}
        if(filteringCheckBox.isChecked()) { dist|=Record.FILTERING;}
        if(disqualCheckBox.isChecked()) { dist|=Record.DISQUAL_POSITIVE;}
        if(jumpCheckBox.isChecked()) { dist|=Record.JUMP_CONCLUSION;}
        if(magnMinCheckBox.isChecked()) { dist|=Record.MAGN_AND_MIN;}
        if(emoReasonCheckBox.isChecked()) { dist|=Record.EMOTIONAL_REASONING;}
        if(mustCheckBox.isChecked()) { dist|=Record.MUST_STATEMENTS;}
        if(labelingCheckBox.isChecked()) { dist|=Record.LABELING;}
        if(personCheckBox.isChecked()) { dist|=Record.PERSONALIZATION;}

        if(id > 0) {
            viewModel.updateRecord(id,
                    situation,
                    thought,
                    disput,
                    emotion,
                    dist,
                    feelings,
                    actions,
                    intensity);
        } else {
            viewModel.addRecord(new DbRecord(
                    null,
                    situation,
                    thought,
                    disput,
                    emotion,
                    dist,
                    feelings,
                    actions,
                    intensity,
                    new Date()
            ));
        }
        finish();
    }

    public void setEditText(View v)
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View promptView = inflater.inflate(R.layout.prompt,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(promptView);

        final TextView promptTextView = promptView.findViewById(R.id.prompt_textview);
        final EditText promptEditText = promptView.findViewById(R.id.prompt_edittext);
        final EditText currentEditText = (EditText)v;



        builder.setCancelable(true);
        switch (v.getId())
        {
            case R.id.situationEditText:
                promptTextView.setText(R.string.newrecord_text_situation);
                break;
            case R.id.thoughtEditText:
                promptTextView.setText(R.string.newrecord_text_thought);
                break;
            case R.id.rationalEditText:
                promptTextView.setText(R.string.newrecord_text_rational);
                break;
            case R.id.emotionEditText:
                promptTextView.setText(R.string.newrecord_text_emotions);
                break;
            case R.id.feelingsEditText:
                promptTextView.setText(R.string.newrecord_text_feelings);
                break;
            case R.id.actionsEditText:
                promptTextView.setText(R.string.newrecord_text_actions);
                break;
                default:break;
        }

        if(!currentEditText.getText().toString().isEmpty())
        {
            promptEditText.setText(currentEditText.getText());
            promptEditText.setSelection(promptEditText.getText().length());
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentEditText.setText(promptEditText.getText());
                currentEditText.clearFocus();
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        try {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        catch (Exception e){}
        dialog.show();
    }
}
