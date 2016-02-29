package bit.ihainan.me.bitunionforandroid.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlUtilTest {

    @Test
    public void testReplaceImage() throws Exception {

    }

    @Test
    public void testParseLocalImage() throws Exception {

    }

    @Test
    public void testReplaceBase() throws Exception {

    }

    @Test
    public void testReplaceQuote() throws Exception {

    }

    @Test
    public void testReplaceDel() throws Exception {

    }

    @Test
    public void testReplaceLastEdit() throws Exception {

    }

    @Test
    public void testReplaceOther() throws Exception {

    }

    @Test
    public void testUbbToHtml() throws Exception {

    }

    @Test
    public void testReplaceUrl() throws Exception {
        String ubbCode = "[url=http://www.baidu.com]百度[/url]";
        String result = HtmlUtil.replaceUrl(ubbCode);
        assertEquals(result, "<a href='http://www.baidu.com'>百度</a>");
    }

    @Test
    public void testReplaceEmotion() throws Exception {
        String ubbCode = ":sweating::sad: :shocked: : smile: :test: :: :bz_71:::::";
        String result = HtmlUtil.replaceEmotion(ubbCode);
        assertEquals(result, "<img src=\"../images/smilies/sweatingbullets.gif\" align=\"absmiddle\" border=\"0\"><img src=\"../images/smilies/sad.gif\" align=\"absmiddle\" border=\"0\"> <img src=\"../images/smilies/shocked.gif\" align=\"absmiddle\" border=\"0\"> : smile: :test: :: <img src=\"../images/bz/71.gif\" align=\"absmiddle\" border=\"0\">::::");
    }
}