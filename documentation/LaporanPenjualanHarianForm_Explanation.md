# Penjelasan Kode: `LaporanPenjualanHarianForm.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [LaporanPenjualanHarianForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/LaporanPenjualanHarianForm.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Papan Informasi Kinerja Harian Kedai**
>
> Bayangkan `LaporanPenjualanHarianForm` adalah sebuah **Papan Pengumuman Kinerja Kedai** yang digantung di ruang belakang **OriTeh Sapuro**:
> - **Window Closed Listener (Alur Navigasi):** Ketika Owner selesai membaca papan pengumuman lalu melipatnya (menutup jendela form), ia secara otomatis kembali melangkah ke **Meja Kerja Owner Dashboard** (`DashboardOwner`).
> - **Statistik Harian (`loadStats`):** Kotak ringkasan cepat di pojok papan tulis. Kotak pertama menunjukkan jumlah pelanggan yang datang ke kasir hari ini (Jumlah Transaksi). Kotak kedua menunjukkan total uang pendapatan kotor yang masuk ke mesin kasir hari ini (Penjualan Hari Ini).
> - **Tabel Laporan Rinci (`loadReports`):** Lembar kertas kasir karbon rangkap dua. Untuk menampilkan baris laporan yang detail, sistem harus mencocokkan **Struk Pembayaran** (`transaksi`) dengan **Daftar Teh yang Dipesan** (`detail_transaksi`) menggunakan nomor nota sebagai jembatan penghubung (SQL JOIN), lalu menempelkannya di papan tulis agar Owner bisa memantau jam demi jam transaksi yang terjadi hari ini.

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
           [ Buka LaporanPenjualanHarianForm ]
                           |
            +--------------+--------------+
            | (Load)                      | (Load)
            v                             v
       [ loadStats() ]             [ loadReports() ]
            |                             |
      (Query MySQL)                 (Query SQL JOIN)
   1. COUNT(*) transaksi         - Join transaksi t & detail_transaksi d
      WHERE tanggal = CURDATE()  - Left join produk p on kode_produk
   2. SUM(total_bayar)           - Filter DATE(tanggal) = CURDATE()
      WHERE tanggal = CURDATE()           |
            |                             v
            v                     (Loop ResultSet rs)
     Render ke Label:                     |
   - Jumlah Transaksi            - Ambil data kolom per kolom
   - Total Penjualan             - Masukkan ke DefaultTableModel
                                          |
                                          v
                                 [ Render ke JTable ]
```

---

## 3. Struktur Komponen Visual (GUI)
Form ini menggunakan kelas `JFrame` mandiri untuk menampilkan data dengan komponen utama:
1. `labelPlaceholderJumlahTransaksi` (`JLabel`): Menampilkan total jumlah nota hari ini.
2. `labelPlaceHolderTransaksi` (`JLabel`): Menampilkan total omzet rupiah hari ini.
3. `labelPlaceHolderTanggalHarian` (`JLabel`): Menampilkan tanggal dan waktu saat laporan dimuat.
4. `jTable1` (`JTable`): Tabel untuk menampilkan rincian barang yang terjual hari ini.

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Konstruktor & Alur Navigasi Otomatis
```java
    public LaporanPenjualanHarianForm() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                javax.swing.JFrame frame = new javax.swing.JFrame("OriTeh Sapuro - Owner Dashboard");
                frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                DashboardOwner doPanel = new DashboardOwner();
                frame.setContentPane(doPanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
```
* **`addWindowListener(...)`** (Baris 22): Mendaftarkan pendengar aktivitas jendela (*window event adapter*).
* **`windowClosed(WindowEvent e)`** (Baris 24): Ketika pengguna mengklik tombol 'X' (tutup) di pojok kanan atas form ini, event ini akan dipicu secara otomatis. Di dalamnya, program secara dinamis merakit dan memunculkan kembali jendela `DashboardOwner` baru. Ini memberikan pengalaman pengguna (UX) navigasi "Kembali ke Menu Utama" yang mulus.

```java
        jLabel1.setText("Laporan Penjualan Harian - OriTeh Sapuro");
        labelPlaceHolderTanggalHarian.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        loadStats();
        loadReports();
    }
