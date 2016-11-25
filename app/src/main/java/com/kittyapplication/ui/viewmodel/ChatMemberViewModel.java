package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.kittyapplication.model.storeg.ChatGroupMember;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.sqlitedb.MySQLiteHelper;

import java.util.ArrayList;

/**
 * Created by MIT on 10/5/2016.
 */
public class ChatMemberViewModel extends KittyViewModel{
    private final Context context;

    public ChatMemberViewModel(Context context) {
        super(context);
        this.context = context;
    }

    public void addMember(ChatGroupMember member) {
        ContentValues values = new ContentValues();
        values.put(KittyBeeContract.GroupChatMember.MEMBER_ID, member.getMemberId());
        values.put(KittyBeeContract.GroupChatMember.MEMBER_NAME, member.getMemberName());
        values.put(KittyBeeContract.GroupChatMember.MEMBER_NUMBER, member.getMemberNumber());
        values.put(KittyBeeContract.GroupChatMember.MEMBER_IMAGE, member.getImage());
        System.out.println("addMember::" + member.toString());
        context.getContentResolver().insert(KittyBeeContract.GroupChatMember.CONTENT_URI, values);
    }

    public ArrayList<ChatGroupMember> getMembers(String dialogId) {
        ArrayList<ChatGroupMember> members = new ArrayList<>();
        Uri uri = KittyBeeContract.GroupChatMember.CONTENT_URI;

        String selection = KittyBeeContract.GroupChatMember.GROUP_ID + "=?";
        String[] selectionArg = new String[]{dialogId};

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                uri,                      // the URI to query
                KittyBeeContract.GroupChatMember.PROJECTION_ALL,   // the projection to use
                selection,                           // the where clause without the WHERE keyword
                selectionArg,                           // any wildcard substitutions
                null);                          // the sort order without the SORT BY keyword

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                //TODO if get String grt group id than use get string 2
                members.add(bindMember(cursor));
            }
            cursor.close();
        }
        MySQLiteHelper.getInstance(context).close();
        return members;
    }

    public ChatGroupMember getMember(int memberId) {
        ChatGroupMember member = new ChatGroupMember();
        Uri uri = KittyBeeContract.GroupChatMember.CONTENT_URI;

        String selection = KittyBeeContract.GroupChatMember.MEMBER_ID + "=?";
        String[] selectionArg = new String[]{"" + memberId};
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                uri,                      // the URI to query
                KittyBeeContract.GroupChatMember.PROJECTION_ALL,   // the projection to use
                selection,                           // the where clause without the WHERE keyword
                selectionArg,                           // any wildcard substitutions
                null);                          // the sort order without the SORT BY keyword

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                //TODO if get String grt group id than use get string 2
                member = bindMember(cursor);
            }
            cursor.close();
        }
        MySQLiteHelper.getInstance(context).close();
        return member;
    }

    private ChatGroupMember bindMember(Cursor cursor) {
        ChatGroupMember member = new ChatGroupMember();
        member.setId(cursor.getInt(0));
        member.setMemberId(cursor.getInt(1));
        member.setMemberName(cursor.getString(2));
        member.setMemberNumber(cursor.getString(3));
        member.setImage(cursor.getBlob(4));
        return member;
    }
}
