package ankur.in.cf;


import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import ankur.in.cf.db.AppContentProvider;
import ankur.in.cf.db.DresserContract;

public class ClothFragment extends Fragment implements View.OnClickListener,LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "CloathFragment" ;
    private Boolean isFabOpen = false;
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private FloatingActionButton addImageAction, addFromGalleryAction, imageCaptureAction;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private int UNIQUE_ID ;
    private static final int CAMERA_REQUEST = 1888;
    private FragmentContract mFragmentContract;
    private static HashMap<String,Integer>  map;
    private int lastPos;

    static {
        map = new HashMap<>();
    }

    private int maxItemCount;

    private LinearLayout action_container;
    private String IDENTIFIER_ID;
    public static String IDENTITY_STRING_KEY = "IDENTITY_STRING_KEY";
    public static String LOADING_DRAWABLE_KEY = "LOADING_DRAWABLE_KEY";
    public static String DEFALUT_LIST_KEY = "DEFALUT_LIST_KEY";
    private ArrayList<Integer> defaultIconList;
    private static Uri _fileUri;
    private int defaultLoadingDrawable ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IDENTIFIER_ID = getArguments().getString(IDENTITY_STRING_KEY);
        defaultLoadingDrawable = R.drawable.loading;
        if(getArguments().getInt(LOADING_DRAWABLE_KEY,-1)!=-1){
            defaultLoadingDrawable = getArguments().getInt(LOADING_DRAWABLE_KEY);
        }
        defaultIconList = (ArrayList) getArguments().getSerializable(DEFALUT_LIST_KEY);
        if(defaultIconList!=null){
            maxItemCount = defaultIconList.size();
        }
        if(IDENTIFIER_ID==null){
            throw new RuntimeException("Identifier String can not be empty");
        }
        UNIQUE_ID = getId();
        if(savedInstanceState!=null){
            _fileUri = savedInstanceState.getParcelable(IMAGE_URI_FOR_CAM_CAPTURE);
            lastPos = savedInstanceState.getInt(CURRENT_INDEX_KEY);
        }
        getActivity().getSupportLoaderManager().restartLoader(UNIQUE_ID, null, this);
        Log.i(TAG, "onCreate Finish loader called with " + UNIQUE_ID);
        mCustomPagerAdapter = new CustomPagerAdapter(getActivity(),defaultLoadingDrawable,defaultIconList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() instanceof FragmentContract){
            mFragmentContract = (FragmentContract)getActivity();
        }
    }

    CustomPagerAdapter mCustomPagerAdapter;
    ViewPager mViewPager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.choth_fragment_view,null,false);
        addImageAction = (FloatingActionButton)v.findViewById(R.id.add_item);
        addFromGalleryAction = (FloatingActionButton)v.findViewById(R.id.select_from_gallery);
        imageCaptureAction = (FloatingActionButton)v.findViewById(R.id.take_image);
        fab_open = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.rotate_backward);
        addImageAction.setOnClickListener(this);
        addFromGalleryAction.setOnClickListener(this);
        imageCaptureAction.setOnClickListener(this);

        mViewPager = (ViewPager) v.findViewById(R.id.pager);
        action_container = (LinearLayout)v.findViewById(R.id.action_container);
        mViewPager.setAdapter(mCustomPagerAdapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeOpenMenuIfOpen();
                if(mFragmentContract!=null) {
                    mFragmentContract.onCloathFragmentPagerTouch(UNIQUE_ID);
                }
                return false;
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected " + position);
                map.put(IDENTIFIER_ID,position);
                //lastPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if(getId() == R.id.shirts_fragment_holder){
            if(getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                setTopNav();
            }else if(getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                setTopNav_test();
            }
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.pager:

                break;
            case R.id.add_item:
                animateFAB();
                break;
            case R.id.select_from_gallery:
                Intent intent = null;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }else{
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
                break;
            case R.id.take_image:
                ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File _photoFile = null;
                try {
                    _photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                _fileUri = Uri.fromFile(_photoFile);

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra( MediaStore.EXTRA_OUTPUT, _fileUri);
                startActivityForResult(intent, CAMERA_REQUEST);
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    public void closeOpenMenuIfOpen() {
        if(isFabOpen){
            addImageAction.startAnimation(rotate_backward);
            addFromGalleryAction.startAnimation(fab_close);
            imageCaptureAction.startAnimation(fab_close);
            addFromGalleryAction.setClickable(false);
            imageCaptureAction.setClickable(false);
            isFabOpen = false;
        }
    }

    public void animateFAB(){
        if(isFabOpen){
            addImageAction.startAnimation(rotate_backward);
            addFromGalleryAction.startAnimation(fab_close);
            imageCaptureAction.startAnimation(fab_close);
            addFromGalleryAction.setClickable(false);
            imageCaptureAction.setClickable(false);
            isFabOpen = false;
        } else {
            addImageAction.startAnimation(rotate_forward);
            addFromGalleryAction.startAnimation(fab_open);
            imageCaptureAction.startAnimation(fab_open);
            addFromGalleryAction.setClickable(true);
            imageCaptureAction.setClickable(true);
            isFabOpen = true;
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
// Check for the freshest data.
                //noinspection ResourceType
                getActivity().getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
                System.out.println("Image Path : " + selectedImagePath);
                insertNewImageUriInDb(selectedImageUri);
                mCustomPagerAdapter.notifyDataSetChanged();
            }
            else if (requestCode == CAMERA_REQUEST ) {
                //Bitmap photo = (Bitmap) data.getExtras().get("data");
//                saveImageinDb(photo);
                if(_fileUri!=null /*&& photo!=null*/)
//                getActivity().getContentResolver().takePersistableUriPermission(_fileUri, takeFlags);
                insertNewImageUriInDb(_fileUri);
                _fileUri = null;
            }
        }
        animateFAB();
    }

    private void insertNewImageUriInDb(Uri selectedImageUri) {
            ContentValues cv = new ContentValues();
            cv.put(DresserContract.Images.IMAGE_DATA, selectedImageUri.toString());
            cv.put(DresserContract.Images.FRAGMENT_COMPONENT_ID, IDENTIFIER_ID);
            getActivity().getContentResolver().insert(AppContentProvider.IMAGE_TABLE_URI, cv);
    }


    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    //private static final int URL_LOADER = UNIQUE_ID;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e(TAG,"Loader Query for " + UNIQUE_ID);
        switch (id) {
            case R.id.pants_fragment_holder:
            case R.id.shirts_fragment_holder:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        AppContentProvider.IMAGE_TABLE_URI,        // Table to query
                        null,     // Projection to return
                        DresserContract.Images.FRAGMENT_COMPONENT_ID+"=?",            // No selection clause
                        new String[]{""+IDENTIFIER_ID},            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }

    }

    public void setTopNav(){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) action_container.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.END;
        int topMargin = params.topMargin;
        params.topMargin = params.bottomMargin;
        params.bottomMargin = topMargin;
        topMargin = params.leftMargin;
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.port_right_margin_add_layout_up);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.port_right_margin_add_layout_up);
        action_container.setLayoutParams(params);
        action_container.setRotation(180.0f);
    }
    public void setTopNav_test(){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) action_container.getLayoutParams();
        params.gravity = Gravity.START | Gravity.TOP;
        int leftmargin = params.leftMargin;
        params.leftMargin = params.rightMargin;
        params.rightMargin = leftmargin;
        action_container.setLayoutParams(params);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCustomPagerAdapter.updateCursor(data);
        maxItemCount = data.getCount();
        if((data==null || data.getCount()==0) && defaultIconList!=null){
            maxItemCount = defaultIconList.size();
        }
        if(map.get(IDENTIFIER_ID)!=null) {
            mViewPager.setCurrentItem(map.get(IDENTIFIER_ID),false);
            //mViewPager.setCurrentItem(lastPos,false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCustomPagerAdapter.updateCursor(null);
    }
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    public void shuffle(){
        int ranIndex = randInt(0,maxItemCount);
        //mCustomPagerAdapter.shuffle(ranIndex);
        mViewPager.setCurrentItem(ranIndex,true);
    }
    public static final String CURRENT_INDEX_KEY = "CURRENT_INDEX_KEY";
    public static final String IMAGE_URI_FOR_CAM_CAPTURE = "IMAGE_URI_FOR_CAM_CAPTURE";
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_INDEX_KEY, mCustomPagerAdapter.getCurrentIemPosition());
        if(_fileUri!=null) {
            outState.putParcelable(IMAGE_URI_FOR_CAM_CAPTURE, _fileUri);
        }
        super.onSaveInstanceState(outState);
    }
    public int getCurrentItemIndex(){
        return mCustomPagerAdapter.getCurrentIemPosition();
    }
    public static interface FragmentContract{
        void onCloathFragmentPagerTouch(int pContainerId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getSupportLoaderManager().destroyLoader(UNIQUE_ID);
    }
}