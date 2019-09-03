package com.keiken.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keiken.R;
import com.keiken.view.IOnBackPressed;
import com.keiken.view.activity.LauncherActivity;
import com.keiken.view.backdrop.BackdropFrontLayer;
import com.keiken.view.backdrop.BackdropFrontLayerBehavior;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;
import static com.keiken.controller.ImageController.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements IOnBackPressed {

    private LinearLayout  backgroundFrame;
    private BackdropFrontLayerBehavior sheetBehavior, sheetBehaviorReviews, sheetBehaviorEdit;
    private MaterialButton menuButton;
    private ImageView upArrow, downArrow, downArrowEdit;
    private LinearLayoutCompat header, headerEdit;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    static final int REQUEST_PHOTO = 1889;
    private static final String NON_NORMAL_CHARACTERS_PATTERN = "\\W|[^!@#\\$%\\^&\\*\\(\\)]";

    private ImageView profileImageView;

    private StorageReference storageReference;
    private ProgressDialog progressDialog = null;

    private Uri storageUrl = null;




    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // activity listener interface
    private OnPageListener pageListener;
    public interface OnPageListener {
        public void onPage1(String s);
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StoricoGiorno1Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout c = (FrameLayout) inflater.inflate(R.layout.fragment_profile, container, false);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        TextView reviewsButton = c.findViewById(R.id.reviews_button);
        TextView logout = c.findViewById(R.id.logout);
        TextView email = c.findViewById(R.id.email);
        TextView contacts = c.findViewById(R.id.contacts);
        final TextView date = c.findViewById(R.id.date);

        profileImageView = c.findViewById(R.id.profile_pic);
        Button changePhotoButton = c.findViewById(R.id.change_photo);
        Button editProfileButton = c.findViewById(R.id.edit_profile);
        Button  confirmEditProfile= c.findViewById(R.id.confirm_edit_profile);


        final EditText nameEditText = c.findViewById(R.id.name_edit);
        final EditText surnameEditText = c.findViewById(R.id.surname_edit);
        final EditText dayEditText = c.findViewById(R.id.day_edit);
        final EditText monthEditText = c.findViewById(R.id.month_edit);
        final EditText yearEditText = c.findViewById(R.id.year_edit);
        final EditText emailEditText = c.findViewById(R.id.email_edit);
        final EditText biografiaEditText = c.findViewById(R.id.bio_edit);
        final EditText passwordEditText = c.findViewById(R.id.password_edit);
        final EditText password2EditText = c.findViewById(R.id.password2_edit);


        final BackdropFrontLayer contentLayout = c.findViewById(R.id.backdrop);
        final BackdropFrontLayer contentLayoutReviews = c.findViewById(R.id.backdrop_reviews);
        final BackdropFrontLayer contentLayoutEdit = c.findViewById(R.id.backdrop_edit);


        sheetBehavior = (BackdropFrontLayerBehavior) BottomSheetBehavior.from(contentLayout);
        sheetBehavior.setFitToContents(false);
        sheetBehavior.setHideable(false);//prevents the boottom sheet from completely hiding off the screen
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);//initially state to fully expanded


        sheetBehaviorReviews = (BackdropFrontLayerBehavior) BottomSheetBehavior.from(contentLayoutReviews);
        sheetBehaviorReviews.setFitToContents(false);
        //sheetBehaviorReviews.setHideable(false);//prevents the boottom sheet from completely hiding off the screen
        sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);//initially state to fully expanded


        sheetBehaviorEdit = (BackdropFrontLayerBehavior) BottomSheetBehavior.from(contentLayoutEdit);
        sheetBehaviorEdit.setFitToContents(false);
        //sheetBehaviorEdit.setHideable(false);//prevents the boottom sheet from completely hiding off the screen
        sheetBehaviorEdit.setState(BottomSheetBehavior.STATE_COLLAPSED);//initially state to fully expanded




        menuButton = c.findViewById(R.id.menu_button);
        upArrow = c.findViewById(R.id.up_arrow);
        downArrow = c.findViewById(R.id.down_arrow);
        downArrowEdit = c.findViewById(R.id.down_arrow_edit);
        header = c.findViewById(R.id.header);
        headerEdit = c.findViewById(R.id.header_edit);

        contentLayout.setBehavior(sheetBehavior);
        contentLayout.setButton(menuButton);
        contentLayout.setDrawable((AnimatedVectorDrawable)getResources().getDrawable(R.drawable.cross_to_points));

        contentLayout.setImageView(upArrow);
        contentLayout.setDrawable2((AnimatedVectorDrawable)getResources().getDrawable(R.drawable.black_to_white_up_arrow));




        contentLayoutReviews.setBehavior(sheetBehaviorReviews);
        contentLayoutEdit.setBehavior(sheetBehaviorEdit);
        contentLayout.setButton(menuButton);
        contentLayout.setDrawable((AnimatedVectorDrawable)getResources().getDrawable(R.drawable.cross_to_points));

        contentLayout.setImageView(upArrow);
        contentLayout.setDrawable2((AnimatedVectorDrawable)getResources().getDrawable(R.drawable.black_to_white_up_arrow));








        final DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        backgroundFrame = c.findViewById(R.id.background_frame_x);

        //sheetBehavior.setPeekHeight(displayMetrics.heightPixels-bFrameHeight);


        ViewTreeObserver viewTreeObserver = backgroundFrame.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    backgroundFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int viewHeight = backgroundFrame.getBottom();

                    int toolbarPx = (int)( 80 * (displayMetrics.densityDpi / 160f));
                    int bottomBarPx = (int)( 56 * (displayMetrics.densityDpi / 160f));

                    int peekHeight = displayMetrics.heightPixels-viewHeight-toolbarPx-bottomBarPx;


                    sheetBehavior.setPeekHeight(peekHeight);
                    //int bottomPx = (int)( 70 * (displayMetrics.densityDpi / 160f));
                    //sheetBehaviorReviews.setPeekHeight(bottomPx);

                    int marginPx = (int)( 20 * (displayMetrics.densityDpi / 160f));
                    sheetBehaviorReviews.setPeekHeight(0);
                    sheetBehaviorEdit.setPeekHeight(0);

                }
            });
        }


        Toolbar toolbar = c.findViewById(R.id.toolbar);
        toolbar.setElevation(0);
        toolbar.setTitle("Profilo");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);



        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sheetBehaviorReviews.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                    menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                    AnimatedVectorDrawable ic =  (AnimatedVectorDrawable)menuButton.getIcon();
                    ic.start();

                    sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                else {

                    if (sheetBehaviorEdit.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                        menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                        AnimatedVectorDrawable ic =  (AnimatedVectorDrawable)menuButton.getIcon();
                        ic.start();

                        sheetBehaviorEdit.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }

                    else {


                        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            //  recursiveLoopChildren(false, contentLayout);
                            menuButton.setIcon(getResources().getDrawable(R.drawable.points_to_cross));
                            AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                            ic.start();

                            upArrow.setImageDrawable((getResources().getDrawable(R.drawable.white_to_black_up_arrow)));
                            AnimatedVectorDrawable ic2 = (AnimatedVectorDrawable) upArrow.getDrawable();
                            ic2.start();

                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        } else {

                            menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                            AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                            ic.start();

                            upArrow.setImageDrawable((getResources().getDrawable(R.drawable.black_to_white_up_arrow)));
                            AnimatedVectorDrawable ic2 = (AnimatedVectorDrawable) upArrow.getDrawable();
                            ic2.start();

                            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            // recursiveLoopChildren(true, contentLayout);

                        }
                    }
                }

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //backlayer button
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(getContext(), LauncherActivity.class));
            }
        });


        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //backlayer button
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:keiken@mail.com"));
                startActivity(emailIntent);
            }
        });

        downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        downArrowEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorEdit.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        headerEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorEdit.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });



        reviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //open reviews layer

                menuButton.setIcon(getResources().getDrawable(R.drawable.points_to_cross));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_EXPANDED);


            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //open editProfile layer
                //startActivity(new Intent(getContext(), EditProfileActivity.class));

                menuButton.setIcon(getResources().getDrawable(R.drawable.points_to_cross));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorEdit.setState(BottomSheetBehavior.STATE_EXPANDED);


            }
        });

        changePhotoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                        || ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                        || ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            0);
                }
                else selectImage();
            }
        });



        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Name, email address, and profileImageView photo Url
            toolbar.setTitle(user.getDisplayName());
            email.setText(user.getEmail());
            setProfilePic(profileImageView);

            emailEditText.setText(user.getEmail());



            CollectionReference yourCollRef = db.collection("utenti");
            Query query = yourCollRef.whereEqualTo("id", user.getUid());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        String name, surname, day, month, year;
                        QuerySnapshot result = task.getResult();
                        try {
                            List<DocumentSnapshot> documents = result.getDocuments();
                            DocumentSnapshot document = documents.get(0);

                            name = document.getString("name");
                            surname = document.getString("surname");
                            if (name != null && surname != null) {
                                nameEditText.setText(name);
                                surnameEditText.setText(surname);
                            }

                            day = document.getString("day");
                            month = document.getString("month");
                            year = document.getString("year");
                            if (day != null && month != null && year != null) {
                                date.setText(day + "/" + month + "/" + year);
                                dayEditText.setText(day);
                                monthEditText.setText(month);
                                yearEditText.setText(year);
                            }
                        }
                        catch (Exception e) {};

                    }
                }
            });

        } //set default values for edit text of "modify profile"

        confirmEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //confirm profile changes on database



                /////////////
                String name = nameEditText.getText().toString();
                String surname = surnameEditText.getText().toString();
                String day = dayEditText.getText().toString();
                String month = monthEditText.getText().toString();
                String year = yearEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String biografia = biografiaEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String password2 = password2EditText.getText().toString();


                if (verifyProfileInformations(name, surname, day, month, year, email, biografia, password, password2)) {

                    CollectionReference yourCollRef = db.collection("utenti");
                    Query query = yourCollRef.whereEqualTo("id", user.getUid());
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                String name, surname, day, month, year;
                                QuerySnapshot result = task.getResult();
                                try {
                                    List<DocumentSnapshot> documents = result.getDocuments();
                                    DocumentSnapshot document = documents.get(0);

                                    name = document.getString("name");
                                    surname = document.getString("surname");
                                    if (name != null && surname != null) {
                                        nameEditText.setText(name);
                                        surnameEditText.setText(surname);
                                    }

                                    day = document.getString("day");
                                    month = document.getString("month");
                                    year = document.getString("year");
                                    if (day != null && month != null && year != null) {
                                        date.setText(day + "/" + month + "/" + year);
                                        dayEditText.setText(day);
                                        monthEditText.setText(month);
                                        yearEditText.setText(year);
                                    }
                                }
                                catch (Exception e) {}

                            }
                        }
                    });



                    menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                    AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                    ic.start();
                    sheetBehaviorEdit.setState(BottomSheetBehavior.STATE_COLLAPSED);


                }




            }
        });


        return c;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    // onAttach : set activity listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // if implemented by activity, set listener
        if(activity instanceof OnPageListener) {
            pageListener = (OnPageListener) activity;
        }
        // else create local listener (code never executed in this example)
        else pageListener = new OnPageListener() {
            @Override
            public void onPage1(String s) {
                Log.d("PAG1","Button event from page 1 : "+s);
            }
        };
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*  public void recursiveLoopChildren(boolean enable, ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);

            child.setEnabled(enable);

            if (child instanceof ViewGroup) {
                recursiveLoopChildren(enable, (ViewGroup) child);
            }
        }
    }  */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            selectImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            if (data == null) { //camera
                try {
                    uploadProfileImage(storageUrl);
                    profileImageView.setImageURI(storageUrl);
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(storageUrl);
                    Objects.requireNonNull(getActivity()).sendBroadcast(mediaScanIntent);
                } catch (Exception e) {
                    Log.e("Error uploading file", e.getMessage());
                }
            } else { //gallery
                Uri filePath = data.getData();
                profileImageView.setImageURI(filePath);
                uploadProfileImage(filePath);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    } @Override

    public boolean onBackPressed() {

        if (sheetBehaviorReviews.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
            AnimatedVectorDrawable ic =  (AnimatedVectorDrawable)menuButton.getIcon();
            ic.start();

            sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);

            return true;
        } //close reviews backdrop frontlayer
        if (sheetBehaviorEdit.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
            AnimatedVectorDrawable ic =  (AnimatedVectorDrawable)menuButton.getIcon();
            ic.start();

            sheetBehaviorEdit.setState(BottomSheetBehavior.STATE_COLLAPSED);

            return true;
        } //close edit profile backdrop frontlayer
        else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            //action not popBackStack

            menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
            AnimatedVectorDrawable ic =  (AnimatedVectorDrawable)menuButton.getIcon();
            ic.start();

            upArrow.setImageDrawable((getResources().getDrawable(R.drawable.black_to_white_up_arrow)));
            AnimatedVectorDrawable ic2 =  (AnimatedVectorDrawable)upArrow.getDrawable();
            ic2.start();

            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            return true;
        } else
            return false;

    }



    private void selectImage() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooser = Intent.createChooser(galleryIntent, "Seleziona app:");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        storageUrl = Uri.fromFile(createVoidImageFile());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, storageUrl);

        startActivityForResult(chooser, REQUEST_PHOTO);
    } //start an activity calling camera and gallety intent to edit profile photo

    private void uploadProfileImage(Uri filePath) {

        if (filePath != null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();


            final StorageReference ref = storageReference.child("images/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri).build();
                                    if (user != null) {
                                        user.updateProfile(profileUpdates);
                                    }

                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }

    }










    //verifica presenza di simboli // TRUE = PRESENZA /// FALSE = ASSENZA
    public static boolean hasSymbols(String input) {
        return input.matches(NON_NORMAL_CHARACTERS_PATTERN);
    }
    public static boolean isBisestile(int anno){
        if ( anno>1800 &&
                ( (anno%400==0) ||
                        (anno%4==0 && anno%100!=0) ) )
            return true;
        else return false;
    }

    public boolean verifyProfileInformations(String name, String surname, String day, String month, String year, String email, String biografia, String password, String password2) {

        if (name.equals("") || surname.equals("") || email.equals("")) {
            Toast.makeText(getContext(), "Nome, cognome ed email non possono essere vuoti.", Toast.LENGTH_LONG).show();
            return false;
        }


        //controllo sui caratteri in ingresso
        if (hasSymbols(name) || hasSymbols(surname) || hasSymbols(day) || hasSymbols(month) || hasSymbols(year)|| hasSymbols(email) || hasSymbols(biografia) || hasSymbols(password) || hasSymbols(password2)  )  {
            Toast.makeText(getContext(), "I campi compilati non possono contenere caratteri speciali. ", Toast.LENGTH_LONG).show();
            return false;
        }
        if((hasSymbols(password)) || hasSymbols(password2)) {
            Toast.makeText(getContext(), "La password non può contenere caratteri speciali. ", Toast.LENGTH_LONG).show();
            return false;
        }
        //

        if (!password.equals("")) {
            if (password.length() < 6) {
                Toast.makeText(getContext(), "La passowrd deve essere di almeno 6 caratteri.", Toast.LENGTH_LONG).show();
                return false;
            }

            if (!password.equals(password2)) {
                Toast.makeText(getContext(), "Le password non corrispondono!", Toast.LENGTH_LONG).show();
                return false;
            }
        }




        if (day != null && month != null && year != null) {

            try {
                int dayInt = Integer.parseInt(day), monthInt = Integer.parseInt(month), yearInt = Integer.parseInt(year);

                //controllo date
                if (dayInt < 1 || dayInt > 31 || monthInt < 1 || monthInt > 12 || yearInt < 1800) {
                    Toast.makeText(getContext(), "Controlla la data inserita. ", Toast.LENGTH_LONG).show();
                    return false;
                }
                if (monthInt == 2) {
                    if (isBisestile(yearInt)) {
                        if (dayInt > 29) {
                            Toast.makeText(getContext(), "Controlla la data inserita. ", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } else {
                        if (dayInt > 28) {
                            Toast.makeText(getContext(), "Controlla la data inserita. ", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                }
                if (monthInt == 11 || monthInt == 4 || monthInt == 6 || monthInt == 9)
                    if (dayInt > 30) {
                        Toast.makeText(getContext(), "Controlla la data inserita. ", Toast.LENGTH_LONG).show();
                        return false;
                    }
                //

                Calendar c = Calendar.getInstance();
                int currentYear = c.get(Calendar.YEAR);
                int currentMonth = c.get(Calendar.MONTH);
                int currentDay = c.get(Calendar.DAY_OF_MONTH);

                if (((yearInt > currentYear)) || (yearInt == currentYear && monthInt > currentMonth)
                        || (yearInt == currentYear && monthInt == currentMonth && dayInt >= currentDay)) {
                    Toast.makeText(getContext(), "La data inserita è sbagliata!", Toast.LENGTH_LONG).show();
                    return false;
                }

            }
            catch (Exception e) {return false;}




        }

        return true;
    }






}


