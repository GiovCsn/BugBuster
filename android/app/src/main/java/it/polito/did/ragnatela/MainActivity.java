package it.polito.did.ragnatela;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.angrybyte.circularslider.CircularSlider;

public class MainActivity extends Activity {

    Unbinder unbinder;

    private String host_url = "192.168.1.71";
    private int host_port = 8080;

    @BindView(R.id.set_display_pixels)
    Button set_display_pixels;

    @BindView(R.id.random_colors)
    Button randomColors;

    @BindView(R.id.move_backward_button)
    Button moveBackwardButton;

    @BindView(R.id.move_forward_button)
    Button moveForwardButton;

    @BindView(R.id.highlight_components_button)
    Button changeColorButton;

    @BindViews({R.id.first_byte_ip, R.id.second_byte_ip, R.id.third_byte_ip, R.id.fourth_byte_ip})
    List<EditText> ip_address_bytes;

    @BindView(R.id.host_port)
    EditText hostPort;

    private TextWatcher myIpTextWatcher;
    private JSONArray pixels_array;

    private Handler mNetworkHandler, mMainHandler, bugNetworkHandler, bugMainHandler;

    private NetworkThread mNetworkThread = null;
    private NetworkThread bugNetworkThread = null;

    // EDIT
    TextView life;
    TextView score;
    TextView angle;
    CircularSlider cSlider;
    Ragno ragno = new Ragno();
    ArrayList<int[]> pixels;

    ArrayList<Bug> buglist = new ArrayList<Bug>();

    Timer timer;
    TimerTask timerTask;
    Timer timerBug;
    TimerTask timerTaskBug;
    Timer timerSpider;
    TimerTask timerTaskSpider;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        unbinder = ButterKnife.bind(this);

        cSlider = (CircularSlider) findViewById(R.id.circular);

        cSlider.setEnabled(false);
        set_display_pixels.setEnabled(false);
        randomColors.setEnabled(false);
        moveBackwardButton.setEnabled(false);
        moveForwardButton.setEnabled(false);
        changeColorButton.setEnabled(false);

        myIpTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (checkCorrectIp()) {
                    cSlider.setEnabled(true);
                    moveBackwardButton.setEnabled(true);
                    moveForwardButton.setEnabled(true);
                    randomColors.setEnabled(true);
                    set_display_pixels.setEnabled(true);
                    changeColorButton.setEnabled(true);
                    Message msg = mNetworkHandler.obtainMessage();
                    msg.what = NetworkThread.SET_SERVER_DATA;
                    msg.obj = host_url;
                    msg.arg1 = host_port;
                    msg.sendToTarget();

                    handleNetworkRequest(NetworkThread.SET_SERVER_DATA, host_url, host_port ,0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        for (EditText ip_byte : ip_address_bytes) {
            ip_byte.addTextChangedListener(myIpTextWatcher);
        }

        hostPort.addTextChangedListener(myIpTextWatcher);

        pixels_array = preparePixelsArray();

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
            }
        };

        startHandlerThread();

        // EDIT

        life = (TextView) findViewById(R.id.life);
        score = (TextView) findViewById(R.id.score);
        angle = (TextView) findViewById(R.id.angle);
        startHandlerBugThread();
        startTimer();
        startTimerBug();
        pixels = loadPixels();
        setDisplayPixels();
        startTimerSpider();
        life.setText(String.valueOf(ragno.getLifebar()));
        score.setText(String.valueOf(ragno.getScore()));

