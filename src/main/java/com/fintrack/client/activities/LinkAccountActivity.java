package com.fintrack.client.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.fintrack.client.R;

public class LinkAccountActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_account);
        setToolbarTitle("Link New Account");
    }

    @Override
    public void setToolbarTitle(String title) {
        // Implement this method to set the toolbar title.
    }
}
