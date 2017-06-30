package it.polito.did.ragnatela;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    public EditText first_b;
    public EditText second_b;
    public EditText third_b;
    public EditText fourth_b;
    private ImageButton sound;
    private int count;
    private Button resume;
    public static MediaPlayer resourcePlayer;
    public static SharedPreferences sharedPref;
    private boolean musicOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sound = (ImageButton) findViewById(R.id.soundOnButton);

        Log.d("MUSIC-ON-create", String.valueOf(sharedPref.getBoolean("MUSIC", true))+" "+String.valueOf(count));
        if (sharedPref.getBoolean("MUSIC", true)){
            resourcePlayer = MediaPlayer.create(this,R.raw.opening);
            resourcePlayer.start();
            resourcePlayer.setLooping(true);
            sound.setImageResource(R.mipmap.sound_on_0);
            count = 0;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("COUNT", count);
            editor.commit();
        }
        else {
            sound.setImageResource(R.mipmap.sound_off_0);
            count = 1;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("COUNT", count);
            editor.commit();
        }


        Button start_game = (Button) findViewById(R.id.start_game);

        first_b = (EditText) findViewById(R.id.first_byte_ip);
        second_b = (EditText) findViewById(R.id.second_byte_ip);
        third_b = (EditText) findViewById(R.id.third_byte_ip);
        fourth_b = (EditText) findViewById(R.id.fourth_byte_ip);


        start_game.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("ip", getIp());
                startActivity(intent);
                if (resourcePlayer != null) {
                    resourcePlayer.stop();
                    resourcePlayer.release();
                    resourcePlayer = null;
                }
            }
        });

        sound.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                count++;
                if (count % 2 != 0) {
                    sound.setImageResource(R.mipmap.sound_off_0);
                    resourcePlayer.stop();
                    resourcePlayer.release();
                    resourcePlayer = null;
                    musicOn = false;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("MUSIC", musicOn);
                    editor.putInt("COUNT", count);
                    editor.commit();
                    Log.d("MUSIC-ON", String.valueOf(sharedPref.getBoolean("MUSIC", true))+" "+String.valueOf(sharedPref.getInt("COUNT", 0)));

                } else {
                    sound.setImageResource(R.mipmap.sound_on_0);
                    resourcePlayer = MediaPlayer.create(MenuActivity.this, R.raw.opening);
                    resourcePlayer.start();
                    resourcePlayer.setLooping(true);
                    musicOn = true;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("MUSIC", musicOn);
                    editor.putInt("COUNT", count);
                    editor.commit();
                    Log.d("MUSIC-ON", String.valueOf(sharedPref.getBoolean("MUSIC", true))+" "+String.valueOf(sharedPref.getInt("COUNT", 0)));
                }

            }
        });

        resumeGame();
    }


    public String getIp() {
        String ip = first_b.getText().toString() + "." + second_b.getText().toString() + "." + third_b.getText().toString() + "." + fourth_b.getText().toString();
        return ip;
    }

    private void resumeGame() {
        resume = (Button) findViewById(R.id.resumeButton);
        resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                /*MainActivity.resourcePlayer.stop();
                MainActivity.resourcePlayer.release();
                MainActivity.resourcePlayer = null;*/
                if (resourcePlayer != null) {
                    resourcePlayer.stop();
                    resourcePlayer.release();
                    resourcePlayer = null;
                }
            }
        });

    }
}