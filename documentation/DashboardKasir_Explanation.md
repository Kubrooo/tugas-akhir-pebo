# Penjelasan Rinci & Panduan Presentasi: `DashboardKasir.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [DashboardKasir.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/DashboardKasir.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Kasir Pintar di Toko OriTeh Sapuro**
>
> Bayangkan `DashboardKasir` adalah sebuah **Mesin POS (Point of Sale)** kasir supermarket modern:
> - **Keranjang Belanja (`cart`):** Kantong belanja plastik fisik. Ketika kasir memilih varian teh di menu (`cbVarianProduk`) dan menekan **Tambah**, teh dimasukkan ke dalam kantong. Jika produk yang sama ditambahkan lagi, kasir tidak mengambil kantong baru, melainkan hanya menambahkan jumlah gelas teh di dalam kantong yang sama.
> - **Numeric Pad Tombol 0-9:** Layar sentuh cepat pada mesin kasir untuk menginput kuantitas pesanan dengan cepat menggunakan jari tanpa harus mengetik di keyboard komputer.
> - **Dynamic Change Calculation (`updateKembalian`):** Kalkulator otomatis pada mesin kasir. Begitu kasir menerima uang lembaran dari pembeli dan mengetikkannya, layar langsung menunjukkan uang kembalian secara *real-time* sebelum struk dicetak.
> - **SQL Transaction (Commit & Rollback):** Kasir mencatat penjualan di buku kasir utama (`transaksi`) dan merinci isi belanjaan di lembar lampiran detail (`detail_transaksi`). 
>   - Jika semua barang berhasil dicatat dan uang pas, kasir membubuhkan stempel resmi **"LUNAS" (Commit)**.
>   - Jika di tengah proses pencatatan tiba-tiba printer macet atau mati listrik, kasir akan menyobek lembaran coretan transaksi tersebut dan membatalkan seluruh transaksi **(Rollback)** agar tidak ada laporan palsu/gantung di buku keuangan.

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
      [ cbVarianProduk ]        [ NumPad (0-9) ]
       (Pilih Varian)          (Input Kuantitas)
              |                        |
              +-----------+------------+
                          v
                 [ btnTambahPesanan ]
                          |
              +-----------+-----------+
              | Di keranjang (cart)?  |
              +-----+-----------+-----+
                    |           |
                  (Ya)        (Tidak)
                    |           |
                    v           v
           Jumlah Bertambah    Buat Objek Transaksi baru
           dan Subtotal Update  di List<Transaksi>
                    |           |
                    +-----+-----+
                          |
                          v
                 [ refreshCartTable ]
            - Render ke tabelBelanjaan (JTable)
            - Jumlahkan Total Bayar
                          |
             (Ketik nominal uang pembeli)
                          v
                 [ tfInputNominalBayar ]
            - Dipicu DocumentListener
            - Hitung: Kembalian = Bayar - Total
            - Render kembalian di layar secara real-time
                          |
                 [ btnSimpanTransaksi ]
                          |
                (Mulai DB Transaction)
                - conn.setAutoCommit(false)
                          |
            +-------------+-------------+
            |                           |
            v                           v
     [ INSERT transaksi ]     [ INSERT detail_transaksi ]
     (1 baris header nota)    (Loop detail produk di cart)
            |                           |
            +-------------+-------------+
                          |
                 /=================\
                /   Apakah Sukses   \
               <     Tanpa Error?    >
                \                   /
                 \=================/
                  /               \
              (Sukses)         (Error)
                /                   \
               v                     v
          [ conn.commit() ]     [ conn.rollback() ]
       Tulis permanen ke DB    Batalkan seluruh transaksi
```

---

## 3. Struktur Variabel State & Inisialisasi
Halaman kasir ini mengelola status (*state*) transaksi menggunakan beberapa variabel utama:
* **`produkList`** (`java.util.List<Models.Produk>`): Tempat menyimpan cache data menu teh aktif yang ditarik dari database MySQL agar tidak perlu berulang kali melakukan query database saat memilih produk.
* **`cart`** (`java.util.List<Models.Transaksi>`): Keranjang belanja sementara untuk menampung item yang sedang dipesan sebelum disimpan permanen ke database.
* **`activeUserId`** & **`activeUserName`**: Menyimpan data kasir yang sedang login untuk keperluan audit database (siapa yang melayani transaksi ini).

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Konstruktor Utama & Konfigurasi Jendela
```java
    public DashboardKasir() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        loadProducts();
        generateNoNota();
        refreshCartTable();
