package ankur.in.cf;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.CircularArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by ankur on 22/12/15.
 */
class CustomPagerAdapter extends PagerAdapter {
    private static String TAG = "CustomPagerAdapter";
    ArrayList<Uri> mImageUris = new ArrayList<>();
    private ArrayList<Integer> defaultIconList;
    Cursor mCursor;
    private int currentItemIndex =-1;

    public void updateCursor(Cursor pCursor) {
        mCursor = pCursor;
        mImageUris.clear();
        if(mCursor!=null) {
            while (mCursor.moveToNext()) {
                mImageUris.add(Uri.parse(mCursor.getString(1)));
            }
            notifyDataSetChanged();
        }
    }

    Context mContext;
    LayoutInflater mLayoutInflater;
    private int defaultImageResource;

    public CustomPagerAdapter(Context context,int pDefaultImageRes,ArrayList<Integer> pDefaultIconList) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultImageResource = pDefaultImageRes;
        defaultIconList = pDefaultIconList;
    }

    @Override
    public int getCount() {
        return mImageUris.size()!=0?mImageUris.size():
                (defaultIconList!=null?defaultIconList.size():0);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
        TextView tv = (TextView)itemView.findViewById(R.id.text);
        tv.setText(""+position);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        if(mImageUris.size()!=0) {
            currentItemIndex = position;
            Picasso.with(mContext).load(mImageUris.get(position)).placeholder(mContext.getResources().getDrawable(defaultImageResource))
                    .resize(200, 200).into(imageView);
        }else if(defaultIconList!=null){
            imageView.setImageResource(defaultIconList.get(position));
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
    public int getCurrentIemPosition(){
        return currentItemIndex;
    }
}