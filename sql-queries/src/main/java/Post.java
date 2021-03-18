import java.util.ArrayList;

public class Post {

    public int postId;
    public String text;
    private String colorCode;
    private String postType;
    private ArrayList<Post> linkTo;

    public Post(int postId, String text, String colorCode, String postType) {
        this.postId = postId;
        this.text = text;
        this.colorCode = colorCode;
        this.postType = postType;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
}
