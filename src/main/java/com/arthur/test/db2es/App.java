package com.arthur.test.db2es;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.StringUtils;

/**
 * Hello world!
 *
 */
public class App 
{
	 private static Connection conn = null;
	    private static PreparedStatement sta=null;
	    static{
	        try {
	            Class.forName("com.mysql.jdbc.Driver");
	            conn = DriverManager.getConnection("jdbc:mysql://192.192.18.66:8066/tpm?useUnicode=true&amp;characterEncoding=UTF8", "root", "123456");
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        System.out.println("Database connection established");
	    }
	    
	    /**
	     * 把查到的数据格式化写入到文件
	     *
	     * @param list 需要存储的数据
	     * @param index 索引的名称
	     * @param type 类型的名称
	     * @param path 文件存储的路径
	     **/
	     public static void writeTable(List<Map> list,String index,String type,String path) throws SQLException, IOException {
	         System.out.println("开始写文件");
	         if(StringUtils.isNullOrEmpty(path)){
	        	 path=System.getProperty("user.dir")+"/bigdata.json";
	         }
	         File file = new File(path);
	         int count = 0;
	         int size = list.size();
	         for(Map map : list){
	        	 FileUtils.write(file,  "{ \"index\" : { \"_index\" : \""+index+"\", \"_type\" : \""+type+"\" } }\n","UTF-8",true);
	        	 FileUtils.write(file, JSON.toJSONString(map)+"\n","UTF-8",true);
	         }
	         System.out.println("写入完成");
	     }
	     

	     /**
	      * 读取数据
	      * @param sql
	      * @return
	      * @throws SQLException
	      */
	     public static List<Map> readTable(String tablename,int start,int end) throws SQLException {
	         System.out.println("开始读数据库");
	         //执行查询
	         sta = conn.prepareStatement("select * from "+tablename+" LIMIT "+start+","+end);
	         ResultSet rs = sta.executeQuery();

	         //获取数据列表
	         List<Map> data = new ArrayList();
	         List<String> columnLabels = getColumnLabels(rs);

	         Map<String, Object> map = null;
	         while(rs.next()){
	             map = new HashMap<String, Object>();

	             for (String columnLabel : columnLabels) {
	                 Object value = rs.getObject(columnLabel);
	                 map.put(columnLabel.toLowerCase(), value);
	             }
	             data.add(map);
	         }
	         sta.close();
	         System.out.println("数据读取完毕");
	         return data;
	     }
	     
	     /**
	      * 获得列名
	      * @param resultSet
	      * @return
	      * @throws SQLException
	      */
	     private static List<String> getColumnLabels(ResultSet resultSet)
	             throws SQLException {
	         List<String> labels = new ArrayList<String>();

	         ResultSetMetaData rsmd = (ResultSetMetaData) resultSet.getMetaData();
	         for (int i = 0; i < rsmd.getColumnCount(); i++) {
	             labels.add(rsmd.getColumnLabel(i + 1));
	         }

	         return labels;
	     }
	     /**
	     * 获得数据库表的总数，方便进行分页
	     *
	     * @param tablename 表名
	     */
	     public static int count(String tablename) throws SQLException {
	         int count = 0;
	         Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	         ResultSet rs = stmt.executeQuery("select count(1) from "+tablename);
	         while (rs.next()) {
	             count = rs.getInt(1);
	         }
	         System.out.println("Total Size = " + count);
	         rs.close();
	         stmt.close();
	         return count;
	     }
	     
	     /**
	      * 执行查询，并持久化文件
	      * 
	      * @param tablename 导出的表明
	      * @param page 分页的大小
	      * @param path 文件的路径
	      * @param index 索引的名称
	      * @param type 类型的名称
	      * @return
	      * @throws SQLException
	      */
	     public static void readDataByPage(String tablename,int page,String path,String index,String type) throws SQLException, IOException {
	         int count = count(tablename);
	         int i =0;
	         for(i =0;i<count;){
	             List<Map> map = readTable(tablename,i,i+page);
	             writeTable(map,index,type,path);
	             i+=page;
	         }
	     }
	     
	     public static void main(String[] args) {
	    	 
	    	 try {
				readDataByPage("brand", 1000, null, "test_index", "test_type");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
