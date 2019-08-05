package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {


    private DatabaseReference mDatabaseReference;
    private EditText etTitle;
    private EditText etPrice;
    private EditText etDescription;
    private FloatingActionButton mFab;
    private TravelDeal mDeal;
    private ImageView iv_dealImage;
    private static final int PICTURE_RESULT = 49;
    private boolean mStoragePermissions;
    private Button btnImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        //FirebaseUtils.openFbReference("traveldeals");
        //mFirebaseDatabase = FirebaseUtils.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtils.mDatabaseReference;
        etTitle = findViewById(R.id.ev_Title);
        etPrice = findViewById(R.id.ev_price);
        etDescription = findViewById(R.id.ev_description);
        iv_dealImage = findViewById(R.id.image_deal);

        Intent intent = getIntent();
        TravelDeal deal =  (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null){
            deal = new TravelDeal();
        }
        this.mDeal = deal;
        updateUI(deal);

        btnImage  = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStoragePermissions){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                    startActivityForResult(Intent.createChooser(intent,
                            "Insert Picture"),PICTURE_RESULT);
                }else{
                verifyStoragePermissions();
                }
            }
        });

    }

    private void clear() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etTitle.requestFocus();

    }

    private void saveDeal() {
        mDeal.setTitle(etTitle.getText().toString());
        mDeal.setDescription(etDescription.getText().toString());
        mDeal.setPrice(etPrice.getText().toString());
        if (mDeal.getId() == null) {
            mDatabaseReference.push().setValue(mDeal);
            Toast.makeText(this,"Travel Deal Inserted Successfully", Toast.LENGTH_SHORT).show();
        }else {
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
            Toast.makeText(this,"Travel Deal Updated Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDeal(){
        if (mDeal != null){
            mDatabaseReference.child(mDeal.getId()).removeValue();
            if(mDeal.getImageName() != null && mDeal.getImageName().isEmpty() == false){
                StorageReference picRef = FirebaseUtils.mStorageReference.child(mDeal.getImageName());
                picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DeleteFile","Image Deleted Successfully ");
                    }
                });
            }
            Toast.makeText(this,"Travel Deal Deleted Successfully", Toast.LENGTH_SHORT).show();

        }
    }

    private void updateUI(TravelDeal deal){
        etTitle.setText(deal.getTitle());
        etDescription.setText(deal.getDescription());
        etPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =  getMenuInflater();
        inflater.inflate(R.menu.deal_menu,menu);
        if (FirebaseUtils.isAdmin == true){
            menu.findItem(R.id.action_save_deal).setVisible(true);
            menu.findItem(R.id.action_del_deal).setVisible(true);
            enableEditTexts(true);
        }else {
            menu.findItem(R.id.action_save_deal).setVisible(false);
            menu.findItem(R.id.action_del_deal).setVisible(false);
            enableEditTexts(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }else if (id == R.id.action_save_deal){
            saveDeal();
            clear();
            backToList();
        }else if(id == R.id.action_del_deal){
            deleteDeal();
            backToList();
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableEditTexts(boolean isEnabled) {
        etTitle.setEnabled(isEnabled);
        etDescription.setEnabled(isEnabled);
        etPrice.setEnabled(isEnabled);
        if (isEnabled) {
             btnImage.setVisibility(View.VISIBLE);
        }else{
            btnImage.setVisibility(View.INVISIBLE);
        }
    }

    private void backToList() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtils.mStorageReference.child(imageUri.getLastPathSegment());
//            ref.putFile(imageUri).addOnSuccessListener(this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    String url = ref.getDownloadUrl().toString();
//                    String pictureName = taskSnapshot.getStorage().getPath();
//                    Log.d("UploadImage","Image Url : "+url);
//                    Log.d("UploadImage","Image Name : "+pictureName);
//                    mDeal.setImageUrl(url);
//                    mDeal.setImageName(pictureName);
//                    showImage(url);
//                }
//            });

            final UploadTask uploadTask = ref.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){

                    }

                    return ref.getDownloadUrl();
                }


            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri imageUrl = task.getResult();
                        String pictureName = uploadTask.getSnapshot().getStorage().getPath();
                        mDeal.setImageUrl(imageUrl.toString());
                        mDeal.setImageName(pictureName);
                        Log.d("UploadImage","Image Url : "+imageUrl.toString());
                        Log.d("UploadImage","Image Name : "+pictureName);
                        showImage(imageUrl.toString());
                    }
                }
            });
        }
    }

    private void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(iv_dealImage);
        }
    }

    public void verifyStoragePermissions(){
        Log.d("", "verifyPermissions: asking user for permissions.");
        String[] permissions = { android.Manifest.permission.READ_EXTERNAL_STORAGE };
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED ) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    DealActivity.this,
                    permissions,
                    PICTURE_RESULT
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d("DealActivity", "onRequestPermissionsResult: requestCode: " + requestCode);
        switch(requestCode){
            case PICTURE_RESULT:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("DealActivity", "onRequestPermissionsResult: User has allowed permission to access: " + permissions[0]);

                }
                break;
        }
    }
}
