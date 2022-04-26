package com.example.depthmapping.ui.home;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.depthmapping.R;
import com.example.depthmapping.Util;
import com.example.depthmapping.classifier.ImageClassifier;
import com.example.depthmapping.databinding.FragmentLoadingBinding;
import com.example.depthmapping.ui.home.recognized.RecognizedImageFragment;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import okhttp3.RequestBody;

import org.json.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class LoadingFragment extends Fragment {
    private String image;

    FragmentLoadingBinding binding;

    public LoadingFragment() {
        // Required empty public constructor
    }

    public static LoadingFragment newInstance(String image) {
        LoadingFragment fragment = new LoadingFragment();
        Bundle args = new Bundle();
        args.putString("image", image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            image = getArguments().getString("image");
            System.out.println(image);
        }

        Bitmap bitmap = Util.convert(image);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();

        String url = "https://5efc-34-122-12-129.ngrok.io";

        RequestBody formBody = new FormBody.Builder()
                .add("Image", image)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("estimateImage", "True")
                .addHeader("countSegmentation", "100")
                .addHeader("colorizeResults", "False")
                .addHeader("predictImage", "True")
                .addHeader("getPredictImage", "True")
                .addHeader("threshold", "0.5")
                .post(formBody)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonString = response.body().string();
                    String estimated = null;
                    String predicted = null;
                    JSONObject obj = null;

                    try {
                        obj = new JSONObject(jsonString);
                        estimated = obj.getString("estimateImage");
                        predicted = obj.getString("predictImage");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(estimated);
                    System.out.println(predicted);

                    String finalEstimated = estimated;
                    String finalPredicted = predicted;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Fragment frag2 = RecognizedImageFragment.newInstance(finalEstimated, image, "db");
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.add(R.id.container, frag2);
                            ft.commit();
                        }
                    });
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoadingBinding.inflate(inflater, container, false);

        binding.avi.show();
        View root = binding.getRoot();
        return root;
    }
}