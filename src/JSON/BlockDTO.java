package JSON;

public class BlockDTO {
    private String content;
    private int position;
    private int x;
    private int y;
    private String colorRGB; // Store color as RGB string "r,g,b" or hex

    public BlockDTO() {
    }

    public BlockDTO(String content, int position, int x, int y, String colorRGB) {
        this.content = content;
        this.position = position;
        this.x = x;
        this.y = y;
        this.colorRGB = colorRGB;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getColorRGB() {
        return colorRGB;
    }

    public void setColorRGB(String colorRGB) {
        this.colorRGB = colorRGB;
    }
}
