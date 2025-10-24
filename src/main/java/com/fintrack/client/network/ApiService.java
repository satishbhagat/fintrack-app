// ApiService.java
package com.fintrack.client.network;

import com.fintrack.client.dto.*;
import com.fintrack.client.models.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.UUID;

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

    @POST("api/v1/incomes/add")
    Call<IncomeResponse> addExtraIncome(@Body AddIncomeRequest request);

    @POST("api/v1/expenses/monthly")
    Call<MonthlyExpense> addMonthlyExpense(@Body AddMonthlyExpenseRequest request);

    @POST("api/v1/user/change-password")
    Call<GenericResponse> changePassword(@Body ChangePasswordRequest request);

    // New Endpoints for Spend Page
    @GET("api/v1/spends/data")
    Call<SpendDataResponse> getSpendData(@Query("userId") String userId);

    @POST("api/v1/expenses/fixed")
    Call<FixedExpenditure> addFixedExpense(@Body AddFixedExpenditureRequest request);

    @POST("api/v1/cards/add")
    Call<CreditCard> addCreditCard(@Body AddCreditCardRequest request);

    // MVP Feature Endpoints
    @POST("api/v1/accounts/link")
    Call<Void> linkAccount(@Body LinkAccountRequest request);

    @POST("api/v1/accounts/sync")
    Call<Void> syncAccounts(@Query("userId") UUID userId);

    @GET("api/v1/categories")
    Call<List<Category>> getCategories(@Query("userId") UUID userId);

    @POST("api/v1/categories")
    Call<Category> createCategory(@Body Category category);

    @GET("api/v1/goals")
    Call<List<SavingsGoal>> getSavingsGoals(@Query("userId") UUID userId);

    @POST("api/v1/goals")
    Call<SavingsGoal> createSavingsGoal(@Body SavingsGoal savingsGoal);

    @POST("api/v1/auth/mfa/setup")
    Call<MfaSetupResponse> setupMfa(@Query("username") String username);

    @POST("api/v1/auth/mfa/verify")
    Call<Void> verifyMfa(@Body VerifyMfaRequest verifyRequest);

    @PUT("api/v1/cards/{id}")
    Call<CreditCard> updateCreditCard(@Path("id") String id,@Body AddCreditCardRequest request);

    @DELETE("api/v1/cards/{id}")
    Call<Void> deleteCreditCard(@Path("id") String id);

    @PUT("api/v1/incomes/{id}")
    Call<IncomeResponse> updateExtraIncome(@Path("id") String id, @Body AddIncomeRequest request);

    @DELETE("api/v1/incomes/{id}")
    Call<Void> deleteExtraIncome(@Path("id") String id);

    @PUT("api/v1/expenses/fixed/{id}")
    Call<FixedExpenditure> updateFixedExpense(@Path("id") String id,@Body AddFixedExpenditureRequest request);

    @DELETE("api/v1/expenses/delete/{id}")
    Call<Void> deleteFixedExpense(@Path("id") String id);
}
