package base;

public enum ClientType {
    //SPECIAL("special"),
    DIRECTIONAL("directional"),
    BIDIRECTIONAL("bidirectional");

    private final String value;

    ClientType(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

    public static ClientType fromValue(String value) {
        for (ClientType clientType : ClientType.values()) {
            if (clientType.value.equals(value)) {
                return clientType;
            }
        }
        return null;
    }

}
