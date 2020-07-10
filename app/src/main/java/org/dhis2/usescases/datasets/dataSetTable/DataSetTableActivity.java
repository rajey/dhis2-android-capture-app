package org.dhis2.usescases.datasets.dataSetTable;

import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jakewharton.rxbinding2.view.RxView;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetTableBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.customviews.AlertBottomDialog;
import org.dhis2.utils.validationrules.ValidationResultViolationsAdapter;
import org.dhis2.utils.validationrules.Violation;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.period.Period;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Observable;
import kotlin.Unit;
import timber.log.Timber;

import static org.dhis2.utils.Constants.NO_SECTION;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

public class DataSetTableActivity extends ActivityGlobalAbstract implements DataSetTableContract.View {

    String orgUnitUid;
    String orgUnitName;
    String periodTypeName;
    String periodInitialDate;
    String catOptCombo;
    String dataSetUid;
    String periodId;

    boolean accessDataWrite;
    private List<String> sections;

    @Inject
    DataSetTableContract.Presenter presenter;
    private ActivityDatasetTableBinding binding;
    private DataSetSectionAdapter viewPagerAdapter;
    private boolean backPressed;
    private DataSetTableComponent dataSetTableComponent;

    private BottomSheetBehavior<View> behavior;
    private boolean isComplete;

    public static Bundle getBundle(@NonNull String dataSetUid,
                                   @NonNull String orgUnitUid,
                                   @NonNull String orgUnitName,
                                   @NonNull String periodTypeName,
                                   @NonNull String periodInitialDate,
                                   @NonNull String periodId,
                                   @NonNull String catOptCombo) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putString(Constants.ORG_UNIT, orgUnitUid);
        bundle.putString(Constants.ORG_UNIT_NAME, orgUnitName);
        bundle.putString(Constants.PERIOD_TYPE, periodTypeName);
        bundle.putString(Constants.PERIOD_TYPE_DATE, periodInitialDate);
        bundle.putString(Constants.PERIOD_ID, periodId);
        bundle.putString(Constants.CAT_COMB, catOptCombo);
        return bundle;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        orgUnitUid = getIntent().getStringExtra(Constants.ORG_UNIT);
        orgUnitName = getIntent().getStringExtra(Constants.ORG_UNIT_NAME);
        periodTypeName = getIntent().getStringExtra(Constants.PERIOD_TYPE);
        periodId = getIntent().getStringExtra(Constants.PERIOD_ID);
        periodInitialDate = getIntent().getStringExtra(Constants.PERIOD_TYPE_DATE);
        catOptCombo = getIntent().getStringExtra(Constants.CAT_COMB);
        dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        accessDataWrite = getIntent().getBooleanExtra(Constants.ACCESS_DATA, true);

        dataSetTableComponent = ((App) getApplicationContext()).userComponent()
                .plus(new DataSetTableModule(this,
                        dataSetUid,
                        periodId,
                        orgUnitUid,
                        catOptCombo
                ));
        dataSetTableComponent.inject(this);
        super.onCreate(savedInstanceState);

        //Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table);
        binding.setPresenter(presenter);
        binding.BSLayout.bottomSheetLayout.setVisibility(View.GONE);
        setViewPager();
        observeSaveButtonClicks();
        presenter.init(orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewPagerAdapter.notifyDataSetChanged();
    }

