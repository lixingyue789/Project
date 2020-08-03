package Crawler;

import dao.Project;
import dao.ProjectDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Author:lxy1999
 * Created:2020/8/2
 */
public class ThreadCrawler extends Crawler{
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        //使用多线程的方式重新组织核心的逻辑，访问Github API变为并行式
        ThreadCrawler crawler = new ThreadCrawler();
        //1.获取首页内容
        String html = crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
        //2.分析项目列表
        List<Project> projects = crawler.parseProjectList(html);
        long startCallAPI = System.currentTimeMillis();
        //3.遍历项目列表，使用多线程的方式，线程池：
        //ExecutorService ：有两种提交任务的操作
        //（1）execute：不关注任务的结果
        //（2）submit：关注任务的结果
        //此处使用submit最主要是能够知道线程池所有的任务什么时候能够全完成
        //等到全都完成以后在保存数据
        List<Future<?>> taskResults = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for(Project project:projects){
            //一个Future对象可以理解为一个写着分配任务的卡片，有很多卡片的
            //线程池中多个线程分别来执行这些任务
            //传入参数要实现Runnable或者Callable
            Future<?> taskResult = executorService.submit(new CrawlerTask(project,crawler));
            taskResults.add(taskResult);
        }
        //等待所有线程池中的任务执行结束后，在进行下一步操作
        for(Future<?> taskResult:taskResults){
            try {
                //调用get方法会阻塞，阻塞到该任务执行完毕，get才会返回
                taskResult.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //所有的任务已经执行完毕了，结束线程池
        executorService.shutdown();

        long finishCallAPI = System.currentTimeMillis();
        //22s
        System.out.println("调用API的时间为："+(finishCallAPI-startCallAPI)+"ms");


        //把project都存到数据库中
        ProjectDao projectDao = new ProjectDao();
        for(int i = 0;i<projects.size();i++){
            Project project = projects.get(i);
            //把project保存到数据库中
            projectDao.save(project);
        }

        //38s
        System.out.println("整体程序执行时间："+(System.currentTimeMillis()-startTime)+"ms");
    }

    static class CrawlerTask implements Runnable{
        private Project project;
        private ThreadCrawler threadCrawler;

        public CrawlerTask(Project project,ThreadCrawler threadCrawler) {
            this.project = project;
            this.threadCrawler = threadCrawler;
        }

        @Override
        public void run() {
            //依赖两个对象：Project对象、Crawler对象
            //基本步骤：
            try {
                System.out.println("抓取 "+project.getName()+" ...");
                //1.调用API获取项目数据
                String repoName = threadCrawler.getRepoName(project.getUrl());
                String jsonString = threadCrawler.getRepoInfo(repoName);
                //2.解析项目数据
                threadCrawler.parseRepoInfo(jsonString,project);
                System.out.println("抓取 "+project.getName()+" done!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
