package com.impulse.impulse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageThreadFragment extends Fragment {

    private AbsListView mListView;
    private ArrayAdapter mAdapter;
    private List<Message> messages;
    private String response;

    private EditText replyEditText;
    private Button replyButton;
    private Button replyImageButton;

    private TextView otherUserName;

    private String otherUserKey;
    private String postId;
    private String userKey;
    private String filePathToSend;

    public static MessageThreadFragment create(String response, String otherUserKey, String postId) {
        MessageThreadFragment fragment = new MessageThreadFragment(response, otherUserKey, postId);
        return fragment;
    }

    public MessageThreadFragment(String response, String otherUserKey, String postId) {
        this.response = response;
        this.otherUserKey = otherUserKey;
        this.postId = postId;
    }

    public MessageThreadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messages = new ArrayList<Message>();
        parsePosts(response);
        mAdapter = new MessageThreadAdapter(getActivity(), R.layout.post_reply_item, messages);
    }

    BroadcastReceiver refreshPage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (otherUserKey.equals(extras.getString("otherUserKey")) && postId.equals(extras.getString("postId"))) {
                refreshThread();
            }
        }
    };

    private void removeNotifications() {
        LinkedList<Notifs> temp = new LinkedList<Notifs>();
        temp.add(new Notifs(otherUserKey, null, postId, null));
        GcmIntentService.notifs.removeAll(temp);
    }

    @Override
    public void onPause() {
        GcmIntentService.viewingUserKey = null;
        GcmIntentService.viewingPostId = null;
        getActivity().unregisterReceiver(refreshPage);
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.impulse.MessageThreadFragment");
        getActivity().registerReceiver(refreshPage, filter);

        GcmIntentService.viewingUserKey = otherUserKey;
        GcmIntentService.viewingPostId = postId;

        removeNotifications();
        refreshThread();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_thread, container, false);
        // Inflate the layout for this fragment
        // Set the adapter
        userKey = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        mListView = (AbsListView) view.findViewById(R.id.message_list);
        replyEditText = (EditText) view.findViewById(R.id.reply_from_thread_editText);
        replyButton = (Button) view.findViewById(R.id.reply_from_thread_button);
        replyImageButton = (Button) view.findViewById(R.id.reply_from_thread_button_image);
        otherUserName = (TextView) view.findViewById(R.id.thread_other_user_name);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        getUserName(otherUserKey);
        // Set OnItemClickListener so we can be notified on item clicks

        replyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/jpg");
                startActivityForResult(photoPickerIntent, 100);
            }
        });


        replyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String message = replyEditText.getText().toString();

                if (message.isEmpty())
                    return;

                replyEditText.setText("");
                new RestClient().createMessage(userKey, otherUserKey, postId, message,"text", new PostCallback() {
                    @Override
                    public void onPostSuccess(String result) {
                        refreshThread();
                    }
                });
            }
        });

        return view;
    }

    private void scrollToEnd() {
        mListView.post(new Runnable() {
            public void run() {
                mListView.setSelection(mListView.getCount() - 1);
            }
        });
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            String userKey = toAdd.get("author").getAsString();
            String message = toAdd.get("message").getAsString();
            String timestamp = toAdd.get("timestamp").getAsString();
            String type = toAdd.get("type").getAsString();
            messages.add(new Message(userKey, message, timestamp, type));
        }
    }

    private void refreshThread() {
        new RestClient().getThread(userKey, otherUserKey, postId, new GetCallback() {
            @Override
            void onDataReceived(String response) {
                if (response.equals(RestClient.ERROR)) {
                    Dialog.noInternetDialog(getActivity());
                }
                else {
                    try {
                        messages.clear();
                        parsePosts(response);
                        mAdapter.notifyDataSetChanged();
                        scrollToEnd();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "An error has ocurred.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void getUserName(String userId) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "name");
        Session session = Session.getActiveSession();
        new Request(session, "/" + userId, requestBundle, HttpMethod.GET, new Request.Callback() {
            public void onCompleted(Response response) {
                GraphObject obj = response.getGraphObject();
                if (obj == null) {
                    otherUserName.setText("Impulse");
                    return;
                }
                JSONObject json = response.getGraphObject().getInnerJSONObject();
                JsonElement elem = new JsonParser().parse(json.toString());
                String name = elem.getAsJsonObject().get("name").getAsString().split(" ")[0];
                otherUserName.setText(name);
            }
        }
        ).executeAsync();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case 100:
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filePathToSend = cursor.getString(columnIndex);
                    cursor.close();
                    Log.i("DPOKADW", filePathToSend);
                    showDialogSendPicture();
                }
        }
    }

    private void showDialogSendPicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Send Picture");
        builder.setMessage("Do you want to send this picture?");
        builder.setCancelable(false);
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new RestClient().createMessage(userKey, otherUserKey, postId, filePathToSend, "image", new PostCallback() {
                    @Override
                    public void onPostSuccess(String result) {
                        if (result.equals(RestClient.ERROR)) {
                            Dialog.noInternetDialog(getActivity());
                        }
                        else {
                            refreshThread();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
