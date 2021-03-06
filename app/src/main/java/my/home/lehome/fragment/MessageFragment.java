/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.home.lehome.fragment;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import my.home.lehome.R;
import my.home.lehome.mvp.presenters.MessageViewPresenter;
import my.home.lehome.mvp.views.SendMessageView;

public class MessageFragment extends Fragment implements SendMessageView {
    public static final String TAG = "MessageFragment";

    enum STATE {
        IDLE, RECORDING, SENDING
    }

    MessageViewPresenter mMessageViewPresenter;

    private int mScreenWidth;
    private int mScreenHeight;
    private ProgressBar mStateProgressBar;
    //    private WaveformView mWaveformView;
    private Button mSendButton;
    private MessageViewHandler mHandler;
    private STATE mState = STATE.IDLE;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        private Rect rect;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                mMessageViewPresenter.startRecording();
                mSendButton.setPressed(true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                    mMessageViewPresenter.cancelRecording();
                } else {
                    mMessageViewPresenter.finishRecording();
                }
                mSendButton.setPressed(false);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (mState == STATE.RECORDING) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        mSendButton.setText(getString(R.string.message_release_to_cancel));
                    } else {
                        mSendButton.setText(getString(R.string.message_release_to_send));
                    }
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                mSendButton.setPressed(false);
            }
            return false;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMessageViewPresenter.cancelSending();
        }
    };

    public MessageFragment() {
    }

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupData();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMessageViewPresenter.start();
    }

    @Override
    public void onStop() {
        mMessageViewPresenter.stop();
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

        View contentView = inflater.inflate(R.layout.fragment_send_message, container, false);
        setupViews(contentView);
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setupData() {
        mMessageViewPresenter = new MessageViewPresenter(this);
        mHandler = new MessageViewHandler();
    }

    @Override
    public void setupViews(View rootView) {
        mSendButton = (Button) rootView.findViewById(R.id.send_message_btn);
        mSendButton.setOnTouchListener(mOnTouchListener);

        mStateProgressBar = (ProgressBar) rootView.findViewById(R.id.message_state_progressBar);
        mStateProgressBar.setVisibility(View.INVISIBLE);

//        mWaveformView = (WaveformView) rootView.findViewById(R.id.message_voice_vitrualizer);

        setRetainInstance(true);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onRecordingBegin() {
        mSendButton.setText(getString(R.string.message_release_to_send));
        mStateProgressBar.setProgress(0);
        mStateProgressBar.setVisibility(View.VISIBLE);
        mState = STATE.RECORDING;
    }

    @Override
    public void onRecordingEnd() {
        mSendButton.setText(getString(R.string.message_press_to_speak));
        mStateProgressBar.setProgress(0);
        mStateProgressBar.setVisibility(View.INVISIBLE);
        mState = STATE.IDLE;
    }

    @Override
    public void putDataForWaveform(short[] notProcessData, int len) {
//        mWaveformView.updateAudioData(notProcessData, len);
    }

    @Override
    public void onRecordingAmplitude(double amplitude) {
        Log.d(TAG, amplitude + " | " + (int) amplitude);
        mStateProgressBar.setProgress((int) amplitude);
    }

    @Override
    public void onSendingMsgBegin(String tag) {
        mSendButton.setText(getString(R.string.message_sending));
        mSendButton.setOnTouchListener(null);
        mSendButton.setOnClickListener(mOnClickListener);
        mState = STATE.SENDING;
    }

    @Override
    public void onSendingMsgSuccess(String tag) {
        mSendButton.setText(getString(R.string.message_press_to_speak));
        mSendButton.setOnTouchListener(mOnTouchListener);
        mSendButton.setOnClickListener(null);

        mState = STATE.IDLE;
        Toast.makeText(getActivity(), getString(R.string.message_sending_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendingMsgFail(String tag) {
        mSendButton.setText(getString(R.string.message_press_to_speak));
        mSendButton.setOnTouchListener(mOnTouchListener);
        mSendButton.setOnClickListener(null);

        mState = STATE.IDLE;
        Toast.makeText(getActivity(), getString(R.string.message_sending_fail), Toast.LENGTH_SHORT).show();
    }

    private class MessageViewHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
