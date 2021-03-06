package com.lwonho92.everchat.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.lwonho92.everchat.ProfileActivity;
import com.lwonho92.everchat.data.EverChatMessage;
import com.lwonho92.everchat.R;
import com.lwonho92.everchat.data.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by MY on 2017-02-08.
 */

public class ChatAdapter extends FirebaseRecyclerAdapter<EverChatMessage, ChatAdapter.ChatAdapterViewHolder> {
    private static final String TAG = "ChatAdapter";
    private static final String clientId = "4XY1BAMwckenaG2O2vvy";//애플리케이션 클라이언트 아이디값";
    private static final String clientSecret = "CU7gWgfDcw";//애플리케이션 클라이언트 시크릿값";

    private static Context mContext;
    private final static String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public static class ChatAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ll_message) LinearLayout linearLayout;
        @BindView(R.id.im_message_photo) CircleImageView photoImageView;
        @BindView(R.id.tv_messenger) TextView messengerTextView;
        @BindView(R.id.tv_message) TextView messageTextView;
        @BindView(R.id.ib_send_picture) ImageButton pictureImageButton;
        @BindView(R.id.tv_timestamp) TextView timestampTextView;
        private String sourceMessage, uid;

        public ChatAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final EverChatMessage everChatMessage, int type) {
            timestampTextView.setText(Utils.getMillisToStr(everChatMessage.getTimestampLong()));
            uid = everChatMessage.getUid();

            GradientDrawable drawable = null;
            if(everChatMessage.getMessage() != null) {
//                for message
                drawable = (GradientDrawable) messageTextView.getBackground();
                pictureImageButton.setVisibility(View.GONE);
                messageTextView.setVisibility(View.VISIBLE);

                new AsyncTask<Void, Void, Object[]>() {
                    @Override
                    protected Object[] doInBackground(Void... params) {
                        String prefDefaultLanguage = mContext.getString(R.string.pref_default_language);
                        String source = everChatMessage.getLanguage();
                        String target = PreferenceManager.getDefaultSharedPreferences(mContext).getString(mContext.getString(R.string.pref_language), prefDefaultLanguage);
                        sourceMessage = everChatMessage.getMessage();
                        Uri uri = Utils.convertSourceMessageToUri(sourceMessage, source, target);

                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        boolean isOnTranslate = pref.getBoolean(mContext.getString(R.string.pref_translate), mContext.getResources().getBoolean(R.bool.pref_default_translate));

                        if(!isOnTranslate || source.equals(target)) {
//                        Both languages are same.
                            return new Object[] {uri, sourceMessage};
                        }
                        else if(source.equals(prefDefaultLanguage) || target.equals(prefDefaultLanguage)) {
//                        At least a language is Korean.
                            return new Object[] {uri, translateMessage(source, target, sourceMessage)};
                        } else {
//                        Both languages are not Korean.
                            String tmp = translateMessage(source, prefDefaultLanguage, sourceMessage);
                            return new Object[] {uri, translateMessage(prefDefaultLanguage, target, tmp)};
                        }
                    }

                    @Override
                    protected void onPostExecute(final Object objects[]) {
                        messageTextView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData((Uri)objects[0]);
                                mContext.startActivity(intent);
                                return true;
                            }
                        });
                        messageTextView.setText((String)objects[1]);
                    }
                }.execute();
            } else if(everChatMessage.getPicture() != null) {
//                for picture
                drawable = (GradientDrawable) pictureImageButton.getBackground();
                pictureImageButton.setVisibility(View.VISIBLE);
                messageTextView.setVisibility(View.GONE);

                Glide.with(ChatAdapter.mContext)
                        .load(everChatMessage.getPicture())
                        .into(pictureImageButton);
                pictureImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(mContext);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawable( new ColorDrawable(android.graphics.Color.TRANSPARENT) );

                        ImageView imageView = new ImageView(mContext);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        Glide.with(mContext)
                                .load(everChatMessage.getPicture())
                                .into(imageView);
                        dialog.addContentView(imageView, new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

                        dialog.show();
                    }
                });
            }

            if(type == 0) {
//                sent message me
                linearLayout.setGravity(Gravity.END);
                drawable.setColor(Color.YELLOW);
            }
            else {
//                sent message others
                linearLayout.setGravity(Gravity.START);
                photoImageView.setVisibility(View.VISIBLE);
                messengerTextView.setText(everChatMessage.getName());
                drawable.setColor(Color.WHITE);

                Glide.with(ChatAdapter.mContext)
                        .load(everChatMessage.getPhotoUrl())
                        .into(photoImageView);
            }
        }

        private String translateMessage(String source, String target, String message) {
            try {
                String utfMessage = URLEncoder.encode(message, "UTF-8");
                String apiURL = mContext.getString(R.string.naver_api_url);
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty(mContext.getString(R.string.naver_client_id), clientId);
                con.setRequestProperty(mContext.getString(R.string.naver_client_secret), clientSecret);
                // post request
                String postParams = "source=" + source + "&target=" + target + "&text=" + utfMessage;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
//                            http code 400 / errorCode TR05 : source와 target이 동일.
//                            http code 400 / errorCode TR06 : source와 target 쌍이 적절하지 않습니다.
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                con.disconnect();

                JSONObject translatedText = new JSONObject(response.toString()).getJSONObject("message").getJSONObject("result");

                return translatedText.getString("translatedText");
            } catch (Exception  e) {
                Log.d(TAG, "Called onConnectionFailed:" + e.toString());
            }

            return "";
        }

        @OnClick(R.id.im_message_photo)
        public void onClick() {
            Intent intent = new Intent(mContext, ProfileActivity.class);
            intent.putExtra(mContext.getString(R.string.profile_selected_uid), uid);
            mContext.startActivity(intent);
        }
    }

    public ChatAdapter(Context context, Class<EverChatMessage> modelClass, int modelLayout, Class<ChatAdapterViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);

        mContext = context;
    }

    @Override
    protected EverChatMessage parseSnapshot(DataSnapshot snapshot) {
        EverChatMessage everChatMessage = super.parseSnapshot(snapshot);
        if(everChatMessage != null) {
            everChatMessage.setId(snapshot.getKey());
        }
        return everChatMessage;
    }

    @Override
    public int getItemViewType(int position) {
        EverChatMessage everChatMessage = getItem(position);

        if(uid.equals(everChatMessage.getUid()))
//            My message
            return 0;
        else
//            Others message
            return 1;
    }

    @Override
    protected void populateViewHolder(ChatAdapterViewHolder viewHolder, EverChatMessage everChatMessage, int position) {
        int type = getItemViewType(position);

        viewHolder.bind(everChatMessage, type);
    }

    @Override
    public void onBindViewHolder(ChatAdapterViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }
}
