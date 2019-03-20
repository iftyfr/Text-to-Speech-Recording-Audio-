package talha.com.bd.ttosrecordaudio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS = 1;
    private TextToSpeech mTTS;
    private EditText mText;
    private AppCompatSeekBar seekBarPitch, seekBarSpeed;
    private ImageView listenBtn;
    private Button play, record, stop, stopRec;
    private MediaRecorder mediaRecorder;
    private String outPutFile;
    private MediaPlayer mediaPlayer;
    private boolean isPermissionsEnabeled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = findViewById(R.id.text);
        seekBarPitch = findViewById(R.id.seekBar_pitch);
        seekBarSpeed = findViewById(R.id.seekBar_speed);
        listenBtn = findViewById(R.id.speakBtn);
        play = findViewById(R.id.playAd);
        record = findViewById(R.id.recordAd);
        stop = findViewById(R.id.stopAd);
        stopRec = findViewById(R.id.stopRec);

        getPermissions();

        play.setEnabled(false);
        stop.setEnabled(false);



        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        listenBtn.setEnabled(true);
                        Toast.makeText(MainActivity.this, "Language supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });




    }

    public void listen(View view) {
        speak();
    }



    private void speak() {
        String text = mText.getText().toString();
        float pitch = (float) seekBarPitch.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (float) seekBarSpeed.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);

        if (TextUtils.isEmpty(text)){
            mTTS.speak("Empty Field", TextToSpeech.QUEUE_FLUSH, null);
        }
        else {

            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }

    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }


    //......................record audio..........................//



    public void recordAudio(View view) {

        if (isPermissionsEnabeled){
            outPutFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ UUID.randomUUID().toString()+"_audio_record.3gp";
            setupMediaRecorder();

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            play.setEnabled(false);
            stopRec.setEnabled(true);
            record.setEnabled(false);
            stop.setEnabled(false);
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
        }
        else {
            getPermissions();
        }

    }

    public void playAudio(View view) {

        stopRec.setEnabled(false);
        record.setEnabled(false);
        stop.setEnabled(true);

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(outPutFile);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

        Toast.makeText(this, "Playing Audio!", Toast.LENGTH_SHORT).show();
    }

    public void stopRecord(View view) {

        mediaRecorder.stop();
        stopRec.setEnabled(false);
        play.setEnabled(true);
        record.setEnabled(true);
        stop.setEnabled(false);
    }

    public void stopAudio(View view) {

        record.setEnabled(true);
        stop.setEnabled(false);
        play.setEnabled(true);
        stopRec.setEnabled(false);

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            setupMediaRecorder();
        }
        Toast.makeText(this, "Done Record!", Toast.LENGTH_SHORT).show();

    }

    private void setupMediaRecorder() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(outPutFile);

    }



    private void getPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            isPermissionsEnabeled = true;
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS);
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    isPermissionsEnabeled = true;
                }
                else {
                    isPermissionsEnabeled = false;
                }
            }

        }
    }

}
