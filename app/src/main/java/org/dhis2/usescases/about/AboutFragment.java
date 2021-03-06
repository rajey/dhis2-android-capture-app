package org.dhis2.usescases.about;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.BuildConfig;
import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.databinding.FragmentAboutBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.extension.ActivityExtensionKt;
import org.hisp.dhis.android.core.user.UserCredentials;

import javax.inject.Inject;

import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.ABOUT_FRAGMENT;

public class AboutFragment extends FragmentGlobalAbstract implements AboutContracts.AboutView {

    @Inject
    AboutContracts.AboutPresenter presenter;

    private FragmentAboutBinding aboutBinding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new AboutModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        aboutBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false);
        aboutBinding.setPresenter(presenter);

        aboutBinding.aboutMore.setMovementMethod(LinkMovementMethod.getInstance());
        aboutBinding.aboutGit.setMovementMethod(LinkMovementMethod.getInstance());
        aboutBinding.aboutDev.setMovementMethod(LinkMovementMethod.getInstance());
        aboutBinding.aboutContact.setMovementMethod(LinkMovementMethod.getInstance());
        setAppVersion();
        setSDKVersion();
        setPrivacyPolicy();
        return aboutBinding.getRoot();
    }

    private void setPrivacyPolicy() {
        aboutBinding.privacyPolicy.setOnClickListener(v -> {
            navigateToPrivacyPolicy();
        });
    }


    private void setAppVersion() {
        try {
            String versionName = getAbstractActivity()
                    .getPackageManager()
                    .getPackageInfo(getAbstractActivity().getPackageName(), 0)
                    .versionName;

            String text = String.format(getString(R.string.about_app), versionName);
            aboutBinding.aboutApp.setText(text);

        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e);
        }
    }

    private void setSDKVersion() {
        String text = String.format(getString(R.string.about_sdk), BuildConfig.SDK_VERSION);
        aboutBinding.appSDK.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    public void renderUserCredentials(UserCredentials userCredentialsModel) {
        String text = String.format(getString(R.string.about_user), userCredentialsModel.username());
        aboutBinding.aboutUser.setText(text);
    }

    @Override
    public void renderServerUrl(String serverUrl) {
        String text = String.format(getString(R.string.about_connected), serverUrl);
        aboutBinding.aboutConnected.setText(text);
    }

    @Override
    public String checkCredentials() {
        return aboutBinding.aboutUser.getText().toString();
    }

    @Override
    public String checkUrl() {
        return aboutBinding.aboutConnected.getText().toString();
    }

    @Override
    public void navigateToPrivacyPolicy() {
        Activity currentActivity = getActivity();
        if (currentActivity != null){
            Intent intent = new Intent(currentActivity, PolicyView.class);
            startActivity(intent);
        }
    }
}
