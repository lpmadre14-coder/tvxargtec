package com.tvxargtec.online.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.utils.ProfileManager;
import com.tvxargtec.online.utils.UserProfile;

import java.util.List;

public class ProfileSwitcherFragment extends Fragment {

    private ProfileManager profileManager;
    private RecyclerView profileGrid;
    private MaterialButton btnAddProfile;
    private ProfileAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_switcher, container, false);
        profileManager = new ProfileManager(requireContext());

        profileGrid = view.findViewById(R.id.profileGrid);
        btnAddProfile = view.findViewById(R.id.btnAddProfile);

        profileGrid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        loadProfiles();

        btnAddProfile.setOnClickListener(v -> showCreateProfileDialog());

        return view;
    }

    private void loadProfiles() {
        List<UserProfile> profiles = profileManager.getProfiles();
        if (adapter == null) {
            adapter = new ProfileAdapter(profiles);
            profileGrid.setAdapter(adapter);
        } else {
            adapter.updateProfiles(profiles);
        }
    }

    private void showCreateProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nuevo perfil");

        EditText input = new EditText(requireContext());
        input.setHint("Nombre del perfil");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(getResources().getColor(R.color.text_primary));
        input.setHintTextColor(getResources().getColor(R.color.text_hint));

        FrameLayout container = new FrameLayout(requireContext());
        container.setPadding(48, 16, 48, 16);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                profileManager.createProfile(name, "");
                loadProfiles();
                Toast.makeText(getContext(), "Perfil creado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Ingresa un nombre", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showPinDialog(UserProfile profile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Perfil protegido");
        builder.setMessage("Ingresa el PIN para acceder a \"" + profile.getName() + "\"");

        EditText input = new EditText(requireContext());
        input.setHint("PIN");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTextColor(getResources().getColor(R.color.text_primary));
        input.setHintTextColor(getResources().getColor(R.color.text_hint));

        FrameLayout container = new FrameLayout(requireContext());
        container.setPadding(48, 16, 48, 16);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            if (profileManager.verifyPin(profile.getId(), input.getText().toString())) {
                selectProfile(profile);
            } else {
                Toast.makeText(getContext(), "PIN incorrecto", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void selectProfile(UserProfile profile) {
        profileManager.setActiveProfile(profile.getId());
        Toast.makeText(getContext(), "Perfil: " + profile.getName(), Toast.LENGTH_SHORT).show();
        MainAty mainAty = MainAty.getInstance();
        if (mainAty != null) {
            mainAty.switchFragment(new ProfileFragment(), R.id.nav_profile);
        }
    }

    private class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

        private List<UserProfile> profiles;

        ProfileAdapter(List<UserProfile> profiles) {
            this.profiles = profiles;
        }

        void updateProfiles(List<UserProfile> newProfiles) {
            this.profiles = newProfiles;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserProfile profile = profiles.get(position);
            holder.bind(profile);
        }

        @Override
        public int getItemCount() {
            return profiles.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCardView card;
            private final ImageView ivAvatar;
            private final TextView tvName;
            private final ImageView ivLock;

            ViewHolder(View itemView) {
                super(itemView);
                card = (MaterialCardView) itemView;
                ivAvatar = itemView.findViewById(R.id.ivProfileAvatar);
                tvName = itemView.findViewById(R.id.tvProfileName);
                ivLock = itemView.findViewById(R.id.ivProfileLock);
            }

            void bind(UserProfile profile) {
                tvName.setText(profile.getName());
                ivLock.setVisibility(profile.isPinProtected() ? View.VISIBLE : View.GONE);

                if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                    try {
                        int resId = getResources().getIdentifier(
                            profile.getAvatar(), "drawable", requireContext().getPackageName());
                        if (resId != 0) {
                            ivAvatar.setImageResource(resId);
                        } else {
                            ivAvatar.setImageResource(R.drawable.ic_profile);
                        }
                    } catch (Exception e) {
                        ivAvatar.setImageResource(R.drawable.ic_profile);
                    }
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }

                card.setOnClickListener(v -> {
                    if (profile.isPinProtected()) {
                        showPinDialog(profile);
                    } else {
                        selectProfile(profile);
                    }
                });

                card.setOnLongClickListener(v -> {
                    showDeleteDialog(profile);
                    return true;
                });
            }
        }
    }

    private void showDeleteDialog(UserProfile profile) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Eliminar perfil")
            .setMessage("¿Eliminar \"" + profile.getName() + "\"?")
            .setPositiveButton("Eliminar", (d, w) -> {
                profileManager.deleteProfile(profile.getId());
                loadProfiles();
                Toast.makeText(getContext(), "Perfil eliminado", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
