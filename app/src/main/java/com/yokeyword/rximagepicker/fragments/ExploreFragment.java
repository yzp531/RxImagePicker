package com.yokeyword.rximagepicker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yokeyword.R;
import com.yokeyword.rximagepicker.adapter.ExploreAdapter;
import com.yokeyword.rximagepicker.helper.RxBus;
import com.yokeyword.rximagepicker.helper.SpacingDecoration;
import com.yokeyword.rximagepicker.model.BucketEntity;
import com.yokeyword.rximagepicker.model.event.AddDetailEvent;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 手机上图片浏览界面
 * Created by Yokeyword on 2015/12/14.
 */
public class ExploreFragment extends Fragment {
    private static final int REQT_READ_EXTERNAL_STORAGE = 123;
    private static final String COLUMN_NAME_COUNT = "v_count";

    private Activity activity;
    private RecyclerView recyclerView;
    private ExploreAdapter adapter;

    private Subscription subscription;

    public static ExploreFragment newInstance() {
        return new ExploreFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        initView(view);
        // 请求权限
        requestReadStoragePermission();
        return view;
    }

    /**
     * 6.0 请求权限
     */
    private void requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkReadStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkReadStoragePermission != PackageManager.PERMISSION_GRANTED) {
                // 使用该方法请求授权 在fragment里将收不到授权结果的回调
                // ActivityCompat.requestPermissions(activity,...,...);
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQT_READ_EXTERNAL_STORAGE);
            } else {
                initData();
            }
        } else {
            initData();
        }
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recy);

        GridLayoutManager manager = new GridLayoutManager(activity, 2);
        adapter = new ExploreAdapter(activity, ExploreFragment.this);
        recyclerView.addItemDecoration(new SpacingDecoration(24, 24, true));
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setOnRecyclerViewItemClickListener((v, position) -> {
            BucketEntity bucketBean = adapter.getItem(position);
            // addFragment
            RxBus.getDefault().post(new AddDetailEvent(bucketBean.getName()));
        });
    }

    private void initData() {
        subscription = cursorObservable()
                .subscribeOn(Schedulers.io())
                .filter(cursor -> {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    return !path.endsWith(".gif");
                })
                .map(cursor -> {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String bucket_name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    int count = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_COUNT));
                    return new BucketEntity(bucket_name, count, path);
                })
                .toList()
                // 如果不用toList()转成List  一定要调用onBackpressureBuffer()方法,防止数据源发射过快,导致异常MissingBackpressureException
//                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bucketEntity -> {
                            adapter.setDatas(bucketEntity);
                        }, throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(activity, R.string.yo_find_exception, Toast.LENGTH_SHORT).show();
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
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                "COUNT(*) AS " + COLUMN_NAME_COUNT
        };

        // SELECT _data, COUNT(*) AS v_count  FROM video WHERE ( GROUP BY bucket_display_name)
        String selection = " 1=1 ) GROUP BY (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME;

        return activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaColumns, selection, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQT_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initData();
            } else {
                Toast.makeText(activity, R.string.yo_denied_permission, Toast.LENGTH_LONG).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
