# Penjelasan Detail & Panduan Belajar Aplikasi OriTeh Sapuro POS

Halo! File ini dibuat khusus untuk membantu kamu dan teman-temanmu memahami bagaimana aplikasi penjualan (Point of Sales) **OriTeh Sapuro** ini bekerja. Di sini, kita akan membahas alur kode, konsep database, dan logika coding dengan bahasa yang sederhana dan mudah dipahami, bahkan untuk yang baru belajar pemrograman (coding).

---

## 1. Konsep Dasar Arsitektur Aplikasi (Konsep MVC)

Aplikasi ini menggunakan pola struktur yang mirip dengan konsep **MVC (Model-View-Controller)**. Bayangkan kamu sedang memesan teh di kafe:
1. **View (Tampilan/GUI)**: Ini seperti **Menu Fisik & Kasir** yang kamu lihat langsung. Tempat kita berinteraksi, memencet tombol, dan melihat data. (Folder `View`)
2. **Model (Data)**: Ini seperti **Bahan-Bahan Teh**. Model adalah tempat penyimpanan sementara untuk struktur data di memori komputer sebelum dikirim ke database. (Folder `Models`)
3. **Controller (Logika)**: Ini seperti **Barista** yang meracik teh. Barista mengambil pesanan dari kasir (View), mengambil bahan (Model), lalu menyimpannya ke dapur (Database). Di aplikasi Java Swing kita, logika ini ditulis di dalam tombol-tombol View itu sendiri.

---

## 2. Cara Java Menghubungkan Diri ke Database (MySQL)

Untuk menghubungkan program Java kita ke database MySQL (yang berjalan di XAMPP), ada dua bagian penting:

