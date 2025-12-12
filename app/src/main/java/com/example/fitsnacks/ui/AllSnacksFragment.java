package com.example.fitsnacks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitsnacks.R;
import com.example.fitsnacks.viewmodel.DashboardViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AllSnacksFragment extends Fragment {
    private DashboardViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_snacks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        RecyclerView list = view.findViewById(R.id.all_snacks_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        final SnackListAdapter adapter = new SnackListAdapter(snack -> {
            if (snack == null) return;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(getString(R.string.dialog_delete_message))
                    .setPositiveButton(R.string.dialog_delete_confirm, (d, w) -> vm.deleteSnack(snack.id))
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        });
        list.setAdapter(adapter);

        vm.allSnacks.observe(getViewLifecycleOwner(), adapter::setItems);
    }
}
