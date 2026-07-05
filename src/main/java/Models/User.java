package Models;

public class User {
    private int idUser;
    private String nama;
    private String username;
    private String password;
    private String role;

    public User() {
    }

    public User(int idUser, String nama, String username, String password, String role) {
        this.idUser = idUser;
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
