package com.fintrack.client.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.fintrack.client.R;

public class CustomCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_category);
        setToolbarTitle("Manage Categories");
    }

    @Override
    public void setToolbarTitle(String title) {
        // ...
    }
}
