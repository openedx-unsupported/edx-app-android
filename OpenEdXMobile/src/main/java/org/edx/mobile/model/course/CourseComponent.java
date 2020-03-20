package org.edx.mobile.model.course;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.api.AuthorizationDenialReason;
import org.edx.mobile.model.api.IPathNode;
import org.edx.mobile.util.VideoUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Default implementation of IBlock
 */
public class CourseComponent implements IBlock, IPathNode {
    protected final static Logger logger = new Logger(CourseComponent.class.getName());
    private String id;
    private String blockId;
    private BlockType type;
    private String name;
    private boolean graded;
    private boolean multiDevice;
    private String blockUrl;
    private String webUrl;
    private BlockCount blockCount;
    protected CourseComponent parent;
    protected CourseComponent root;
    protected List<CourseComponent> children = new ArrayList<>();
    private String courseId;
    private String format;
    private String dueDate;
    private String authorizationDenialMessage;
    private AuthorizationDenialReason authorizationDenialReason;

    public CourseComponent() {
    }

    public CourseComponent(@NonNull CourseComponent other) {
        this.id = other.id;
        this.blockId = other.blockId;
        this.type = other.type;
        this.name = other.name;
        this.graded = other.graded;
        this.multiDevice = other.multiDevice;
        this.blockUrl = other.blockUrl;
        this.webUrl = other.webUrl;
        this.blockCount = other.blockCount;
        this.parent = null;
        this.root = new CourseComponent();
        this.root.courseId = other.root.courseId;
        this.children = other.children;
        this.courseId = other.courseId;
        this.format = other.format;
        this.dueDate = other.dueDate;
        this.authorizationDenialMessage = other.authorizationDenialMessage;
        this.authorizationDenialReason = other.authorizationDenialReason;
    }

