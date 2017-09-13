package com.arnab.voicerecognizer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String WAITING = "waiting";
    private final String BUSY = "busy";
    private String state = WAITING;
    private final String TAG = "Speech Recognizer";
    private SpeechRecognizer sr;
    private TextToSpeech tts;
    private Intent recognizer_intent;

    private ListView listView = null;
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button speakButton = (Button)findViewById(R.id.button);
        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = adapter.getItem(position);
                String message = item.substring(item.indexOf("["),item.indexOf("]"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speakUnder20(message);
                } else {
                    speak21OrAbove(message);
                }
            }
        });
        speakButton.setOnClickListener(this);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int arg0) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });
        recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button){
            if(WAITING.equals(WAITING)){
                sr.startListening(recognizer_intent);
                setStateToBusy();
            } else {
                sr.stopListening();
                setStateToWaiting();
            }
        }
    }

    private void setStateToWaiting(){
        ((Button)findViewById(R.id.button)).setText("Click to Speak");
        state = WAITING;
    }
    private void setStateToBusy(){
        ((Button)findViewById(R.id.button)).setText("Listening");
        state = BUSY;
    }

    @SuppressWarnings("deprecation")
    private void speakUnder20(String message){
        tts.speak(message,TextToSpeech.QUEUE_FLUSH,null);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speak21OrAbove(String message) {
        String utteranceId=this.hashCode() + "";
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            String outputTemp = null;
            switch(error){
                case 1:
                    outputTemp = "Error : [NETWORK TIMEOUT]";
                    break;
                case 2:
                    outputTemp = "Error : [NETWORK ERROR]";
                    break;
                case 3:
                    outputTemp = "Error : [AUDIO ERROR]";
                    break;
                case 4:
                    outputTemp = "Error : [SERVER ERROR]";
                    break;
                case 5:
                    outputTemp = "Error : [CLIENT ERROR]";
                    break;
                case 6:
                    outputTemp = "Error : [SPEECH TIMEOUT]";
                    break;
                case 7:
                    outputTemp = "Error : [NO MATCH FOUND]";
                    break;
                case 8:
                    outputTemp = "Error : [RECOGNIZER BUSY]";
                    break;
                case 9:
                    outputTemp = "Error : [INSUFFICIENT PERMISSIONS]";
                    break;
            }
            Log.d(TAG,  outputTemp);
            adapter.add(outputTemp);
            setStateToWaiting();
            Log.d(TAG, "error " + error);
//            mText.setText("error " + error);
        }
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float [] confidence = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

            for (int i = 0; i < data.size(); i++)
            {
                String outputTemp = "result : [" + data.get(i)+"], Confidence : ["+confidence[i]+"]";
                Log.d(TAG, outputTemp);
                adapter.add(outputTemp);
                str += outputTemp;
            }
            setStateToWaiting();;
            Log.d(TAG, "results: "+String.valueOf(data.size()));
//            mText.setText("results: "+String.valueOf(data.size()));
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
