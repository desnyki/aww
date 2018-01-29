package com.desnyki.aww.data.source.local;

import android.provider.BaseColumns;

/**
 * Created by desnyki on 25/01/18.
 */

public class PostsPersistenceContract {
// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PostsPersistenceContract() {}

    /* Inner class that defines the table contents */
    public static abstract class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "post";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_COMMENT_COUNT = "comment_count";
        public static final String COLUMN_NAME_UPVOTE_COUNT = "upvote_count";
    }
}
