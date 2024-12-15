package com.app.src.abcqr.ui.adapter;

import static com.app.src.abcqr.utils.DateTimeConverters.fromTimestamp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.app.src.abcqr.R;
import com.app.src.abcqr.data.model.MyQR;

import java.util.List;


public class QRAdapter extends RecyclerView.Adapter<QRAdapter.MyQRViewHolder> {

    private Context context;
    private List<MyQR> myQRList;
    private OnItemClickListener listener;

    public QRAdapter(Context context, List<MyQR> cityClocks, OnItemClickListener listener) {
        this.context = context;
        this.myQRList = cityClocks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyQRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qr, parent, false);
        return new MyQRViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyQRViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MyQR myQR = myQRList.get(position);
        holder.type.setText(myQR.getType());
        String dContent = handleContent(myQR.getMessage());
        holder.displayContent.setText(dContent);
        holder.datetime.setText(fromTimestamp(myQR.getTime()).toString().substring(0, 19));
        holder.dButton.setOnClickListener(v -> {
            listener.onDButtonClick(position);
        });

        holder.pButton.setOnClickListener(v -> {
            listener.onPButtonClick(position);
        });
    }

    public interface OnItemClickListener {
        void onDButtonClick(int position);

        void onPButtonClick(int position);
    }

    @Override
    public int getItemCount() {
        return myQRList.size();
    }

    public static class MyQRViewHolder extends RecyclerView.ViewHolder {
        public String content;
        public TextView type;
        public TextView displayContent;
        public TextView datetime;
        public ImageButton dButton, pButton;

        public MyQRViewHolder(View view) {
            super(view);
            type = view.findViewById(R.id.qr_type);
            displayContent = view.findViewById(R.id.qr_content);
            datetime = view.findViewById(R.id.qr_datetime);
            dButton = view.findViewById(R.id.delete_qr_item);
            pButton = view.findViewById(R.id.perform_qr_item);
        }
    }

    public void update(List<MyQR> list) {
        myQRList = list;
        notifyDataSetChanged();
    }

    private String handleContent(String content) {
        if (content.startsWith("tel:")) {
            String phoneNumber = content.substring(4); // Remove "tel:"
            return "Call " + phoneNumber;
        } else if (content.startsWith("smsto:")) {
            String smsContent = content.substring(6); // Remove "smsto:"
            String[] parts = smsContent.split(":");
            String phoneNumber = parts[0];
            String message = parts.length > 1 ? parts[1] : "";
            return "SMS to " + phoneNumber + ", " + message;
        } else if (content.startsWith("mailto:")) {
            String mailContent = content.substring(7); // Remove "mailto:"
            String[] parts = mailContent.split("\\?|&");
            String recipient = parts[0];
            String subject = "";
            String body = "";

            for (int i = 1; i < parts.length; i++) {
                if (parts[i].startsWith("subject=")) {
                    subject = parts[i].substring(8);
                } else if (parts[i].startsWith("body=")) {
                    body = parts[i].substring(5);
                }
            }

            return "Mail to " + recipient + " - " + subject + " - " + body;
        } else if (content.startsWith("MECARD:")) {
            String mecardContent = content.substring(7);
            String[] fields = mecardContent.split(";");
            StringBuilder formatted = new StringBuilder("Phone ");

            for (String field : fields) {
                String[] pair = field.split(":");
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = pair[1];
                    switch (key) {
                        case "N":
                            formatted.append(value).append(" ");
                            break;
                        case "TEL":
                            formatted.append(value).append(" ");
                            break;
                        case "EMAIL":
                            formatted.append(value).append(" ");
                            break;
                    }
                }
            }

            return formatted.toString().trim();
        }
        return content;
    }
}