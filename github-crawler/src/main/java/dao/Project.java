package dao;

/**
 * Author:lxy1999
 * Created:2020/7/30
 */
public class Project {
    //项目名字
    private String name;
    //项目主页链接
    private String url;
    //项目描述信息
    private String description;

    //以下属性是需要统计的信息
    //需要根据该项目的url进入到对应页面，从页面上获取属性
    private int startCount;
    private int forkCount;
    private int openedIssueCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStartCount() {
        return startCount;
    }

    public void setStartCount(int startCount) {
        this.startCount = startCount;
    }

    public int getForkCount() {
        return forkCount;
    }

    public void setForkCount(int forkCount) {
        this.forkCount = forkCount;
    }

    public int getOpenedIssueCount() {
        return openedIssueCount;
    }

    public void setOpenedIssueCount(int openedIssueCount) {
        this.openedIssueCount = openedIssueCount;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", startCount=" + startCount +
                ", forkCount=" + forkCount +
                ", openedIssueCount=" + openedIssueCount +
                '}';
    }
}
