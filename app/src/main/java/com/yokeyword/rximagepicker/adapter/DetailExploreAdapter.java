package com.yokeyword.rximagepicker.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.yokeyword.R;
import com.yokeyword.rximagepicker.helper.OnRecyclerViewItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片空间适配器
 * Created by Yokeyword on 2015/12/14.
 */
public class DetailExploreAdapter extends RecyclerView.Adapter<DetailExploreAdapter.MyViewHolder> {

    private Fragment fragment;
    private LayoutInflater inflater;
    private OnRecyclerViewItemClickListener listener;
    private List<File> items = new ArrayList<>();

    // 用来控制CheckBox的选中状况
    private static List<Boolean> mChecked;

    private int width, height;

    public DetailExploreAdapter(Context context, Fragment fragment) {
        this.fragment = fragment;
        inflater = LayoutInflater.from(context);

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2 * 4, context.getResources().getDisplayMetrics());
        width = (dm.widthPixels - space) / 3;
        height = width;
    }

    public void setDatas(List<File> beans) {
        items.clear();
        items.addAll(beans);
        notifyDataSetChanged();
    }


    public void addData(File file) {
        items.add(file);
        if (mChecked == null) {
            mChecked = new ArrayList<>();
        }
        mChecked.add(false);
        notifyItemInserted(getItemCount() - 1);
    }

    private void initData() {
        mChecked = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            mChecked.add(false);
        }
    }

    public boolean getChecked(int position) {
        return mChecked.get(position);
    }

    public void setChecked(int position) {
        mChecked.set(position, !mChecked.get(position));
        notifyItemChanged(position);
    }

    public boolean isNoneChecked() {
        for (int i = 0; i < getItemCount(); i++) {
            if (getChecked(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 得到选中的文件path
     *
     * @return
     */
    public ArrayList<String> getCheckedList() {
        ArrayList<String> checkedFiles = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            if (mChecked.get(i)) {
                File f = items.get(i);
                checkedFiles.add(f.getAbsolutePath());
            }
        }

        return checkedFiles;
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_explore_detail, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        holder.pic.setLayoutParams(params);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(v, (int) v.getTag()));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setTag(position);

        File item = items.get(position);
        Glide.with(fragment)
                .load(new File(item.getAbsolutePath()))
                .placeholder(R.drawable.bg_grey)
                .centerCrop()
                .crossFade()
                .into(holder.pic);

        if (mChecked.get(position)) {
            holder.pic.setAlpha(0.4f);
            holder.checkbox.setChecked(true);
        } else {
            holder.pic.setAlpha(1f);
            holder.checkbox.setChecked(false);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView pic;
        CheckBox checkbox;

        public MyViewHolder(View itemView) {
            super(itemView);
            pic = (ImageView) itemView.findViewById(R.id.pic);
            checkbox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
