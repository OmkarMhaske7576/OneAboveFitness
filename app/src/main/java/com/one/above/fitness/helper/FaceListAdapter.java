package com.one.above.fitness.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.one.above.fitness.R;
import com.one.above.fitness.activities.FaceImageListContainer;
import com.one.above.fitness.pojo.FaceImgData;

import java.util.LinkedList;

public class FaceListAdapter extends RecyclerView.Adapter<FaceListAdapter.RecyclerViewHolder> {

    private final Context context;
    private LinkedList<FaceImgData> faceImgDataList; // Original Values

    public FaceListAdapter(Context context, LinkedList<FaceImgData> data) {
        this.context = context;
        this.faceImgDataList = data;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.face_image_list, viewGroup, false);

        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int i) {
        try {
            FaceImgData faceImgData = faceImgDataList.get(i);
            holder.image.setImageBitmap(faceImgData.getImageData());
            holder.selectImgRadio.setTag(i);
            holder.memberId.setText(new StringBuilder("Member Id : ").append(faceImgData.getUserId()).toString());
            holder.selectImgRadio.setChecked(i == FaceImageListContainer.selectedPosition);
            holder.selectImgRadio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemCheckChanged(v);
                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return (null != faceImgDataList ? faceImgDataList.size() : 0);
    }

    private void itemCheckChanged(View v) {
        FaceImageListContainer.selectedPosition = (Integer) v.getTag();
        notifyDataSetChanged();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        RadioButton selectImgRadio;
        ImageView image;
        TextView memberId;

        RecyclerViewHolder(View view) {
            super(view);

            selectImgRadio = view.findViewById(R.id.selectImg);
            image = view.findViewById(R.id.image);
            memberId = view.findViewById(R.id.memberId);
        }
    }
}
