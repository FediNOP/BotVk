
package nop;

import com.Demo;
import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Button;
import com.petersamokhin.bots.sdk.objects.Color;
import com.petersamokhin.bots.sdk.objects.Keyboard;
import com.petersamokhin.bots.sdk.objects.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nop.CommandRunner;
import ru.nop.Keyboards;
import ru.nop.Time;
import ru.nop.Weather;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


//TODO Linux Commands
//TODO Notifying users about changes
//TODO MAFIA Game
//TODO Getting changes from DB


public class Main {

    private static String token = "73fc947d4e4019e6664180184f2f7c3361413b05c66dd817b15dee4a576fd6d7cb67124c302e4ae3126e9";
    private static int id = 139604069;

    //  private static String token = "ad8a1812febce75de10b71933654bb6e51e7ab4c621048ccf44581f55f9840d3023fdbeed60d0c4dc1c04";
    //  private static int id = 172020474;
    private static final int creator = 195267161;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static Group group;
    private static Map<Integer, String> usersID = new HashMap<>();
    private static Map<Integer, Integer> newUserStage = new HashMap<>();
    private static int newUserYear = 0;
    private static String newUserGroup;

    private static Map<Integer, String> groups = new HashMap<>();
    private static Map<Integer, String> prepodMap = new HashMap<>();
    private static DataBase dataBase = new DataBase();

    private static List<Integer> admin;

    private static boolean errmode = true;

    private static void Start() {


        readConfigFile();
        System.out.println("Getting data");
        logger.info("Getting data");
        DataBase.update();
        groups = dataBase.getGroups();
        prepodMap = dataBase.getPrepods();
        Keyboards.loadKeyboards();
        usersID = dataBase.getUsers();
        admin = getAdminList();
        Time.load();
        Weather.Update();



    }

    private static void readConfigFile() {

        System.out.println("Reading config file");
        logger.info("Reading config file");
        List<String> lines = new ArrayList<>();
        try {

            File inFile = new File("config.cfg");
            if (!inFile.exists()) {
                inFile.createNewFile();
                FileOutputStream out = new FileOutputStream("config.cfg");
                out.write(Demo.config().getBytes());
                out.flush();
                out.close();
                logger.warn("Config file not found");
                System.out.println("Config file not found");
                return;

            }


            Files.lines(Paths.get("config.cfg"), StandardCharsets.UTF_8).filter(line -> !line.contains("#")).forEach(lines::add);


            dataBase.setDateBase(lines.get(0), lines.get(1), lines.get(2));
            token = lines.get(3);
            id = Integer.parseInt(lines.get(4));

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("From reading file", e);

        }

    }


    public static void main(String[] arg) {

        try {

            System.out.println("Starting...");
            logger.info("Starting...");
            //Start();
            readConfigFile();
            group = new Group(id, token);
            logger.info("Successfully Started");
            System.out.println("Ok");
            logger.info("Waiting messages...");



            group.onSimpleTextMessage(message -> {


                logger.info(message.authorId() + ": " + message.getText());


                if(errmode){

                    message.text("Бот временно недоступен\uD83D\uDE14\n" +
                            "Приносим свои извинения\uD83D\uDE22");
                    message.send();
                    return;

                }

                try {


                    if (!checkUser(message.authorId())) {

                        message = newUser(message);
                        if(message != null)
                            message.send();

                    } else {


                        message = checkMessage(message);
                        if(message != null)
                            message.send();

                    }

                } catch (Exception e) {


                    e.printStackTrace();
                    logger.error("Main error", e);

                }


            });


        } catch (Exception e) {
            logger.error("Error Main", e);


        }


    }

