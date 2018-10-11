package e.antti.connectiontest;

public class MyEmoji{
    String name;
    String code;
    String emoji;

    MyEmoji(){

    }

    MyEmoji(String name,String emoji){
        this.name = name;
        this.emoji = emoji;
    }

    public void addName(String name){
        this.name = name;
    }

    public void addEmoji(String emoji){
        this.emoji = emoji;
    }

}