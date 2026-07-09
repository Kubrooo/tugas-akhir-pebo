package Models;

import java.util.Date;

public class DetailTransaksi {
    private String noNota;
    private String kodeProduk;
    private Date tanggal;
    private double bayar;
    private double kembalian;

    public DetailTransaksi() {
    }

    public DetailTransaksi(String noNota, String kodeProduk, Date tanggal, double bayar, double kembalian) {
        this.noNota = noNota;
        this.kodeProduk = kodeProduk;
        this.tanggal = tanggal;
        this.bayar = bayar;
        this.kembalian = kembalian;
    }

    public String getNoNota() {
        return noNota;
    }

    public void setNoNota(String noNota) {
        this.noNota = noNota;
    }

    public String getKodeProduk() {
        return kodeProduk;
    }

    public void setKodeProduk(String kodeProduk) {
        this.kodeProduk = kodeProduk;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public double getBayar() {
        return bayar;
    }

    public void setBayar(double bayar) {
        this.bayar = bayar;
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
    }
}
