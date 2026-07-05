package Models;

public class Transaksi {
    private String kodeBarang;
    private String namaBarang;
    private double hargaSatuan;
    private int jumlah;
    private double subTotal;

    public Transaksi() {
    }

    public Transaksi(String kodeBarang, String namaBarang, double hargaSatuan, int jumlah, double subTotal) {
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.hargaSatuan = hargaSatuan;
        this.jumlah = jumlah;
        this.subTotal = subTotal;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public double getHargaSatuan() {
        return hargaSatuan;
    }

    public void setHargaSatuan(double hargaSatuan) {
        this.hargaSatuan = hargaSatuan;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }
}
