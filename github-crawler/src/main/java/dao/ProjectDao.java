package dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:lxy1999
 * Created:2020/8/2
 */
//负责针对project对象进行存储
public class ProjectDao {
    public void save(Project project){
        //通过save方法就能把一个project对象保存在数据库中
        //1.获取数据库的连接
        Connection connection = DBUtil.getConnection();
        //2.构造PrepareStatement对象拼装SQL语句
        PreparedStatement statement = null;
        String sql = "insert into project_table values(?,?,?,?,?,?,?)";
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1,project.getName());
            statement.setString(2,project.getUrl());
            statement.setString(3,project.getDescription());
            statement.setInt(4,project.getStartCount());
            statement.setInt(5,project.getForkCount());
            statement.setInt(6,project.getOpenedIssueCount());
            //根据simpleDateFormat类来完成
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            statement.setString(7,simpleDateFormat.format(System.currentTimeMillis()));
            //3.执行SQL语句，完成数据库的插入操作
            int ret = statement.executeUpdate();
            if(ret!=1){
                System.out.println("数据库执行插入数据错误");
                return;
            }
            System.out.println("数据插入成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,null);
        }
    }

    public List<Project> selectProjectByDate(String date){
        List<Project> projects = new ArrayList<>();
        //1.获取数据库的连接
        Connection connection = DBUtil.getConnection();
        //2.拼装SQL语句
        String sql = "select name,url,starCount,forkCount,openedIssueCount from project_table"+
                "where date = ? order by starCount desc";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1,date);
            resultSet = statement.executeQuery();
            while (resultSet.next()){
                Project project = new Project();
                project.setName(resultSet.getString("name"));
                project.setUrl(resultSet.getString("url"));
                project.setStartCount(resultSet.getInt("starCount"));
                project.setForkCount(resultSet.getInt("forkCount"));
                project.setOpenedIssueCount(resultSet.getInt("openedIssueCount"));
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return projects;
    }
}
