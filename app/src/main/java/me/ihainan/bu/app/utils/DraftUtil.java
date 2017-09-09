package me.ihainan.bu.app.utils;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ihainan.bu.app.models.Draft;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 草稿箱工具类
 */

public class DraftUtil {
    public final static String DRAFT_LIST_TAG = "BU_DRAFT_LIST_TAG";

    public static List<Draft> getDraftList(Context context) throws IOException {
        String jsonStr = BUApplication.getCache(context).getAsString(DRAFT_LIST_TAG);
        List<Draft> draftList = new ArrayList<>();
        if (jsonStr != null && !"".equals(jsonStr)) {
            draftList = BUApi.MAPPER.readValue(jsonStr,
                    new TypeReference<List<Draft>>() {
                    });
        }

        return draftList;
    }

    public static List<Draft> getDraftList(Context context, String action) throws IOException {
        List<Draft> draftList = getDraftList(context);
        List<Draft> newDraftList = new ArrayList<>();
        if (action == null || action.equals("")) return newDraftList;
        for (int i = 0; i < draftList.size(); ++i) {
            if (action.equals(draftList.get(i).action)) newDraftList.add(draftList.get(i));
        }
        return newDraftList;
    }

    public static void saveDraft(Context context, Draft draft) throws IOException {
        List<Draft> draftList = getDraftList(context);
        draftList.add(0, draft);

        Gson gson = new GsonBuilder().create();
        JsonArray jsArray = gson.toJsonTree(draftList).getAsJsonArray();
        BUApplication.getCache(context).put(DRAFT_LIST_TAG, jsArray.toString());
    }

    public static void removeDraft(Context context) throws IOException {
        List<Draft> draftList = getDraftList(context);

        draftList.clear();

        Gson gson = new GsonBuilder().create();
        JsonArray jsArray = gson.toJsonTree(draftList).getAsJsonArray();
        BUApplication.getCache(context).put(DRAFT_LIST_TAG, jsArray.toString());
    }

    public static void removeDraft(Context context, Draft draft) throws IOException {
        List<Draft> draftList = getDraftList(context);

        for (int i = 0; i < draftList.size(); ++i) {
            if (draftList.get(i).id.equals(draft.id)) {
                draftList.remove(i);
                break;
            }
        }

        Gson gson = new GsonBuilder().create();
        JsonArray jsArray = gson.toJsonTree(draftList).getAsJsonArray();
        BUApplication.getCache(context).put(DRAFT_LIST_TAG, jsArray.toString());
    }
}
