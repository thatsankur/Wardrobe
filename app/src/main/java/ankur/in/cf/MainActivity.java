package ankur.in.cf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import ankur.in.cf.db.AppContentProvider;
import ankur.in.cf.db.DresserContract;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ClothFragment.FragmentContract {
    private FloatingActionButton shuffle, save, addToList, displayList;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private ClothFragment shirt,pants;
    ArrayList<Integer> defaultShirts,defaultPants ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        defaultPants=new ArrayList<>();
        defaultPants.add(R.drawable.p1);
        defaultPants.add(R.drawable.p2);

        defaultShirts = new ArrayList<>();
        defaultShirts.add(R.drawable.s1);
        defaultShirts.add(R.drawable.s2);

        Bundle b1= new Bundle();
        b1.putString(ClothFragment.IDENTITY_STRING_KEY, "shirts");
        b1.putInt(ClothFragment.LOADING_DRAWABLE_KEY, R.drawable.loading);
        //b1.putSerializable(ClothFragment.DEFALUT_LIST_KEY, defaultShirts);

        Bundle b2= new Bundle();
        b2.putString(ClothFragment.IDENTITY_STRING_KEY,"pants");
        b2.putInt(ClothFragment.LOADING_DRAWABLE_KEY, R.drawable.loading);
        //b2.putSerializable(ClothFragment.DEFALUT_LIST_KEY, defaultPants);

        shirt = new ClothFragment();
        shirt.setArguments(b1);

        pants = new ClothFragment();
        pants.setArguments(b2);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.shirts_fragment_holder, shirt);
        ft.replace(R.id.pants_fragment_holder, pants);
        ft.commitAllowingStateLoss();

        initilizeActions();
        alarmMethod();

    }

    private void initilizeActions() {
        shuffle = (FloatingActionButton)findViewById(R.id.shuffle);
        shuffle.setOnClickListener(this);
        save = (FloatingActionButton)findViewById(R.id.save);
        save.setOnClickListener(this);
        addToList = (FloatingActionButton)findViewById(R.id.add_to_list);
        addToList.setOnClickListener(this);
        displayList = (FloatingActionButton)findViewById(R.id.display_list);
        displayList.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.shuffle:
                if(shirt!=null){
                    shirt.shuffle();
                }
                if(pants!=null){
                    pants.shuffle();
                }
                closeMenuifOpen();
                break;
            case R.id.save:
                animateFAB();
                break;
            case R.id.add_to_list:
                int shirtID=-1,pantId=-1;
                if(shirt!=null){
                    shirtID = shirt.getCurrentItemIndex();
                }
                if(pants!=null){
                    pantId = pants.getCurrentItemIndex();
                }
                if(shirtID>-1 && pantId >-1){
                    //save to fav table
                    saveFavinDB(shirtID,pantId);
                    Toast.makeText(this,"Added to Favourites",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Add Items first to save favourite list!",Toast.LENGTH_SHORT).show();
                }
                closeMenuifOpen();
                break;
            case R.id.display_list:
                showFavList();
                closeMenuifOpen();
                break;
        }
    }

    private void closeMenuifOpen() {
        if (isFabOpen){
            save.startAnimation(rotate_backward);
            addToList.startAnimation(fab_close);
            displayList.startAnimation(fab_close);
            addToList.setClickable(false);
            displayList.setClickable(false);
            isFabOpen = false;
        }
    }

    private Boolean isFabOpen = false;
    public void animateFAB(){
        if (isFabOpen){
            save.startAnimation(rotate_backward);
            addToList.startAnimation(fab_close);
            displayList.startAnimation(fab_close);
            addToList.setClickable(false);
            displayList.setClickable(false);
            isFabOpen = false;
        } else {
            save.startAnimation(rotate_forward);
            addToList.startAnimation(fab_open);
            displayList.startAnimation(fab_open);
            addToList.setClickable(true);
            displayList.setClickable(true);
            isFabOpen = true;
        }
    }

    public void setTopNav(){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) save.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        CoordinatorLayout.LayoutParams params1 = (CoordinatorLayout.LayoutParams) addToList.getLayoutParams();
        params1.gravity = Gravity.TOP | Gravity.RIGHT;
        params1.topMargin = params1.bottomMargin;

        CoordinatorLayout.LayoutParams params2 = (CoordinatorLayout.LayoutParams) displayList.getLayoutParams();
        params2.gravity = Gravity.TOP | Gravity.RIGHT;
        params2.topMargin = params2.bottomMargin;

        save.setLayoutParams(params);
        addToList.setLayoutParams(params1);
        displayList.setLayoutParams(params2);
    }
    private void saveFavinDB(int pShirtID,int pPantID) {
        ContentValues cv = new ContentValues();
        cv.put(DresserContract.Favourit.FAV_PANT, pPantID);
        cv.put(DresserContract.Favourit.FAV_SHIRT, pShirtID);
        getContentResolver().insert(AppContentProvider.FAV_TABLE_URI, cv);
    }
    private void showFavList(){
        Cursor cursor = getContentResolver().query(AppContentProvider.FAV_TABLE_URI, null, null, null, null);
        StringBuffer sb = new StringBuffer();
        while (cursor.moveToNext()){
            sb.append(cursor.getColumnName(1)+" "+cursor.getString(1) +" "+cursor.getColumnName(2)+" "+ cursor.getString(2));
        }
        Toast.makeText(this,sb.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCloathFragmentPagerTouch(int pContainerId) {
        switch (pContainerId){
            case R.id.shirts_fragment_holder:
            case R.id.pants_fragment_holder:
                closeMenuifOpen();
                shirt.closeOpenMenuIfOpen();
                pants.closeOpenMenuIfOpen();
                break;
        }
    }
    private void alarmMethod() {
        Intent myIntent = new Intent(this, NotificationService.class);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);

        // Set the alarm to start at approximately 6:00 am.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

        Toast.makeText(MainActivity.this, "Start Alarm", Toast.LENGTH_LONG)
                .show();
    }
}
