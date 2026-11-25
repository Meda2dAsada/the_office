import base.Client;
import base.ClientType;

public class Main {
    public static void main(String[] args) {
        OficinaTheOffice.main(args);

        Client client = new Client(
                "localhost",
                5000,
                ClientType.BIDIRECTIONAL
        );
        client.connect();
    }
}