    private void setViewPager() {
        viewPagerAdapter = new DataSetSectionAdapter(this, accessDataWrite, getIntent().getStringExtra(Constants.DATA_SET_UID));
        binding.viewPager.setUserInputEnabled(false);
        binding.viewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.dataset_overview);
            } else {
                tab.setText(viewPagerAdapter.getSectionTitle(position));
            }
        }).attach();
    }

    @Override
    public void setSections(List<String> sections) {
        this.sections = sections;
        if (sections.contains(NO_SECTION) && sections.size() > 1) {
            sections.remove(NO_SECTION);
            sections.add(getString(R.string.tab_tables));
        }
        viewPagerAdapter.swapData(sections);
        binding.viewPager.setCurrentItem(1);
    }

    public void updateTabLayout(String section, int numTables) {
        if (sections.get(0).equals(NO_SECTION)) {
            sections.remove(NO_SECTION);
            sections.add(getString(R.string.tab_tables));
            viewPagerAdapter.swapData(sections);
            binding.viewPager.setCurrentItem(1);
        }
    }

    public DataSetTableContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Boolean accessDataWrite() {
        return accessDataWrite;
    }

    @Override
    public String getDataSetUid() {
        return dataSetUid;
    }

    @Override
    public String getOrgUnitName() {
        return orgUnitName;
    }

    @Override
    public void renderDetails(DataSet dataSet, String catComboName, Period period, boolean isComplete) {
        this.isComplete = isComplete;
        binding.dataSetName.setText(dataSet.displayName());
        StringBuilder subtitle = new StringBuilder(
                DateUtils.getInstance().getPeriodUIString(period.periodType(), period.startDate(), Locale.getDefault())
        )
                .append(" | ")
                .append(orgUnitName);
        if (!catComboName.equals("default")) {
            subtitle.append(" | ")
                    .append(catComboName);
        }
        binding.dataSetSubtitle.setText(subtitle);
    }

    public void update() {
        presenter.init(orgUnitUid, periodTypeName, catOptCombo, periodInitialDate, periodId);
    }

    @Override
    public void back() {
        if (getCurrentFocus() == null || backPressed)
            super.back();
        else {
            backPressed = true;
            binding.getRoot().requestFocus();
            back();
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public boolean isBackPressed() {
        return backPressed;
    }

    public DataSetTableComponent getDataSetTableComponent() {
        return dataSetTableComponent;
    }

    @Override
    public Observable<Object> observeSaveButtonClicks() {
        return RxView.clicks(binding.saveButton);
    }

    @Override
    public void showMandatoryMessage(boolean isMandatoryFields) {
        String message;
        if (isMandatoryFields) {
            message = getString(R.string.field_mandatory_v2);
        } else {
            message = getString(R.string.field_required);
        }
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.saved))
                .setMessage(message)
                .setPositiveButton(getString(R.string.button_ok), () -> Unit.INSTANCE)
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void showValidationRuleDialog() {
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.saved))
                .setMessage(getString(R.string.run_validation_rules))
                .setPositiveButton(getString(R.string.yes), () -> {
                    presenter.executeValidationRules();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.no), () -> {
                    showSuccessValidationDialog();
                    return Unit.INSTANCE;
                })
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void showSuccessValidationDialog() {
        AlertBottomDialog.Companion.getInstance()
                .setTitle(getString(R.string.validation_success_title))
                .setMessage(getString(R.string.mark_dataset_complete))
                .setPositiveButton(getString(R.string.yes), () -> {
                    presenter.completeDataSet();
                    return Unit.INSTANCE;
                })
                .setNegativeButton(getString(R.string.no), () -> {
                    finish();
                    return Unit.INSTANCE;
                })
                .show(getSupportFragmentManager(), AlertBottomDialog.class.getSimpleName());
    }

    @Override
    public void savedAndCompleteMessage() {
        Snackbar.make(binding.getRoot(), R.string.dataset_saved_completed, BaseTransientBottomBar.LENGTH_SHORT).show();
        finish();
    }


    @Override
    public void showErrorsValidationDialog(List<Violation> violations) {
        configureShapeDrawable();

        binding.BSLayout.bottomSheetLayout.setVisibility(View.VISIBLE);
        binding.saveButton.animate()
                .translationY(-ExtensionsKt.getDp(48))
                .start();
        binding.BSLayout.setErrorCount(violations.size());
        binding.BSLayout.title.setText(
                getResources().getQuantityText(R.plurals.error_message, violations.size())
        );
        binding.BSLayout.violationsViewPager.setAdapter(new ValidationResultViolationsAdapter(this, violations));
        binding.BSLayout.dotsIndicator.setViewPager(binding.BSLayout.violationsViewPager);

        behavior = BottomSheetBehavior.from(binding.BSLayout.bottomSheetLayout);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        animateArrowDown();
                        binding.saveButton.animate()
                                .translationY(0)
                                .start();
                        binding.saveButton.hide();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        animateArrowUp();
                        binding.saveButton.show();
                        binding.saveButton.animate()
                                .translationY(-ExtensionsKt.getDp(48))
                                .start();
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_SETTLING:
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                /*UnUsed*/
            }

            private void animateArrowDown() {
                binding.BSLayout.collapseExpand.animate()
                        .scaleY(-1f).setDuration(200)
                        .start();
            }

            private void animateArrowUp() {
                binding.BSLayout.collapseExpand.animate()
                        .scaleY(1f).setDuration(200)
                        .start();
            }
        });
    }

    @Override
    public void showCompleteToast() {
        Snackbar.make(binding.viewPager, R.string.dataset_completed, BaseTransientBottomBar.LENGTH_SHORT)
                .show();
        finish();
    }

    @Override
    public void closeExpandBottom() {
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void cancelBottomSheet() {
        binding.BSLayout.bottomSheetLayout.setVisibility(View.GONE);
        binding.saveButton.show();
    }

    @Override
    public void completeBottomSheet() {
        cancelBottomSheet();
        presenter.completeDataSet();
    }

    private void configureShapeDrawable() {
        int cornerSize = getResources().getDimensionPixelSize(R.dimen.rounded_16);
        ShapeAppearanceModel appearanceModel = new ShapeAppearanceModel().toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cornerSize)
                .setTopRightCorner(CornerFamily.ROUNDED, cornerSize)
                .build();

        int elevation = getResources().getDimensionPixelSize(R.dimen.elevation);
        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(appearanceModel);
        int color = ResourcesCompat.getColor(getResources(), R.color.white, null);
        shapeDrawable.setFillColor(ColorStateList.valueOf(color));

        binding.BSLayout.bottomSheetLayout.setBackground(shapeDrawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.BSLayout.bottomSheetLayout.setElevation(elevation);
        } else {
            ViewCompat.setElevation(binding.BSLayout.bottomSheetLayout, elevation);
        }
    }

    public void showMoreOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        int menu = R.menu.dataset_menu;
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());

        popupMenu.getMenu().findItem(R.id.reopen).setVisible(isComplete);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.showHelp) {
                analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                showTutorial(true);
            } else if (itemId == R.id.reopen) {
                presenter.reopenDataSet();
            }
            return true;

        });
        popupMenu.show();
    }
}
