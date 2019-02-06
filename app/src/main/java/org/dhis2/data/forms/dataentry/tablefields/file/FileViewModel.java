package org.dhis2.data.forms.dataentry.tablefields.file;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

@AutoValue
public abstract class FileViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, String description,
                                        String dataElement, List<String> listCategoryOption, String storeBy, int row, int column) {
        return new AutoValue_FileViewModel(id, label, mandatory, value, section, null,
                true, null, null, null,description, dataElement, listCategoryOption, storeBy, row, column);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_FileViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description(),dataElement(),listCategoryOption(), storeBy(), row(), column());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_FileViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error,description(),dataElement(),listCategoryOption(), storeBy(), row(), column());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_FileViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(),description(),dataElement(),listCategoryOption(), storeBy(), row(), column());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_FileViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description(),dataElement(),listCategoryOption(), storeBy(), row(), column());
    }
}
