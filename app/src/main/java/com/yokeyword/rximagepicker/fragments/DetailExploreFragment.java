package com.yokeyword.rximagepicker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.yokeyword.R;
import com.yokeyword.rximagepicker.ImagePickerActivity;
import com.yokeyword.rximagepicker.adapter.DetailExploreAdapter;
import com.yokeyword.rximagepicker.helper.OnRecyclerViewItemClickListener;
import com.yokeyword.rximagepicker.helper.RxBus;
import com.yokeyword.rximagepicker.helper.SpacingDecoration;
import com.yokeyword.rximagepicker.model.event.AddPreviewEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 手机上图片详细 界面
 * Created by Yokeyword on 2015/12/14.
 */
public class DetailExploreFragment extends BaseFragment implements OnRecyclerViewItemClickListener, View.OnClickListener {
    private static final String ARG_BUCKET = "arg_bucket";
    private static final String ARG_IS_MULTIPLE = "arg_is_multiple";

    private RecyclerView recyclerView;
    private TextView tvBtnPreview, tvBtnYes, tvPickPicCount;

    private Subscription subscription;

    // 专辑名称
    private String bucket_name = "";
    private boolean isMultiplePick;
    private int lastPosition = -1;
    DetailExploreAdapter adapter;

    public static DetailExploreFragment newInstance(String bucket_name, boolean isMultiplePick) {
        DetailExploreFragment fragment = new DetailExploreFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_BUCKET, bucket_name);
        bundle.putBoolean(ARG_IS_MULTIPLE, isMultiplePick);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_detail, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recy);
        tvBtnPreview = (TextView) view.findViewById(R.id.tv_btn_preview);
        tvBtnYes = (TextView) view.findViewById(R.id.tv_btn_yes);
        tvPickPicCount = (TextView) view.findViewById(R.id.tv_pick_pic_count);

        tvBtnYes.setOnClickListener(this);
        tvBtnPreview.setOnClickListener(this);

        GridLayoutManager manager = new GridLayoutManager(_activity, 3);
        adapter = new DetailExploreAdapter(_activity, DetailExploreFragment.this);
        recyclerView.addItemDecoration(new SpacingDecoration(12, 12, false));
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setOnRecyclerViewItemClickListener(this);
    }

    private void initData() {
        subscription = cursorObservable()
                .subscribeOn(Schedulers.io())
                // 延迟60ms发射数据 形成动画, delay默认在computation线程 要主动切换到当前的线程
                .delay(60, TimeUnit.MILLISECONDS, Schedulers.immediate())
                .filter(cursor -> {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    return !path.endsWith(".gif");
                })
                .map(cursor -> {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    return new File(path);
                })
//                .toList()
                // 如果不用toList()转成List  一定要调用onBackpressureBuffer()方法,防止数据源发射过快,导致异常MissingBackpressureException
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        file -> {
                            adapter.addData(file);
                        }, throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(_activity, R.string.yo_find_exception, Toast.LENGTH_SHORT).show();
                        }
                );
    }

    private Observable<Cursor> cursorObservable() {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    Cursor cursor = getCursor();
                    while (cursor.moveToNext()) {
                        subscriber.onNext(cursor);
                    }
                    subscriber.onCompleted();
                    cursor.close();
                }
            }
        });
    }

    private Cursor getCursor() {
        String[] mediaColumns = new String[]{
                MediaStore.Images.Media.DATA,
        };
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";

        return _activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaColumns, selection, new String[]{bucket_name}, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bucket_name = getArguments().getString(ARG_BUCKET);
        isMultiplePick = getArguments().getBoolean(ARG_IS_MULTIPLE, true);
    }

    @Override
    public void onItemClick(View v, int position) {
        if (!isMultiplePick) {  // single mode
            if (lastPosition >= 0 && position != lastPosition) {
                adapter.setChecked(lastPosition);
            }
        }

        adapter.setChecked(position);

        tvPickPicCount.setText(String.format(getString(R.string.yo_select_pic_count), adapter.getCheckedList().size()));

        if (adapter.isNoneChecked()) {
            tvBtnPreview.setTextColor(ContextCompat.getColor(_activity, R.color.yo_text_light));
            tvBtnYes.setTextColor(ContextCompat.getColor(_activity, R.color.yo_text_light));
        } else {
            tvBtnPreview.setTextColor(ContextCompat.getColor(_activity, R.color.yo_blue));
            tvBtnYes.setTextColor(ContextCompat.getColor(_activity, R.color.yo_blue));
        }

        if (adapter.getChecked(position)) {
            lastPosition = position;
        } else {
            lastPosition = -1;
        }
    }

    @Override
    public void onClick(View v) {
        ArrayList<String> checkedPics = adapter.getCheckedList();

        if (checkedPics.size() == 0) {
            Toast.makeText(_activity, R.string.yo_tip_select, Toast.LENGTH_LONG).show();
        } else {
            if (v.getId() == R.id.tv_btn_preview) {
                // 预览
                RxBus.getDefault().post(new AddPreviewEvent(checkedPics));
            } else if (v.getId() == R.id.tv_btn_yes) {
                Intent data = new Intent();
                if (!isMultiplePick) {
                    data.putExtra(ImagePickerActivity.EXTRA_SINGLE_PICKER, checkedPics.get(0));
                }
                data.putStringArrayListExtra(ImagePickerActivity.EXTRA_MULTIPLE_PICKER, checkedPics);
                _activity.setResult(Activity.RESULT_OK, data);
                _activity.finish();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
