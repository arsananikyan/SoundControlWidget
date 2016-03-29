package com.picsarttraining.soundcontrolwidget;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private View playButton;
    private SoundControlView soundControlView;
    private MediaPlayer mediaPlayer;
    private int maxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

        playButton = findViewById(R.id.play_button);
        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        mediaPlayer.setLooping(false);

        soundControlView = (SoundControlView) findViewById(R.id.sound_control_view);
        soundControlView.setOnStateChangedListener(new SoundControlView.OnStateChangedListener() {
            @Override
            public void onStateChanged(double rotationSize) {
                int volume = (int)(maxVolume * (rotationSize / (2 * Math.PI)));
                Log.e("MainActivity", volume + "");
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                } else
                    mediaPlayer.pause();
            }
        });

    }
    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
