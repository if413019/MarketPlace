package com.example.gabbygiordano.marketplace;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.example.gabbygiordano.marketplace.R.layout.notification;

/**
 * Created by tanvigupta on 7/20/17.
 */

public class AppNotificationAdapter extends RecyclerView.Adapter<AppNotificationAdapter.ViewHolder> {

    private List<AppNotification> mAppNotifications;

    Context context;
    static Context mContext;

    // pass Items array into constructor
    public AppNotificationAdapter(List<AppNotification> appNotifications, Context context) {
        mAppNotifications = appNotifications;
        mContext = context;
    }

    // inflate layout and cache references into ViewHolder for each row
    // only called when entirely new row is to be created
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemView = inflater.inflate(notification, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);

        return viewHolder;
    }

    // bind values based on position of the element
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // get the data according to position
        final AppNotification appNotification = mAppNotifications.get(position);

        // populate the views according to item data
        holder.tvBuyerName.setText(appNotification.getBuyer().getString("name"));
        holder.tvInterestItem.setText(appNotification.getItem().getString("item_name"));

        holder.ibEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");

                String buyerEmail = appNotification.getBuyer().getString("publicEmail");
                String itemName = appNotification.getItem().getItemName();

                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {buyerEmail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "MarketPlace request for " + itemName);
                intent.putExtra(Intent.EXTRA_TEXT, "");
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(Intent.createChooser(intent, ""));
                }
            }
        });

        holder.ibMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buyerPhone = appNotification.getBuyer().get("phone").toString();

                Uri smsUri = Uri.parse("tel:" + buyerPhone);
                Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
                intent.putExtra("address", buyerPhone);
                intent.putExtra("sms_body", "");
                intent.setType("vnd.android-dir/mms-sms");
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAppNotifications.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        mAppNotifications.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<AppNotification> list) {
        mAppNotifications.addAll(list);
        notifyDataSetChanged();
    }

    public void add(AppNotification appNotification) {
        mAppNotifications.add(appNotification);
        notifyItemInserted(mAppNotifications.size() - 1);
    }

    public static Context getContext() {
        return mContext;
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView ivProfileImage;
        public TextView tvBuyerName;
        public TextView tvInterested;
        public TextView tvInterestItem;
        public ImageButton ibEmail;
        public ImageButton ibMessage;

        // constructor
        public ViewHolder(View itemView) {
            super(itemView);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
            tvInterested = itemView.findViewById(R.id.tvInterested);
            tvInterestItem = itemView.findViewById(R.id.tvInterestItem);
            ibEmail = itemView.findViewById(R.id.ibEmail);
            ibMessage = itemView.findViewById(R.id.ibMessage);
        }
    }
}

