/*
 * Copyright (c) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 * the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or
 *       other materials provided with the distribution.
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package dev.mijey.tizenconsumersaagentv2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.samsung.android.sdk.accessory.SAAgentV2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessoryActivity extends Activity {
    private static final String TAG = "AccessoryActivity(C)";
    private static TextView mTextView;
    private static ToggleButton buttonConnect;
    private static MessageAdapter mMessageAdapter;
    private ListView mMessageListView;

    // Instances of SAAgentV2
    private AccessoryConsumer mAccessoryConsumer = null;

    private SAAgentV2.RequestAgentCallback mAgentCallback1 = new SAAgentV2.RequestAgentCallback() {
        @Override
        public void onAgentAvailable(SAAgentV2 agent) {
            mAccessoryConsumer = (AccessoryConsumer) agent;
        }

        @Override
        public void onError(int errorCode, String message) {
            Log.e(TAG, "Agent initialization error: " + errorCode + ". ErrorMsg: " + message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessory);

        initializeAccessory();
    }

    @Override
    protected void onDestroy() {
        // Clean up connections
        destroyAccessory();
        super.onDestroy();
    }

    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.buttonConnect: {
                if (mAccessoryConsumer != null) {
                    if (buttonConnect.isChecked()) {
                        buttonConnect.setChecked(false);
                        mAccessoryConsumer.findPeers();
                    } else {
                        if (mAccessoryConsumer.closeConnection() == false) {
                            updateTextView("Disconnected");
                            Toast.makeText(getApplicationContext(), R.string.ConnectionAlreadyDisconnected, Toast.LENGTH_LONG).show();
                            mMessageAdapter.clear();
                        }
                    }
                }
                break;
            }
            case R.id.buttonSend1: {
                if (mAccessoryConsumer != null) {
                    mAccessoryConsumer.sendData("Hello Accessory!");
                } else {
                    Toast.makeText(getApplicationContext(), R.string.ConnectionAlreadyDisconnected, Toast.LENGTH_LONG).show();
                }
                break;
            }
            default:
        }
    }

    public static void updateToggleButton(boolean enable) {
        buttonConnect.setChecked(enable);
    }

    public static void addMessage(String data) {
        mMessageAdapter.addMessage(new Message(data));
    }

    public static void updateTextView(final String str) {
        mTextView.setText(str);
    }

    private void initializeAccessory() {
        mTextView = (TextView) findViewById(R.id.tvStatus);
        mMessageListView = (ListView) findViewById(R.id.lvMessage);
        mMessageAdapter = new MessageAdapter();
        mMessageListView.setAdapter(mMessageAdapter);
        buttonConnect = ((ToggleButton) findViewById(R.id.buttonConnect));

        updateTextView("Disconnected");

        SAAgentV2.requestAgent(getApplicationContext(), AccessoryConsumer.class.getName(), mAgentCallback1);
    }

    private void destroyAccessory() {
        if (mAccessoryConsumer != null) {
            if (mAccessoryConsumer.closeConnection() == false) {
                updateTextView("Disconnected");
                mMessageAdapter.clear();
            }
            mAccessoryConsumer.releaseAgent();
            mAccessoryConsumer = null;
        }
    }

    private class MessageAdapter extends BaseAdapter {
        private static final int MAX_MESSAGES_TO_DISPLAY = 20;
        private List<Message> mMessages;

        public MessageAdapter() {
            mMessages = Collections.synchronizedList(new ArrayList<Message>());
        }

        void addMessage(final Message msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMessages.size() == MAX_MESSAGES_TO_DISPLAY) {
                        mMessages.remove(0);
                        mMessages.add(msg);
                    } else {
                        mMessages.add(msg);
                    }
                    notifyDataSetChanged();
                    mMessageListView.setSelection(getCount() - 1);
                }
            });
        }

        void clear() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.clear();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View messageRecordView = null;
            if (inflator != null) {
                messageRecordView = inflator.inflate(R.layout.message, null);
                TextView tvData = (TextView) messageRecordView.findViewById(R.id.tvData);
                Message message = (Message) getItem(position);
                tvData.setText(message.data);
            }
            return messageRecordView;
        }
    }


    private static final class Message {
        String data;

        public Message(String data) {
            super();
            this.data = data;
        }
    }
}