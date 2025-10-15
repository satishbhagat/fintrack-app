package com.fintrack.client.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.fintrack.client.R;

public class MFASetupActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mfasetup);
        setToolbarTitle("Set Up MFA");
    }

    @Override
    public void setToolbarTitle(String title) {
        // ...
    }
}
