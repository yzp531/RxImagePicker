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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yokeyword.R;
import com.yokeyword.rximagepicker.helper.OnRecyclerViewItemClickListener;
import com.yokeyword.rximagepicker.model.BucketEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 相册 适配器
 * Created by Yokeyword on 2015/12/14.
 */
public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.MyViewHolder> {
    private Context context;
    private Fragment fragment;
    private LayoutInflater inflater;
    private OnRecyclerViewItemClickListener listener;
    private List<BucketEntity> items = new ArrayList<>();

    private int width, height;

    public ExploreAdapter(Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        inflater = LayoutInflater.from(context);

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        width = (dm.widthPixels - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3 * 8, context.getResources().getDisplayMetrics())) / 2;
        height = (int) (width * 1.0);
    }

    public void setDatas(List<BucketEntity> beans) {
        items.clear();
        items.addAll(beans);
        notifyDataSetChanged();
    }


    public void addData(BucketEntity bean) {
        items.add(bean);
        notifyItemInserted(items.size() - 1);
    }

    public BucketEntity getItem(int position) {
        return items.get(position);
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_explore, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        holder.pic.setLayoutParams(params);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(v, (int) v.getTag());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setTag(position);

        BucketEntity item = items.get(position);

        Glide.with(fragment)
                .load(new File(item.getPath()))
                .placeholder(R.drawable.bg_grey)
                .centerCrop()
                .crossFade()
                .into(holder.pic);

        String bucket_name = item.getName();
        if (bucket_name.toLowerCase().equals("camera")) {
            holder.name.setText(R.string.yo_my_pic);
        } else if (bucket_name.toLowerCase().equals("screenshots")) {
            holder.name.setText(R.string.yo_screenshots);
        } else {
            holder.name.setText(bucket_name);
        }

        holder.count.setText(String.format(context.getString(R.string.yo_count), item.getCount()));
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
        TextView name, count;

        public MyViewHolder(View itemView) {
            super(itemView);

            pic = (ImageView) itemView.findViewById(R.id.pic);
            name = (TextView) itemView.findViewById(R.id.name);
            count = (TextView) itemView.findViewById(R.id.count);
        }
    }
}
