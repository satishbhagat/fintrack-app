// ApiService.java
package com.fintrack.client.network;

import com.fintrack.client.dto.*;
import com.fintrack.client.models.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("api/v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("api/v1/profile")
    Call<UserProfile> getProfile();

    @PUT("api/v1/profile")
    Call<GenericResponse> updateProfile(@Body UpdateProfileRequest request);

    @POST("api/v1/incomes/extra")
    Call<IncomeResponse> addExtraIncome(@Body AddIncomeRequest request);

    @POST("api/v1/expenses/fixed")
    Call<FixedExpenditureResponse> addFixedExpenditure(@Body AddFixedExpenditureRequest request);

    @GET("api/v1/expenses/monthly/{year}/{month}")
    Call<ExpensesResponse> getMonthlyExpenses(@Path("year") String year, @Path("month") String month);

    @POST("api/v1/expenses/monthly")
    Call<MonthlyExpense> addMonthlyExpense(@Body AddMonthlyExpenseRequest request);

    @PUT("api/v1/expenses/monthly/{id}")
    Call<GenericResponse> updateExpense(@Path("id") String expenseId, @Body UpdateExpenseRequest request);

    @POST("api/v1/cards")
    Call<CreditCard> addCreditCard(@Body AddCreditCardRequest request);

    @POST("api/v1/auth/login")
    Call<GenericResponse> loginUser(@Body AuthRequest authRequest);

    @GET("api/v1/dashboard")
    Call<DashboardResponse> getDashboard(@Query("emailId") String emailId);

    @POST("api/v1/profile/setup")
    Call<GenericResponse> setupProfile( ProfileSetupRequest request);

    @POST("api/v1/dashboard/save")
    Call<DashboardResponse> saveDashboard(DashboardRequest dashboardRequest);
}