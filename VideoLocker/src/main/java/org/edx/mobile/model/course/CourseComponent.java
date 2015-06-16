package org.edx.mobile.model.course;

import android.text.TextUtils;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.api.IPathNode;

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
    private BlockType type;
    private String name;
    private boolean graded;
    private boolean responsiveUI;
    private String blockUrl;
    private String webUrl;
    private BlockCount blockCount;
    protected CourseComponent parent;
    protected CourseComponent root;
    protected List<CourseComponent> children = new ArrayList<>();
    private String courseId;
    private String format;

    public CourseComponent(){}

    /**
     *
     * @param blockModel
     * @param parent  is null if and only if this is the root
     */
    public CourseComponent(BlockModel blockModel, CourseComponent parent){
        this.id = blockModel.id;
        this.type = blockModel.type;
        this.name = blockModel.displayName;
        this.graded = blockModel.graded;
        this.blockUrl = blockModel.blockUrl;
        this.webUrl = blockModel.webUrl;
        this.responsiveUI =  blockModel.responsiveUI;
        this.format = blockModel.format;
        this.blockCount = blockModel.blockCount == null ? new BlockCount() : blockModel.blockCount;
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
    public BlockType getType() {
        return type;
    }

    @Override
    public void setType(BlockType type) {
        this.type = type;
    }

    @Override
    public String getDisplayName() {
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


    public boolean isResponsiveUI() {
        return responsiveUI;
    }

    public void setResponsiveUI(boolean responsiveUI) {
        this.responsiveUI = responsiveUI;
    }

    public boolean isContainer(){
        return children != null && children.size() > 0;
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
    public List<HasDownloadEntry> getVideos(){
        List<CourseComponent> videos = new ArrayList<>();
        fetchAllLeafComponents(videos, EnumSet.of(BlockType.VIDEO));
        for (Iterator<CourseComponent> videosIterator = videos.iterator();
                videosIterator.hasNext();) {
            CourseComponent videoComponent = videosIterator.next();
            if (!(videoComponent instanceof VideoBlockModel)) {
                videosIterator.remove();
            }
        }
        return (List)videos;
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

    @Override
    public String getName() {
        return  getDisplayName();
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getCourseId(){
        if( TextUtils.isEmpty(courseId) ){
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
}
