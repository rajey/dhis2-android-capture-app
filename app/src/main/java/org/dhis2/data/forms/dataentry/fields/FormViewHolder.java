package org.dhis2.data.forms.dataentry.fields;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.customviews.CustomDialog;
import org.hisp.dhis.android.core.common.ObjectStyle;

import java.util.Objects;


public abstract class FormViewHolder extends RecyclerView.ViewHolder {

    protected ViewDataBinding binding;
    protected ImageView description;
    protected StringBuilder label;
    protected String descriptionText;
    protected MutableLiveData<String> currentUid;
    protected String fieldUid;
    protected ObjectStyle objectStyle;
    protected static String selectedFieldUid;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.description = binding.getRoot().findViewById(R.id.descriptionLabel);
        if (description != null) {
            description.setOnClickListener(v ->
                    new CustomDialog(
                            itemView.getContext(),
                            label.toString(),
                            descriptionText != null ? descriptionText : itemView.getContext().getString(R.string.empty_description),
                            itemView.getContext().getString(R.string.action_close),
                            null,
                            Constants.DESCRIPTION_DIALOG,
                            null
                    ).show());
        }
    }

    public void initFieldFocus() {
        if (currentUid != null) {
            currentUid.observeForever(fieldUid -> {
                if(fieldWasSelectedManually() || currentSelectedItem(fieldUid)){
                    Drawable bgDrawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg);
                    itemView.setBackground(bgDrawable);
                } else {
                    itemView.setBackgroundResource(R.color.form_field_background);
                }
            });

        }
    }

    private boolean fieldWasSelectedManually() {
        return selectedFieldUid != null && selectedFieldUid.equals(this.fieldUid);
    }

    private boolean currentSelectedItem(String fieldUid) {
        return selectedFieldUid == null && Objects.equals(fieldUid, this.fieldUid);
    }

    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void openKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        boolean shown = imm.showSoftInput(v.findFocus(), InputMethodManager.SHOW_FORCED);
       /* if (!imm.showSoftInput(v, 0))
            v.postDelayed(() -> openKeyboard(v), 500);*/
    }

    public void clearBackground(boolean isSarchMode) {
        if (!isSarchMode) {
            itemView.setBackgroundResource(R.color.form_field_background);
        }
    }

    public void setSelectedBackground(boolean isSarchMode) {
        if (!isSarchMode) {
            selectedFieldUid = fieldUid;
            currentUid.setValue(fieldUid);
        }
    }
}