```
* **`initComponents();`**: Method built-in Swing untuk menginisialisasi semua komponen visual GUI (seperti tombol, panel, tabel) yang didesain di editor visual NetBeans.
* **`this.setLocationRelativeTo(null);`**: Menentukan posisi window relatif terhadap komponen lain. Parameter `null` berarti form kasir diletakkan tepat di **tengah-tengah layar** saat dijalankan.
* **`this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);`**: Mengatur aksi ketika tombol close (`X`) ditekan. Konstanta `DISPOSE_ON_CLOSE` (nilai integer `2`) memastikan hanya jendela kasir ini yang ditutup dan dibersihkan dari memori, sedangkan aplikasi utama (dan dashboard lain jika ada) tetap berjalan di background.
* **`loadProducts();`**: Memanggil fungsi lokal untuk memuat daftar menu dari database ke dalam ComboBox pilihan produk.
* **`generateNoNota();`**: Memanggil fungsi pembuat kode nota transaksi unik secara dinamis.
* **`refreshCartTable();`**: Memanggil fungsi untuk mengosongkan dan menyiapkan tabel belanjaan kasir.

---

### B. Implementasi Numeric Pad Dinamis
```java
        java.awt.event.ActionListener numPadListener = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JButton btn = (javax.swing.JButton) e.getSource();
                appendJumlah(btn.getText());
            }
        };
        btnJumlahPembelian1.addActionListener(numPadListener);
        // ... (tombol 2 s.d 0)
```
* **`numPadListener`**: Daripada membuat listener terpisah untuk 10 tombol angka (0-9), kode ini menerapkan prinsip *code reusability* dengan mendefinisikan satu listener bersama.
* **`e.getSource()`**: Mengidentifikasi tombol (`JButton`) mana yang memicu event klik tersebut, kemudian dicasting menjadi `JButton` untuk mengambil angka labelnya menggunakan method bawaan **`btn.getText()`**. Nilai teks ini dikirim ke method **`appendJumlah()`**.

```java
    private void appendJumlah(String digit) {
        String current = tfJumlahPembelianReadOnly.getText().trim();
        if (current.equals("0") || current.isEmpty()) {
            tfJumlahPembelianReadOnly.setText(digit);
        } else {
            tfJumlahPembelianReadOnly.setText(current + digit);
        }
    }
```
* **`appendJumlah(digit)`**: Mengatur penambahan digit kuantitas pada text field. 
  * Jika nilai saat ini kosong atau masih `"0"`, teks diganti langsung dengan angka baru (`Kondisi A`).
  * Jika sudah berisi angka lain, angka baru akan ditempelkan di belakangnya (`Kondisi B`, contoh: `1` ditempeli `2` menjadi `12`), mensimulasikan input mesin kalkulator fisik.

---

### C. Pemantauan Input Uang & Perubahan Kembalian Dinamis
```java
        tfInputNominalBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
        });
```
* **`DocumentListener`**: Pendengar event dinamis tingkat dokumen. Listener ini mendeteksi setiap perubahan karakter teks di kolom "Nominal Bayar" secara real-time. Baik saat mengetik karakter (`insertUpdate`), menghapus karakter (`removeUpdate`), maupun memformat teks (`changedUpdate`), kalkulator kembalian akan langsung dipicu secara otomatis tanpa menunggu kasir menekan tombol cari/hitung.

```java
    private void updateKembalian() {
        String bayarStr = tfInputNominalBayar.getText().trim();
        String totalStr = labelPlaceHolderTotalBayar.getText().trim();
        try {
            double bayar = Double.parseDouble(bayarStr);
            double total = Double.parseDouble(totalStr);
            double kembalian = bayar - total;
            labelPlaceHolderKembalian.setText(String.format("%.0f", kembalian));
        } catch (NumberFormatException e) {
            labelPlaceHolderKembalian.setText("0");
        }
    }
