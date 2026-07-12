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



---


---

## Source Code Lengkap (LaporanPenjualanHarianForm.java)

Berikut adalah kode sumber lengkap dari file [LaporanPenjualanHarianForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/LaporanPenjualanHarianForm.java):

```java
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

/**
 *
 * @author Ardiansyah
 */
public class LaporanPenjualanHarianForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LaporanPenjualanHarianForm.class.getName());

    /**
     * Creates new form LaporanPenjualanForm
     */
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
        
        jLabel1.setText("Laporan Penjualan Harian - OriTeh Sapuro");
        labelPlaceHolderTanggalHarian.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        loadStats();
        loadReports();
    }
    
    private void loadStats() {
        try (java.sql.Connection conn = Models.Koneksi.getConnection()) {
            // 1. Today's Transactions
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transaksi WHERE DATE(tanggal) = CURDATE()")) {
                if (rs.next()) {
                    labelPlaceholderJumlahTransaksi.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // 2. Today's Sales
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT SUM(total_bayar) FROM transaksi WHERE DATE(tanggal) = CURDATE()")) {
                if (rs.next()) {
                    double totalToday = rs.getDouble(1);
                    labelPlaceHolderTransaksi.setText(String.format("%.0f", totalToday));
                }
            }       
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load stats: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadReports() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "No Nota", "Tanggal", "Kode Produk", "Harga Satuan", "Jumlah", "SubTotal", "Bayar", "Kembalian"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(model);
        
        String query = "SELECT t.no_nota, t.tanggal, d.kode_produk, COALESCE(p.harga_produk, d.harga_satuan) AS harga_satuan, d.jumlah, d.subtotal, t.bayar, t.kembalian " +
                       "FROM transaksi t " +
                       "JOIN detail_transaksi d ON t.no_nota = d.no_nota " +
                       "LEFT JOIN produk p ON d.kode_produk = p.kode_produk " +
                       "WHERE DATE(t.tanggal) = CURDATE() " +
                       "ORDER BY t.tanggal DESC";
                       
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
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load laporan: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        labelPlaceholderJumlahTransaksi = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        labelPlaceHolderTransaksi = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        labelPlaceHolderTanggalHarian = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Jumlah Transaksi :");

        labelPlaceholderJumlahTransaksi.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        labelPlaceholderJumlahTransaksi.setText("0");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("Transaksi");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelPlaceholderJumlahTransaksi)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(labelPlaceholderJumlahTransaksi))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Laporan Penjualan Harian - OriTeh Sapuro");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Penjualan Hari Ini :");

        labelPlaceHolderTransaksi.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        labelPlaceHolderTransaksi.setText("0");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("Rp");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelPlaceHolderTransaksi)))
                .addContainerGap(107, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(labelPlaceHolderTransaksi))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Tanggal");

        labelPlaceHolderTanggalHarian.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        labelPlaceHolderTanggalHarian.setText("PlaceHolderHarian");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelPlaceHolderTanggalHarian))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(jLabel6))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(labelPlaceHolderTanggalHarian)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(93, 93, 93)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new LaporanPenjualanHarianForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelPlaceHolderTanggalHarian;
    private javax.swing.JLabel labelPlaceHolderTransaksi;
    private javax.swing.JLabel labelPlaceholderJumlahTransaksi;
    // End of variables declaration//GEN-END:variables
}

```