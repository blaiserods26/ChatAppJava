package com.example.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.database.Model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class new_post extends AppCompatActivity{

    String temp,temp1,temp2;
    EditText editText,editText2;
    Button button,button1;
    String c;
    int flag=0;
    ImageView imageView;
    String username;



    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Objects.requireNonNull(getSupportActionBar()).hide();

        imageView = findViewById(R.id.image_view);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef2 = database.getReference("count");
        editText= findViewById(R.id.edit);
        editText2=findViewById(R.id.edit2);
        button = findViewById(R.id.btn);
        button1= findViewById(R.id.buttonPanel);






        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag=1;
                myRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FirebaseUser fuser;
                        DatabaseReference reference;
                        fuser = FirebaseAuth.getInstance().getCurrentUser();
                        reference = FirebaseDatabase.getInstance().getReference("MyUsers")
                                .child(fuser.getUid());



                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Users user = dataSnapshot.getValue(Users.class);
                                username=(user.getUsername()).toString();

                                DatabaseReference myRef5 = database.getReference("author");
                                HashMap<String, Object> hashMap5 = new HashMap<>();
                                hashMap5.put(c,username);
                                myRef5.updateChildren(hashMap5);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                        temp = String.valueOf(snapshot.getValue());
                        int count=Integer.parseInt(temp);
                        if(flag==1) {flag=0;
                            addcount(count);
                            uploadImage(count,imageUri);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    private void addcount(int count) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef2 = database.getReference("count");
        DatabaseReference myRef1 = database.getReference("message");
        DatabaseReference myRef3 = database.getReference("description");
        DatabaseReference myRef4 = database.getReference("url");
        temp2= editText2.getText().toString().trim();
        temp1= editText.getText().toString().trim();
        HashMap<String, Object> hashMap = new HashMap<>();
        c=String.valueOf(count);
        hashMap.put(c , temp1);
        myRef1.updateChildren(hashMap);

        HashMap<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put(c , temp2);
        myRef3.updateChildren(hashMap1);

        count++;
        myRef2.setValue(count);
        finish();
    }







    private void uploadImage(int count,Uri uri) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File....");
        progressDialog.show();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = formatter.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName+".jpg");
        try {

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageView = findViewById(R.id.image_view);
                            imageView.setImageURI(imageUri);
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String a= uri.toString();
                                    Toast.makeText(new_post.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef4 = database.getReference("url");

                                    c=String.valueOf(count);
                                    HashMap<String, Object> hashMap4 = new HashMap<>();
                                    hashMap4.put(c , a);
                                    myRef4.updateChildren(hashMap4);

                                    //I have no idea what it is but apparently app uploads has it
                                    // acc to me it just crashes the app
                                    // Note to self (slayde) see if it necessary
                                    // progressDialog.dismiss();



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(new_post.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Toast.makeText(new_post.this, "Failed to Upload", Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch (Exception e){

        }
    }


    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){
            imageView = findViewById(R.id.image_view);
            imageUri = data.getData();
            imageView.setImageURI(imageUri);


        }
    }




}