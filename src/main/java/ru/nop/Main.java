package ru.nop;

import com.Demo;
import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Button;
import com.petersamokhin.bots.sdk.objects.Color;
import com.petersamokhin.bots.sdk.objects.Keyboard;
import com.petersamokhin.bots.sdk.objects.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static String token = "73fc947d4e4019e6664180184f2f7c3361413b05c66dd817b15dee4a576fd6d7cb67124c302e4ae3126e9";
    private static int id = 139604069;
    private static final int creator = 195267161;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Group group;

    private static Map<Integer, String> usersID = new HashMap<>();
    private static Map<Integer, Integer> newUserStage = new HashMap<>();
    private static int newUserYear = 0;
    private static String newUserGroup;

    private static int[] admins = {creator, 504890822, 284041369};

    //Modules
    private static DataBase dataBase;
    private static Keyboards keyboards;
    private static CommandRunner runner;


    private static void start(){

        keyboards = new Keyboards();
        runner = new CommandRunner();

        System.out.println("Starting...");
        logger.info("Starting...");

        dataBase = new DataBase();
        readConfigFile();
        dataBase.connect();
        keyboards.loadKeyboards();

        group = new Group(id,token);

        System.out.println("OK");
        logger.info("OK");
        logger.info("Waiting messages...");

    }



    public static void main(String[] args) {

        try {


            start();


            group.onSimpleTextMessage(message -> {

                logger.info(message.authorId() + ": " + message.getText());

                if (dataBase.checkUser(message.authorId())) {

                    checkMessage(message).send();

                } else if (newUser(message) != null) {

                    message.send();

                }


            });

        }catch (Exception e){

            logger.error("",e);
            runner.run("service bot restart");


        }

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

    private static Message newUser(Message message) {

        if (!newUserStage.containsKey(message.authorId()))
            newUserStage.put(message.authorId(), 0);


        if (newUserStage.get(message.authorId()) == 0) {

            message.keyboard(keyboards.getKeyb("newUser"));
            newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);

        } else if (newUserStage.get(message.authorId()) == 1) {

            switch (message.getText().toLowerCase()) {

                case "студент":
                    message.keyboard(keyboards.getKeyb("year")).text("Выберите курс");
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    break;

                case "преподаватель":
                    String result = "";
                    List<String> teachers = dataBase.getTeachers();

                    for(int i = 1; i<teachers.size(); i++)
                        result += i + "." + teachers.get(i) + "\n";


                    message.text(result);
                    newUserStage.put(message.authorId(), 5);
                    break;

                default:
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) - 1);
                    message.text("Выберите роль");
                    message.keyboard(keyboards.getKeyb("newUser"));
                    break;
            }

        } else if (newUserStage.get(message.authorId()) == 2) {


            switch (message.getText().toLowerCase()) {

                case "1 курс":
                    newUserYear = 1;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(keyboards.getKeyb("groupsName")).text("Выберите специальность");
                    break;

                case "2 курс":
                    newUserYear = 2;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(keyboards.getKeyb("groupsName")).text("Выберите специальность");
                    break;
                case "3 курс":
                    newUserYear = 3;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(keyboards.getKeyb("groupsName")).text("Выберите специальность");
                    break;

                case "4 курс":
                    newUserYear = 4;
                    newUserStage.put(message.authorId(), newUserStage.get(message.authorId()) + 1);
                    message.keyboard(keyboards.getKeyb("groupsName")).text("Выберите специальность");
                    break;

                default:
                    message.text("Выберите курс");
                    message.keyboard(Keyboards.getKeyb("year"));
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

                    message.text("Групп нет").keyboard(Keyboards.getKeyb("newUser"));
                    newUserStage.put(message.authorId(), 1);

                }

            } else
                message.text("Выберите специальность").keyboard(Keyboards.getKeyb("groupsName"));



        } else if (newUserStage.get(message.authorId()) == 4) {

            if (DataBase.checkGroup(newUserGroup + " " + message.getText())) {

                usersID.put(message.authorId(), newUserGroup + " " + message.getText());
                dataBase.addUser(message.authorId(), newUserGroup + " " + message.getText());
                newUserStage.remove(message.authorId());
                message.keyboard(keyboards.getKeyb("menu")).text("Вы подписаны на бота");


            } else
                message.text("Выберите группу").keyboard(getGroupsNumsKeyb(newUserYear, newUserGroup));




        } else if (newUserStage.get(message.authorId()) == 5) {

            if (message.getText().matches("[-+]?\\d+"))
                if (dataBase.checkTeacher(Integer.parseInt(message.getText()))) {
                    String teacher = dataBase.getTeachers().get(Integer.parseInt(message.getText()));
                    usersID.put(message.authorId(), teacher);
                    dataBase.addUser(message.authorId(), teacher);
                    newUserStage.remove(message.authorId());
                    message.keyboard(keyboards.getKeyb("menu")).text("Здравствуйте, " + teacher);

                } else
                    message.text("Такого преподавтеля нет");
            else
                message.text("Введите число");




        } else {

            return null;
        }


        return message;

    }

    private static Keyboard getGroupsNumsKeyb(int year, String group) {

        Keyboard keyboard = new Keyboard().setOneTime(true);

        List<String> groups = dataBase.getGroups(Integer.parseInt(new SimpleDateFormat("yy").format(new Date())) - year, group);


        for (String num : groups)
            keyboard.addButtons(1, new Button().setColor(Color.Primary).setLabel(num));

        return keyboard;


    }

    private static Message checkMessage(Message message) {

        logger.info("Checking message");

        if (message.getText().substring(0, 1).equals("!") && message.authorId() == creator)
            return checkCommand(message);


        switch (message.getText().toLowerCase()) {

            case "привет":
                message.text("Бонжур");
                message.keyboard(keyboards.getKeyb("menu"));
                break;

            case "расписание":
                  message.keyboard(keyboards.getKeyb("menu")).text(getTimeTableByTime(message.authorId(),0) + "\n" + checkOnpasxalka(message.authorId()));
                break;

            case "изменения":

              // message.text(DataBase.getChanges(usersID.get(message.authorId())));

                break;

            case "на всю неделю":
                message.text(getTimeTableByTime(message.authorId(), -1))
                       .keyboard(keyboards.getKeyb("menu"));
                break;



            case "на завтра":
                message.text(getTimeTableByTime(message.authorId(), 1))
                        .keyboard(keyboards.getKeyb("menu"));
                break;


            case "звонки":
                message.text(getTimeToBreak())
                        .keyboard(keyboards.getKeyb("menu"));
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
                for (int id : admins)
                    sendMessage(("vk.com/id" + message.authorId() + " : " + message.getText()), id);
                break;

            case "погода":
                message.keyboard(keyboards.getKeyb("menu"));
                message.text(Weather.getWeather());
                break;


            default:
                logger.info("Default");
                return null;



        }


        return message;

    }

    private static Message checkCommand(Message message) {

        String text = message.getText().toLowerCase();

        System.out.println(text);

        if (text.contains("status")) {

            message.text("UsersId: " + usersID.size() + "\n"
                    + "TotalMemory: " + Runtime.getRuntime().totalMemory() + "\n"
                    + "FreeMemory: " + Runtime.getRuntime().freeMemory() + "\n"
                    + "Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            message.keyboard(keyboards.getKeyb("menu"));

            return message;

        } else if (text.contains("sendall")) {

            for (int id : usersID.keySet()) {

                sendMessage(message.getText().replaceAll("!sendall", ""), id);

            }


        }else if(text.contains("restart")) {


        }else if(text.contains("help")) {

            String info = "!status\n"
                    + "!sendall\n"
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


        return message.text("").keyboard(keyboards.getKeyb("menu"));
    }

    private static String getTimeTableByTime(int id, int add) {

        logger.info("Getting timetable today for id: " + id);

        String result[] = DataBase.getTimeTable(id, add);

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

    public static void sendMessage(String text, int id) {


        Message msg = new Message().from(group).to(id).text(text).keyboard(keyboards.getKeyb("menu"));
        msg.send();

    }

    private static String getTimeToBreak() {

        if (new SimpleDateFormat("EEEE", new Locale("ru")).format(new Date()).equals("суббота"))
            return Demo.timePeremenaSubbota();
        else
            return Demo.peremenaTime();

    }

    private static String checkOnpasxalka(int id) {

        switch (id) {

            case 284041369:
                return "Тикай тубипзд)))))";

            case creator:
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

