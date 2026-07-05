package Models;

public class Produk {
    private String kodeProduk;
    private String namaProduk;
    private double hargaProduk;

    public Produk() {
    }

    public Produk(String kodeProduk, String namaProduk, double hargaProduk) {
        this.kodeProduk = kodeProduk;
        this.namaProduk = namaProduk;
        this.hargaProduk = hargaProduk;
    }

    public String getKodeProduk() {
        return kodeProduk;
    }

    public void setKodeProduk(String kodeProduk) {
        this.kodeProduk = kodeProduk;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public double getHargaProduk() {
        return hargaProduk;
    }

    public void setHargaProduk(double hargaProduk) {
        this.hargaProduk = hargaProduk;
    }
}