```
* **`Double.parseDouble()`**: Mengubah representasi teks nominal bayar dan total bayar menjadi tipe numerik desimal (`double`).
* **`bayar - total`**: Menghitung selisih uang. Hasil kembalian diformat tanpa desimal di belakang koma (`%.0f`) dan langsung ditampilkan pada label kembalian di layar. Jika input tidak valid (misalnya huruf atau kosong), catch block mendeteksi `NumberFormatException` dan menyetel kembalian kembali ke `"0"`.

---

### D. Pengaturan Model Tabel Belanjaan (`refreshCartTable`)
Tabel belanjaan menggunakan konsep pemisahan data model dan visual.
```java
    private void refreshCartTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "Kode Barang", "Nama Barang", "Harga Satuan", "Jumlah", "SubTotal"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelBelanjaan.setModel(model);
```
* **`DefaultTableModel`**: Wadah penyimpan data baris dan kolom yang akan ditampilkan oleh tabel.
* **`new Object[][]{}`**: Menginisialisasi baris data awal sebagai array dua dimensi kosong (0 baris).
* **`new String[]{"No", "Kode Barang", ...}`**: Mendefinisikan judul kolom (header) tabel.
* **`isCellEditable` (Override)**: Mengubah method bawaan model agar mengembalikan nilai **`false`**. Ini memblokir kasir agar tidak bisa mengubah atau mengetik data secara manual di sel tabel (Read-Only).
* **`tabelBelanjaan.setModel(model);`**: Menghubungkan kertas data `model` ke visual komponen `tabelBelanjaan` di GUI.

```java
        double total = 0;
        int no = 1;
        for (Models.Transaksi t : cart) {
            model.addRow(new Object[]{
                no++,
                t.getKodeProduk(),
                t.getNamaProduk(),
                t.getHargaSatuan(),
                t.getJumlah(),
                t.getSubTotal()
            });
            total += t.getSubTotal();
        }
        labelPlaceHolderTotalBayar.setText(String.format("%.0f", total));
        updateKembalian();
    }
```
* **`model.addRow(new Object[]{...})`**: Menambahkan satu baris data transaksi secara dinamis ke dalam model dari daftar list keranjang belanja (`cart`).
* **`total += t.getSubTotal();`**: Menghitung akumulasi total belanjaan dari seluruh produk di keranjang.

---

### E. Penarikan Katalog Produk (`loadProducts`)
Method ini bertugas memuat daftar produk aktif dari database ke pilihan ComboBox.
```java
    private void loadProducts() {
        cbVarianProduk.removeAllItems();
        produkList.clear();
        String query = "SELECT * FROM produk WHERE is_deleted = 0";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Models.Produk p = new Models.Produk(
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    rs.getDouble("harga_produk")
                );
                produkList.add(p);
                cbVarianProduk.addItem(p.getNamaProduk() + " (Rp " + (int)p.getHargaProduk() + ")");
            }
        } catch (java.sql.SQLException ex) { ... }
    }
```
* **`conn.createStatement()`**: Membuat objek `Statement` standar untuk mengeksekusi query SQL statis (tanpa parameter input).
* **`stmt.executeQuery(query)`**: Mengeksekusi query `SELECT` dan mengembalikan data dalam bentuk objek `ResultSet`.
* **`rs.next()`**: Menggeser kursor pembaca ke baris berikutnya. Menghasilkan `true` jika baris data tersedia, dan `false` jika data telah habis.
* **`rs.getString("nama_produk")` & `rs.getDouble("harga_produk")`**: Mengambil nilai data pada kolom database berdasarkan nama kolomnya dengan tipe data yang sesuai di Java.

---

### F. Generator Nomor Nota Otomatis (`generateNoNota`)
Method ini mencari jumlah transaksi hari ini untuk membuat nomor nota urut.
```java
        String query = "SELECT COUNT(*) FROM transaksi WHERE no_nota LIKE ?";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    String nextNota = prefix + String.format("%04d", count);
                    labelPlaceHolderNoNota.setText(nextNota);
                    // ...
                }
            }
        }
