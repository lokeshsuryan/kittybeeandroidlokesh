package com.kittyapplication.model;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/10/16.
 */

public class DiaryDao {

    private ChatData chatData;
    private DiaryResponseDao diaryData;

    public ChatData getChatData() {
        return chatData;
    }

    public void setChatData(ChatData chatData) {
        this.chatData = chatData;
    }

    public DiaryResponseDao getDiaryData() {
        return diaryData;
    }

    public void setDiaryData(DiaryResponseDao diaryData) {
        this.diaryData = diaryData;
    }
}
