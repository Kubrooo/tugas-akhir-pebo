package Models;

import java.util.Date;

public class DetailTransaksi {
    private String noNota;
    private String kodeBarang;
    private Date tanggal;
    private double bayar;
    private double kembalian;

    public DetailTransaksi() {
    }

    public DetailTransaksi(String noNota, String kodeBarang, Date tanggal, double bayar, double kembalian) {
        this.noNota = noNota;
        this.kodeBarang = kodeBarang;
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

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
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
