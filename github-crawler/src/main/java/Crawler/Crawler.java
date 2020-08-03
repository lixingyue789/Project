package Crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dao.Project;
import dao.ProjectDao;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Author:lxy1999
 * Created:2020/7/24
 */
public class Crawler {
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Gson gson = new GsonBuilder().create();
    private HashSet<String> urlBlackList = new HashSet<>();

    {
        urlBlackList.add("https://github.com/events");
        urlBlackList.add("https://github.community");
        urlBlackList.add("https://github.com/about");
        urlBlackList.add("https://github.com/pricing");
        urlBlackList.add("https://github.com/contact");
    }

    public static void main(String[] args) throws IOException {
        //信息证书有问题，写一段忽略证书的代码，防止报异常
        try {
            SslUtils.ignoreSsl();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();

        Crawler crawler = new Crawler();
        //1.获取入口页面
        //大约4s
        String html = crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
        //System.out.println(respBody);

        long finishTime = System.currentTimeMillis();
        System.out.println("获取入口页面时间："+(finishTime-startTime)+"ms");

        //2.解析入口页面，获取项目列表
        //大约0.3s
        List<Project> projects = crawler.parseProjectList(html);
        //System.out.println(projects);

        System.out.println("解析入口页面时间："+(System.currentTimeMillis()-finishTime)+"ms");
        finishTime = System.currentTimeMillis();

        //3.遍历项目列表，调用github API获取项目信息
        //大约2min（138s）
        for(int i = 0;i<projects.size();i++){
            try {
                Project project = projects.get(i);
                System.out.println("抓取："+project.getName()+"  ....");
                String repoName = crawler.getRepoName(project.getUrl());
                String jsonString = crawler.getRepoInfo(repoName);
                //System.out.println(jsonString);
                //System.out.println("===============================");
                //解析每个仓库的JSON数据，得到需要的信息
                crawler.parseRepoInfo(jsonString,project);
//            System.out.println(project);
//            System.out.println("=================================");
                System.out.println("抓取"+project.getName()+"  done");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        System.out.println("解析所有项目的时间："+(System.currentTimeMillis()-finishTime)+"ms");
        finishTime = System.currentTimeMillis();

        //把project都存到数据库中
        //大约4s
        ProjectDao projectDao = new ProjectDao();
        for(int i = 0;i<projects.size();i++){
            Project project = projects.get(i);
            //把project保存到数据库中
            projectDao.save(project);
        }

        System.out.println("存储数据库的时间："+(System.currentTimeMillis()-finishTime)+"ms");
        finishTime = System.currentTimeMillis();
        //总时间：147s
        System.out.println("整个项目的总时间："+(finishTime-startTime)+"ms");

    }

    public String getPage(String url) throws IOException {
        //信息证书有问题，写一段忽略证书的代码，防止报异常
        try {
            SslUtils.ignoreSsl();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //OkHttpClient对象，一个程序包含一个就行
        //response、request、call这些每次请求都要创建
        //1.创建一个OKHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request对象，此处的Builder是辅助构造Request对象的类
        Request request = new Request.Builder().url(url).build();
        //3.创建call独对象（此对象负责进行一次网络访问的操作）
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            System.out.println("请求失败！");
            return null;
        }
        return response.body().string();
    }

    public List<Project> parseProjectList(String html) {
        ArrayList<Project> result = new ArrayList<>();
        //使用Jsoup分析一下页面结构，把其中的li标签获取到
        //1.先创建一个document对象（文档对象，对应一个html）
        //相当于把html字符串转换成document对象
        //document相当于一个描述页面的树形结构
        Document document = Jsoup.parse(html);
        //2.使用getElementByTag方法获取所有的li标签
        //elements对象相当于一个集合类，包含了很多element对象，每个element对象对应一个li标签
        Elements elements = document.getElementsByTag("li");
        for (Element li : elements) {
            Elements allLink = li.getElementsByTag("a");
            if (allLink.size() == 0) {
                //li标签中没有包含a标签
                continue;
            }
            //一个项目的li标签中中只有一个a标签
            Element link = allLink.get(0);
//            //输出a标签中的内容
//            System.out.println(link.text());
//            //输出href中的内容
//            System.out.println(link.attr("href"));
//            //输出li标签中的内容
//            System.out.println(li.text());
//            System.out.println("============================");
            String url = link.attr("href");
            if (!url.startsWith("https://github.com")) {
                //如果链接不是以https://github.com开头的就过滤掉
                continue;
            }
//            if(url.equals("https://github.community")||url.equals("https://github.com/events")){
//                continue;
//            }
            if (urlBlackList.contains(url)) {
                continue;
            }
            Project project = new Project();
            project.setName(link.text());
            project.setUrl(link.attr("href"));
            project.setDescription(li.text());
            result.add(project);
        }
        return result;
    }

    //调用github API获取指定仓库的信息
    //repoName 形如： doov-io/doov
    public String getRepoInfo(String repoName) throws IOException {
        String userName = "lixingyue789";
        String password = "lixingle789";
        //进行身份认证
        //针对用户名，密码进行base64加密
        String credential = Credentials.basic(userName, password);
        String url = "https://api.github.com/repos/" + repoName;

        //信息证书有问题，写一段忽略证书的代码，防止报异常
        try {
            SslUtils.ignoreSsl();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //OKHttpClient对象已经创建过了，不需要重复创建
        Request request = new Request.Builder().url(url).header("Authorization", credential).build();
        Call call = okHttpClient.newCall(request);//Authorization
        Response response = call.execute();
        if (!response.isSuccessful()) {
            System.out.println("访问 Github API 失败！ url =" + url);
            return null;
        }
        return response.body().string();
    }

    //把项目中的url提取出其中的仓库名字和作者名字
    public String getRepoName(String url){
        int lastOne = url.lastIndexOf("/");
        int lastTwo = url.lastIndexOf("/",lastOne-1);
        if(lastOne==-1||lastTwo==-1){
            System.out.println("当前URL不是一个标准的url！url："+url);
            return null;
        }
        return url.substring(lastTwo+1);
    }

    //获取该仓库的相关信息
    //第一个参数jsonString表示github API获得的结果
    //第二个参数project表示解析出的star数、fork数、open_issue数保存到project对象中
    public void parseRepoInfo(String jsonString,Project project){
        Type type = new TypeToken<HashMap<String,Object>>(){}.getType();
        HashMap<String,Object> hashMap = gson.fromJson(jsonString,type);
        //HashMap中的key的名字源于github API的返回值
        Double starC = (Double)hashMap.get("stargazers_count");
        int starCount = Double.valueOf(starC).intValue();
        project.setStartCount(starCount);
        Double forkC = (Double) hashMap.get("forks_count");
        int forkCount = Double.valueOf(forkC).intValue();
        project.setForkCount(forkCount);
        Double openIssueC = (Double) hashMap.get("open_issues_count");
        int openIssueCount = Double.valueOf(openIssueC).intValue();
        project.setOpenedIssueCount(openIssueCount);
//        Integer forkCount = (Integer) hashMap.get("forks_count");
//        project.setForkCount(forkCount);
//        Integer openIssueCount = (Integer)hashMap.get("open_issues_count");
//        project.setOpenedIssueCount(openIssueCount);
    }
}

