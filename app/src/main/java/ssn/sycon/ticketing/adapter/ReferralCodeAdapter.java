package ssn.sycon.ticketing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

import ssn.sycon.ticketing.R;
import ssn.sycon.ticketing.model.ReferralData;

public class ReferralCodeAdapter extends RecyclerView.Adapter<ReferralCodeAdapter.ReferralCodeHolder> {
    ArrayList<ReferralData> referralDataArrayList;
    ArrayList<String> top;
    Context context;

    public ArrayList<String> getTop() {
        return top;
    }

    public void setTop(ArrayList<String> top) {
        this.top = top;
    }

    public ReferralCodeAdapter(ArrayList<ReferralData> referralDataArrayList, Context context) {
        this.referralDataArrayList = referralDataArrayList;
        this.context = context;
    }

    public ArrayList<ReferralData> getReferralDataArrayList() {
        return referralDataArrayList;
    }

    public void setReferralDataArrayList(ArrayList<ReferralData> referralDataArrayList) {
        this.referralDataArrayList = referralDataArrayList;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ReferralCodeAdapter.ReferralCodeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.referral_single_item,parent,false);
        ReferralCodeHolder referralCodeHolder = new ReferralCodeHolder(v);
        return referralCodeHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReferralCodeAdapter.ReferralCodeHolder holder, int position) {
        final ReferralData referralData = referralDataArrayList.get(position);
        holder.nameTV.setText(referralData.getName());
        holder.codeTV.setText("#"+referralData.getCode().toString());
        holder.numberTV.setText(referralData.getReferred().toString());
        if(referralData.getCode().toString().equals(top.get(0))){
            holder.medal1IV.setVisibility(View.VISIBLE);
            holder.medal2IV.setVisibility(View.GONE);
            holder.medal3IV.setVisibility(View.GONE);
        }else{
            if(referralData.getCode().toString().equals(top.get(1))){
                holder.medal1IV.setVisibility(View.GONE);
                holder.medal2IV.setVisibility(View.VISIBLE);
                holder.medal3IV.setVisibility(View.GONE);
            }else{
                if(referralData.getCode().toString().equals(top.get(2))){
                    holder.medal1IV.setVisibility(View.GONE);
                    holder.medal2IV.setVisibility(View.GONE);
                    holder.medal3IV.setVisibility(View.VISIBLE);
                }else{
                    holder.medal1IV.setVisibility(View.GONE);
                    holder.medal2IV.setVisibility(View.GONE);
                    holder.medal3IV.setVisibility(View.GONE);
                }
            }
        }


    }

    @Override
    public int getItemCount() {
        return referralDataArrayList.size();
    }

    public class ReferralCodeHolder extends RecyclerView.ViewHolder{
        TextView nameTV;
        TextView codeTV;
        TextView numberTV;
        ImageView medal1IV;
        ImageView medal2IV;
        ImageView medal3IV;

        public ReferralCodeHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            codeTV = itemView.findViewById(R.id.codeTV);
            numberTV = itemView.findViewById(R.id.numberTV);
            medal1IV = itemView.findViewById(R.id.medal1IV);
            medal2IV = itemView.findViewById(R.id.medal2IV);
            medal3IV = itemView.findViewById(R.id.medal3IV);
        }
    }
}
