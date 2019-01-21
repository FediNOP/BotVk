package ru.nop;

import com.petersamokhin.bots.sdk.objects.Button;
import com.petersamokhin.bots.sdk.objects.Color;
import com.petersamokhin.bots.sdk.objects.Keyboard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Keyboards {


    private static Map<String,Keyboard> keyboardMap = new HashMap<>();

    public static void loadKeyboards(){


        List<String> names = DataBase.getGroupsNames().stream().distinct().collect(Collectors.toList());
        Keyboard groupsNamesKeyboard = new Keyboard().setOneTime(true);
        int partitionSize = names.size()/3+1;
        List<List<String>> name = new LinkedList<>();

        for (int i = 1; i < names.size(); i += partitionSize)
            name.add(names.subList(i, Math.min(i + partitionSize, names.size())));
        //stream().forEach(name -> groupsNamesKeyboard.addButtons(groupsNamesKeyboard.getButtons().size()+1, new Button().setLabel(name).setColor(Color.Primary)));

        for(int i=1;i<name.size();i++)
            for(int n=0;n<name.get(i).size();n++)
                groupsNamesKeyboard.addButtons(i,new Button().setColor(Color.Primary).setLabel(name.get(i).get(n)));

        keyboardMap.put("groupsName",groupsNamesKeyboard);






        keyboardMap.put("year", new Keyboard().setOneTime(true)
                        .addButtons(1, new Button().setLabel("1 Курс").setColor(Color.Primary))
                        .addButtons(1, new Button().setLabel("2 Курс").setColor(Color.Primary))
                        .addButtons(1, new Button().setLabel("3 Курс").setColor(Color.Primary))
                        .addButtons(1, new Button().setLabel("4 Курс").setColor(Color.Primary)));

        keyboardMap.put("menu", new Keyboard().setOneTime(false)
                        .addButtons(1, new Button().setLabel("Расписание").setColor(Color.Primary))
                        .addButtons(1, new Button().setLabel("Звонки").setColor(Color.Primary))
                        .addButtons(2, new Button().setLabel("На завтра").setColor(Color.Primary))
                        .addButtons(2, new Button().setLabel("На всю неделю").setColor(Color.Primary))
                        .addButtons(4, new Button().setLabel("Отписаться от бота"))
                        .addButtons(3,new Button().setLabel("Погода").setColor(Color.Positive))
                        .addButtons(3, new Button().setLabel("Тех. поддержка").setColor(Color.Positive)));
                      //  .addButtons(1, new Button().setLabel("Изменения").setColor(Color.Primary)));

        keyboardMap.put("newUser", new Keyboard().setOneTime(true)
                        .addButtons(1, new Button().setLabel("Студент").setColor(Color.Primary))
                        .addButtons(1, new Button().setLabel("Преподаватель")));

    }


    public static Keyboard getKeyb(String name){

        return keyboardMap.get(name);

    }



}
