package me.gingerninja.authenticator.util.backup;

public class NotNinjAuthZipFile extends RuntimeException {
    public NotNinjAuthZipFile() {
        super("The file is not a valid NinjAuth ZIP file");
    }
}
