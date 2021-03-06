package example.com.pocketnews.model;

public class NewsItem {

    private String title;
    private String section;
    private String webUrl;
    private String thumbnailUrl;
    private String publishDate;
    private String authorName;

    public NewsItem(String title, String section, String webUrl, String thumbnailUrl, String publishDate, String authorName) {
        this.title = title;
        this.section = section;
        this.webUrl = webUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.publishDate = publishDate;
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getSection() {
        return section;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public String getAuthorName() {
        return authorName;
    }
}