```
* **`conn.prepareStatement(query)`**: Membuat objek `PreparedStatement` untuk query berparameter agar aman dari SQL Injection.
* **`stmt.setString(1, prefix + "%")`**: Menyuntikkan nilai String ke parameter tanda tanya (`?`) ke-1. Tanda `%` adalah wildcard SQL untuk mencari nota yang diawali prefiks tanggal hari ini.
* **`rs.getInt(1)`**: Mengambil hasil dari fungsi agregasi `COUNT(*)` (kolom indeks ke-1 hasil query) yang merepresentasikan jumlah transaksi yang terjadi pada hari itu.

---

### G. Penyimpanan Transaksi dengan ACID Database Transaction
Proses menyimpan data ganda ke tabel `transaksi` dan `detail_transaksi` harus aman.
```java
        java.sql.Connection conn = null;
        try {
            conn = Models.Koneksi.getConnection();
            conn.setAutoCommit(false); // 1. TRANSAKSI MANUAL DIMULAI
            
            // Simpan Induk Transaksi
            String queryTransaksi = "INSERT INTO transaksi (no_nota, bayar, kembalian, total_bayar, id_user) VALUES (?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmtT = conn.prepareStatement(queryTransaksi)) {
                stmtT.setString(1, nota);
                stmtT.setDouble(2, bayar);
                stmtT.setDouble(3, kembalian);
                stmtT.setDouble(4, total);
                stmtT.setInt(5, activeUserId);
                stmtT.executeUpdate(); // Mengeksekusi penulisan non-SELECT
            }
            
            // Simpan Detail Transaksi (Looping)
            String queryDetail = "INSERT INTO detail_transaksi (no_nota, kode_produk, nama_produk, harga_satuan, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmtD = conn.prepareStatement(queryDetail)) {
                for (Models.Transaksi item : cart) {
                    stmtD.setString(1, nota);
                    stmtD.setString(2, item.getKodeProduk());
                    stmtD.setString(3, item.getNamaProduk());
                    stmtD.setDouble(4, item.getHargaSatuan());
                    stmtD.setInt(5, item.getJumlah());
                    stmtD.setDouble(6, item.getSubTotal());
                    stmtD.executeUpdate();
                }
            }
            
            conn.commit(); // 2. SIMPAN PERMANEN DATA JIKA SEMUA SUKSES
```
* **`conn.setAutoCommit(false);`**: Mematikan penyimpanan otomatis. Database tidak akan langsung menuliskan data ke harddisk sampai diperintahkan lewat `commit()`.
* **`stmtT.executeUpdate()` & `stmtD.executeUpdate()`**: Mengeksekusi query manipulasi data (INSERT). Mengembalikan angka berupa jumlah baris database yang terpengaruh.
* **`conn.commit();`**: Menyimpan seluruh data header transaksi dan detail item transaksi secara bersamaan secara permanen ke database MySQL.

```java
        } catch (java.sql.SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // 3. BATALKAN SEMUA PERUBAHAN JIKA TERJADI ERROR
                } catch (java.sql.SQLException rollbackEx) { ... }
            }
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + ex.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 4. KEMBALIKAN KE SETELAN DEFAULT
                    conn.close();             // 5. TUTUP KONEKSI
                } catch (java.sql.SQLException e) { ... }
            }
        }