```
* **`SimpleDateFormat`** (Baris 36): Memformat waktu sistem saat ini ke format standar Indonesia/ISO (`Tahun-Bulan-Hari Jam:Menit:Detik`) untuk dipasang di layar penunjuk waktu laporan.

---

### B. Memuat Ringkasan Angka Statistik (`loadStats`)
```java
    private void loadStats() {
        try (java.sql.Connection conn = Models.Koneksi.getConnection()) {
```
* Membuka koneksi database dengan metode yang aman menggunakan try-with-resources.

```java
            // 1. Today's Transactions
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transaksi WHERE DATE(tanggal) = CURDATE()")) {
                if (rs.next()) {
                    labelPlaceholderJumlahTransaksi.setText(String.valueOf(rs.getInt(1)));
                }
            }
```
* **`SELECT COUNT(*)`** (Baris 45): Menggunakan fungsi agregasi SQL untuk menghitung berapa banyak total baris data transaksi yang masuk hari ini.
* **`DATE(tanggal) = CURDATE()`**: Memotong data tanggal-waktu di database agar hanya menyisakan bagian tanggal saja, lalu membandingkannya dengan tanggal hari ini (`CURDATE()`).
* **`rs.getInt(1)`** (Baris 47): Mengambil hasil kolom pertama hasil hitung (COUNT) dan memasangnya pada label jumlah transaksi.

```java
            // 2. Today's Sales
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT SUM(total_bayar) FROM transaksi WHERE DATE(tanggal) = CURDATE()")) {
                if (rs.next()) {
                    double totalToday = rs.getDouble(1);
                    labelPlaceHolderTransaksi.setText(String.format("%.0f", totalToday));
                }
            }
```
* **`SELECT SUM(total_bayar)`** (Baris 53): Menjumlahkan nilai kolom `total_bayar` untuk transaksi hari ini untuk mendapatkan total omzet penjualan.
* **`String.format("%.0f", totalToday)`** (Baris 56): Membuang angka di belakang koma dari nilai desimal omzet agar tampil rapi sebagai nominal rupiah bulat.

---

### C. Memuat Data Tabel Rinci (`loadReports`)
Fungsi ini bertugas memformat isi JTable dan menjalankan query relasional database yang cukup kompleks.

```java
    private void loadReports() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "No Nota", "Tanggal", "Kode Barang", "Harga Satuan", "Jumlah", "SubTotal", "Bayar", "Kembalian"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Membuat tabel read-only
            }
        };
        jTable1.setModel(model);
```
* **`isCellEditable`** (Baris 70): Di-override mengembalikan nilai `false` agar data laporan di dalam tabel tidak bisa diedit secara tidak sengaja oleh Owner dengan melakukan klik ganda di sel tabel.

```java
        String query = "SELECT t.no_nota, t.tanggal, d.kode_produk, COALESCE(p.harga_produk, d.harga_satuan) AS harga_satuan, d.jumlah, d.subtotal, t.bayar, t.kembalian " +
                       "FROM transaksi t " +
                       "JOIN detail_transaksi d ON t.no_nota = d.no_nota " +
                       "LEFT JOIN produk p ON d.kode_produk = p.kode_produk " +
                       "WHERE DATE(t.tanggal) = CURDATE() " +
                       "ORDER BY t.tanggal DESC";
```
* **Analisis SQL Query (Penting untuk Sidang):**
  * **`JOIN detail_transaksi d ON t.no_nota = d.no_nota`** (Baris 78): Relasi **Inner Join** antara tabel header transaksi (`t`) dengan detail transaksi (`d`) berdasarkan kesamaan kolom kunci `no_nota`. Ini diperlukan karena data tanggal dan nominal bayar ada di tabel induk, sementara data barang dan jumlah beli berada di tabel detail.
  * **`LEFT JOIN produk p ON d.kode_produk = p.kode_produk`** (Baris 79): Relasi **Left Join** dengan tabel produk (`p`) untuk mengamankan data harga master produk jika dibutuhkan.
  * **`COALESCE(p.harga_produk, d.harga_satuan)`** (Baris 76): Fungsi MySQL untuk memilih nilai pertama yang tidak bernilai NULL. Jika harga di master produk kosong, gunakan harga satuan transaksi yang tercatat di tabel detail.
  * **`ORDER BY t.tanggal DESC`** (Baris 81): Menampilkan data transaksi paling baru di baris paling atas (Urutan menurun/Descending).

```java
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {
            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    no++,
                    rs.getString("no_nota"),
                    rs.getString("tanggal"),
                    rs.getString("kode_produk"),
                    rs.getDouble("harga_satuan"),
                    rs.getInt("jumlah"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("bayar"),
                    rs.getDouble("kembalian")
                });
            }
        }
```
* **`while(rs.next())`** (Baris 87): Perulangan baris demi baris dari hasil pencarian database.
* **`model.addRow(...)`** (Baris 88): Memasukkan array data kolom hasil query ke dalam model JTable untuk dirender ke layar monitor.
