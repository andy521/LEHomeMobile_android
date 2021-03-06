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

package my.home.lehome.activity;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import my.home.common.util.PrefUtil;
import my.home.lehome.R;
import my.home.lehome.asynctask.NfcReadNdefAsyncTask;

public class WakeupActivity extends Activity {

    public static final String TAG = WakeupActivity.class.getName();

    public static final String INTENT_VOICE_COMMAND = "my.home.lehome.VOICE_COMMAND";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wakeup);

        Window wind = this.getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        wind.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "onResume");

        Intent intent = getIntent();
        if (intent.getAction().equals("android.intent.action.VOICE_COMMAND")) {
            Intent voiceIntent = new Intent(INTENT_VOICE_COMMAND);
            startActivity(voiceIntent);
        } else if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            if (PrefUtil.getbooleanValue(getApplicationContext(), "pref_nfc_cmd_enable", true)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NfcReadNdefAsyncTask(getApplicationContext()).execute(tag);
            }
            finish();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d(TAG, "onPause");
        finish(); // TODO - why finish here not in onResume?
    }
}
