package Models;

import java.util.Date;

public class DetailTransaksi {
    private String noNota;
    private String kodeProduk;
    private Date tanggal;

    public DetailTransaksi() {
    }

    public DetailTransaksi(String noNota, String kodeProduk, Date tanggal) {
        this.noNota = noNota;
        this.kodeProduk = kodeProduk;
        this.tanggal = tanggal;
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
}
