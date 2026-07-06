package com.tvxargtec.online.fragment;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tvxargtec.online.R;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;
import com.tvxargtec.online.utils.CustomCategory;
import com.tvxargtec.online.utils.CustomCategoryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomCategoriesFragment extends Fragment {

    private CustomCategoryManager categoryManager;
    private RecyclerView rvCategories;
    private CategoryAdapter adapter;

    private final int[] PRESET_COLORS = new int[]{
            0xFF7C3AED, 0xFFEF0044, 0xFF00E5FF, 0xFF00FF66, 0xFFFFBB00
    };
    private final String[] PRESET_COLOR_NAMES = new String[]{
            "Violeta", "Carmesí", "Cian", "Verde", "Ámbar"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        categoryManager = new CustomCategoryManager(requireContext());

        rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CategoryAdapter();
        rvCategories.setAdapter(adapter);

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddCategory);
        fabAdd.setOnClickListener(v -> showCreateCategoryDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void showCreateCategoryDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 16, 40, 16);

        EditText input = new EditText(requireContext());
        input.setHint("Nombre de la categoría");
        layout.addView(input);

        TextView colorLabel = new TextView(requireContext());
        colorLabel.setText("Color:");
        colorLabel.setTextColor(getResources().getColor(R.color.text_secondary));
        colorLabel.setTextSize(13f);
        colorLabel.setPadding(0, 24, 0, 8);
        layout.addView(colorLabel);

        RadioGroup colorGroup = new RadioGroup(requireContext());
        colorGroup.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(PRESET_COLOR_NAMES[i]);
            rb.setTextColor(getResources().getColor(R.color.text_primary));
            rb.setId(i);
            if (i == 0) rb.setChecked(true);
            colorGroup.addView(rb);
        }
        layout.addView(colorGroup);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nueva categoría")
                .setView(layout)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getActivity(), "Ingresa un nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int checkedId = colorGroup.getCheckedRadioButtonId();
                    int color = (checkedId >= 0 && checkedId < PRESET_COLORS.length)
                            ? PRESET_COLORS[checkedId] : PRESET_COLORS[0];
                    categoryManager.createCategory(name, color);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showCategoryOptions(CustomCategory category) {
        String[] options = {"Renombrar", "Eliminar", "Ver canales"};
        new AlertDialog.Builder(requireContext())
                .setTitle(category.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showRenameDialog(category);
                            break;
                        case 1:
                            showDeleteConfirmation(category);
                            break;
                        case 2:
                            showManageChannelsDialog(category);
                            break;
                    }
                })
                .show();
    }

    private void showRenameDialog(CustomCategory category) {
        EditText input = new EditText(requireContext());
        input.setText(category.getName());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle("Renombrar categoría")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        categoryManager.renameCategory(category.getId(), newName);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDeleteConfirmation(CustomCategory category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar categoría")
                .setMessage("¿Eliminar \"" + category.getName() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    categoryManager.deleteCategory(category.getId());
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showManageChannelsDialog(CustomCategory category) {
        List<Channel> allChannels = ChannelDataManager.getChannels(requireContext());
        Set<String> assignedIds = category.getChannelIds();

        boolean[] checked = new boolean[allChannels.size()];
        for (int i = 0; i < allChannels.size(); i++) {
            checked[i] = assignedIds.contains(allChannels.get(i).getId());
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 16, 40, 16);

        EditText searchInput = new EditText(requireContext());
        searchInput.setHint("Buscar canal...");
        layout.addView(searchInput);

        LinearLayout checkboxContainer = new LinearLayout(requireContext());
        checkboxContainer.setOrientation(LinearLayout.VERTICAL);
        checkboxContainer.setPadding(0, 8, 0, 0);

        List<CheckBox> checkBoxes = new ArrayList<>();
        for (int i = 0; i < allChannels.size(); i++) {
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(allChannels.get(i).getTitle());
            cb.setTextColor(getResources().getColor(R.color.text_primary));
            cb.setChecked(checked[i]);
            final int idx = i;
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> checked[idx] = isChecked);
            checkBoxes.add(cb);
            checkboxContainer.addView(cb);
        }

        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    Channel ch = allChannels.get(i);
                    boolean matches = ch.getTitle() != null && ch.getTitle().toLowerCase().contains(query);
                    checkBoxes.get(i).setVisibility(matches ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        android.widget.ScrollView scrollView = new android.widget.ScrollView(requireContext());
        scrollView.addView(checkboxContainer);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 400));
        layout.addView(scrollView);

        new AlertDialog.Builder(requireContext())
                .setTitle("Canales en \"" + category.getName() + "\"")
                .setView(layout)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    for (int i = 0; i < allChannels.size(); i++) {
                        String channelId = allChannels.get(i).getId();
                        if (checked[i]) {
                            categoryManager.addChannelToCategory(category.getId(), channelId);
                        } else {
                            categoryManager.removeChannelFromCategory(category.getId(), channelId);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_custom_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CustomCategory category = categoryManager.getCategories().get(position);
            holder.bind(category);
        }

        @Override
        public int getItemCount() {
            return categoryManager.getCategories().size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View colorIndicator;
            TextView tvName;
            TextView tvCount;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                colorIndicator = itemView.findViewById(R.id.viewColorIndicator);
                tvName = itemView.findViewById(R.id.tvCategoryName);
                tvCount = itemView.findViewById(R.id.tvChannelCount);

                itemView.setOnLongClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos >= 0 && pos < categoryManager.getCategories().size()) {
                        showCategoryOptions(categoryManager.getCategories().get(pos));
                        return true;
                    }
                    return false;
                });
            }

            void bind(CustomCategory category) {
                tvName.setText(category.getName());
                int count = category.getChannelIds().size();
                tvCount.setText(count + " canal" + (count != 1 ? "es" : ""));

                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(8f);
                drawable.setColor(category.getColor());
                colorIndicator.setBackground(drawable);
            }
        }
    }
}
