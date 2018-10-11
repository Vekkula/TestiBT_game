package e.antti.connectiontest;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.text.emoji.widget.EmojiButton;
import android.support.text.emoji.widget.EmojiTextView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GameActivity extends Activity {

    /** old variables **/
    String connectedDevice;
    TextView textView;
    /** old variables **/

    public static void setNumberOfRounds(int numberOfRounds) {
        NUMBER_OF_ROUNDS = numberOfRounds;
    }
    static int NUMBER_OF_ROUNDS = 5;
    ProgressBar progressBar;
    int progressIncrement = 0;// 100/NUMBEROFROUNDS
    int progressTotal = 0;
    //other UI componenents
    TextView timeView,scores;
    Chronometer time;
    EmojiTextView emojiView;
    EmojiButton ans1,ans2,ans3,ans4,ans5,ans6,ans7,ans8,ans9;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);
        setContentView(R.layout.activity_game);

        Bundle extras = getIntent().getExtras();

        /** old code **/
        connectedDevice = extras.getString("Connected Device name");
        textView = findViewById(R.id.texter);
        textView.setText("This is game activity.  You are connected with device: " + connectedDevice);
        /** old code **/


        initializeEmojis(); //fill Arraylist<myEmoji> emojis

        //Progressbar stuff
        progressBar = findViewById(R.id.progress);
        progressBar.setMax(100);
        progressIncrement = 100/NUMBER_OF_ROUNDS;

        scores = findViewById(R.id.scores);
        timeView = findViewById(R.id.timeView);//TextView next to chronometer
        time = findViewById(R.id.time);//chronometer
        time.start();
        emojiView = findViewById(R.id.emoji);
        ans1 = findViewById(R.id.answer1);
        ans2 = findViewById(R.id.answer2);
        ans3 = findViewById(R.id.answer3);
        ans4 = findViewById(R.id.answer4);
        ans5 = findViewById(R.id.answer5);
        ans6 = findViewById(R.id.answer6);
        ans7 = findViewById(R.id.answer7);
        ans8 = findViewById(R.id.answer8);
        ans9 = findViewById(R.id.answer9);
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



class MyEmoji{
    String name;
    String code;
    String emoji;

    MyEmoji(){

    }

    MyEmoji(String name,String emoji){
        this.name = name;
        this.emoji = emoji;
    }

    public void addName(String name){
        this.name = name;
    }

    public void addEmoji(String emoji){
        this.emoji = emoji;
    }

}
