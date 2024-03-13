package com.one.above.fitness.helper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.one.above.fitness.R;
import com.one.above.fitness.activities.FaceImageListContainer;
import com.one.above.fitness.activities.FaceListActivity;
import com.one.above.fitness.activities.FaceRegisterActivity;
import com.one.above.fitness.database.SQLiteDatabaseHandler;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.pojo.FaceImgData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.RecyclerViewHolder> implements Filterable {

    private final Context context;
    private List<FaceData> mOriginalValues; // Original Values
    private List<FaceData> mDisplayedValues;
    SQLiteDatabaseHandler db;

    public MemberListAdapter(Context context, List<FaceData> data) {

        this.context = context;
        db = new SQLiteDatabaseHandler(context);
        this.mOriginalValues = data;
        this.mDisplayedValues = data;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_list_adapter, viewGroup, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int i) {

        try {

            FaceData faceData = mDisplayedValues.get(i);

            holder.name.setText("Name : " + faceData.getName());
            holder.memberId.setText("Member Id : " + faceData.getMemberID());
            holder.type.setText("Type : " + faceData.getType());
            System.out.println(">>. type >>>" + faceData.getType());
            LinkedList<FaceImgData> imageDataById = db.getImageDataById(faceData.getMemberID());
            for (FaceImgData faceImgData : imageDataById) {
                if (faceImgData.getIsSelected() == 1) {
                    holder.userImg.setImageBitmap(faceImgData.getImageData());
                }
            }
            holder.addFace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, FaceRegisterActivity.class);
                    intent.putExtra("USER-OBJ", faceData);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            holder.deleteMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FaceListActivity.deleteMember(faceData);
                }
            });

            holder.viewFaces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FaceImageListContainer.class);
                    intent.putExtra("USER-OBJ", faceData);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mDisplayedValues = (ArrayList<FaceData>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<FaceData> FilteredArrList = new ArrayList<FaceData>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<FaceData>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String dataString = mOriginalValues.get(i).getName();
                        String dataMemberId = mOriginalValues.get(i).getMemberID();
                        if (dataMemberId.toLowerCase().startsWith(constraint.toString()) || dataString.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(new FaceData(mOriginalValues.get(i).getId(), mOriginalValues.get(i).getName(), mOriginalValues.get(i).getMemberID(), mOriginalValues.get(i).getDistance(), mOriginalValues.get(i).getExtra(), mOriginalValues.get(i).getStartTime(), mOriginalValues.get(i).getEndTime(), mOriginalValues.get(i).getTimeFormat(), mOriginalValues.get(i).getUserImage(), mOriginalValues.get(i).getBranchno(), mOriginalValues.get(i).getType()));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

    @Override
    public int getItemCount() {
        return (null != mDisplayedValues ? mDisplayedValues.size() : 0);
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        ImageView userImg;
        TextView name, memberId, type;
        LinearLayout userLayout;
        Button addFace, viewFaces, deleteMember;

        RecyclerViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.name);
            memberId = view.findViewById(R.id.memberId);
            type = view.findViewById(R.id.type);
            userImg = view.findViewById(R.id.userImg);
            userLayout = view.findViewById(R.id.userLayout);
            addFace = view.findViewById(R.id.addFace);
            viewFaces = view.findViewById(R.id.viewFaces);
            deleteMember = view.findViewById(R.id.deleteMember);

        }

    }
}
