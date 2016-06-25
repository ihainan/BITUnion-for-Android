package me.ihainan.bu.app.ui.assist;

import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;

import org.xml.sax.XMLReader;

public class HtmlTagHandler implements Html.TagHandler {

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equals("strike") || tag.equals("s") || tag.equals("del")) {
            SpannableStringBuilder text = (SpannableStringBuilder) output;
            if (opening) start(text, new Strike());
            else {
                end(text, Strike.class, new StrikethroughSpan());
            }
        }
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int length = text.length();
        text.setSpan(mark, length, length, Spanned.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, Class kind, Object newSpan) {
        int length = text.length();
        Object span = getLast(text, kind);  // 获取最后一个 Span
        int where = text.getSpanStart(span);
        text.removeSpan(span);
        if (where != length) {
            text.setSpan(newSpan, where, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static Object getLast(SpannableStringBuilder text, Class kind) {
        int length = text.length();
        Object[] spans = text.getSpans(0, length, kind);
        return spans.length > 0 ? spans[spans.length - 1] : null;
    }
}

class Strike {
}