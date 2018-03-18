/**
 * Created by User on 17.03.2018.
 */
public class UrlSets {

    private String url;
    private Integer deep;

    public UrlSets(String url, Integer deep) {
        this.url = url;
        this.deep = deep;
    }
    public UrlSets() {}

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDeep(Integer deep) {
        this.deep = deep;
    }

    public String getUrl() {
        return url;
    }

    public Integer getDeep() {
        return deep;
    }

    @Override
    public String toString() {
        return  url+ " " + deep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrlSets)) return false;

        UrlSets urlSets = (UrlSets) o;

        if (getUrl().equals(urlSets.getUrl())){
            return true;
        } else {
            return  false;
        }

    }

    @Override
    public int hashCode() {
        int result = getUrl().hashCode();
        result = 31 * result;
        return result;
    }
}
