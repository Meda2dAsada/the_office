import base.Client;
import base.ClientType;
import base.Sprite;
import base.Window;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Client client = new Client("localhost", 5000, ClientType.BIDIRECTIONAL);
        client.connect();
    }
}