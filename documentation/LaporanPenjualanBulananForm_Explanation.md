# Penjelasan Kode: `LaporanPenjualanBulananForm.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [LaporanPenjualanBulananForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/LaporanPenjualanBulananForm.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Buku Besar Laporan Keuangan Bulanan Toko**
>
> Bayangkan `LaporanPenjualanBulananForm` adalah **Buku Besar Rekapitulasi Keuangan** di laci meja Owner **OriTeh Sapuro** yang digunakan di akhir bulan:
> - **Window Closed Listener (Alur Navigasi):** Ketika Owner menutup buku rekap bulanan ini (menutup form), Owner kembali menghadap ke **Meja Kerja Owner Dashboard** (`DashboardOwner`).
> - **Filter Bulan Berjalan (`MONTH(tanggal) = MONTH(CURDATE())`):** Seperti halaman kalender bulan aktif. Sistem hanya akan membaca baris transaksi yang tercatat pada lembaran bulan ini (misal Juli 2026). Transaksi bulan lalu tidak akan masuk dalam pembukuan ini.
> - **Statistik Bulanan (`loadStats`):** Menampilkan ringkasan total nota transaksi terkumpul bulan ini (Jumlah Transaksi Bulanan) beserta total uang kas kotor bulanan (Penjualan Bulan Ini).
> - **Tabel Ringkasan Nota (`loadReports`):** Berbeda dari laporan harian yang merinci nama teh yang dibeli per gelas, laporan bulanan ini bertindak sebagai **Daftar Ringkasan Nota**. Ia hanya mencatat nomor nota, tanggal, total bayar, nominal uang dari pembeli, dan uang kembaliannya. Ini membantu Owner memantau aktivitas transaksi secara makro/luas.

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
           [ Buka LaporanPenjualanBulananForm ]
                           |
            +--------------+--------------+
            | (Load)                      | (Load)
            v                             v
       [ loadStats() ]             [ loadReports() ]
            |                             |
      (Query MySQL)                 (Query SQL)
   1. COUNT(*) transaksi         - SELECT no_nota, tanggal, total_bayar,
      WHERE MONTH = MONTH(NOW())   bayar, kembalian FROM transaksi
      AND YEAR = YEAR(NOW())     - WHERE MONTH = MONTH(NOW())
   2. SUM(total_bayar)             AND YEAR = YEAR(NOW())
      WHERE MONTH = MONTH(NOW())          |
      AND YEAR = YEAR(NOW())              |
            |                             v
            v                     (Loop ResultSet rs)
     Render ke Label:                     |
   - Transaksi Bulanan           - Ambil data transaksi bulanan
   - Penjualan Bulanan           - Masukkan ke DefaultTableModel
                                          |
                                          v
                                 [ Render ke JTable ]
```

---

## 3. Struktur Komponen Visual (GUI)
Form ini menggunakan kelas `JFrame` mandiri dengan susunan komponen:
1. `labelPlaceHolderTransaksiBulanan` (`JLabel`): Menampilkan jumlah total nota transaksi bulan ini.
2. `labelPlaceHolderPenjualanBulanan` (`JLabel`): Menampilkan total omzet penjualan bulan ini.
3. `jLabel8` (`JLabel`): Menampilkan penunjuk tanggal/waktu sistem saat laporan dibuka.
4. `jTable1` (`JTable`): Tabel untuk menampilkan daftar nota transaksi bulanan.

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Konstruktor & Navigasi Tutup Jendela
```java
    public LaporanPenjualanBulananForm() {
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
        jLabel8.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        loadStats();
        loadReports();
    }
```
* **`windowClosed(WindowEvent e)`** (Baris 24-32): Sama seperti pada laporan harian, listener ini memastikan bahwa ketika jendela laporan bulanan ditutup, sistem akan merekonstruksi dan membuka kembali instance `DashboardOwner` baru di layar agar Owner tidak terdampar di layar kosong.

---

### B. Menghitung Angka Rekapitulasi Bulanan (`loadStats`)
```java
    private void loadStats() {
        try (java.sql.Connection conn = Models.Koneksi.getConnection()) {
```
* Membuka koneksi database MySQL menggunakan try-with-resources.

```java
            // 1. Monthly Transactions Count
            String sqlCount = "SELECT COUNT(*) FROM transaksi WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE())";
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sqlCount)) {
                if (rs.next()) {
                    labelPlaceHolderTransaksiBulanan.setText(String.valueOf(rs.getInt(1)));
                }
            }
```
* **`MONTH(tanggal) = MONTH(CURDATE())`** (Baris 42): Fungsi MySQL `MONTH()` mengekstrak angka bulan (1-12) dari kolom tanggal. Kode ini membandingkannya dengan bulan saat ini (`CURDATE()`).
* **`YEAR(tanggal) = YEAR(CURDATE())`**: Memastikan tahun data transaksi juga cocok dengan tahun saat ini. Hal ini penting untuk mencegah data dari bulan yang sama di tahun-tahun sebelumnya ikut terhitung.

```java
            // 2. Monthly Total Sales
            String sqlSum = "SELECT SUM(total_bayar) FROM transaksi WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE())";
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sqlSum)) {
                if (rs.next()) {
                    double totalMonth = rs.getDouble(1);
                    labelPlaceHolderPenjualanBulanan.setText(String.format("%.0f", totalMonth));
                }
            }
```
* **`SUM(total_bayar)`** (Baris 51): Menjumlahkan total nominal belanjaan seluruh nota transaksi yang valid pada bulan berjalan untuk menghitung total pendapatan bulanan kedai.

---

### C. Mengisi Data Tabel Ringkasan Bulanan (`loadReports`)
```java
    private void loadReports() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "No Nota", "Tanggal", "Total Bayar", "Bayar", "Kembalian"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(model);
```
* Membuat model tabel 6 kolom ("No", "No Nota", "Tanggal", "Total Bayar", "Bayar", "Kembalian") dan mematikan fungsi edit sel langsung di tabel.

```java
        String query = "SELECT no_nota, tanggal, total_bayar, bayar, kembalian " +
                       "FROM transaksi " +
                       "WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE()) " +
                       "ORDER BY tanggal DESC";
```
* **Perbedaan Logika Database (Penting untuk Sidang):**
  * Laporan bulanan ini **tidak menggunakan JOIN** dengan tabel `detail_transaksi` karena data yang disajikan hanya data nota tingkat atas (*high-level transaction header*). Hal ini membuat query berjalan jauh lebih cepat dibandingkan dengan memuat ratusan ribu rincian barang belanjaan.
  * Hasil disortir secara mundur berdasarkan tanggal transaksi teranyar (`ORDER BY tanggal DESC`).

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
                    rs.getDouble("total_bayar"),
                    rs.getDouble("bayar"),
                    rs.getDouble("kembalian")
                });
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load laporan: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
```
* Mengambil data dari hasil database (`ResultSet`) dan menambahkannya satu per satu sebagai baris baru pada model visual tabel JTable. Jika terjadi kendala SQL, tampilkan kotak pop-up error database.
