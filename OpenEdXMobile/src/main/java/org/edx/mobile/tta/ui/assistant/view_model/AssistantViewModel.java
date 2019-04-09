package org.edx.mobile.tta.ui.assistant.view_model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.gson.JsonElement;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowAssistantItemBinding;
import org.edx.mobile.databinding.TRowContentBinding;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.SourceType;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.assistant.AssistantModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.edx.mobile.tta.ui.course.CourseDashboardActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.ContentSourceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class AssistantViewModel extends BaseViewModel implements AIListener {

    public ObservableField<String> hintText = new ObservableField<>();
    public AssistantAdapter assistantAdapter;
    public LinearLayoutManager linearLayoutManager;

    public ObservableInt scrollPosition=new ObservableInt();
    public ObservableField<String> requestText = new ObservableField<>();
    public ObservableBoolean isListening = new ObservableBoolean();
    private AIService aiService;

    public AssistantViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        //assistant  recycler adapter
        linearLayoutManager = new LinearLayoutManager(getActivity());
        assistantAdapter = new AssistantAdapter(getActivity());
        //welcome message
        addToAdapter(new AssistantModel("Welcome "+mDataManager.getLoginPrefs().getCurrentUserProfile().name,false));
        addToAdapter(new AssistantModel("Tap on mic to start conversation or Type into edit box.",false));
        addToAdapter(new AssistantModel("What would you like to read today?",false));
        //init API.AI
        final AIConfiguration config = new AIConfiguration(mDataManager.getConfig().getDialogFlowClientToken(),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(context, config);
        aiService.setListener(this);
//        hintText.set("Tap on mic to start conversation \nor \nType into edit field.");
    }

    @Override
    public void onResume() {
        linearLayoutManager = new LinearLayoutManager(mActivity);

    }

    private void addToAdapter(AssistantModel model) {
        //add request to recycler adapter
        assistantAdapter.add(model);
        scrollPosition.set(assistantAdapter.getItemCount()-1);
    }

    @Override
    public void onResult(AIResponse response) {
        hintText.set(""); //reset hint text
        // Show results in TextView.
        addToAdapter(new AssistantModel(response.getResult().getResolvedQuery(), true));
        processResponse(response);
    }

    @Override
    public void onError(AIError error) {
//        resultText.set(error.toString());
        requestText.set("");
        isListening.set(false);
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        isListening.set(true);
        requestText.set("Listening ...");
    }

    @Override
    public void onListeningCanceled() {
        requestText.set("");
        isListening.set(false);
    }

    @Override
    public void onListeningFinished() {
        requestText.set("");
        isListening.set(false);
    }

    public void startListening() {
        aiService.startListening();
    }

    @SuppressLint("StaticFieldLeak")
    public void send() {
        hintText.set("");
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(requestText.get());
        addToAdapter(new AssistantModel(requestText.get(), true));
        requestText.set("");//reset edit text
        //request from here
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    return aiService.textRequest(requests[0]);
                } catch (AIServiceException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    // process aiResponse here
                    processResponse(aiResponse);
                }
            }
        }.execute(aiRequest);
    }

    private void processResponse(AIResponse response) {
        Result result = response.getResult();
        Log.d("MK", "onResult: " + result.toString());
        List<String> tags = new ArrayList<>();
        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                if (entry.getKey().equals("tag")) {//add into tag list
                    tags.add(entry.getValue().getAsString());
                } else if (entry.getKey().equals("title")) {
                    parameterString += entry.getValue().getAsString();
                }
//                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }
        Log.d("MK", "Parameters: " + parameterString);
//        resultText.set("Query:" + result.getResolvedQuery() +
//                "\nAction: " + result.getAction() +
//                "\nParameters: " + parameterString);
        mActivity.showLoading();
        mDataManager.findContentForAssistant(parameterString, tags, new OnResponseCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                mActivity.hideLoading();
                if (data == null || data.size() == 0) {
                    addToAdapter(new AssistantModel("Sorry! No result found. Try searching something else.", false));
                } else {
                    //found data
                    addToAdapter(new AssistantModel(data.size() > 1 ? "Here are some results. What would you like to read?"
                            : "Here is the result", false));
                    AssistantModel model = new AssistantModel();
                    model.setContentList(data);
                    addToAdapter(model); //update on adapter
                }

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
//               data not found
                e.printStackTrace();
                addToAdapter(new AssistantModel("Error occurred! " + e.getMessage(), false));
            }
        });
    }

    public void showContentDashboard(Content selectedContent){

        Bundle parameters = new Bundle();
        parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
        if (selectedContent.getSource().getType().equalsIgnoreCase(SourceType.course.name()) ||
                selectedContent.getSource().getType().equalsIgnoreCase(SourceType.edx.name())) {
            ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);
        } else {
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

    }


    public class AssistantAdapter extends MxInfiniteAdapter<AssistantModel> {

        public AssistantAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull AssistantModel model, @Nullable OnRecyclerItemClickListener<AssistantModel> listener) {
            if (binding instanceof TRowAssistantItemBinding) {
                TRowAssistantItemBinding itemBinding = (TRowAssistantItemBinding) binding;
                if (model.getContentList() != null) {  //make card item
                    itemBinding.requestText.setVisibility(View.GONE);
                    itemBinding.responseText.setVisibility(View.GONE);
                    itemBinding.contentCardList.setVisibility(View.VISIBLE);
                    itemBinding.contentCardList.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));
                    ContentListAdapter contentListAdapter=new ContentListAdapter(getContext());
                    itemBinding.contentCardList.setAdapter(contentListAdapter);
                    contentListAdapter.addAll(model.getContentList());
                } else {
                    itemBinding.contentCardList.setVisibility(View.GONE);
                    itemBinding.requestText.setVisibility(model.isRequest() ? View.VISIBLE : View.GONE);
                    itemBinding.responseText.setVisibility(model.isRequest() ? View.GONE : View.VISIBLE);
                    if (model.isRequest()) {
                        itemBinding.requestText.setText(model.getText());
                    } else {
                        itemBinding.responseText.setText(model.getText());
                    }
                }

            }
        }
    }

    public class ContentListAdapter extends MxInfiniteAdapter<Content> {

        public ContentListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowContentBinding) {
                TRowContentBinding contentBinding = (TRowContentBinding) binding;
                contentBinding.contentCategory.setText(model.getSource().getTitle());
                contentBinding.contentCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContentSourceUtil.getSourceDrawable_10x10(model.getSource().getName()),
                        0, 0, 0);
                contentBinding.contentTitle.setText(model.getName());
                Glide.with(mActivity)
                        .load(model.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(contentBinding.contentImage);
                contentBinding.getRoot().setOnClickListener(v -> {
                    showContentDashboard(model);
//                    if (listener != null) {
//                        listener.onItemClick(v, model);
//                    }
                });
            }
        }
    }
}
