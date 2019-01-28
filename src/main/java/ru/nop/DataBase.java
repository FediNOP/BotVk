package ru.nop;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataBase {

    private static String url = "jdbc:mysql://127.0.0.1:3306/timetable?autoReconnect=true";
    private static String user = "root";
    private static String pass = "";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    private static Connection connection;


    public static void setDateBase(String _url, String _user, String _pass) {


        url = "jdbc:" + _url + "&useLegacyDatetimeCode=false" + "&amp" + "&serverTimezone=UTC";
        user = _user;
        pass = _pass;


    }


    public static void connect() {

        try {

            connection = DriverManager.getConnection(url, user, pass);


        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();
        }

    }


    public static boolean checkUser(int id) {

        try {

            final String sql = "SELECT count(*) from users WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            final ResultSet resultset = ps.executeQuery();
            if (resultset.next())
                if (resultset.getInt(1) > 0)
                    return true;
                else return false;

            else return false;

        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();
            return false;
        }


    }

    public static boolean checkGroup(String name){

        try {

            final String sql = "SELECT count(*) from groups WHERE name = ?";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            final ResultSet resultset = ps.executeQuery();
            if (resultset.next())
                if (resultset.getInt(1) > 0)
                    return true;
                else return false;

            else return false;

        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();
            return false;
        }





    }

    public static boolean checkTeacher(int id){

        try {

            final String sql = "SELECT count(*) FROM teachers WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            final ResultSet resultset = ps.executeQuery();
            if (resultset.next())
                if (resultset.getInt(1) > 0)
                    return true;
                else return false;

            else return false;

        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();
            return false;
        }



    }

    public static List<String> getGroupsNames() {

        List<String> result = new ArrayList<>();
        result.add("");

        try {

            final String sql = "SELECT DISTINCT abbreviation FROM `groups` WHERE 1";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
                result.add(rs.getString(1));


            ps.close();
            return result;


        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();
            return null;

        }

    }


    public static List<String> getTeachers() {

        List<String> result = new LinkedList<>();
        result.add("");

        final String sql = "SELECT * FROM `teachers`";

        try {

            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next())
                result.add(resultSet.getString("name"));


            ps.close();
            return result;


        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();
            return null;

        }


    }

    public static void addUser(int id, String groupId) {

        try {

            final String sql = "INSERT INTO `users`(`id`, `groupName`) VALUES (?,?)";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setString(2, groupId);
            ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();

        }


    }

    public static String getUser(int id){

        final String sql = "SELECT `groupName` FROM `users` WHERE id = ?";

        try {

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1,id);

            ResultSet resultSet = ps.executeQuery();

            if(resultSet.next())
                return resultSet.getString("groupName");

            return null;


        }catch (SQLException e){

            logErr(e);
            e.printStackTrace();
            return null;

        }



    }

    public static void removeUser(int id){

        final String sql = "DELETE FROM `users` WHERE id = ?";

        try{

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1,id);

            ps.executeUpdate();

        }catch (SQLException e){

            e.printStackTrace();
            logErr(e);

        }


    }

    private static void logErr(Exception e) {

        logger.error("", e);

    }

    public List<String> getGroups(int year, String group) {

        List<String> result = new ArrayList<>();

        final String sql = "SELECT level, count, number FROM `groups` WHERE abbreviation = ? AND level = ?";

        try {

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, group);
            ps.setInt(2, year);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next())
                result.add(resultSet.getString("level") + "-" + resultSet.getString("count") + "-" + resultSet.getString("number"));

            return result;


        } catch (SQLException e) {

            logErr(e);
            e.printStackTrace();
            return null;

        }


    }

    public static String[] getTimeTable(int id, int addDay){

        int day = DateTime.now().plusDays(addDay).dayOfWeek().get();
        final String sql;
        String name = getUser(id);
        List<String> listOfResult = new ArrayList<>();
        String[] bigResult;
        String result = "";

        boolean isStudent = checkGroup(name);

        if(addDay != -1) {

            if (isStudent)
                sql = "SELECT _day, _group, _number, _subgroup, _action, _lesson, _teacher, _room " +
                        "FROM `timetable_with_changes` WHERE id_day = ? AND _group = ? ORDER BY _number";
            else
                sql = "SELECT _day, _group, _number, _subgroup, _action, _lesson, _room, _teacher " +
                        "FROM `timetable_with_changes` WHERE id_day = ? AND _teacher = ? ORDER BY _number";


        }else {

            if (isStudent)
                sql = "SELECT _day, _group, _number, _subgroup, _action, _lesson, _teacher, _room " +
                        "FROM `timetable_with_changes` WHERE _group = ? ORDER BY id_day";
            else
                sql = "SELECT _day, _group, _number, _subgroup, _action, _lesson, _room, _teacher " +
                        "FROM `timetable_with_changes` WHERE  _teacher = ? ORDER BY id_day";

        }


        try {


            PreparedStatement ps = connection.prepareStatement(sql);

            if(addDay != -1) {
                ps.setInt(1, day);
                ps.setString(2, name);
            }else {
                ps.setString(1,name);
            }

            ResultSet resultSet = ps.executeQuery();

            if (resultSet == null || !resultSet.isBeforeFirst()) {
                logger.warn("No data");
                return null;
            }



            resultSet.next();

            if(isStudent)
                result += resultSet.getString("_day") + " " + resultSet.getString("_group") + "\n\n";
            else
                result += resultSet.getString("_day") + " " + resultSet.getString("_teacher") + "\n\ns";

            String offsetDay = resultSet.getString("_day");

            do {


                result += resultSet.getInt("_number") + ". ";

                if (resultSet.getInt("_subgroup") > 1)
                    result += "Подгруппа №2\n";


                result += resultSet.getString("_lesson") + ". ";

                if (isStudent)
                    result += resultSet.getString("_teacher");

                result += "\nКабинет: " + resultSet.getString("_room") + " "
                        + resultSet.getString("_action") + "\n";


                if (addDay == -1 && !offsetDay.equals(resultSet.getString("_day"))) {

                    offsetDay = resultSet.getString("_day");
                    result += "\n" + resultSet.getString("_day") + "\n";

                }
                if (result.length() > 3000) {
                    listOfResult.add(result);
                    result = "";

                }


            } while (resultSet.next());

            listOfResult.add(result);
            bigResult = new String[listOfResult.size()];
            listOfResult.toArray(bigResult);
            logger.info("Ok");
            logger.info("Result:" + bigResult.length);
            listOfResult.clear();
            return bigResult;


        }catch (SQLException e){

            logErr(e);
            e.printStackTrace();
            return null;

        }


    }


}
