package Server;

import Entities.Human;
import Entities.Moves;
import Exceptions.NotAliveException;
import World.WorldManager;

public class ClientCommandHandler {

    private Client client;

    public ClientCommandHandler(Client client) {
        this.client = client;
    }

    public void executeCommand(String command) {
        String[] commands = command.split(" ");
        if (commands.length == 0) return;

        switch(commands[0]) {
            case "help":
                helpClient();
                break;
            case "chat":
                Server.sendToAllClients(command.replace(commands[0]+" ", ""), client);
                break;
            case "select":
                if (commands.length < 2)
                    sendMessage(cActions.SEND,"Отсутсвуют аргументы\n");
                else {
                    Boolean flag = true;
                    for (Client c : Server.getClients()) {
                        if (commands[1].equals(c.getKey())) {
                            if (c != client) sendMessage(cActions.SEND,"Персонаж уже выбран другим игроком\n");
                            else sendMessage(cActions.SEND, "Вы уже выбрали этого персонажа\n");
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        Human sel = WorldManager.getHuman(commands[1]);
                        if (sel != null) {
                            if (client.getKey() != null) Server.remPlayer(client.getKey());
                            client.setKey(commands[1]);
                            client.setHuman(sel);
                            sendMessage(cActions.SEND, "Выбран персонаж: " + sel.getName() + "\n");
                            sendMessage(cActions.DESERIALIZE, "", sel);
                            Server.addPlayer(client, commands[1], sel);
                            client.setName(sel.getName());
                        } else
                            sendMessage(cActions.SEND, "Персонаж не найден\n");
                    }
                }
                break;
            case "createnew":
                if (WorldManager.getHuman(commands[1]) == null) {
                    if (client.getKey() != null) Server.remPlayer(client.getKey());
                    Human human = (Human) client.readObject();
                    WorldManager.addNewHuman(commands[1], human);
                    client.setHuman(human);
                    client.setKey(commands[1]);
                    Server.addPlayer(client, commands[1], human);
                    sendMessage(cActions.SEND, "Выбран персонаж: " + human.getName() + "\n");
                    client.sendMessage(cActions.DESERIALIZE, "", human);
                    client.setName(human.getName());
                } else {
                    client.readObject();
                    sendMessage(cActions.SEND, "Выбранный идентификатор уже занят.\n");}
                break;
            case "move":
                if (commands.length < 2)
                    sendMessage(cActions.SEND,"Отсутсвуют аргументы\n");
                else {
                    if (client.getHuman() != null) {
                        Moves move = null;
                        try {
                            move = Moves.valueOf(commands[1].toUpperCase());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (move != null) {
                            try {
                                Human crte = client.getHuman();
                                crte.move(move);
                                sendMessage(cActions.SEND,"Перемещение успешно. Новые координаты: "+crte.getLocation().getName()+"\n");
                                Server.movPlayer(client, client.getKey(), move);
                            } catch (NotAliveException e) {
                                sendMessage(cActions.SEND, e.getMessage());
                            }
                        }
                    }
                }
                break;
            case "exit":
                if (client.getKey() != null)
                    Server.remPlayer(client.getKey());
                break;
            default:
                sendMessage(cActions.SEND, "Команда не найдена\n");
        }
    }

    public void sendMessage(cActions action, String str) {
        client.sendMessage(action, str);
    }

    public void sendMessage(cActions action, String str, Object obj) {
        client.sendMessage(action, str, obj);
    }

    public void helpClient() {
        sendMessage(cActions.SEND, "Справка по командам: \n" +
                "select [идентифкатор персонажа] - выбрать персонажа, которого вы создали ранее\n" +
                "createnew - создать нового персонажа\n" +
                "move [направление] - переместить выбранного персонажа, где направление:left, right, forward, back\n" +
                "showstats - вывести информацию о персонаже\n" +
                "chat [сообщение] - отправить сообщение другим игрокам\n" +
                "help - справка по командам\n" +
                "exit - отключиться от сервера\n");
    }
}
