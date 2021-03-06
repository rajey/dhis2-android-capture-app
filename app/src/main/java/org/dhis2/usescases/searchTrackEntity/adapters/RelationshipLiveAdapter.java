package org.dhis2.usescases.searchTrackEntity.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.customviews.ImageDetailBottomDialog;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import java.io.File;

import kotlin.Unit;

public class RelationshipLiveAdapter extends PagedListAdapter<SearchTeiModel, SearchRelationshipViewHolder> {

    private static final DiffUtil.ItemCallback<SearchTeiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<SearchTeiModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull SearchTeiModel oldItem, @NonNull SearchTeiModel newItem) {
            return oldItem.getTei().uid().equals(newItem.getTei().uid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SearchTeiModel oldItem, @NonNull SearchTeiModel newItem) {
            return oldItem.getTei().uid().equals(newItem.getTei().uid());
        }
    };

    private SearchTEContractsModule.Presenter presenter;
    private final FragmentManager fm;

    public RelationshipLiveAdapter(SearchTEContractsModule.Presenter presenter, FragmentManager fm) {
        super(DIFF_CALLBACK);
        this.presenter = presenter;
        this.fm = fm;
    }

    @NonNull
    @Override
    public SearchRelationshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false);
        return new SearchRelationshipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRelationshipViewHolder holder, int position) {
        holder.bind(
                presenter,
                getItem(position),
                () -> {
                    getItem(holder.getAdapterPosition()).toggleAttributeList();
                    notifyItemChanged(holder.getAdapterPosition());
                    return Unit.INSTANCE;
                },
                path -> {
                    new ImageDetailBottomDialog(null, new File(path))
                            .show(fm, ImageDetailBottomDialog.TAG);
                    return Unit.INSTANCE;
                });
    }
}
