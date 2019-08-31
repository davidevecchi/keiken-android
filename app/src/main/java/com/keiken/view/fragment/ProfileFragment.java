package com.keiken.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.keiken.R;
import com.keiken.view.IOnBackPressed;
import com.keiken.view.activity.LauncherActivity;
import com.keiken.view.backdrop.BackdropFrontLayer;
import com.keiken.view.backdrop.BackdropFrontLayerBehavior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import static com.keiken.controller.ImageController.setProfilePic;

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
    private BackdropFrontLayerBehavior sheetBehavior, sheetBehaviorReviews;
    private MaterialButton menuButton;
    private ImageView upArrow, downArrow;
    private LinearLayoutCompat header;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;




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


        TextView reviewsButton = c.findViewById(R.id.reviews_button);
        TextView logout = c.findViewById(R.id.logout);
        TextView email = c.findViewById(R.id.email);
        ImageView profilePic = c.findViewById(R.id.profile_pic);
        TextView contacts = c.findViewById(R.id.contacts);
        TextView date = c.findViewById(R.id.date);


        final BackdropFrontLayer contentLayout = c.findViewById(R.id.backdrop);
        final BackdropFrontLayer contentLayoutReviews = c.findViewById(R.id.backdrop_reviews);


        sheetBehavior = (BackdropFrontLayerBehavior) BottomSheetBehavior.from(contentLayout);
        sheetBehavior.setFitToContents(false);
        sheetBehavior.setHideable(false);//prevents the boottom sheet from completely hiding off the screen
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);//initially state to fully expanded


        sheetBehaviorReviews = (BackdropFrontLayerBehavior) BottomSheetBehavior.from(contentLayoutReviews);
        sheetBehaviorReviews.setFitToContents(false);
        //sheetBehaviorReviews.setHideable(false);//prevents the boottom sheet from completely hiding off the screen
        sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);//initially state to fully expanded




        menuButton = c.findViewById(R.id.menu_button);
        upArrow = c.findViewById(R.id.up_arrow);
        downArrow = c.findViewById(R.id.down_arrow);
        header = c.findViewById(R.id.header);

        contentLayout.setBehavior(sheetBehavior);
        contentLayout.setButton(menuButton);
        contentLayout.setDrawable((AnimatedVectorDrawable)getResources().getDrawable(R.drawable.cross_to_points));

        contentLayout.setImageView(upArrow);
        contentLayout.setDrawable2((AnimatedVectorDrawable)getResources().getDrawable(R.drawable.black_to_white_up_arrow));




        contentLayoutReviews.setBehavior(sheetBehaviorReviews);
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

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        reviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                menuButton.setIcon(getResources().getDrawable(R.drawable.points_to_cross));
                AnimatedVectorDrawable ic = (AnimatedVectorDrawable) menuButton.getIcon();
                ic.start();

                sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_EXPANDED);


            }
        });











        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(getContext(), LauncherActivity.class));
            }
        });








        FirebaseUser user = mAuth.getCurrentUser();




        if (user != null) {
            // Name, email address, and profileImageView photo Url
            toolbar.setTitle(user.getDisplayName());
            email.setText(user.getEmail());
            setProfilePic(profilePic);



        }



        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:keiken@mail.com"));
                startActivity(emailIntent);
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
    public boolean onBackPressed() {
        if (sheetBehaviorReviews.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            menuButton.setIcon(getResources().getDrawable(R.drawable.cross_to_points));
            AnimatedVectorDrawable ic =  (AnimatedVectorDrawable)menuButton.getIcon();
            ic.start();

            sheetBehaviorReviews.setState(BottomSheetBehavior.STATE_COLLAPSED);

            return true;
        }

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



}


