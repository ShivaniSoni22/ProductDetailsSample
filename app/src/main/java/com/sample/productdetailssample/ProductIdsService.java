package com.sample.productdetailssample;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ProductIdsService {

    @GET
    Call<ArrayList<String>> getProductIdsResponse(@Url String emptyString);
}