package e.antti.connectiontest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.text.emoji.widget.EmojiButton;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static android.provider.Settings.Global.DEVICE_NAME;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_GAME_ACTIVITY = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    final private int REQUEST_CODE_ASK_ACCESS_FINE_LOCTION = 124;

    private String mConnectedDeviceName = null;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    //local bluetooth adapater
    private BluetoothAdapter mBluetoothAdapter = null;

    //Conncection handler
    private ConnectionHandler connectionHandler = null;

    //UI Elements
    TextView headerView;
    TextView infoView;
    Button visibleButton;
    Button connectButton;
    Button disconnectButton;
    Button gameButton;

    View mView;
    TextView timeView;
    ProgressBar progressBar;
    GridLayout gridLayout;
    TextView scores;
    Chronometer time;
    EmojiTextView emojiView;
    EmojiButton ans1,ans2,ans3,ans4,ans5,ans6,ans7,ans8,ans9;

    public static void setNumberOfRounds(int numberOfRounds) {
        NUMBER_OF_ROUNDS = numberOfRounds;
    }
    static int NUMBER_OF_ROUNDS = 5;
    int progressIncrement = 0;// 100/NUMBEROFROUNDS
    int progressTotal = 0;

    //highly professional data-arrays
    String[] emojiArray = {
            "✌","😂","😝","😁","😱","👉","🙌","🍻","🔥","🌈",
            "☀","🎈","🌹","💄","🎀","⚽","🎾","🏁","😡","👿",
            "🐻","🐶","🐬","🐟","🍀","👀","🚗","🍎","💝","💙",
            "👌","❤","😍","😉","😓","😳","💪","💩","🍸","🔑",//<- you've copied names this far
            "💖","🌟","🎉","🌺","🎶","👠","🏈","⚾","🏆","👽",
            "💀","🐵","🐮","🐩","🐎","💣","👃","👂","🍓","💘",
            "💜","👊","💋","😘","😜","😵","🙏","👋","🚽","💃",
            "💎","🚀","🌙","🎁","⛄","🌊","⛵","🏀","🎱","💰",
            "👶","👸","🐰","🐷","🐍","🐫","🔫","👄","🚲","🍉","💛","💚"};
    String[] emojiCodeArr = {"U+270C","U+1F602","U+1F61D","U+1F601","U+1F631","U+1F449"};
    String[] emojiNameArr ={"victory hand","face with tears of joy","squinting face with tongue",
            "beaming face with smiling eyes","face screaming in fear","backhand index pointing right",
            "raising hands","clinking beer mugs","fire","rainbow","sun","balloon","rose","lipstick",
            "ribbon","soccer ball","tennis","chequered flag","pouting face", "angry face with horns",
            "bear face","dog face","dolphin","fish","four leaf clover","eyes","automobile","red apple",
            "heart with ribbon","blue heart","OK hand","red heart","smiling face with heart-eyes",
            "winking face","downcast face with sweat","flushed face","flexed biceps","pile of poo",
            "cocktail glass","key"};

    //Arraylist of MyEmoji objects
    ArrayList<MyEmoji> emojis = new ArrayList<MyEmoji>(40); //remember to add capacity
    MyEmoji answer = new MyEmoji(); //answer MyEmoji-object you can compare your choice against
    String lastAnswer = "";
    Random rng = new Random();
    long elapsedTime; // time, is reseted on a correct answer

    //scoredata
    ArrayList<Long> scoreArr = new ArrayList<>();
    int scoreIndex= 0;
    long answerSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //hide the title bar

        headerView = findViewById(R.id.header);
        infoView = findViewById(R.id.info);
        visibleButton = findViewById(R.id.visible_button);
        connectButton = findViewById(R.id.connect_button);
        disconnectButton = findViewById(R.id.disconnect_button);
        gameButton = findViewById(R.id.gameButton);

        mView = findViewById(R.id.back_view);
        emojiView = findViewById(R.id.emoji);
        timeView = findViewById(R.id.timeView);
        time = findViewById(R.id.time);
        progressBar = findViewById(R.id.progress);
        gridLayout = findViewById(R.id.grid);
        scores = findViewById(R.id.scores);
        ans1 = findViewById(R.id.answer1);
        ans2 = findViewById(R.id.answer2);
        ans3 = findViewById(R.id.answer3);
        ans4 = findViewById(R.id.answer4);
        ans5 = findViewById(R.id.answer5);
        ans6 = findViewById(R.id.answer6);
        ans7 = findViewById(R.id.answer7);
        ans8 = findViewById(R.id.answer8);
        ans9 = findViewById(R.id.answer9);

        //gameButton.setEnabled(false);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        connectionHandler = new ConnectionHandler(this, mHandler);

        // If the adapter is null, then Blue   tooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        defineContent();
        checkPermission();
    }

    public void OpenConnect(View view) {
        Intent OpenConnectivity = new Intent(this, DeviceListActivity.class);
        startActivityForResult(OpenConnectivity, REQUEST_CONNECT_DEVICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_ACCESS_FINE_LOCTION);
            }

        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (connectionHandler != null) {
            if (connectionHandler.getState() == connectionHandler.STATE_NONE) {
                connectionHandler.start();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    connectionHandler.connect(device);
                    gameButton.setEnabled(true);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled
                    //you can set stuff here
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    defineContent();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    defineContent();
                    break;
            }
        }
    };

    public void MakeVisible(View view) {
        ensureDiscoverable();
        Log.wtf("näkyvissä", "MakeVisible: made possible");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (connectionHandler != null) connectionHandler.stop();
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void checkPermission()
    {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_ACCESS_FINE_LOCTION);
        }
    }

    public void disconnect(View view) {
        connectionHandler.start();
        defineContent();
        //gameButton.setEnabled(false);
    }

    public void StartGame(View view) {
        if(connectionHandler.mState == 3) {
            /*
            Intent OpenGame= new Intent(this, GameActivity.class);
            String data = mConnectedDeviceName;
            OpenGame.putExtra("Connected Device name", data);
            startActivityForResult(OpenGame, REQUEST_GAME_ACTIVITY);*/
            defineContent();
        } else {
            Toast.makeText(this,"U cant do that", Toast.LENGTH_SHORT).show();
        }
    }

    //No time for good coding conventions, so gotta do it this way.
    private void defineContent() {
        if(connectionHandler.mState == 3) {
            //Connection elements
            headerView.setVisibility(View.GONE);
            infoView.setVisibility(View.GONE);
            visibleButton.setVisibility(View.GONE);
            connectButton.setVisibility(View.GONE);
            disconnectButton.setVisibility(View.GONE);
            gameButton.setVisibility(View.GONE);

            //Game elements
            mView.setVisibility(View.VISIBLE);
            emojiView.setVisibility(View.VISIBLE);
            timeView.setVisibility(View.VISIBLE);
            time.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            gridLayout.setVisibility(View.VISIBLE);
            scores.setVisibility(View.VISIBLE);
            initEmoji();
        } else {
            //Connection elements
            headerView.setVisibility(View.VISIBLE);
            infoView.setVisibility(View.VISIBLE);
            visibleButton.setVisibility(View.VISIBLE);
            connectButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.VISIBLE);
            gameButton.setVisibility(View.VISIBLE);

            //Game elements
            mView.setVisibility(View.GONE);
            emojiView.setVisibility(View.GONE);
            timeView.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            gridLayout.setVisibility(View.GONE);
            scores.setVisibility(View.GONE);
        }
    }

    private void initEmoji() {
        initializeEmojis(); //fill Arraylist<myEmoji> emojis

        //Progressbar stuff
        progressBar.setMax(100);
        progressIncrement = 100/NUMBER_OF_ROUNDS;

        //elapsedTime = 0;
        time.start();

        final ArrayList<EmojiButton> emojiButtons = new ArrayList<>();
        emojiButtons.add(ans1);
        emojiButtons.add(ans2);
        emojiButtons.add(ans3);
        emojiButtons.add(ans4);
        emojiButtons.add(ans5);
        emojiButtons.add(ans6);
        emojiButtons.add(ans7);
        emojiButtons.add(ans8);
        emojiButtons.add(ans9);
        randomizeEmoji(emojiButtons);

        // OnClickListener for all buttons
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmojiButton b = (EmojiButton)v;
                String buttonText = b.getText().toString();
                lastAnswer = answer.name;

                //Log.d("click", "onClick: buttontext: " + buttonText + " answer.name: " + answer.name);
                if (buttonText == answer.emoji){
                    time.stop();
                    Log.d("click", "onClick: Correct Answer Elapsed time: "+elapsedTime);
                    Toast toast = Toast.makeText(getApplicationContext(),"You win!!! ",Toast.LENGTH_SHORT);
                    toast.show();
                    randomizeEmoji(emojiButtons);
                    //reset timer
                    addScore(elapsedTime);

                    time.setBase(SystemClock.elapsedRealtime());
                    time.start();
                }else{
                    Log.d("click", "onClick: Wrong Anwer  Elapsed time: "+elapsedTime);
                    Toast toast = Toast.makeText(getApplicationContext(),"you lose!!!",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };
        //sets the previously made onclicklistener to all buttons
        for (EmojiButton emojibutton:emojiButtons) {
            emojibutton.setOnClickListener(onClickListener);
        }

        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                long minutes = ((SystemClock.elapsedRealtime() - time.getBase())/1000) / 60;
                long seconds = ((SystemClock.elapsedRealtime() - time.getBase())/1000) % 60;
                elapsedTime = SystemClock.elapsedRealtime();
                Log.d("chronometer", "onChronometerTick: " + minutes + " : " + seconds);

/*                long minutes = ((elapsedTime - time.getBase()) / 1000) / 60;
                long seconds = ((elapsedTime - time.getBase()) / 1000) % 60;*/
                //elapsedTime = elapsedTime + 1000;
                Log.d("chronometer", "onChronometerTick: " + minutes + " : " + seconds);
            }
        });
    }

    private void addScore(long score){
        progressTotal += progressIncrement;
        progressBar.setProgress(progressTotal);
        //long minutes = ((score - time.getBase()) / 1000) / 60;
        answerSeconds = ((score - time.getBase()) / 1000) % 60;
        //Log.d("score", "addScore: seconds: "+ answerSeconds);
        scoreArr.add(answerSeconds);
        if (scoreArr.size() > 5) {
            scoreArr.remove(0);
            scoreIndex--;
        }
        scores.setText("Last 5 scores in seconds\n");
        for (Long answer: scoreArr  ) {

            scores.append(answer+", ");
        }
        scoreIndex++;
    }

    private void randomizeEmoji(ArrayList<EmojiButton> emojiButtons){
        int i = 0,rngNumber;
        ArrayList<MyEmoji> answerOptions = new ArrayList<>();
        ArrayList<Integer> integers = randomNumbers();
        for (EmojiButton emojiButton: emojiButtons) {
            rngNumber = integers.get(i);    //~0-20 range atm
            emojiButton.setText(emojis.get(rngNumber).emoji);
            Log.d("rng", "randomizeEmoji: rngNumber: " + rngNumber + " emojiArray[rngNumber]: "+ emojiArray[rngNumber]);
            answerOptions.add(emojis.get(rngNumber));
            i++;
        }
        Log.d("rng", "randomizeEmoji: answerOptions: ");
        int answerRngNumber = rng.nextInt(9);
        answer = answerOptions.get(answerRngNumber);

        while(lastAnswer == answer.name){
            answerRngNumber = rng.nextInt(9);
            answer = answerOptions.get(answerRngNumber);
        }
        emojiView.setText(answer.name);
    }

    //makes an arraylist of n unique integers, n = emojiNameArr.length
    private ArrayList<Integer> randomNumbers(){
        int i;
        ArrayList<Integer> arrInt = new ArrayList<>();
        for(int a = 0; a<emojiNameArr.length; a++){
            arrInt.add(a);
        }
        Collections.shuffle(arrInt);
        Log.d("rng", "randomNumbers: "+arrInt);
        return arrInt;
    }

    //fills the emojis Arraylist
    private void initializeEmojis() {
        Log.d("initialize", "initializeEmojis: emojiNameArr.length: "+ emojiNameArr.length+ " emojiArray.length: "+ emojiArray.length);
        for (int i = 0; i < emojiNameArr.length; ) {
            if (i == emojiNameArr.length)break;
            MyEmoji emoji = new MyEmoji();
            emoji.addName(emojiNameArr[i]);
            emojis.add(emoji);
            i++;
        }
        for (int i = 0; i < emojiNameArr.length; i++) {
            emojis.get(i).addEmoji(emojiArray[i]);
        }

    }
}
