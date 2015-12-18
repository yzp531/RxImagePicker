package com.yokeyword.rximagepicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;


import com.yokeyword.R;
import com.yokeyword.rximagepicker.fragments.DetailExploreFragment;
import com.yokeyword.rximagepicker.fragments.ExploreFragment;
import com.yokeyword.rximagepicker.fragments.PreviewFragment;
import com.yokeyword.rximagepicker.helper.RxBus;
import com.yokeyword.rximagepicker.model.event.AddDetailEvent;
import com.yokeyword.rximagepicker.model.event.AddPreviewEvent;


import rx.subscriptions.CompositeSubscription;

/**
 * 图库浏览界面
 * Created by Yokeyword on 2015/12/14.
 */
public class ImagePickerActivity extends AppCompatActivity {
    public static final String EXTRA_MULTIPLE_PICKER = "extra_multiple_picker";
    public static final String EXTRA_SINGLE_PICKER = "extra_single_picker";

    private static final String EXTRA_IS_MULTIPLE = "extra_is_multiple";

    private Toolbar toolbar;
    private boolean isMultiplePick = true;

    private CompositeSubscription rxSubscriptions = new CompositeSubscription();

    public static Intent getCallingIntent(Context context, boolean multiplePick) {
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(EXTRA_IS_MULTIPLE, multiplePick);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isMultiplePick = bundle.getBoolean(EXTRA_IS_MULTIPLE, true);
        }

        initView(savedInstanceState);
        initRxBus();
    }

    protected void initView(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, ExploreFragment.newInstance()).commit();
            toolbar.setTitle(R.string.yo_pick_pic);
        } else {
            // 回退栈里包含 PicPreviewFragment时 TitleBar标题为"预览"
            if (getSupportFragmentManager().getBackStackEntryCount() == 2) {
                toolbar.setTitle(R.string.yo_preview);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRxBus() {
        // 接收 切换相册事件
        rxSubscriptions.add(RxBus.getDefault().toObserverable(AddDetailEvent.class)
                .map(addDetailEvent -> addDetailEvent.getBucketName())
                .subscribe(bucketName -> {
                    addFragment(DetailExploreFragment.newInstance(bucketName, isMultiplePick));
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(this, R.string.yo_switch_bucket_exception, Toast.LENGTH_SHORT).show();
                }));
        // 接收 切换预览事件
        rxSubscriptions.add(RxBus.getDefault().toObserverable(AddPreviewEvent.class)
                .map(addPreviewEvent -> addPreviewEvent.getImgs())
                .subscribe(imgs -> {
                    toolbar.setTitle(R.string.yo_preview);
                    addFragment(PreviewFragment.newInstance(imgs, isMultiplePick));
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(this, R.string.yo_switch_preview_exception, Toast.LENGTH_SHORT).show();
                }));
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        trans.add(R.id.container, fragment);
        trans.addToBackStack(null);
        try {
            trans.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            toolbar.setTitle(R.string.yo_pick_pic);
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消事件 防止内存泄漏
        if (!rxSubscriptions.isUnsubscribed()) {
            rxSubscriptions.unsubscribe();
        }
    }
}
