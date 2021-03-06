package fr.voxeet.sdk.sample.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.voxeet.android.media.MediaStream;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.voxeet.sdk.sample.R;
import fr.voxeet.sdk.sample.Recording;
import fr.voxeet.sdk.sample.adapters.ParticipantAdapter;
import fr.voxeet.sdk.sample.adapters.RecordedConferencesAdapter;
import fr.voxeet.sdk.sample.application.SampleApplication;
import fr.voxeet.sdk.sample.dialogs.ConferenceOutput;
import sdk.voxeet.com.toolkit.views.uitookit.VideoView;
import voxeet.com.sdk.core.VoxeetPreferences;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.success.ConferenceJoinedSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceLeftSuccessEvent;
import voxeet.com.sdk.events.success.ConferenceUserJoinedEvent;
import voxeet.com.sdk.events.success.ConferenceUserLeftEvent;
import voxeet.com.sdk.events.success.ConferenceUserUpdatedEvent;
import voxeet.com.sdk.events.success.MessageReceived;
import voxeet.com.sdk.events.success.ScreenStreamAddedEvent;
import voxeet.com.sdk.events.success.ScreenStreamRemovedEvent;
import voxeet.com.sdk.json.ConferenceEnded;
import voxeet.com.sdk.json.RecordingStatusUpdateEvent;
import voxeet.com.sdk.models.RecordingStatus;
import voxeet.com.sdk.models.abs.ConferenceUser;

import static android.view.View.VISIBLE;

/**
 * Created by RomainBenmansour on 4/21/16.
 */
public class CreateConfActivity extends AppCompatActivity {

    private int action;

    private static final String TAG = CreateConfActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST = 0x0010;

    @Bind(R.id.participants)
    protected ListView participants;

    @Bind(R.id.join_conf_layout)
    protected ViewGroup joinLayout;

    @Bind(R.id.conference_options)
    protected ViewGroup conferenceOptions;

    @Bind(R.id.text_layout)
    protected ViewGroup sendText;

    @Bind(R.id.send_broadcast)
    protected Button sendBroadcast;

    @Bind(R.id.conference_editext)
    protected EditText editextConference;

    @Bind(R.id.broadcast_editext)
    protected EditText broadcastConference;

    @Bind(R.id.conference_alias)
    protected TextView aliasId;

    @Bind(R.id.screen_share)
    protected VideoView screenShare;

    @Bind(R.id.video_stream)
    protected VideoView videoStream;

    @Bind(R.id.leaveConf)
    protected Button leave;

    @Bind(R.id.toggle_video)
    protected Button video;

    @Bind(R.id.record)
    protected Button record;

    @Bind(R.id.mute)
    protected Button mute;

    @Bind(R.id.recorded_conf_list)
    protected ListView recordedConferences;

    @NonNull
    private Map<String, MediaStream> mediaStreamMap = new HashMap<>();

    private ParticipantAdapter adapter;

    private Handler handler;

    @Nullable
    private ProgressDialog dialog = null;

