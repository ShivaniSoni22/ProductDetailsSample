package com.sample.productdetailssample;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    final int ITEM_LOAD_COUNT = 10;
    private int total_item = 0, last_visible_item;
    private boolean isLoading = false, isMaxData = false;
    private String last_node = "", last_key = "";
    private boolean isGoneInOnPause;
    private ArrayList<String> mProductsResponseList;
    private FirebaseDatabase mDatabase;
    private Boolean isResponseFetched = false;
    private List<Product> mNewProductList;
    private FirebaseRecyclerPagingAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mProductsResponseList = new ArrayList<>();
        mNewProductList = new ArrayList<>();
        getProductIdResponse();
        getLastKeyFromFirebase();

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter = new FirebaseRecyclerPagingAdapter(this);
        recyclerView.setAdapter(mAdapter);

        getProductName();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                total_item = layoutManager.getItemCount();
                last_visible_item = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && total_item <= ((last_visible_item + ITEM_LOAD_COUNT))) {
                    getProductName();
                    isLoading = true;
                }
            }
        });

//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                // check if the new child added is last node of fetched list or not
//                if (!last_node.equals(previousChildName) && mProductsResponseList.contains(previousChildName)) {
//                    // to check if new added child is in the list or not with the help of previous key
//                    for (Product product : mNewProductList) {
//                        if (product.getId().equals(previousChildName)) {
//                            //position of new child added
//                            int position = mNewProductList.indexOf(product) + 1;
//                            if (mNewProductList.size() > 0 && position > 0) {
//                                mNewProductList.add(position, new Product(snapshot.getKey(),
//                                        String.valueOf(snapshot.getValue())));
//                                mAdapter.removeLastItem();
//                                last_node = mAdapter.getLastItemId();
//                                mAdapter.notifyItemInserted(position);
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (!last_node.equals(previousChildName) && mProductsResponseList.contains(previousChildName)) {
//
//                }
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//            }
//        };

        refreshListOnDataChange();
    }

    void getProductIdResponse() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://35.154.26.203/product-ids/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProductIdsService service = retrofit.create(ProductIdsService.class);

        Call<ArrayList<String>> call = service.getProductIdsResponse("");

        call.enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<String>> call, @NotNull Response<ArrayList<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mProductsResponseList = response.body();
                    isResponseFetched = true;
                }
            }

            @Override
            public void onFailure(@NotNull Call<ArrayList<String>> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void refreshListOnDataChange() {
        Query q = mDatabase.getReference()
                .child("product-name");

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                refreshData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.getMessage();
            }
        });
    }

    private void refreshData() {
        if (mNewProductList.size() > 0) {
            isMaxData = false;
            last_node = mAdapter.getLastItemId();
            mAdapter.removeLastItem();
            mAdapter.notifyDataSetChanged();
            getLastKeyFromFirebase();
            getProductName();
        }
    }

    private void getProductName() {
        if (!isMaxData) {
            Query query;
            if (TextUtils.isEmpty(last_node))
                query = mDatabase.getReference()
                        .child("product-name")
                        .orderByKey()
                        .limitToFirst(ITEM_LOAD_COUNT);

            else
                query = mDatabase.getReference()
                        .child("product-name")
                        .orderByKey()
                        .startAt(last_node)
                        .limitToFirst(ITEM_LOAD_COUNT);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot productSnapShot : dataSnapshot.getChildren()) {
                            List<String> productDetail = new ArrayList<>();
                            productDetail.add(productSnapShot.getKey());
                            productDetail.add(String.valueOf(productSnapShot.getValue()));
                            if (isResponseFetched && mProductsResponseList.contains(productSnapShot.getKey()))
                                getProductDescription(productDetail);
                        }
                    } else {
                        isMaxData = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.getMessage();
                }
            });
        }
    }

    private void getProductDescription(List<String> productDetail) {
        if (!isMaxData) {
            new Thread(() -> {
                try {
                    DataSnapshot dataSnapshot = Tasks.await(mDatabase.getReference()
                            .child("product-description")
                            .child(productDetail.get(0))
                            .get());
                    if (dataSnapshot.exists()) {
                        productDetail.add(String.valueOf(dataSnapshot.getValue()));
                    } else
                        productDetail.add("-");
                    getProductImage(productDetail);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void getProductImage(List<String> productDetail) {
        if (!isMaxData) {
            new Thread(() -> {
                try {
                    DataSnapshot dataSnapshot = Tasks.await(mDatabase.getReference()
                            .child("product-image")
                            .child(productDetail.get(0))
                            .get());
                    if (dataSnapshot.exists()) {
                        productDetail.add(String.valueOf(dataSnapshot.getValue()));
                    } else
                        productDetail.add("-");
                    getProductPrice(productDetail);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void getProductPrice(List<String> productDetail) {
        if (!isMaxData) {
            new Thread(() -> {
                DataSnapshot dataSnapshot = null;
                try {
                    dataSnapshot = Tasks.await(
                            mDatabase.getReference()
                                    .child("product-price")
                                    .child(productDetail.get(0))
                                    .get());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    productDetail.add(String.valueOf(dataSnapshot.getValue()));
                } else
                    productDetail.add("-");
                String id = productDetail.get(0);
                String title = productDetail.get(1);
                String description = productDetail.get(2);
                String image_url = productDetail.get(3);
                String price = productDetail.get(4);

                mNewProductList.add(new Product(id, title, description, image_url, price));
                last_node = mNewProductList.get(mNewProductList.size() - 1).getId();
                if (last_node.equals(last_key)) {
                    last_node = "end";
                    isMaxData = true;
                }
                //to update UI
                runOnUiThread(() -> {
                    mAdapter.addAll(mNewProductList);
                    isLoading = false;
                });
            }).start();
        }
    }

    private void getLastKeyFromFirebase() {
        Query getLastKey = mDatabase.getReference()
                .child("product-name")
                .orderByKey()
                .limitToLast(1);

        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot lastKey : dataSnapshot.getChildren()) {
                    last_key = lastKey.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", "error, failed to get last key");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        isGoneInOnPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGoneInOnPause) {
            refreshData();
        }
    }

}