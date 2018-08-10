package example.com.pocketnews;

public class NewsItem {

    private String title;
    private String section;
    private String webUrl;
    private String thumbnailUrl;
    private String publishDate;

    NewsItem(String title, String section, String webUrl, String thumbnailUrl, String publishDate) {
        this.title = title;
        this.section = section;
        this.webUrl = webUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.publishDate = publishDate;
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
}