```
* **`conn.rollback();`**: Menjamin keutuhan data. Jika salah satu proses penyimpanan detail belanjaan gagal, seluruh transaksi dibatalkan kembali ke kondisi awal, mencegah data transaksi menggantung (nota terbuat tapi rincian tehnya kosong).
* **`finally`**: Blok penutup yang menjamin koneksi database selalu dikembalikan ke status `setAutoCommit(true)` dan ditutup secara aman (`conn.close()`) agar sumber daya sistem dibebaskan kembali.

---

## 5. Pertanyaan Sidang yang Sering Ditanyakan (FAQ Sidang)

### Q: Apa perbedaan penggunaan `createStatement()` dan `prepareStatement()`?
*   **Jawaban:** `createStatement()` digunakan untuk mengeksekusi query SQL statis yang tidak membutuhkan parameter masukan pengguna (seperti load produk aktif). Sedangkan `prepareStatement()` digunakan untuk query dinamis yang memiliki parameter masukan (ditandai dengan `?`), yang mengompilasi query terlebih dahulu agar lebih aman dari celah keamanan *SQL Injection*.

### Q: Apa fungsi dari angka `1`, `2`, `3` di dalam parameter pengesetan seperti `setString(1, nota)` atau `setDouble(2, bayar)`?
*   **Jawaban:** Angka tersebut adalah nomor urut indeks dari parameter tanda tanya (`?`) pada query SQL dihitung dari kiri ke kanan. Urutan parameter dimulai dari indeks **1**, bukan 0.

### Q: Apa perbedaan dari method `executeQuery()` dan `executeUpdate()`?
*   **Jawaban:** `executeQuery()` digunakan untuk mengeksekusi query pembacaan data (perintah SQL `SELECT`) dan mengembalikan objek `ResultSet` yang berisi baris-baris data hasil query. Sementara `executeUpdate()` digunakan untuk mengeksekusi query manipulasi data (seperti `INSERT`, `UPDATE`, atau `DELETE`) dan mengembalikan angka jumlah baris yang berhasil diubah.

### Q: Mengapa kita harus memanggil `rs.next()` pada objek `ResultSet`?
*   **Jawaban:** Saat `ResultSet` dikembalikan dari eksekusi database, pointer pembaca data berada pada posisi sebelum baris pertama. Pemanggilan `rs.next()` memindahkan kursor pembaca ke baris berikutnya. Jika baris tersebut berisi data, `rs.next()` mengembalikan nilai `true`, sehingga kita bisa membaca nilai kolomnya. Jika tidak ada baris data lagi, ia akan mengembalikan `false`.

### Q: Jelaskan pentingnya perintah `conn.setAutoCommit(false)` dan `conn.commit()` pada penyimpanan transaksi!
*   **Jawaban:** Perintah tersebut digunakan untuk menjaga integritas data (transaksi ACID). Kita menonaktifkan auto-commit agar data nota dan rincian belanjaan disimpan sementara di memori database. Setelah semua rincian dipastikan terkirim tanpa error, kita memanggil `conn.commit()` agar semua data ditulis permanen secara bersamaan. Jika terjadi error di tengah jalan, kita memanggil `conn.rollback()` untuk membatalkan seluruh proses agar database tidak rusak karena data induk dan detail yang tidak sinkron.



---


---

## Source Code Lengkap (DashboardKasir.java)

Berikut adalah kode sumber lengkap dari file [DashboardKasir.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/DashboardKasir.java):

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
public class DashboardKasir extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardKasir.class.getName());

    /**
     * Creates new form DashboardKasir
     */
    private java.util.List<Models.Produk> produkList = new java.util.ArrayList<>();
    private java.util.List<Models.Transaksi> cart = new java.util.ArrayList<>();
    private int activeUserId = 1;
    private String activeUserName = "Kasir";

    public DashboardKasir() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        loadProducts();
        generateNoNota();
        refreshCartTable();
        
        // Setup numeric pad
        java.awt.event.ActionListener numPadListener = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JButton btn = (javax.swing.JButton) e.getSource();
                appendJumlah(btn.getText());
            }
        };
        btnJumlahPembelian1.addActionListener(numPadListener);
        btnJumlahPembelian2.addActionListener(numPadListener);
        btnJumlahPembelian3.addActionListener(numPadListener);
        btnJumlahPembelian4.addActionListener(numPadListener);
        btnJumlahPembelian5.addActionListener(numPadListener);
        btnJumlahPembelian6.addActionListener(numPadListener);
        btnJumlahPembelian7.addActionListener(numPadListener);
        btnJumlahPembelian8.addActionListener(numPadListener);
        btnJumlahPembelian9.addActionListener(numPadListener);
        btnJumlahPembelian0.addActionListener(numPadListener);
        
        // Dynamic Change Calculation
        tfInputNominalBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
        });
        
        // Cart click selection
        tabelBelanjaan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabelBelanjaan.getSelectedRow();
                if (row != -1) {
                    tfJumlahPembelianReadOnly.setText(String.valueOf(cart.get(row).getJumlah()));
                }
            }
        });
        
        // Button Action Listeners
        btnTambahPesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahPesananActionPerformed(evt);
            }
        });
        
        btnEditPesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditPesananActionPerformed(evt);
            }
        });
        
        btnHapusPesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusPesananActionPerformed(evt);
            }
        });
        
        btnSimpanTransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanTransaksiActionPerformed(evt);
            }
        });
        
        btnCancelTransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelTransaksiActionPerformed(evt);
            }
        });
        
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DashboardKasir.this.dispose();
                new LoginForm().setVisible(true);
            }
        });
    }

    public void setCashierInfo(String nama, int idUser) {
        this.activeUserId = idUser;
        this.activeUserName = nama;
        jLabel2.setText("Hallo : " + nama);
    }

    private void loadProducts() {
        cbVarianProduk.removeAllItems();
        produkList.clear();
        String query = "SELECT * FROM produk WHERE is_deleted = 0";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Models.Produk p = new Models.Produk(
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    rs.getDouble("harga_produk")
                );
                produkList.add(p);
                cbVarianProduk.addItem(p.getNamaProduk() + " (Rp " + (int)p.getHargaProduk() + ")");
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load produk: " + ex.getMessage());
        }
    }

    private void generateNoNota() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("ddMMyyyy");
        String dateStr = sdf.format(new java.util.Date());
        String prefix = "TR-" + dateStr + "-";
        
        String query = "SELECT COUNT(*) FROM transaksi WHERE no_nota LIKE ?";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    String nextNota = prefix + String.format("%04d", count);
                    labelPlaceHolderNoNota.setText(nextNota);
                    // Also update timestamp label (jLabel12)
                    java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm");
                    jLabel12.setText(sdfFull.format(new java.util.Date()));
                }
            }
        } catch (java.sql.SQLException ex) {
            labelPlaceHolderNoNota.setText(prefix + "0001");
        }
    }

    private void appendJumlah(String digit) {
        String current = tfJumlahPembelianReadOnly.getText().trim();
        if (current.equals("0") || current.isEmpty()) {
            tfJumlahPembelianReadOnly.setText(digit);
        } else {
            tfJumlahPembelianReadOnly.setText(current + digit);
        }
    }

    private void refreshCartTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "Kode Barang", "Nama Barang", "Harga Satuan", "Jumlah", "SubTotal"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelBelanjaan.setModel(model);
        
        double total = 0;
        int no = 1;
        for (Models.Transaksi t : cart) {
            model.addRow(new Object[]{
                no++,
                t.getKodeProduk(),
                t.getNamaProduk(),
                t.getHargaSatuan(),
                t.getJumlah(),
                t.getSubTotal()
            });
            total += t.getSubTotal();
        }
        
        labelPlaceHolderTotalBayar.setText(String.format("%.0f", total));
        updateKembalian();
    }

    private void updateKembalian() {
        String bayarStr = tfInputNominalBayar.getText().trim();
        String totalStr = labelPlaceHolderTotalBayar.getText().trim();
        
        if (bayarStr.isEmpty()) {
            labelPlaceHolderKembalian.setText("0");
            return;
        }
        
        try {
            double bayar = Double.parseDouble(bayarStr);
            double total = Double.parseDouble(totalStr);
            double kembalian = bayar - total;
            labelPlaceHolderKembalian.setText(String.format("%.0f", kembalian));
        } catch (NumberFormatException e) {
            labelPlaceHolderKembalian.setText("0");
        }
    }

    private void btnTambahPesananActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedIndex = cbVarianProduk.getSelectedIndex();
        if (selectedIndex == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Silakan pilih produk terlebih dahulu!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Models.Produk p = produkList.get(selectedIndex);
        String qtyStr = tfJumlahPembelianReadOnly.getText().trim();
        int qty = 1;
        if (!qtyStr.isEmpty()) {
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) qty = 1;
            } catch (NumberFormatException e) {
                qty = 1;
            }
        }
        
        boolean found = false;
        for (Models.Transaksi t : cart) {
            if (t.getKodeProduk().equals(p.getKodeProduk())) {
                t.setJumlah(t.getJumlah() + qty);
                t.setSubTotal(t.getJumlah() * t.getHargaSatuan());
                found = true;
                break;
            }
        }
        
        if (!found) {
            Models.Transaksi t = new Models.Transaksi(
                p.getKodeProduk(),
                p.getNamaProduk(),
                p.getHargaProduk(),
                qty,
                qty * p.getHargaProduk()
            );
            cart.add(t);
        }
        
        refreshCartTable();
        tfJumlahPembelianReadOnly.setText("");
    }

    private void btnEditPesananActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = tabelBelanjaan.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Silakan pilih item di keranjang yang ingin diedit!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String qtyStr = tfJumlahPembelianReadOnly.getText().trim();
        if (qtyStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Masukkan jumlah pembelian baru menggunakan tombol angka!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus lebih besar dari 0!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah tidak valid!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Models.Transaksi t = cart.get(selectedRow);
        t.setJumlah(qty);
        t.setSubTotal(qty * t.getHargaSatuan());
        
        refreshCartTable();
        tfJumlahPembelianReadOnly.setText("");
    }

    private void btnHapusPesananActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = tabelBelanjaan.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Silakan pilih item di keranjang yang ingin dihapus!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        cart.remove(selectedRow);
        refreshCartTable();
        tfJumlahPembelianReadOnly.setText("");
    }

    private void btnCancelTransaksiActionPerformed(java.awt.event.ActionEvent evt) {
        cart.clear();
        refreshCartTable();
        tfInputNominalBayar.setText("");
        tfJumlahPembelianReadOnly.setText("");
        labelPlaceHolderKembalian.setText("0");
    }

    private void btnSimpanTransaksiActionPerformed(java.awt.event.ActionEvent evt) {
        if (cart.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Keranjang belanja kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String totalStr = labelPlaceHolderTotalBayar.getText();
        String bayarStr = tfInputNominalBayar.getText().trim();
        
        if (bayarStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Silakan masukkan nominal pembayaran!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double total = Double.parseDouble(totalStr);
        double bayar;
        try {
            bayar = Double.parseDouble(bayarStr);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nominal pembayaran tidak valid!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (bayar < total) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nominal pembayaran kurang!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double kembalian = bayar - total;
        String nota = labelPlaceHolderNoNota.getText();
        
        java.sql.Connection conn = null;
        try {
            conn = Models.Koneksi.getConnection();
            conn.setAutoCommit(false);
            
            String queryTransaksi = "INSERT INTO transaksi (no_nota, bayar, kembalian, total_bayar, id_user) VALUES (?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmtT = conn.prepareStatement(queryTransaksi)) {
                stmtT.setString(1, nota);
                stmtT.setDouble(2, bayar);
                stmtT.setDouble(3, kembalian);
                stmtT.setDouble(4, total);
                stmtT.setInt(5, activeUserId);
                stmtT.executeUpdate();
            }
            
            String queryDetail = "INSERT INTO detail_transaksi (no_nota, kode_produk, nama_produk, harga_satuan, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmtD = conn.prepareStatement(queryDetail)) {
                for (Models.Transaksi item : cart) {
                    stmtD.setString(1, nota);
                    stmtD.setString(2, item.getKodeProduk());
                    stmtD.setString(3, item.getNamaProduk());
                    stmtD.setDouble(4, item.getHargaSatuan());
                    stmtD.setInt(5, item.getJumlah());
                    stmtD.setDouble(6, item.getSubTotal());
                    stmtD.executeUpdate();
                }
            }
            
            conn.commit();
            javax.swing.JOptionPane.showMessageDialog(this, "Transaksi Berhasil Disimpan!\nKembalian: Rp " + (int)kembalian);
            
            cart.clear();
            refreshCartTable();
            tfInputNominalBayar.setText("");
            labelPlaceHolderKembalian.setText("0");
            tfJumlahPembelianReadOnly.setText("");
            generateNoNota();
            
        } catch (java.sql.SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (java.sql.SQLException rollbackEx) {
                    logger.log(java.util.logging.Level.SEVERE, null, rollbackEx);
                }
            }
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + ex.getMessage(), "Error Database", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException e) {
                    logger.log(java.util.logging.Level.SEVERE, null, e);
                }
            }
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        labelPlaceHolderNoNota = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        cbVarianProduk = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        btnJumlahPembelian1 = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        btnJumlahPembelian2 = new javax.swing.JButton();
        btnJumlahPembelian3 = new javax.swing.JButton();
        btnJumlahPembelian4 = new javax.swing.JButton();
        btnJumlahPembelian5 = new javax.swing.JButton();
        btnJumlahPembelian6 = new javax.swing.JButton();
        btnJumlahPembelian7 = new javax.swing.JButton();
        btnJumlahPembelian8 = new javax.swing.JButton();
        btnJumlahPembelian9 = new javax.swing.JButton();
        btnJumlahPembelian0 = new javax.swing.JButton();
        tfJumlahPembelianReadOnly = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        btnTambahPesanan = new javax.swing.JButton();
        btnEditPesanan = new javax.swing.JButton();
        btnHapusPesanan = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelBelanjaan = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        labelPlaceHolderTotalBayar = new javax.swing.JLabel();
        tfInputNominalBayar = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        labelPlaceHolderKembalian = new javax.swing.JLabel();
        btnSimpanTransaksi = new javax.swing.JButton();
        btnCancelTransaksi = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("OriTeh Sapuro - POS");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Hallo : Kasir");

        btnLogout.setText("LogOut");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("No. Nota:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("05-07-2026 13:50");

        labelPlaceHolderNoNota.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        labelPlaceHolderNoNota.setText("TR-05072026-0001");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Tanggal:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelPlaceHolderNoNota, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(labelPlaceHolderNoNota))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cbVarianProduk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Input Pembelian :");

        btnJumlahPembelian1.setText("1");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Varian Produk :");

        btnJumlahPembelian2.setText("2");

        btnJumlahPembelian3.setText("3");

        btnJumlahPembelian4.setText("4");

        btnJumlahPembelian5.setText("5");

        btnJumlahPembelian6.setText("6");

        btnJumlahPembelian7.setText("7");

        btnJumlahPembelian8.setText("8");

        btnJumlahPembelian9.setText("9");

        btnJumlahPembelian0.setText("0");

        tfJumlahPembelianReadOnly.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Jumlah Pembelian :");

        btnTambahPesanan.setText("Tambah");

        btnEditPesanan.setText("Edit");

        btnHapusPesanan.setText("Hapus");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnJumlahPembelian6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnJumlahPembelian7, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnJumlahPembelian8, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnJumlahPembelian9, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnJumlahPembelian0, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel11)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnTambahPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(tfJumlahPembelianReadOnly, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(btnJumlahPembelian1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnJumlahPembelian2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnJumlahPembelian3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnJumlahPembelian4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnJumlahPembelian5, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(25, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(cbVarianProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(btnHapusPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbVarianProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15)
                .addGap(2, 2, 2)
                .addComponent(tfJumlahPembelianReadOnly, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnJumlahPembelian1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian3, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnJumlahPembelian6, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian7, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian8, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian9, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJumlahPembelian0, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTambahPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHapusPesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tfJumlahPembelianReadOnly.getAccessibleContext().setAccessibleName("");
        tfJumlahPembelianReadOnly.getAccessibleContext().setAccessibleDescription("");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Keranjang Belanja");

        tabelBelanjaan.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabelBelanjaan);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Total Bayar:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setText("Rp");

        labelPlaceHolderTotalBayar.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        labelPlaceHolderTotalBayar.setText("0");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Nominal Bayar :");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Kembalian :");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel7.setText("Rp");

        labelPlaceHolderKembalian.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        labelPlaceHolderKembalian.setText("0");

        btnSimpanTransaksi.setText("simpan transaksi");

        btnCancelTransaksi.setText("cancel transaksi");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(79, 79, 79)
                                .addComponent(jLabel8)
                                .addGap(98, 98, 98))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelPlaceHolderTotalBayar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                                .addComponent(tfInputNominalBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(61, 61, 61)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelPlaceHolderKembalian)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSimpanTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(87, 87, 87))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(labelPlaceHolderTotalBayar)
                    .addComponent(tfInputNominalBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(labelPlaceHolderKembalian))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpanTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Pilih Produk");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLogout)))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(btnLogout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(24, Short.MAX_VALUE))
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
        java.awt.EventQueue.invokeLater(() -> new DashboardKasir().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelTransaksi;
    private javax.swing.JButton btnEditPesanan;
    private javax.swing.JButton btnHapusPesanan;
    private javax.swing.JButton btnJumlahPembelian0;
    private javax.swing.JButton btnJumlahPembelian1;
    private javax.swing.JButton btnJumlahPembelian2;
    private javax.swing.JButton btnJumlahPembelian3;
    private javax.swing.JButton btnJumlahPembelian4;
    private javax.swing.JButton btnJumlahPembelian5;
    private javax.swing.JButton btnJumlahPembelian6;
    private javax.swing.JButton btnJumlahPembelian7;
    private javax.swing.JButton btnJumlahPembelian8;
    private javax.swing.JButton btnJumlahPembelian9;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSimpanTransaksi;
    private javax.swing.JButton btnTambahPesanan;
    private javax.swing.JComboBox<String> cbVarianProduk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelPlaceHolderKembalian;
    private javax.swing.JLabel labelPlaceHolderNoNota;
    private javax.swing.JLabel labelPlaceHolderTotalBayar;
    private javax.swing.JTable tabelBelanjaan;
    private javax.swing.JTextField tfInputNominalBayar;
    private javax.swing.JTextField tfJumlahPembelianReadOnly;
    // End of variables declaration//GEN-END:variables
}

```