### A. Library Penghubung (JDBC Driver) di `pom.xml`
Java tidak bisa langsung berbicara dengan MySQL tanpa "penerjemah". Penerjemah ini bernama **JDBC Driver**. Kita menambahkannya di file konfigurasi proyek Maven (`pom.xml`):
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```
*Analogi:* Ini seperti memasang kabel colokan tambahan di komputer agar bisa tersambung ke printer.

### B. Kelas Koneksi (`Models/Koneksi.java`)
Ini adalah kelas khusus yang bertugas membuka pintu gerbang ke database.
```java
public class Koneksi {
    private static final String URL = "jdbc:mysql://localhost:3306/db_oriteh_sapuro";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Memanggil Driver Penerjemah
        } catch (ClassNotFoundException e) {
            System.err.println("Driver tidak ditemukan!");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD); // Membuka Koneksi
    }
}
```
*   `localhost:3306`: Komputer kita sendiri yang menjalankan database MySQL.
*   `db_oriteh_sapuro`: Nama database yang kita pakai.
*   `root` & `""`: Username dan Password default dari XAMPP.

---

## 3. Logika Desain Database (`database.sql`)

Database kita memiliki 4 tabel utama. Mari kita bahas alurnya:

1.  **Tabel `user`**
    *   Digunakan untuk proses Login.
    *   *Kenapa tidak ada AUTO_INCREMENT di `id_user`?* Sesuai instruksi dosen, kita tidak menggunakan fitur otomatis database ini. Jadi saat memasukkan user baru, kita harus menentukan angka ID-nya secara manual (contoh: ID `1` untuk Kasir, `2` untuk Owner).
2.  **Tabel `produk`**
    *   Menyimpan menu minuman teh beserta harganya. Kunci utamanya (`Primary Key`) adalah `kode_produk` (seperti `T001`).
3.  **Tabel `transaksi` (Struk/Nota Penjualan - Header)**
    *   Menyimpan ringkasan transaksi: nomor nota, tanggal transaksi, total belanja, uang yang dibayar pelanggan, dan uang kembalian.
4.  **Tabel `detail_transaksi` (Daftar barang yang dibeli - Detail)**
    *   Satu nota bisa berisi banyak produk yang dibeli (misal: 2 Original Teh dan 1 Lemon Teh). Semua daftar barang belanjaan itu disimpan di tabel ini.
    *   Tabel ini dihubungkan ke tabel `transaksi` menggunakan `no_nota` sebagai **Foreign Key** (Kunci Tamu).

---

## 4. Cara Kerja Fitur & Kode Program

Mari kita bedah logika coding di balik layar untuk setiap fitur:

### Fitur A: Sistem Login Multi-Role (`LoginForm.java`)
Saat tombol "Login" diklik, program menjalankan alur berikut:
1.  Mengambil teks dari inputan username (`tfUsername`) dan password (`tfPassword`).
2.  Bertanya ke database menggunakan query SQL:
    ```sql
    SELECT * FROM user WHERE username = ? AND password = ?
    ```
    *(Tanda tanya `?` digunakan demi keamanan agar aplikasi tidak mudah diretas/SQL Injection).*
3.  Jika database menemukan data yang cocok, program membaca kolom `role`:
    *   Jika role-nya **Kasir**, sistem membuka halaman `DashboardKasir` dan mengirimkan info nama kasir agar di layar muncul tulisan `"Hallo : [Nama Kasir]"`.
    *   Jika role-nya **Owner**, sistem akan membuka `DashboardOwner`.
4.  Menutup halaman Login (`this.dispose()`).

### Fitur B: Manajemen Menu Produk (`ProdukForm.java` - CRUD)
Fitur ini khusus untuk Owner untuk menambah, mengedit, menghapus, atau mencari produk.
*   **Pencarian Real-Time (Search)**:
    Setiap kali kamu mengetik satu huruf di kolom pencarian, event `keyReleased` akan langsung bekerja dan menjalankan pencarian ke database secara otomatis:
    ```sql
    SELECT * FROM produk WHERE kode_produk LIKE ? OR nama_produk LIKE ?
    ```
*   **Memilih Baris Tabel (Mouse Click)**:
    Saat baris produk di tabel diklik, program mengambil data dari baris tersebut dan memindahkannya ke kolom inputan di bawahnya agar mudah diedit atau dihapus.
*   **Tambah/Edit/Hapus (CRUD)**:
    Menjalankan perintah SQL standar seperti `INSERT INTO produk...`, `UPDATE produk...`, atau `DELETE FROM produk...` lalu memanggil fungsi `loadData()` untuk memperbarui tabel secara instan.

### Fitur C: Layanan Transaksi Kasir (`DashboardKasir.java` - POS)
Ini adalah fitur paling kompleks. Berikut adalah detail cara kerjanya:
*   **Tombol Angka (Numpad)**:
    Saat kamu mengklik tombol `1` sampai `0`, program akan membaca angka di tombol tersebut dan menyambungkannya (append) ke dalam kotak teks jumlah pembelian.
*   **Keranjang Belanja Sementara (di Memori Java)**:
    Belanjaan belum langsung disimpan ke database saat kasir menekan tombol "Tambah". Belanjaan disimpan di dalam list memori Java (`List<Transaksi> cart`).
    *   Jika kasir menambah produk yang sama (misal OriTeh Susu ditambah lagi), kode Java akan mencari apakah kode barang tersebut sudah ada di keranjang. Jika ada, jumlahnya saja yang ditambah tanpa membuat baris baru di tabel keranjang.
*   **Hitung Kembalian Otomatis**:
    Kita memasang pendengar ketikan (`DocumentListener`) di kotak input pembayaran (`tfInputNominalBayar`). Begitu kasir mengetik uang pembayaran, komputer langsung menghitung uang kembalian secara otomatis tanpa perlu menekan tombol apa pun.
*   **Membuat Nomor Nota Otomatis**:
    Nomor nota dibuat dengan format: `TR-[TanggalHariIni]-[NomorUrut]`.
    *Contoh:* `TR-05072026-0001`.
    Program menghitung berapa transaksi yang sudah terjadi hari ini di database menggunakan perintah `COUNT(*)`. Jika hari ini sudah ada 2 transaksi, transaksi berikutnya otomatis diberi nomor urut `0003`.
*   **Simpan Transaksi (Konsep Commit & Rollback)**:
    Saat tombol "Simpan Transaksi" ditekan, data disimpan ke database. Karena transaksi ini melibatkan 2 tabel berbeda (`transaksi` dan `detail_transaksi`), kita menggunakan sistem transaksi database:
    1.  `conn.setAutoCommit(false)`: Kita kunci database agar perubahan tidak langsung permanen.
    2.  Simpan ringkasan nota ke tabel `transaksi`.
    3.  Gunakan perulangan (`for-loop`) untuk menyimpan daftar barang belanjaan satu per satu ke tabel `detail_transaksi`.
    4.  `conn.commit()`: Jika semua proses sukses, data disimpan permanen secara bersamaan.
    5.  `conn.rollback()`: Jika di tengah jalan ada error (misal koneksi terputus saat menyimpan barang ke-2), semua data yang terlanjur masuk akan ditarik kembali agar database tidak rusak atau selisih.

### Fitur D: Laporan Penjualan & Statistik (`LaporanPenjualanForm.java`)
Fitur ini menampilkan grafik data penjualan secara ringkas:
*   **Jumlah Transaksi**: Menggunakan query `SELECT COUNT(*) FROM transaksi` untuk menghitung berapa kali transaksi terjadi.
*   **Penjualan Hari Ini**: Menghitung omzet hari ini saja dengan menyaring tanggal transaksi:
    ```sql
    SELECT SUM(total_bayar) FROM transaksi WHERE DATE(tanggal) = CURDATE()
    ```
*   **Produk Terlaris (Best Seller)**:
    Program mencari nama produk dengan jumlah penjualan terbanyak menggunakan query pengelompokan (`GROUP BY`) dan pengurutan (`ORDER BY DESC`):
    ```sql
    SELECT nama_produk, SUM(jumlah) AS total_qty 
    FROM detail_transaksi 
    GROUP BY kode_produk, nama_produk 
    ORDER BY total_qty DESC LIMIT 1
    ```

---

## 5. Cara Membuat Nomor Urut di Tabel GUI (Bukan di Database)

Sesuai instruksi: **"give number to each tabel but not for the database, only for the view"** (berikan nomor urut pada tabel di aplikasi, tapi jangan disimpan di kolom database).

### Bagaimana logika kodenya?
Di database kita, tabel `produk`, `transaksi`, dan `detail_transaksi` **tidak memiliki** kolom nomor urut `1, 2, 3...`. 

Ketika program Java mengambil data dari database, kita membuat variabel penghitung biasa (misal `int no = 1;`). Setiap kali baris data dibaca, kita masukkan nilai variabel `no` ini ke kolom pertama tabel UI, lalu menambah nilainya (`no++`).

Contoh potongan kodenya:
```java
// 1. Buat model tabel baru dengan kolom pertama bernama "No"
DefaultTableModel model = new DefaultTableModel(
    new Object[][]{},
    new String[]{"No", "Kode Produk", "Nama Produk", "Harga"}
);
jTable1.setModel(model);

