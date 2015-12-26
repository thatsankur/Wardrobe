package ankur.in.cf.db;

import android.provider.BaseColumns;

/**
 * Created by ankur on 23/12/15.
 */
public final class DresserContract {
    public DresserContract(){}
    public static abstract class Images implements BaseColumns {
        public static final String TABLE_NAME = "images_table";
        public static final String IMAGE_DATA = "uri";
        public static final String FRAGMENT_COMPONENT_ID = "component_id";
    }
    public static abstract class Favourit implements BaseColumns {
        public static final String TABLE_NAME = "fav_table";
        public static final String FAV_PANT = "_id_pant";
        public static final String FAV_SHIRT = "_id_shirt";
    }

}
