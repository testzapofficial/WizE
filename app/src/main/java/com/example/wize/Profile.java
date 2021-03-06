package com.example.wize;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {

    Button logout,edit;
    ImageView back;
    private android.app.AlertDialog.Builder builder;
    private android.app.AlertDialog dialog;
    ToggleButton menu;
    private GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    String userId;
    TextView name,username,friends,posts,groups;
   RecyclerView rview;
   TagsAdapter adapter;
   ImageView cutlogout;
   Button cancellogout,dologout;
   TextView goodbye;
   String bye;
   String names;
   String usernames;
   CircleImageView img;
    RecyclerView postView;
    ImageAdapter adapter2;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Query query;
    List<Integer> imagess=new ArrayList<>();
   public Profile() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        menu=view.findViewById(R.id.imageView6);
        back=view.findViewById(R.id.backfromprofile);
        logout=view.findViewById(R.id.buttonLo);
        edit=view.findViewById(R.id.buttonedit);
        name=view.findViewById(R.id.textView11);
        username=view.findViewById(R.id.textView12);
        posts=view.findViewById(R.id.textView14);
        groups=view.findViewById(R.id.textView13);
        friends=view.findViewById(R.id.textView15);
        rview=view.findViewById(R.id.recyclerView);
        img=view.findViewById(R.id.circleImageView2);
        postView=view.findViewById(R.id.profilepost);
        firebaseAuth= FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();
        userId=firebaseAuth.getCurrentUser().getUid();
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient= GoogleSignIn.getClient(getActivity(),gso);
        StorageReference storageReference= FirebaseStorage.getInstance().getReference();

        StorageReference profoleRef=storageReference.child("ProfileImg").child(userId);
        profoleRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(img);
            }
        });

        DocumentReference documentReference2= fStore.collection("Posts").document(userId);
        documentReference2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                postView.setLayoutManager(new GridLayoutManager(getContext(),2));

                postView.setNestedScrollingEnabled(false);
                CollectionReference cRef=db.collection("Posts");
                query = cRef.orderBy("timeStamp", Query.Direction.DESCENDING).whereEqualTo("userId",userId).whereEqualTo("type","Image Post");
                FirestoreRecyclerOptions<postModel> optionz = new FirestoreRecyclerOptions.Builder<postModel>()
                        .setQuery(query, postModel.class)
                        .build();
                adapter2= new ImageAdapter(optionz);
                postView.setAdapter(adapter2);
                adapter2.startListening();

            }
        });



        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (menu.isChecked()) {
                    logout.setVisibility(view.VISIBLE);
                    edit.setVisibility(view.VISIBLE);
                }
                else
                {
                    logout.setVisibility(view.INVISIBLE);
                    edit.setVisibility(view.INVISIBLE);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),HomeActivity.class));
                getActivity().finish();
            }
        });

        DocumentReference documentReference= fStore.collection("Users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

               names=value.getString("Full_Name");
                usernames=value.getString("UserName");
                long friend= value.getLong("Friends");
                long post= value.getLong("Posts");
                long group= value.getLong("Groups");
                name.setText(names);
                username.setText("@"+usernames);
                posts.setText(String.valueOf(post));
                groups.setText(String.valueOf(group));
                friends.setText(String.valueOf(friend));
                bye=usernames;

            }
        });
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<Map<String, Object>> interests = (List<Map<String, Object>>) document.get("User_Interests");
                       rview.setLayoutManager(new StaggeredGridLayoutManager(2,LinearLayoutManager.HORIZONTAL));
                        adapter=new TagsAdapter(interests);
                        rview.setAdapter(adapter);
                    }
                }
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder = new android.app.AlertDialog.Builder(getContext());
                final View vie = getLayoutInflater().inflate(R.layout.logoutpopup,null);
                builder.setView(vie);
                dialog = builder.create();
                dialog.show();
                cutlogout=(ImageView)dialog.findViewById(R.id.cutlogin);
                cancellogout=(Button)dialog.findViewById(R.id.cancelforlogout);
                dologout=(Button)dialog.findViewById(R.id.dologout);
                goodbye=(TextView)dialog.findViewById(R.id.textViewgoodbue);
                goodbye.setText("We will miss you "+"@"+bye+" !");
                cutlogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                cancellogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dologout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().signOut();
                        googleSignInClient.signOut();
                        startActivity(new Intent(getActivity(),SignIn.class));
                        getActivity().finish();
                    }
                });
            }

        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),Editprofile.class);
                intent.putExtra("fullName",names);
                startActivity(intent);
            }
        });
        return view;



    }
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}