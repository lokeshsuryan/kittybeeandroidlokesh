package com.kittyapplication.providers;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.kittyapplication.sqlitedb.SQLConstants;

/**
 * Created by Scorpion on 17-09-2016.
 */
public class KittyBeeContract {
    /**
     * The authority of the kittybee provider.
     */
    public static final String AUTHORITY = "com.kittybee.database.provider";
    /**
     * The content URI for the top-level kittybee authority.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    /**
     * A selection clause for ID based queries.
     */
    public static final String SELECTION_ID_BASED = BaseColumns._ID + " = ? ";

    /**
     * Constants for the Group table of the KittyBee provider.
     */
    public static final class Groups implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "groups");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.groups";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.groups";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Rules table of the KittyBee provider.
     */
    public static final class Rules implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "rules");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.rules";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.rules";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Group table of the KittyBee provider.
     */
    public static final class Diaries implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "diaries");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.diaries";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.diaries";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Group table of the KittyBee provider.
     */
    public static final class Summary implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "summary");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.summary";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.summary";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Bill table of the KittyBee provider.
     */
    public static final class Bill implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "bills");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.bills";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.bills";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {SQLConstants.KEY_GROUP_ID,
                SQLConstants.KEY_KITTY_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Group table of the KittyBee provider.
     */
    public static final class Attendance implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "attendance");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.attendance";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.attendance";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {SQLConstants.KEY_GROUP_ID,
                SQLConstants.KEY_KITTY_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Group table of the KittyBee provider.
     */
    public static final class Venue implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "venue");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.venue";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.venue";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Personal Note table of the KittyBee provider.
     */
    public static final class KittyPersonalNote implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "kittypersonalnotes");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.kittypersonalnotes";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.kittypersonalnotes";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, NOTE_ID, TIMESTAMP};
    }

    /**
     * Constants for the Personal Note table of the KittyBee provider.
     */
    public static final class PersonalNote implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "personalnotes");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.personalnotes";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.personalnotes";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, NOTE_ID, TIMESTAMP};
    }

    /**
     * Constants for the Personal Note table of the KittyBee provider.
     */
    public static final class KittyNotes implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "kittynotes");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.kittynotes";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.kittynotes";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, NOTE_ID, TIMESTAMP};
    }

    /**
     * Constants for the Bill table of the KittyBee provider.
     */
    public static final class UpdateDiary implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "diariesupdate");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.diariesupdate";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.diariesupdate";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the Group table of the KittyBee provider.
     */
    public static final class Setting implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "settings");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.settings";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.settings";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

    /**
     * Constants for the QBDialog table of the KittyBee provider.
     */
    public static final class QBDialog implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, "qbdialogs");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee.qbdialogs";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee.qbdialogs";
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
    }

//    /**
//     * Constants for the QBDialog table of the KittyBee provider.
//     */
//    public static final class Kitties implements CommonColumns {
//        /**
//         * The content URI for this table.
//         */
//        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
//                CONTENT_URI, "kitties");
//        /**
//         * The mime type of a directory of items.
//         */
//        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
//                + "/vnd.kittybee.kitties";
//        /**
//         * The mime type of a single item.
//         */
//        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
//                + "/vnd.kittybee.kitties";
//        /**
//         * A projection of all columns in the items table.
//         */
//        public static final String[] PROJECTION_ALL = {_ID, DATA, TIMESTAMP};
//    }

    /**
     * Constants for the QBDialog table of the KittyBee provider.
     */
    public static final class Kitties implements CommonColumns {
        public static final String CLEAR_MESSAGE = "clear_message";
        public static final String DELETED = "deleted";
        public static final String PATH = "kitties";
        public static final String QB_DIALOG = "qb_dialog";
        public static final String GROUP = "_group";
        public static final String QB_DIALOG_ID = "qb_dialog_id";
        public static final String SYNCABLE = "syncable";
//        public static final String IS_PRIVATE = "is_private";
        public static final String PRIVATE_CHAT_MEMBER_ID = "private_chat_member_id";
        public static final String LAST_MESSAGE_PAGE_NO = "last_message_page_no";
        public static final String QB_LAST_MSG_TIMESTAMP = "last_msg_time";
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, PATH);
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee." + PATH;
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee." + PATH;
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                GROUP_ID,
                QB_DIALOG_ID,
                GROUP,
                QB_DIALOG,
                CLEAR_MESSAGE,
                DELETED,
                SYNCABLE,
                LAST_MESSAGE_PAGE_NO,
                QB_LAST_MSG_TIMESTAMP,
                PRIVATE_CHAT_MEMBER_ID,
                TIMESTAMP
        };
    }

    /**
     * Constants for the Personal Note table of the KittyBee provider.
     */
    public static final class ChatMessage implements CommonColumns {
        public static final String SENT = "sent";
        public static final String FAIL = "fail";
        public static final String READ = "read";
        public static final String DELIVERED = "delivered";
        public static final String DELETED = "deleted";
        public static final String MESSAGE_ID = "chatId";
        public static final String PATH = "message";
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, PATH);
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee." + PATH;
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee." + PATH;
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                KITTY_ID,
                MESSAGE_ID,
                DATA,
                SENT,
                READ,
                DELIVERED,
                DELETED,
                TIMESTAMP
        };
    }

    /**
     * Constants for the Personal Note table of the KittyBee provider.
     */
    public static final class GroupChatMember implements CommonColumns {
        public static final String MEMBER_ID = "memberId";
        public static final String MEMBER_NUMBER = "memberNumber";
        public static final String MEMBER_NAME = "memberName";
        public static final String MEMBER_IMAGE = "memberImage";
        public static final String PATH = "group_chat_member";
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KittyBeeContract.
                CONTENT_URI, PATH);
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.kittybee." + PATH;
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.kittybee." + PATH;
        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                MEMBER_ID,
                MEMBER_NAME,
                MEMBER_NUMBER,
                MEMBER_IMAGE,
                TIMESTAMP
        };
    }

    /**
     * This interface defines common columns found in multiple tables.
     */
    public interface CommonColumns extends BaseColumns {
        /**
         * The group id of the item.
         */
        String GROUP_ID = "group_id";

        /**
         * The data the item.
         */
        String DATA = "_data";

        /**
         * The kitty id of the item.
         */
        String KITTY_ID = "kitty_id";

        String TIMESTAMP = "journal_timestamp";

        String NOTE_ID = "note_id";

        String DELETED = "deleted";

        String UPDATED = "updated";
    }
}