    private ConferenceOutput conferenceOutput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_conf_activity);

        ButterKnife.bind(this);

        adapter = new ParticipantAdapter(this);

        participants.setAdapter(adapter);

        conferenceOutput = new ConferenceOutput(this);

        handler = new Handler(Looper.getMainLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else
            initConf();
    }

    @OnClick(R.id.toggle_video)
    public void toggleVideo() {
        VoxeetSdk.toggleSdkVideo();
    }

    @OnClick(R.id.leaveConf)
    public void leave() {
        VoxeetSdk.leaveSdkConference();
    }

    @OnClick(R.id.record)
    public void toggleRecord() {
        VoxeetSdk.toggleSdkRecording();
    }

    @OnClick(R.id.send_broadcast)
    public void sendBroadcast() {
        VoxeetSdk.sendSdkBroadcast(broadcastConference.getText().toString());
    }

    @OnClick(R.id.mute)
    public void mute() {
        boolean muted = !VoxeetSdk.isSdkMuted();
        if (muted)
            mute.setText("Muted");
        else
            mute.setText("Not Muted");

        VoxeetSdk.muteSdkConference(muted);
    }

    @OnClick(R.id.audio_routes)
    public void audioRoutes() {
        if (conferenceOutput != null) {
            if (conferenceOutput.isVisible()) {
                conferenceOutput.dismiss();
            } else {
                conferenceOutput.show(CreateConfActivity.this.getFragmentManager(), ConferenceOutput.TAG);
            }
        }
    }

    @OnClick(R.id.join)
    public void join() {
        VoxeetSdk.register(this, this);

        if (action == MainActivity.JOIN)
            VoxeetSdk.joinSdkConference(editextConference.getText().toString());
        else if (action == MainActivity.REPLAY)
            VoxeetSdk.replaySdkConference(editextConference.getText().toString());

        showProgress();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initConf();
            else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();

                finish();
            }
        }
    }

    private void initConf() {
        if (getIntent().hasExtra("join")) {
            action = MainActivity.JOIN;

            setTitle("Join Conference");

            displayJoin();
        } else if (getIntent().hasExtra("replay")) {
            action = MainActivity.REPLAY;

            setTitle("Replay Conference");

            List<Recording> confs = ((SampleApplication) getApplication()).getRecordedConferences();
            if (confs.size() > 0) {
                recordedConferences.setVisibility(VISIBLE);
                recordedConferences.setAdapter(new RecordedConferencesAdapter(this, confs));
                recordedConferences.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        VoxeetSdk.replaySdkConference(((Recording) recordedConferences.getItemAtPosition(i)).conferenceId);

                        showProgress();

                        joinLayout.setVisibility(View.GONE);
                    }
                });
            }

            displayJoin();
        } else if (getIntent().hasExtra("demo")) {
            action = MainActivity.DEMO;

            showProgress();

            setTitle("Demo Conference");

            VoxeetSdk.createSdkDemo();
        } else {
            action = MainActivity.CREATE;

            showProgress();

            setTitle("Create Conference");

            VoxeetSdk.createSdkConference();
        }

        VoxeetSdk.register(this, this);
    }

    private void displayJoin() {
        joinLayout.setVisibility(VISIBLE);
    }

    @Override
    public void onBackPressed() {
//        if (VoxeetSdk.isSdkConferenceLive())
//            displayLeaveDialog();
//        else
        super.onBackPressed();
    }

    private void displayLeaveDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.leave_conference_title));
        alertDialog.setMessage(getString(R.string.leave_conference_message));
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                VoxeetSdk.leaveSdkConference();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void updateStreams(ConferenceUser user, MediaStream mediaStream) {
        mediaStreamMap.put(user.getUserId(), mediaStream);

        if (user.getUserId().equalsIgnoreCase(VoxeetPreferences.id())) {
            if (action == MainActivity.REPLAY)
                addParticipant(user);
            else if (mediaStream != null && mediaStream.hasVideo()) { //enabling own video
                videoStream.setVisibility(VISIBLE);
                videoStream.attach(user.getUserId(), mediaStream);
            } else { // disabling own video
                videoStream.setVisibility(View.GONE);
                videoStream.unAttach();
            }
        } else { // displaying participant conference video
            addParticipant(user);
        }
    }

    private void addParticipant(ConferenceUser user) {
        adapter.addParticipant(user);
        adapter.updateMediaStreams(mediaStreamMap);
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceJoinedSuccessEvent event) {
        hideProgress();

        leave.setVisibility(VISIBLE);

        joinLayout.setVisibility(View.GONE);

        conferenceOptions.setVisibility(VISIBLE);

        if (action == MainActivity.DEMO || action == MainActivity.REPLAY) {
            record.setVisibility(View.GONE);
            video.setVisibility(View.GONE);
        } else {
            aliasId.setVisibility(VISIBLE);
            aliasId.setText(event.getAliasId() != null ? event.getAliasId() : event.getConferenceId());

            sendText.setVisibility(VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceLeftSuccessEvent event) {
        onConferenceEnding();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceEnded event) {
        onConferenceEnding();
    }

    private void onConferenceEnding() {
        VoxeetSdk.unregister(CreateConfActivity.this);

        screenShare.unAttach(); // unattaching just in case

        videoStream.unAttach(); // unattaching just in case

        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserLeftEvent event) {
        adapter.removeParticipant(event.getUser());
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserJoinedEvent event) {
        updateStreams(event.getUser(), event.getMediaStream());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final ConferenceUserUpdatedEvent event) {
        updateStreams(event.getUser(), event.getMediaStream());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScreenStreamAddedEvent event) {
        MediaStream mediaStream = event.getMediaStream();
        if (mediaStream != null && mediaStream.hasVideo()) { // attaching stream
            screenShare.setVisibility(VISIBLE);
            screenShare.attach(event.getPeer(), mediaStream);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RecordingStatusUpdateEvent event) {
        if (event.getRecordingStatus().equalsIgnoreCase(RecordingStatus.RECORDING.name())) {
            ((SampleApplication) getApplication()).saveRecordingConference(new Recording(event.getConferenceId(), event.getTimeStamp()));

            record.setText("Stop Recording");
        } else {
            record.setText("Start Recording");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScreenStreamRemovedEvent event) { // unattaching stream
        screenShare.setVisibility(View.GONE);
        screenShare.unAttach();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageReceived event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void showProgress() {
        try {
            if (dialog != null)
                return;
            dialog = ProgressDialog.show(this, "", getString(R.string.loading));
            dialog.setCancelable(true);
        } catch (Exception e) {
            Log.e(TAG, "error", e.getCause());
        }
    }

    public void hideProgress() {
        try {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "error", e.getCause());
        }
    }
}