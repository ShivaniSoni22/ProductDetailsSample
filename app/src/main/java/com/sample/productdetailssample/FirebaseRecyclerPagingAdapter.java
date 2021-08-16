package com.sample.productdetailssample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRecyclerPagingAdapter extends RecyclerView.Adapter<FirebaseRecyclerPagingAdapter.MyViewHolder> {

    List<Product> productList;
    Context context;

    public FirebaseRecyclerPagingAdapter(Context context) {
        this.productList = new ArrayList<>();
        this.context = context;
    }

    public void addAll(List<Product> newProducts) {
        int initSize = productList.size();
        productList.addAll(newProducts);
        notifyItemRangeChanged(initSize, newProducts.size());

    }

    public String getLastItemId() {
        return productList.get(productList.size() - 1).getId();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_item_products, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txtProductName.setText(productList.get(position).getTitle());
        holder.txtProductDescription.setText(productList.get(position).getDescription());
        if(productList.get(position).getPrice().equals("-")) {
            holder.txtProductPrice.setText(productList.get(position).getPrice());
        }
        else
            holder.txtProductPrice.setText(String.format("₹ %s", productList.get(position).getPrice()));
        if(!productList.get(position).getImage_url().equals("-")){
            Glide.with(context).load(productList.get(position).getImage_url()).into(holder.imgProductPicture);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void removeLastItem() {
        productList.remove(productList.size() - 1);
    }

    public boolean isEmpty() {
        return productList.isEmpty();
    }

    public void clear() {
        productList.clear();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProductPicture;
        private final TextView txtProductName;
        private final TextView txtProductDescription;
        private final TextView txtProductPrice;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txt_product_name);
            imgProductPicture = itemView.findViewById(R.id.img_product_picture);
            txtProductDescription = itemView.findViewById(R.id.txt_product_description);
            txtProductPrice = itemView.findViewById(R.id.txt_product_price);
        }

    }

}