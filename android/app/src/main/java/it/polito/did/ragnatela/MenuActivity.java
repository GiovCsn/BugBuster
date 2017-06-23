package it.polito.did.ragnatela;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MenuActivity extends AppCompatActivity {

    public EditText first_b;
    public EditText second_b;
    public EditText third_b;
    public EditText fourth_b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button start_game = (Button) findViewById(R.id.start_game);

        first_b = (EditText) findViewById(R.id.first_byte_ip);
        second_b = (EditText) findViewById(R.id.second_byte_ip);
        third_b = (EditText) findViewById(R.id.third_byte_ip);
        fourth_b = (EditText) findViewById(R.id.fourth_byte_ip);


        start_game.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("ip", getIp());
                startActivity(intent);
            }
        });
    }


    public String getIp() {
        String ip = first_b.getText().toString()+"."+second_b.getText().toString()+"."+third_b.getText().toString()+"."+fourth_b.getText().toString();
        return ip;
    }
}