// 2. Ambil data dari database
int no = 1; // Variabel nomor urut dimulai dari angka 1
while (rs.next()) {
    model.addRow(new Object[]{
        no++, // Masukkan nomor urut ke kolom indeks 0, lalu no bertambah jadi 2, 3, dst.
        rs.getString("kode_produk"),
        rs.getString("nama_produk"),
        rs.getDouble("harga_produk")
    });
}
```
Dengan cara ini, tabel di layar aplikasi akan terlihat rapi dengan nomor urut `1, 2, 3...`, namun database kita tetap bersih dan hemat penyimpanan karena tidak perlu menyimpan kolom nomor urut tersebut.

---

## 6. Kamus Istilah Coding untuk Pemula

Jika kamu menemukan kata-kata asing di dalam kode program, berikut penjelasannya:

*   **`PreparedStatement`**: Alat di Java untuk menjalankan query database SQL secara aman. Sangat direkomendasikan dibanding `Statement` biasa karena mencegah peretasan (SQL Injection).
*   **`ResultSet`**: Objek penampung hasil data yang dikirim oleh database MySQL setelah kita melakukan perintah `SELECT`.
*   **`try-with-resources`**: Penulisan blok error handling `try { ... }` yang otomatis menutup koneksi database setelah selesai digunakan (contoh: `try (Connection conn = ...) { ... }`). Ini sangat penting agar memori komputer tidak bocor/lemot.
*   **`dispose()`**: Perintah untuk menutup jendela/form aktif saat ini agar hilang dari layar dan memorinya dibersihkan.
*   **`setVisible(true)`**: Perintah untuk memunculkan jendela/form ke layar komputer.
*   **`ArrayList` / `List`**: Array dinamis di Java yang ukurannya bisa bertambah atau berkurang secara otomatis sesuai kebutuhan (sangat cocok untuk keranjang belanja).

Selamat belajar! Jika ada bagian kode yang masih membuatmu bingung, bacalah perlahan-lapan sambil mencocokkannya dengan file java terkait di NetBeans. Semangat kuliahnya!