        cSlider.setOnSliderMovedListener(new CircularSlider.OnSliderMovedListener() {
            @Override
            public void onSliderMoved(double pos) {
                double posNorm = Math.toDegrees(pos*(2 * Math.PI));
                if(posNorm < 0) {
                    posNorm = posNorm * (-1);
                } else {
                    posNorm = 360 - posNorm;
                }
                angle.setText(String.valueOf(posNorm));
                ragno.setAngle(Math.toRadians(posNorm));
                //setDisplayPixels();
            }
        });
    }

    public void startHandlerThread() {
        mNetworkThread = new NetworkThread(mMainHandler);
        mNetworkThread.start();
        mNetworkHandler = mNetworkThread.getNetworkHandler();
    }

    public void startHandlerBugThread() {
        bugNetworkThread = new NetworkThread(bugMainHandler);
        bugNetworkThread.start();
        bugNetworkHandler = bugNetworkThread.getNetworkHandler();
    }

    private boolean checkCorrectIp() {
        StringBuilder sb = new StringBuilder();
        int port;

        if (hostPort.getText().length() == 0)
            return false;

        for (EditText editText : ip_address_bytes) {
            sb.append(editText.getText().toString());
            sb.append(".");
        }
        //cancello l'ultimo "."
        sb.deleteCharAt(sb.length() - 1);

        port = Integer.parseInt(hostPort.getText().toString());
        if (validIP(sb.toString()) && port >= 0 & port <= 65535) {
            host_url = sb.toString();
            host_port = port;
            return true;
        } else
            return false;
    }

    //from http://stackoverflow.com/questions/4581877/validating-ipv4-string-in-java
    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            if (ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        if (mNetworkThread != null && mNetworkHandler != null) {
            mNetworkHandler.removeMessages(mNetworkThread.SET_PIXELS);
            mNetworkHandler.removeMessages(mNetworkThread.SET_DISPLAY_PIXELS);
            mNetworkHandler.removeMessages(mNetworkThread.SET_SERVER_DATA);
            mNetworkThread.quit();
            try {
                mNetworkThread.join(100);
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            } finally {
                mNetworkThread = null;
                mNetworkHandler = null;
            }
        }
        if (bugNetworkThread != null && bugNetworkHandler != null) {
            bugNetworkHandler.removeMessages(bugNetworkThread.SET_PIXELS);
            bugNetworkHandler.removeMessages(bugNetworkThread.SET_DISPLAY_PIXELS);
            bugNetworkHandler.removeMessages(bugNetworkThread.SET_SERVER_DATA);
            bugNetworkThread.quit();
            try {
                bugNetworkThread.join(100);
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            } finally {
                bugNetworkThread = null;
                bugNetworkHandler = null;
            }
        }

        timerTask.cancel();
        timerTaskBug.cancel();
        timerTaskSpider.cancel();
        timer.cancel();
        timerBug.cancel();
        timerSpider.cancel();

    }

    @OnClick(R.id.random_colors)
    void setRandomColors() {

        try {
            JSONArray pixels_array = preparePixelsArray();

            for (int i = 0; i < pixels_array.length(); i++) {
                ((JSONObject) pixels_array.get(i)).put("r", (int) (Math.random() * 255.0f));
                ((JSONObject) pixels_array.get(i)).put("g", (int) (Math.random() * 255.0f));
                ((JSONObject) pixels_array.get(i)).put("b", (int) (Math.random() * 255.0f));
            }
            handleNetworkRequest(NetworkThread.SET_PIXELS, pixels_array, 0 ,0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.set_display_pixels)
    void setDisplayPixels() {
        try {
            JSONArray pixels_array = preparePixelsArray();
            // RANDOM
//            for (int i = 0; i < pixels_array.length(); i++) {
//                ((JSONObject) pixels_array.get(i)).put("r", (int) (Math.random() * 255.0f));
//                ((JSONObject) pixels_array.get(i)).put("g", (int) (Math.random() * 255.0f));
//                ((JSONObject) pixels_array.get(i)).put("b", (int) (Math.random() * 255.0f));
//            }

            // LOAD PIXELS
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    int index = x + y * 32;
                    ((JSONObject) pixels_array.get(index)).put("r", (int) (0.0f));
                    ((JSONObject) pixels_array.get(index)).put("g", (int) (0.0f));
                    ((JSONObject) pixels_array.get(index)).put("b", (int) (0.0f));
                }
            }

            for (int i = 0; i < pixels.size(); i++){
                int px = pixels.get(i)[0] - 16;
                int py = pixels.get(i)[1] - 16;

                int x = (int) Math.round(px * Math.cos(ragno.getAngle()) - py * Math.sin(ragno.getAngle())) + 16;
                int y = (int) Math.round(px * Math.sin(ragno.getAngle()) + py * Math.cos(ragno.getAngle())) + 16;

                int index = x + y * 32;
                if(index < (32*32) && index > 0) {
                    ((JSONObject) pixels_array.get(index)).put("r", (int) (255.0f));
                    ((JSONObject) pixels_array.get(index)).put("g", (int) (255.0f));
                    ((JSONObject) pixels_array.get(index)).put("b", (int) (255.0f));
                }
            }

            handleNetworkRequest(NetworkThread.SET_DISPLAY_PIXELS, pixels_array, 0 ,0);
        } catch (JSONException e) {
            // There should be no Exception
        }
    }

    @OnClick(R.id.highlight_components_button)
    void highLightComponents() {
        try {
            pixels_array = preparePixelsArray();
            handleNetworkRequest(NetworkThread.SET_PIXELS, pixels_array, 0 ,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.move_forward_button)
    void movePixelsForward() {
        try {
//            JSONArray jsonArray = new JSONArray();
//            for (int i = 0; i < pixels_array.length(); i++) {
//                jsonArray.put(pixels_array.get((i + pixels_array.length() - 10) % pixels_array.length()));
//            }
//            pixels_array = jsonArray;
//            handleNetworkRequest(NetworkThread.SET_PIXELS, pixels_array, 0 ,0);

            // EDIT
            ragno.update(Math.PI / 8);
            setDisplayPixels();
            // ----

        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @OnClick(R.id.move_backward_button)
    void movePixelsBackward() {
        try {
//            JSONArray jsonArray = new JSONArray();
//            for (int i = 0; i < pixels_array.length(); i++) {
//                jsonArray.put(pixels_array.get((i + 10) % pixels_array.length()));
//            }
//            pixels_array = jsonArray;
//            handleNetworkRequest(NetworkThread.SET_PIXELS, pixels_array, 0 ,0);

            // EDIT
            ragno.update(-Math.PI / 8);
            setDisplayPixels();
            // ----

        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private void handleNetworkRequest(int what, Object payload, int arg1, int arg2) {
        Message msg = mNetworkHandler.obtainMessage();
        msg.what = what;
        msg.obj = payload;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.sendToTarget();
    }

    private void handleNetworkRequest(Handler handler,int what, Object payload, int arg1, int arg2) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        msg.obj = payload;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.sendToTarget();
    }

    JSONArray preparePixelsArray() {
        JSONArray pixels_array = new JSONArray();
        JSONObject tmp;
        try {
            for (int i = 0; i < 1072; i++) {
                tmp = new JSONObject();
                tmp.put("a", 0);
                if (i < 522) {
                    tmp.put("g", 0);
                    tmp.put("b", 0);
                    tmp.put("r", 0);
                } else if (i < 613) {
                    tmp.put("r", 255);
                    tmp.put("g", 0);
                    tmp.put("b", 255);
                } else if (i < 791) {
                    tmp.put("b", 0);
                    tmp.put("g", 0);
                    tmp.put("r", 0);
                } else {
                    tmp.put("b", 0);
                    tmp.put("g", 0);
                    tmp.put("r", 0);
                }
                pixels_array.put(tmp);
            }
        } catch (JSONException exception) {
            // No errors expected here
        }
        return pixels_array;
    }

    // EDIT

    private ArrayList<int[]> loadPixels(){
        Bitmap img;
        img = BitmapFactory.decodeResource(getResources(), R.drawable.spider);
        int count = 0;
        ArrayList<int[]> imgV = new ArrayList<>();

        for(int x = 0; x < img.getWidth(); x++) {
            for(int y = 0; y < img.getHeight(); y++) {
                if(img.getPixel(x, y) == -1){
                    imgV.add(new int[2]);
                    imgV.get(count)[0] = x;
                    imgV.get(count)[1] = y;
                    count++;
                }
            }
        }
        return imgV;

    }

    void showBug() {
        JSONArray pixels_array = preparePixelsArray();
        try {
            for(Bug bug : buglist) {
                int[] colors = new int[3];
                if(bug.getType() == 0) {
                    colors[0] = 0;    // R
                    colors[1] = 255;  // G
                    colors[2] = 0;    // B
                }else {
                    colors[0] = 255;
                    colors[1] = 0;
                    colors[2] = 255;
                }

                ((JSONObject) pixels_array.get(bug.getPos1())).put("r", colors[0]);
                ((JSONObject) pixels_array.get(bug.getPos1())).put("g", colors[1]);
                ((JSONObject) pixels_array.get(bug.getPos1())).put("b", colors[2]);
                ((JSONObject) pixels_array.get(bug.getPos2())).put("r", colors[0]);
                ((JSONObject) pixels_array.get(bug.getPos2())).put("g", colors[1]);
                ((JSONObject) pixels_array.get(bug.getPos2())).put("b", colors[2]);
                ((JSONObject) pixels_array.get(bug.getPos1() - 1)).put("r", colors[0]);
                ((JSONObject) pixels_array.get(bug.getPos1() - 1)).put("g", colors[1]);
                ((JSONObject) pixels_array.get(bug.getPos1() - 1)).put("b", colors[2]);
                ((JSONObject) pixels_array.get(bug.getPos2() + 1)).put("r", colors[0]);
                ((JSONObject) pixels_array.get(bug.getPos2() + 1)).put("g", colors[1]);
                ((JSONObject) pixels_array.get(bug.getPos2() + 1)).put("b", colors[2]);
            }
            handleNetworkRequest(bugNetworkHandler, NetworkThread.SET_PIXELS, pixels_array, 0 ,0);
        } catch (Exception e) {
            // Exception
        }
    }

    void deathControl(int i) {
        if(!buglist.get(i).isAlive()) {
            buglist.remove(i);
        }
    }

    void eatControl(int i) {
        Bug bug = buglist.get(i);
        boolean eated = false;
        double angleDeg = Math.toDegrees(ragno.getAngle());
        if((angleDeg > 360) || (angleDeg < -360)) {
            angleDeg = angleDeg % 360;
        }
        if(angleDeg < 0) {
            angleDeg = 360 + angleDeg;
        }
        if(bug.isChecked()) {
            int tirante = bug.getTirante();
            switch (tirante) {
                case 1:
                    if((angleDeg >= 340) || (angleDeg <= 20)){
                        eated = true;
                    }
                    break;
                case 2:
                    if((angleDeg >= 40) && (angleDeg <= 80)){
                        eated = true;
                    }
                    break;
                case 3:
                    if((angleDeg >= 130) && (angleDeg <= 170)){
                        eated = true;
                    }
                    break;
                case 4:
                    if((angleDeg >= 190) && (angleDeg <= 230)){
                        eated = true;
                    }
                    break;
                case 5:
                    if((angleDeg > 280) && (angleDeg < 320)){
                        eated = true;
                    }
                    break;
            }
            if(eated) {
                ragno.upScore();
            }
            else {
                ragno.hit();
            }
        }
    }

    // Timer per aggiornamento delle posizioni dei Bug
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 500); //
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                for(int i = 0; i < buglist.size(); i++) {
                    eatControl(i);
                    deathControl(i);
                    buglist.get(i).update();
                }
                showBug();

                handler.post(new Runnable() {
                    public void run() {
                        life.setText(String.valueOf(ragno.getLifebar()));
                        score.setText(String.valueOf(ragno.getScore()));
                    }
                });

            }
        };
    }

    // Timer per la creazione di nuovi Bug
    public void startTimerBug() {
        //set a new Timer
        timerBug = new Timer();

        //initialize the TimerTask's job
        initializeTimerTaskBug();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timerBug.schedule(timerTaskBug, 0, 5000); //
    }

    public void initializeTimerTaskBug() {

        timerTaskBug = new TimerTask() {
            public void run() {

                Bug bug = new Bug();
                buglist.add(bug);
            }
        };
    }

    // Timer per aggiornamento dell'angolo del ragno
    public void startTimerSpider() {
        //set a new Timer
        timerSpider = new Timer();

        //initialize the TimerTask's job
        initializeTimerTaskSpider();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timerSpider.schedule(timerTaskSpider, 0, 500); //
    }

    public void initializeTimerTaskSpider() {

        timerTaskSpider = new TimerTask() {
            public void run() {

                setDisplayPixels();

            }
        };
    }
}
