// ApiService.java
package com.fintrack.client.network;

import com.fintrack.client.dto.*;
import com.fintrack.client.models.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("api/v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @PUT("api/v1/expenses/monthly/{id}")
    Call<Void> updateExpense(@Path("id") String expenseId, @Body UpdateExpenseRequest request);


    @POST("api/v1/dashboard/save")
    Call<DashboardResponse> saveDashboard(@Body DashboardRequest dashboardRequest);

    @POST("api/v1/auth/login")
    Call<AuthResponse> loginUser(@Body AuthRequest authRequest);

    @GET("api/v1/dashboard")
    Call<DashboardResponse> getDashboard(@Query("emailId") String emailId, @Query("year") int year, @Query("month") int month);

    @POST("api/v1/profile/setup")
    Call<GenericResponse> setupProfile( @Body ProfileSetupRequest request);

    @POST("api/v1/incomes/extra")
    Call<GenericResponse> addExtraIncome(@Body ExtraIncome request);

    @POST("api/v1/expenses/monthly")
    Call<MonthlyExpense> addMonthlyExpense(@Body AddMonthlyExpenseRequest request);

    @POST("api/v1/user/change-password")
    Call<GenericResponse> changePassword(@Body ChangePasswordRequest request);

    // New Endpoints for Spend Page
    @GET("api/v1/spends/data")
    Call<SpendDataResponse> getSpendData(@Query("userId") String userId);

    @POST("api/v1/expenses/fixed")
    Call<GenericResponse> addFixedExpense(@Body AddFixedExpenditureRequest request);

    @POST("api/v1/cards/add")
    Call<GenericResponse> addCreditCard(@Body AddCreditCardRequest request);
}

