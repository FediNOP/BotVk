package nop;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class DataBase {


    private static String url = "jdbc:mysql://127.0.0.1:3306/timetable?" + "&useLegacyDatetimeCode=false" + "&amp" + "&serverTimezone=UTC";
    private static String user = "root";
    private static String pass = "";

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;

    private static Map<Integer, String> groups = new HashMap<>();
    private static Map<Integer, String> lessons = new HashMap<>();
    private static Map<Integer, String> rooms = new HashMap<>();
    private static Map<Integer, String> teachers = new HashMap<>();
    private static Map<Integer, String> users = new HashMap<>();


    private static final Logger logger = LoggerFactory.getLogger(DataBase.class);


    public static void update() {


        groups = getData("groups");
        lessons = getData("lessons");
        rooms = getData("rooms");
        teachers = getData("teachers");
        users = getData("users");



    }


    public static String getChanges(String groupname){


        LocalTime nextday = new LocalTime(16,38,50);
        DateTime date = DateTime.now();

        if(DateTime.now().toLocalTime().isAfter(nextday))
            date = date.plusDays(1);





        logger.info("Getting Changes");
        logger.info("SELECT date, groupname, action, number, lesson, teacher, room FROM `changes` WHERE groupname = '" + groupname + "' AND date = '" + date.toLocalDate() + "'");
        String sql = "SELECT date, groupname, action, number, lesson, teacher, room FROM `changes` WHERE groupname = '" + groupname + "' AND date = '" + date.toLocalDate() + "'";
        String result = "";

        try{

            connection = DriverManager.getConnection(url,user,pass);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()){


                result += resultSet.getString("action") + "\nУрок №: "
                        + resultSet.getString("number") + " | "
                        + resultSet.getString("lesson") + " | "
                        + resultSet.getString("teacher") + " | Кабинет: "
                        + resultSet.getString("room") + "\n";


            }

            if(result.length() <= 0)
                return "Изменения скоро будут";
            else
               return result;


        }catch (SQLException e){

            logger.error("From getChanges",e);
            return "Изменения скоро будут";

        } finally {

        try {
            connection.close();
        } catch (SQLException se) { /*can't do anything */ }
        try {
            statement.close();
        } catch (SQLException se) { /*can't do anything */ }
        try {
            resultSet.close();
        } catch (SQLException se) { /*can't do anything */ }


    }



    }


    // TODO delete this method
    public static Map getTime(String day){



        try {

            connection = DriverManager.getConnection(url,user,pass);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT `time_start`, `time_end` FROM `times`");


            Map<String,String> time = new HashMap<>();

            while (resultSet.next()){

               time.put(resultSet.getString(1),resultSet.getString(2));

            }

            return time;

        }catch (SQLException e){

            logger.error("From getTime",e);
            return null;
        }






    }


    public static Map<Integer, String> getData(String table) {

        logger.info("Getting data from table: " + table);
        String sql = "SELECT * FROM `" + table + "`";
        Map<Integer, String> result = new HashMap<>();

        try {

            connection = DriverManager.getConnection(url, user, pass);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);


            while (resultSet.next()) {

               // java.sql.Blob blob = resultSet.getBlob(2);

                result.put(resultSet.getInt(1), resultSet.getString(2));

            }


            return result;

        } catch (SQLException e) {

            e.printStackTrace();
            logger.error("Error from getData", e);

            return null;

        } finally {

            try {
                connection.close();
            } catch (SQLException se) { /*can't do anything */ }
            try {
                statement.close();
            } catch (SQLException se) { /*can't do anything */ }
            try {
                resultSet.close();
            } catch (SQLException se) { /*can't do anything */ }


        }


    }


    public static String[] getTimeTable(String groupname, int add, String prepod) {

        int day = DateTime.now().dayOfWeek().get();
        logger.info("getTimeTable with params groupName:" + groupname + " day:" + day + " add:" + add + " Prepod:" + prepod);
        String sql;
        String result = "";
        String[] bigResult;
        List<String> listofResult = new ArrayList<>();




        if (add > 0)
            day = DateTime.now().plusDays(add).dayOfWeek().get();


        if (add == -1)
            sql = "SELECT\n" +
                    "\tweek.name AS _day, groups.name AS _groupname, changes.number AS _number,\n" +
                    "    actions.name AS _action, lessons.name AS _lesson, teachers.name AS _teacher,\n" +
                    "    rooms.name AS _room\n" +
                    "FROM\n" +
                    "\tchanges, groups, lessons, teachers, rooms, week, actions\n" +
                    "WHERE\n" +
                    "    changes.groupname = "+ getKey(groups,groupname.toLowerCase()) + " AND\n" +
                    "\n" +
                    "\tchanges.day = week.id AND changes.groupname = groups.id AND\n" +
                    "    changes.lesson = lessons.id AND changes.teacher = teachers.id AND\n" +
                    "    changes.room = rooms.id AND changes.action = actions.id\n" +
                    "\n" +
                    "UNION\n" +
                    "    \n" +
                    "SELECT\n" +
                    "\tweek.name, groups.name, timetable.number, ('*'), lessons.name, teachers.name, rooms.name\n" +
                    "FROM\n" +
                    "\ttimetable, groups, lessons, teachers, rooms, week, changes\n" +
                    "WHERE\n" +
                    "    timetable.groupname = " + getKey(groups,groupname.toLowerCase()) + " AND\n" +
                    "    \n" +
                    "    timetable.number <> changes.number AND    \n" +
                    "    \n" +
                    "\ttimetable.day = week.id AND timetable.groupname = groups.id AND\n" +
                    "    timetable.lesson = lessons.id AND timetable.teacher = teachers.id AND\n" +
                    "    timetable.room = rooms.id\n" +
                    "    \n" +
                    "ORDER BY _day, _number;\n";
        else
            sql = "SELECT\n" +
                "\tweek.name AS _day, groups.name AS _groupname, changes.number AS _number,\n" +
                "    actions.name AS _action, lessons.name AS _lesson, teachers.name AS _teacher,\n" +
                "    rooms.name AS _room\n" +
                "FROM\n" +
                "\tchanges, groups, lessons, teachers, rooms, week, actions\n" +
                "WHERE\n" +
                "    changes.day = " + day + " AND changes.groupname = " + getKey(groups,groupname.toLowerCase()) + " AND\n" +
                "\n" +
                "\tchanges.day = week.id AND changes.groupname = groups.id AND\n" +
                "    changes.lesson = lessons.id AND changes.teacher = teachers.id AND\n" +
                "    changes.room = rooms.id AND changes.action = actions.id\n" +
                "\n" +
                "UNION\n" +
                "    \n" +
                "SELECT\n" +
                "\tweek.name, groups.name, timetable.number, ('*'), lessons.name, teachers.name, rooms.name\n" +
                "FROM\n" +
                "\ttimetable, groups, lessons, teachers, rooms, week, changes\n" +
                "WHERE\n" +
                "    timetable.day = " + day + " AND timetable.groupname = " + getKey(groups,groupname.toLowerCase()) + " AND\n" +
                "    \n" +
                "    timetable.number <> changes.number AND    \n" +
                "    \n" +
                "\ttimetable.day = week.id AND timetable.groupname = groups.id AND\n" +
                "    timetable.lesson = lessons.id AND timetable.teacher = teachers.id AND\n" +
                "    timetable.room = rooms.id\n" +
                "    \n" +
                "ORDER BY _number;\n";





        logger.info(sql);
        try {

            connection = DriverManager.getConnection(url, user, pass);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            if (resultSet == null || !resultSet.isBeforeFirst()) {
                logger.warn("No data");
                return null;
            }

            resultSet.next();

             result += resultSet.getString("_day") + " " + resultSet.getString("_groupname") + "\n";

             String offsetDay = resultSet.getString("_day");

            do {


                result += resultSet.getInt("_number") + ". "
                        + resultSet.getString("_lesson") + ". "
                        + resultSet.getString("_teacher") + "\nКабинет: "
                        + resultSet.getString("_room") + " "
                        + resultSet.getString("_action") + "\n";


                if(add == -1 && !offsetDay.equals(resultSet.getString("_day"))) {

                    offsetDay = resultSet.getString("_day");
                    result += "\n" + resultSet.getString("_day") + "\n";

                }
                if (result.length() > 3000) {
                    listofResult.add(result);
                    result = "";

                }


            } while (resultSet.next());

            listofResult.add(result);
            bigResult = new String[listofResult.size()];
            listofResult.toArray(bigResult);
            logger.info("Ok");
            logger.info("Result:" + bigResult.length);
            return bigResult;

        } catch (SQLException e) {
            logger.error("Errot from getTimeTable", e);
           // e.printStackTrace();
            return null;

        } finally {

            try {
                connection.close();
            } catch (SQLException se) { /*can't do anything */ }
            try {
                statement.close();
            } catch (SQLException se) { /*can't do anything */ }
            try {
                resultSet.close();
            } catch (SQLException se) { /*can't do anything */ }

        }


    }


    private static Integer getKey(Map map, String value) {

        Set<Map.Entry<Integer, String>> entrySet = map.entrySet();

        for (Map.Entry<Integer, String> pair : entrySet) {
            if (value.equals(pair.getValue().toLowerCase())) {
                return pair.getKey();
            }
        }

        return null;

    }


    public static Map getGroups() {


        return groups;

    }


    public static List<String> getGroupsNames() {


        List<String> names = new ArrayList<>(groups.values());
        names.replaceAll(name -> name.replaceAll("[0-9-^\\w]", "").trim());
        return names;

    }

    public static List getGroupsNumbers() {


        List<String> names = new ArrayList<>(groups.values());
        names.replaceAll(name -> name.replaceAll("[А-я]", "").trim());
        return names;

    }


    public static Map getPrepods() {
        return teachers;
    }


    public static void addUser(int id, String name) {

        try {

            System.out.println(groups.containsValue(name));
            String sql;
            int nameId;
            if (teachers.containsValue(name))
                nameId = getKey(teachers, name.toLowerCase());
            else
                nameId = getKey(groups,name.toLowerCase());

            sql = "INSERT INTO `users`(`id`, `name`) VALUES (" + id + "," + nameId + ")";
            connection = DriverManager.getConnection(url,user,pass);
            statement = connection.createStatement();
            statement.execute(sql);

        }catch (SQLException e){

            logger.error("Error to adding user",e);


        }
    }

    public static Map getUsers() {

        List<Integer> usersid = new ArrayList<>(users.keySet());
        List<String> usersGroupsId = new ArrayList<>(users.values());

        List<String> usersGroups = new ArrayList<>();

        for(String group : usersGroupsId)
            usersGroups.add(groups.get(Integer.parseInt(group)));

        users.clear();

        for(int i=0; i<usersid.size(); i++)
            users.put(usersid.get(i),usersGroups.get(i));


        return users;
    }

    public static void removeUser(int id) {

        String sql = "DELETE FROM `users` WHERE id = " + id;

        try {

            connection = DriverManager.getConnection(url, user, pass);
            statement = connection.createStatement();
            statement.executeUpdate(sql);

        } catch (SQLException e) {
            logger.error("Error from removeUser:", e);
            e.printStackTrace();

        } finally {

            try {
                connection.close();
            } catch (SQLException se) { /*can't do anything */ }
            try {
                statement.close();
            } catch (SQLException se) { /*can't do anything */ }

        }

    }


    public static void setDateBase(String _url, String _user, String _pass) {

        url = "jdbc:" + _url + "&useLegacyDatetimeCode=false" + "&amp" + "&serverTimezone=UTC";
        user = _user;
        pass = _pass;

    }

}
