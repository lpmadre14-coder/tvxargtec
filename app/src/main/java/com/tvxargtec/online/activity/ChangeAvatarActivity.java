package com.tvxargtec.online.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.tvxargtec.online.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Activity para cambiar el avatar del usuario
 */
public class ChangeAvatarActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final String PREF_NAME = "user_data";

    private ImageView ivPreview;
    private Button btnTakePhoto, btnChooseFromGallery, btnSaveAvatar, btnCancel;
    private Uri selectedImageUri;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_avatar);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        initViews();
        loadCurrentAvatar();
        setupListeners();
    }

    private void initViews() {
        ivPreview = findViewById(R.id.ivPreview);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChooseFromGallery = findViewById(R.id.btnChooseFromGallery);
        btnSaveAvatar = findViewById(R.id.btnSaveAvatar);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadCurrentAvatar() {
        String avatarUrl = sharedPreferences.getString("user_avatar", "");
        if (!avatarUrl.isEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_account)
                .error(R.drawable.ic_account)
                .circleCrop()
                .into(ivPreview);
        }
    }

    private void setupListeners() {
        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnChooseFromGallery.setOnClickListener(v -> chooseFromGallery());
        btnSaveAvatar.setOnClickListener(v -> saveAvatar());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
        } else {
            Toast.makeText(this, "No hay galería disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Foto capturada
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                selectedImageUri = saveBitmapToFile(imageBitmap);
                displayImage(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Imagen seleccionada de galería
                selectedImageUri = data.getData();
                displayImage(selectedImageUri);
            }
        }
    }

    private void displayImage(Object imageData) {
        if (imageData instanceof Bitmap) {
            Glide.with(this)
                .load((Bitmap) imageData)
                .circleCrop()
                .into(ivPreview);
        } else if (imageData instanceof Uri) {
            Glide.with(this)
                .load((Uri) imageData)
                .circleCrop()
                .into(ivPreview);
        }
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveAvatar() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Por favor selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Conectar con API para subir avatar
        // Guardar localmente por ahora
        String avatarPath = selectedImageUri.toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_avatar", avatarPath);
        editor.apply();

        Toast.makeText(this, "Avatar actualizado", Toast.LENGTH_SHORT).show();
        finish();
    }
}