    // TODO check commands
    private static Message checkCommand(Message message) {

        String text = message.getText().toLowerCase();

        System.out.println(text);

        if (text.contains("status")) {

            message.text("UsersId: " + usersID.size() + "\n"
                    + "TotalMemory: " + Runtime.getRuntime().totalMemory() + "\n"
                    + "FreeMemory: " + Runtime.getRuntime().freeMemory() + "\n"
                    + "Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            message.keyboard(Keyboards.keyboardMap.get("menu"));

            return message;

        } else if (text.contains("sendall")) {

            for (int id : usersID.keySet()) {

                sendMessage(message.getText().replaceAll("!sendall", ""), id);

            }


        }else if(text.contains("update")) {

            DataBase.update();
            message.text("Updating");

        }else if(text.contains("restart")) {


        }else if(text.contains("help")) {

            String info = "!status\n"
                    + "!sendall\n"
                    + "!update\n"
                    + "!run\n"
                    + "!errmode";

            message.text(info);

            return message;

        }else if(text.contains("run")) {

            message.text(CommandRunner.run(message.getText().replaceAll("!run", "")));
            return message;

        }else if(text.contains("errmode")){

            message.text("Бот отключен");
            return message;

        } else
            return message.text("Invalid command");


        return message.text("").keyboard(Keyboards.keyboardMap.get("menu"));
    }

    // TODO admin list
    private static List getAdminList() {

        List<Integer> admins = new ArrayList<>();

        admins.add(creater);
        admins.add(504890822); //Ваня
        admins.add(284041369); //Федя

        return admins;

    }

    private static Message checkMessage(Message message) {

        logger.info("Checking message");

        if (message.getText().substring(0, 1).equals("!") && message.authorId() == creater)
            return checkCommand(message);


        switch (message.getText().toLowerCase()) {

            case "привет":
                message.text("Бонжур");
                message.keyboard(Keyboards.keyboardMap.get("menu"));
                break;

            case "расписание":
                message.keyboard(Keyboards.keyboardMap.get("menu")).text(getTimeTableByTime(message.authorId(),0) + "\n" + checkOnpasxalka(message.authorId()));
                break;

            case "изменения":

                message.text(DataBase.getChanges(usersID.get(message.authorId())));

                break;

            case "на всю неделю":
                message.text(getTimeTableByTime(message.authorId(), -1))
                        .keyboard(Keyboards.keyboardMap.get("menu"));
                break;



            case "на завтра":
                message.text(getTimeTableByTime(message.authorId(), 1))
                        .keyboard(Keyboards.keyboardMap.get("menu"));
                break;


            case "звонки":
                message.text(getTimeToPeremena())
                        .keyboard(Keyboards.keyboardMap.get("menu"));
                break;


            case "отписаться от бота":
                usersID.remove(message.authorId());
                DataBase.removeUser(message.authorId());
                message = newUser(message);
                message.text("Вы отписались от бота");
                break;

            case "тех. поддержка":
            case "помогите":
            case "помощь":
            case "help":
            case "админ":
                for (int id : admin)
                    sendMessage(("vk.com/id" + message.authorId() + " : " + message.getText()), id);
                break;

            case "погода":
                message.keyboard(Keyboards.keyboardMap.get("menu"));
                message.text(Weather.getWeather());
                break;


            default:
                logger.info("Default");
                return null;



        }


        return message;

    }



    private static String getTimeTableByTime(int id, int add) {

        logger.info("Getting timetable today for id: " + id);
        String groupName = null;
        String prepod = null;

        if (prepodMap.containsValue(usersID.get(id)))
            prepod = usersID.get(id);
        else groupName = usersID.get(id);

        String result[] = DataBase.getTimeTable(groupName, add, prepod);

        if (result == null || result.length <= 0) {
            logger.info("No classes");
            return "Нет занятий";
        } else if (result.length > 1) {
            logger.info("Sending message...");
            for (String obj : result)
                sendMessage(obj, id);

            logger.info("Ok");
            return "";

        } else {

            return result[0];

        }

    }


    private static String getTimeToPeremena() {

        if (new SimpleDateFormat("EEEE", new Locale("ru")).format(new Date()).equals("суббота"))
            return Demo.timePeremenaSubbota();
        else
            return Demo.peremenaTime();

    }


    private static Message newUser(Message message) {

        if (!newUserStage.containsKey(message.authorId()))
            newUserStage.put(message.authorId(), 0);

        if (newUserStage.get(message.authorId()) == 0) {

            message.keyboard(Keyboards.keyboardMap.get("newUser"));
            newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);

        } else if (newUserStage.get(message.authorId()) == 1) {

            switch (message.getText().toLowerCase()) {

                case "студент":
                    message.keyboard(Keyboards.keyboardMap.get("year")).text("Выберите курс");
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    break;

                case "преподаватель":
                    message.text(MapToString(prepodMap));
                    newUserStage.put(message.authorId(), 5);
                    break;

                default:
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) - 1);
                    message.text("Выберите роль");
                    message.keyboard(Keyboards.keyboardMap.get("newUser"));
                    break;
            }


        } else if (newUserStage.get(message.authorId()) == 2) {


            switch (message.getText().toLowerCase()) {

                case "1 курс":
                    newUserYear = 0;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(Keyboards.keyboardMap.get("groupsName")).text("Выберите специальность");
                    break;

                case "2 курс":
                    newUserYear = 1;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(Keyboards.keyboardMap.get("groupsName")).text("Выберите специальность");
                    break;
                case "3 курс":
                    newUserYear = 2;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(Keyboards.keyboardMap.get("groupsName")).text("Выберите специальность");
                    break;

                case "4 курс":
                    newUserYear = 3;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(Keyboards.keyboardMap.get("groupsName")).text("Выберите специальность");
                    break;

                default:
                    message.text("Выберите курс");
                    message.keyboard(Keyboards.keyboardMap.get("year"));
                    break;
            }


        } else if (newUserStage.get(message.authorId()) == 3) {

            if (DataBase.getGroupsNames().contains(message.getText())) {

                newUserGroup = message.getText();
                Keyboard keyb = getGroupsNumsKeyb(newUserYear, newUserGroup);
                if (keyb.getButtons().size() > 0) {
                    message.keyboard(keyb).text("Выберите группу");
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);

                } else {

                    message.text("Групп нет").keyboard(Keyboards.keyboardMap.get("newUser"));
                    newUserStage.put(message.authorId(), 1);

                }

            } else
                message.text("Выберите специальность").keyboard(Keyboards.keyboardMap.get("groupsName"));


        } else if (newUserStage.get(message.authorId()) == 4) {

            if (DataBase.getGroupsNumbers().contains(message.getText())) {

                usersID.put(message.authorId(), newUserGroup + " " + message.getText());
                DataBase.addUser(message.authorId(), newUserGroup + " " + message.getText());
                newUserStage.remove(message.authorId());
                message.keyboard(Keyboards.keyboardMap.get("menu")).text("Вы подписаны на бота");


            } else
                message.text("Выберите группу").keyboard(getGroupsNumsKeyb(newUserYear, newUserGroup));


        } else if (newUserStage.get(message.authorId()) == 5) {

            if (message.getText().matches("[-+]?\\d+"))
                if (prepodMap.get(Integer.parseInt(message.getText())) != null) {
                    usersID.put(message.authorId(), prepodMap.get(Integer.parseInt(message.getText())));
                    DataBase.addUser(message.authorId(), prepodMap.get(Integer.parseInt(message.getText())));
                    newUserStage.remove(message.authorId());
                    message.keyboard(Keyboards.keyboardMap.get("menu"));

                } else
                    message.text("Такого преподавтеля нет");
            else
                message.text("Введите число");


        }


        return message;

    }


    private static boolean checkUser(int id) {
        return usersID.containsKey(id);
    }


    public static void sendMessage(String text, int id) {


        Message msg = new Message().from(group).to(id).text(text).keyboard(Keyboards.keyboardMap.get("menu"));
        msg.send();

    }


    private static String MapToString(Map map) {

        String _groups = "";

        Set<Map.Entry<Integer, String>> entrySet = map.entrySet();

        for (Map.Entry<Integer, String> pair : entrySet)
            _groups += pair.getKey() + ". " + pair.getValue() + "\n";


        return _groups;

    }


    private static Keyboard getGroupsNumsKeyb(int year, String group) {

        Keyboard keyboard = new Keyboard().setOneTime(true);
        String[] nums = new String[DataBase.getGroupsNumbers().size()];
        DataBase.getGroupsNumbers().toArray(nums);
        List<String> groupeFilter = new ArrayList<>();
        for (String num : nums) {
            if (Integer.parseInt(num.substring(0, 2)) == Integer.parseInt(new SimpleDateFormat("yy").format(new Date())) - year) {
                groupeFilter.add(num);
            }
        }


        groupeFilter = groupeFilter.stream().distinct().filter(g -> groups.containsValue(group + " " + g)).collect(Collectors.toList());


        for (String num : groupeFilter)
            keyboard.addButtons(1, new Button().setColor(Color.Primary).setLabel(num));

        return keyboard;


    }

    private static String checkOnpasxalka(int id) {

        switch (id) {

            case 284041369:
                return "Тикай тубипзд)))))";

            case creater:
                return "Аве Создатель";

            case 271663680:
                return "❤";

            case 214649672:
                return "Сабриночка❤❤❤";

            default:
                return "";

        }

    }


}
