package org.edx.mobile.model.mocked;

/**
 *  NOTE : the whole class should be moved to unit test package after
 *  server side api is ready
 */
public class MockedCourseOutlineProvider {
    public String getCourseOutline(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append( getCourseOutlineLeaf( "Chapter 1: introduction", "cid-1", "Background", "sqid-1", "v1", "vid-1", "npn-1", "video", "a video 1"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 1: introduction", "cid-1", "Background", "sqid-1", "v1", "vid-1", "npn-2", "problem", "a problem 2"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 1: introduction", "cid-1", "Background", "sqid-1", "v1", "vid-1", "npn-3", "video", "a video 3"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 1: introduction", "cid-1", "It's me", "sqid-2", "v2", "vid-2", "npn-4", "html", "a html 4"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 1: introduction", "cid-1", "It's me", "sqid-2", "v2", "vid-2", "npn-5", "video", "a video 5"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Set up", "sqid-3", "v3", "vid-3", "npn-6", "problem", "a problem 6"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Stand up", "sqid-3", "v3", "vid-3", "npn-7", "video", "a video 7"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Walkout", "sqid-4", "v4", "vid-4", "npn-8", "problem", "a problem 6"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Walkout", "sqid-4", "v4", "vid-4", "npn-9", "video", "a video 7"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Burnout", "sqid-5", "v5", "vid-5", "npn-10", "problem", "a problem 6"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Burnout", "sqid-5", "v5", "vid-5", "npn-11", "video", "a video 7"));

        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Knock Knock", "sqid-6", "v6", "vid-6", "npn-12", "problem", "a problem 6"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Knock Knock", "sqid-6", "v6", "vid-6", "npn-13", "video", "a video 7"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Blacksheepwall", "sqid-7", "v7", "vid-7", "npn-14", "problem", "a problem 6"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "Blacksheepwall", "sqid-7", "v7", "vid-7", "npn-15", "video", "a video 7"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "show me the money", "sqid-8", "v8", "vid-8", "npn-16", "problem", "a problem 6"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 2: hello world", "cid-2", "show me the money", "sqid-8", "v8", "vid-8", "npn-17", "video", "a video 7"));


        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 3: happy ending", "cid-3", "Happy forever", "sqid-9", "v9", "vid-9", "npn-18", "problem", "a problem 6"));
        sb.append(",");
        sb.append( getCourseOutlineLeaf( "Chapter 3: happy ending", "cid-3", "Dream", "sqid-10", "v9", "vid-10", "npn-19", "video", "a video 7"));
        sb.append("]");
        return sb.toString();
    }


    public String getCourseOutlineLeaf(String chapterName, String chapterId,
                                        String seqName, String seqId,
                                        String verticalName, String verticalId,
                                        String namedPathName, String category, String leafName){
        return  "{" +
            "  \"section_url\": \"http://some-url-for-section/\"," +
            "  \"path\": [" +
            "  {" +
            "       \"category\": \"chapter\"," +
            "        \"name\": \"" + chapterName + "\"," +
            "        \"id\": \"" + chapterId + "\" " +
            "    }," +
            "    {" +
            "        \"category\": \"sequential\"," +
            "       \"name\": \"" + seqName + "\"," +
            "        \"id\": \"" + seqId + "\"" +
            "    }," +
            "    {" +
            "        \"category\": \"vertical\"," +
            "        \"name\": \"" + verticalName + "\"," +
            "        \"id\": \"" + verticalId + "\"" +
            "    }" +
            "    ]," +
            "    \"unit_url\": \"https://some-url-for-vertical\"," +
            "    \"named_path\": [" +
            "    \"Overview\"," +
            "        \"edX Tutorial\"," +
            "        \"" + namedPathName + "\"" +
            "    ]," +
            "    \"summary\": {" +
            "    \"category\": \"" + category + "\"," +
            "        \"name\": \"" + leafName + "\"," +
            "        \"video_url\": \"\"," +
            "        \"language\": \"en\"," +
            "        \"encoded_videos\": null," +
            "        \"video_thumbnail_url\": null," +
            "        \"only_on_web\": false," +
            "        \"duration\": null," +
            "        \"transcripts\": {" +
            "        \"en\": \"https://some-url-for-leaf\"" +
            "    }," +
            "    \"id\": \"i4x://MITx/6.002_4x/video/edx_toplevelnav\"," +
            "        \"size\": 0" +
            " }" +
            " } " ;
    }
}