    /**
     *
     * @param blockModel
     * @param parent  is null if and only if this is the root
     */
    public CourseComponent(BlockModel blockModel, CourseComponent parent){
        this.id = blockModel.id;
        this.blockId = blockModel.blockId;
        this.type = blockModel.type;
        this.name = blockModel.displayName;
        this.graded = blockModel.graded;
        this.blockUrl = blockModel.studentViewUrl;
        this.webUrl = blockModel.lmsWebUrl;
        this.multiDevice =  blockModel.studentViewMultiDevice;
        this.format = blockModel.format;
        this.dueDate = blockModel.dueDate;
        this.authorizationDenialMessage = blockModel.authorizationDenialMessage;
        this.authorizationDenialReason = blockModel.authorizationDenialReason;
        this.blockCount = blockModel.blockCounts == null ? new BlockCount() : blockModel.blockCounts;
        this.parent = parent;
        if ( parent == null){
            this.root = this;
        } else {
            parent.getChildren().add(this);
            //we cache the root to improve the performance
            this.root = (CourseComponent)parent.getRoot();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getBlockId() {
        return blockId;
    }

    @Override
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    @Override
    public BlockType getType() {
        return type;
    }

    @Override
    public void setType(BlockType type) {
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        if (TextUtils.isEmpty(name)) {
            return MainApplication.instance().getString(R.string.untitled_block);
        }
        return name;
    }

    @Override
    public void setDisplayName(String name) {
        this.name = name;
    }

    @Override
    public boolean isGraded() {
        return graded;
    }

    @Override
    public void setGraded(boolean graded) {
        this.graded = graded;
    }

    @Override
    public String getBlockUrl() {
        return blockUrl;
    }

    @Override
    public void setBlockUrl(String url) {
        this.blockUrl = url;
    }

    @Override
    public String getWebUrl() {
        return webUrl;
    }

    @Override
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @Override
    public BlockCount getBlockCount() {
        return blockCount;
    }

    @Override
    public void setBlockCount(BlockCount count) {
        this.blockCount = blockCount;
    }

    @Override
    public CourseComponent getParent() {
        return parent;
    }

    @Override
    public List<IBlock> getChildren() {
        return (List) children;
    }

    @Override
    public CourseComponent getRoot(){
        return root;
    }


    public boolean isMultiDevice() {
        return multiDevice;
    }

    public void setMultiDevice(boolean multiDevice) {
        this.multiDevice = multiDevice;
    }

    public boolean isContainer(){
        return type != null ? type.isContainer() : (children != null && children.size() > 0);
    }

    /**
     * get direct children who have child.  it is not based on the block type, but on
     * the real tree structure.
     * @return
     */
    public List<CourseComponent> getChildContainers(){
        List<CourseComponent> childContainers = new ArrayList<>();
        if ( children != null ){
            for(CourseComponent c : children){
                if ( c.isContainer() )
                    childContainers.add(c);
            }
        }
        return childContainers;
    }

    /**
     * get direct children who is leaf.  it is not based on the block type, but on
     * the real tree structure.
     * @return
     */
    public List<CourseComponent> getChildLeafs(){
        List<CourseComponent> childLeafs = new ArrayList<>();
        if ( children != null ){
            for(CourseComponent c : children){
                if ( !c.isContainer() )
                    childLeafs.add(c);
            }
        }
        return childLeafs;
    }

    /**
     * recursively find the first node by matcher. return null if get nothing.
     */
    public CourseComponent find(Filter<CourseComponent> matcher){
        if ( matcher.apply(this ) )
            return this;
        if ( !isContainer() )
            return null;
        CourseComponent found = null;
        for(CourseComponent c : children){
            found = c.find(matcher);
            if ( found != null )
                return found;
        }
        return null;
    }

    /**
     * return all videos blocks under this node
     */
    public List<VideoBlockModel> getVideos() {
        return (List<VideoBlockModel>) (List) getVideos(false);
    }

    /**
     * return all the downloadable videos blocks under this node
     */
    public List<CourseComponent> getVideos(boolean downloadableOnly) {
        List<CourseComponent> videos = new ArrayList<>();
        fetchAllLeafComponents(videos, EnumSet.of(BlockType.VIDEO));
        // Confirm that these are actually VideoBlockModel instances.
        // This is necessary because if for some reason the data is null,
        // then the block is represented as an HtmlBlockModel, even if
        // the type is video. This should not actually happen in practice
        // though; this is just a safeguard to handle that unlikely case.
        for (Iterator<CourseComponent> videosIterator = videos.iterator();
             videosIterator.hasNext(); ) {
            CourseComponent videoComponent = videosIterator.next();
            if (!(videoComponent instanceof VideoBlockModel) ||
                    downloadableOnly && videoComponent.getDownloadableVideosCount() == 0) {
                // Remove a video component if its not downloadable when we're only looking for the
                // ones that are downloadable
                videosIterator.remove();
            }
        }
        return videos;
    }

    /**
     * @return count of videos that have encoded files available
     * and {@link VideoData#onlyOnWeb} set to <code>false</code>
     */
    public int getDownloadableVideosCount() {
        int downloadableCount = 0;
        List<VideoBlockModel> videos = getVideos();
        for (VideoBlockModel video : videos) {
            if (VideoUtil.isVideoDownloadable(video.getData())) {
                downloadableCount++;
            }
        }
        return downloadableCount;
    }

    /**
     * used for navigation.
     * @return <code>true</code> if it is the last child of direct parent. or it does not has direct parent
     *         <code>false</code> if it is not
     */
    public boolean isLastChild(){
        if ( parent == null )
            return true;
        List<IBlock> sibling = parent.getChildren();
        if ( sibling == null ) {
            return false;  //it wont happen. TODO - should we log here?
        }
        return sibling.indexOf(this) == sibling.size() -1;
    }

    /**
     * we get all the leaves below this node.  if this node itself is leaf,
     * just add it to list
     * @param leaves
     */
    public void fetchAllLeafComponents(List<CourseComponent> leaves, EnumSet<BlockType> types){
         if ( !isContainer() && types.contains(type)){
             leaves.add(this);
         } else {
             for( CourseComponent comp : children ){
                 comp.fetchAllLeafComponents(leaves, types);
             }
         }
    }

    /**
     * get the ancestor based on level, level = 0, means itself.
     * if level is out of the boundary, just return the toppest one
     * @param level
     * @return it will never return null.
     */
    public CourseComponent getAncestor(int level){
        if ( parent == null || level == 0 )
            return this;

        IBlock root = parent;
        while ( level != 0  && root.getParent() != null ){
            root = root.getParent();
            level--;
        }
        return (CourseComponent)root;
    }

    /**
     * get ancestor with give blockType, starting from itself
     */
    public CourseComponent getAncestor(EnumSet<BlockType> types){
        if( types.contains(type) )
            return this;
        IBlock ancestor = parent;
        if ( ancestor == null )
            return null;
        do{
           if ( types.contains( ancestor.getType() ) )
               return (CourseComponent) ancestor;
        }while ((ancestor = ancestor.getParent()) != null );
        return null;
    }

    @Override
    public boolean equals(Object obj){
        if ( obj == null || !(obj instanceof CourseComponent) )
            return false;
        CourseComponent other = (CourseComponent)obj;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode(){
        return this.id.hashCode();
    }


    //// implement IPathNode interface, for backward compatibility only
    @Override
    public boolean isChapter() {
        return  getType() == BlockType.CHAPTER;
    }

    @Override
    public boolean isSequential() {
        return  getType() == BlockType.SEQUENTIAL;
    }

    @Override
    public boolean isVertical() {
        return  getType() == BlockType.VERTICAL;
    }

    @Override
    public String getCategory() {
        return  getType().name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Not meant to be user facing. See {@link #getDisplayName()}
     */
    public String getInternalName() {
        return name;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    public String getDueDate() {
        return dueDate;
    }

    @Override
    public String getCourseId(){
        if( courseId == null || courseId.length() == 0 ){
            //root should always has a course id, add the check to avoid loop
            if ( root == this ){
                logger.debug( "root does not has a course id set!!! for " + id);
                return "";
            }
            return root.getCourseId();
        } else {
            return courseId;
        }
    }
    @Override
    public void setCourseId(String courseId){
        this.courseId = courseId;
    }

    /**
     * calculate and construct a Path object
     */
    public BlockPath getPath(){
        BlockPath path = new BlockPath();
        path.addPathNodeToPathFront(this);
        CourseComponent nodeAbove = parent;
        while ( nodeAbove != null ){
            path.addPathNodeToPathFront(nodeAbove);
            nodeAbove = nodeAbove.getParent();
        }
        return path;
    }

    public static CourseComponent getCommonAncestor(CourseComponent node1, CourseComponent node2){
        List<CourseComponent> path1 = node1.getPath().getPath();
        List<CourseComponent> path2 = node2.getPath().getPath();
        if ( path1.isEmpty() || path2.isEmpty() )
            return null;
        for(int i = path1.size() -1; i >=0; i --){
            CourseComponent comp1 = path1.get(i);
            for(int j = path2.size() -1; j >=0; j --){
                CourseComponent comp2 = path2.get(j);
                if (comp1.equals(comp2))
                    return comp1;
            }
        }
        return null;
    }

    public AuthorizationDenialReason getAuthorizationDenialReason() {
        return authorizationDenialReason;
    }
}
